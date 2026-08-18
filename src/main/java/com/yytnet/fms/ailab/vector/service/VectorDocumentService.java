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

    private static final int EXPECTED_DIMENSION = 2560;
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
            List<VectorSearchItemResp> results = vectorDocumentQueryRepository
                    .searchByCosineDistance(pgVectorLiteralConverter.toLiteral(queryEmbedding), topK)
                    .stream()
                    .map(row -> new VectorSearchItemResp(
                            row.getId(),
                            row.getTitle(),
                            row.getContent(),
                            row.getDistance(),
                            toSimilarity(row.getDistance())
                    ))
                    .toList();

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

    private float[] embedAndValidate(String text) {
        float[] embedding = embeddingModel.embed(text);
        if (embedding == null || embedding.length == 0) {
            throw new AiVectorStoreException("模型返回了空的文本向量", null);
        }
        if (embedding.length != EXPECTED_DIMENSION) {
            throw new AiVectorStoreException("文本向量维度不是预期的 2560", null);
        }
        return embedding;
    }

    private double toSimilarity(double distance) {
        return 1.0 - distance;
    }
}
