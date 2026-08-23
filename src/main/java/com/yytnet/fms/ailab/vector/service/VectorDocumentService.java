package com.yytnet.fms.ailab.vector.service;

import com.yytnet.fms.ailab.common.exception.AiVectorStoreException;
import com.yytnet.fms.ailab.vector.dto.req.VectorDocumentCreateReq;
import com.yytnet.fms.ailab.vector.dto.req.VectorSearchReq;
import com.yytnet.fms.ailab.vector.dto.resp.VectorDocumentCreateResp;
import com.yytnet.fms.ailab.vector.dto.resp.VectorSearchItemResp;
import com.yytnet.fms.ailab.vector.dto.resp.VectorSearchResp;
import com.yytnet.fms.ailab.vector.repository.VectorDocumentCommandRepository;
import com.yytnet.fms.ailab.vector.repository.VectorDocumentQueryRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorDocumentService {

    // 当前 qwen3-embedding:4b 已验证返回 2560 维向量。
    // 表结构 ai_document_embedding.embedding 也是 vector(2560)，所以这里做一次维度保护：
    // 如果以后更换 embedding 模型，这个值和 schema.sql 里的 vector 维度都需要一起调整。
    private static final int EXPECTED_DIMENSION = 2560;

    // 用户不传 topK 时，默认只取最相似的 3 条文档，便于学习阶段观察返回结果。
    private static final int DEFAULT_TOP_K = 3;

    private final EmbeddingModel embeddingModel;
    private final PgVectorLiteralConverter pgVectorLiteralConverter;
    private final VectorDocumentCommandRepository vectorDocumentCommandRepository;
    private final VectorDocumentQueryRepository vectorDocumentQueryRepository;

    public VectorDocumentService(EmbeddingModel embeddingModel,
                                 PgVectorLiteralConverter pgVectorLiteralConverter,
                                 VectorDocumentCommandRepository vectorDocumentCommandRepository,
                                 VectorDocumentQueryRepository vectorDocumentQueryRepository) {
        this.embeddingModel = embeddingModel;
        this.pgVectorLiteralConverter = pgVectorLiteralConverter;
        this.vectorDocumentCommandRepository = vectorDocumentCommandRepository;
        this.vectorDocumentQueryRepository = vectorDocumentQueryRepository;
    }

    public VectorDocumentCreateResp create(VectorDocumentCreateReq req) {
        try {
            // 入库流程：
            // 1. 先把原始 content 交给 EmbeddingModel，得到 float[] 向量。
            // 2. 再把 float[] 转成 pgvector 可以识别的字符串字面量，例如 "[0.1,0.2,...]"。
            // 3. 最后通过 Repository 执行原生 INSERT，把向量写入 PostgreSQL + pgvector。
            float[] embedding = embedAndValidate(req.content());
            Long id = vectorDocumentCommandRepository.save(
                    req.title(),
                    req.content(),
                    pgVectorLiteralConverter.toLiteral(embedding)
            );

            return new VectorDocumentCreateResp(
                    id,
                    req.title(),
                    embedding.length,
                    "文档已生成 embedding 并写入 pgvector"
            );
        } catch (RuntimeException ex) {
            throw new AiVectorStoreException("文档向量入库失败", ex);
        }
    }

    public VectorSearchResp search(VectorSearchReq req) {
        try {
            int topK = req.topK() == null ? DEFAULT_TOP_K : req.topK();

            float[] queryEmbedding = embedAndValidate(req.query());
            List<VectorSearchItemResp> results = searchSimilarDocuments(queryEmbedding, topK);

            return new VectorSearchResp(
                    req.query(),
                    queryEmbedding.length,
                    topK,
                    results,
                    "distance 使用 pgvector cosine distance，值越小越相似；similarity = 1 - distance。"
            );
        } catch (RuntimeException ex) {
            throw new AiVectorStoreException("文档相似度检索失败", ex);
        }
    }

    /**
     * 给 RAG 等内部能力复用的相似文档检索方法。
     *
     * <p>这个方法只负责“检索资料”，不负责把资料交给 Chat Model 回答；
     * 后者属于 RAG Service 的职责。</p>
     */
    public List<VectorSearchItemResp> searchSimilarDocuments(String query, int topK) {
        try {
            float[] queryEmbedding = embedAndValidate(query);
            return searchSimilarDocuments(queryEmbedding, topK);
        } catch (RuntimeException ex) {
            throw new AiVectorStoreException("文档相似度检索失败", ex);
        }
    }

    public Long createChunk(String documentTitle,
                            String chunkTitle,
                            String content,
                            int chunkIndex,
                            int chunkCount,
                            int chunkStart,
                            int chunkEnd,
                            String sourceType,
                            String sourceName,
                            String externalId) {
        try {
            // 一个长文档会被拆成多个 chunk；每个 chunk 都单独生成 embedding 并作为一条记录写入 pgvector。
            float[] embedding = embedAndValidate(content);
            return vectorDocumentCommandRepository.saveChunk(
                    chunkTitle,
                    content,
                    pgVectorLiteralConverter.toLiteral(embedding),
                    documentTitle,
                    chunkIndex,
                    chunkCount,
                    chunkStart,
                    chunkEnd,
                    sourceType,
                    sourceName,
                    externalId
            );
        } catch (RuntimeException ex) {
            throw new AiVectorStoreException("文档片段向量入库失败", ex);
        }
    }

    public int deleteChunksByDocumentTitle(String documentTitle) {
        try {
            // RAG 重新导入同一篇文档时使用。
            // 这里只按 document_title 删除 chunk，不按 title 删除普通向量 demo 记录。
            return vectorDocumentCommandRepository.deleteChunksByDocumentTitle(documentTitle);
        } catch (RuntimeException ex) {
            throw new AiVectorStoreException("按文档标题删除旧文档片段失败", ex);
        }
    }

    private List<VectorSearchItemResp> searchSimilarDocuments(float[] queryEmbedding, int topK) {
        // 检索流程和入库流程的前半段一致：
        // 用户问题也要先变成同一个 embedding 模型生成的 2560 维向量。
        // 只有“查询向量”和“文档向量”在同一个向量空间里，pgvector 的距离计算才有意义。
        return vectorDocumentQueryRepository
                .searchByCosineDistance(pgVectorLiteralConverter.toLiteral(queryEmbedding), topK)
                .stream()
                .map(row -> new VectorSearchItemResp(
                        row.getId(),
                        row.getTitle(),
                        row.getContent(),
                        row.getDocumentTitle(),
                        row.getChunkIndex(),
                        row.getChunkCount(),
                        row.getChunkStart(),
                        row.getChunkEnd(),
                        row.getSourceType(),
                        row.getSourceName(),
                        row.getExternalId(),
                        row.getDistance(),
                        toSimilarity(row.getDistance())
                ))
                .toList();
    }

    private float[] embedAndValidate(String text) {
        // EmbeddingModel 是 Spring AI 对“文本向量化模型”的抽象。
        // 当前配置使用 Ollama 的 qwen3-embedding:4b；这里不会调用 Chat Model，也不会生成自然语言回答。
        float[] embedding = embeddingModel.embed(text);
        if (embedding.length == 0) {
            throw new AiVectorStoreException("模型返回了空的文本向量", null);
        }
        if (embedding.length != EXPECTED_DIMENSION) {
            throw new AiVectorStoreException("文本向量维度不是预期的 2560", null);
        }
        return embedding;
    }

    private double toSimilarity(double distance) {
        // pgvector 的 <=> 返回 cosine distance：越小越相似。
        // 为了接口展示更直观，这里临时换算成 similarity：越大越相似。
        return 1.0 - distance;
    }
}
