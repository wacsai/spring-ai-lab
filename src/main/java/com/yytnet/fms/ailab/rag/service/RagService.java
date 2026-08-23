package com.yytnet.fms.ailab.rag.service;

import com.yytnet.fms.ailab.common.exception.AiRagException;
import com.yytnet.fms.ailab.rag.dto.req.RagChatReq;
import com.yytnet.fms.ailab.rag.dto.req.RagDocumentImportReq;
import com.yytnet.fms.ailab.rag.dto.resp.RagChatResp;
import com.yytnet.fms.ailab.rag.dto.resp.RagCitationResp;
import com.yytnet.fms.ailab.rag.dto.resp.RagDocumentChunkResp;
import com.yytnet.fms.ailab.rag.dto.resp.RagDocumentImportResp;
import com.yytnet.fms.ailab.rag.dto.resp.RagReferenceResp;
import com.yytnet.fms.ailab.vector.dto.resp.VectorSearchItemResp;
import com.yytnet.fms.ailab.vector.service.VectorDocumentService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RagService {

    private static final int DEFAULT_TOP_K = 3;
    private static final double DEFAULT_MAX_DISTANCE = 0.6;

    // RAG 阶段继续挂 SimpleLoggerAdvisor，方便从日志里观察最终发给模型的 context 和 question。
    private static final SimpleLoggerAdvisor SIMPLE_LOGGER_ADVISOR = SimpleLoggerAdvisor.builder()
            .order(0)
            .build();

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个面向 Java / Spring Boot 开发者的 Spring AI 学习助手。
            当前接口用于学习最小 RAG，也就是“先检索资料，再基于资料回答”。

            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 只能基于【参考资料】回答，不要使用参考资料之外的知识补充事实
            - 如果参考资料不足以回答问题，明确回答“资料不足，无法基于当前知识库回答”
            - 如果使用了参考资料，回答中尽量点明使用了哪条资料，例如“根据资料 1”

            【参考资料】
            {context}
            """;

    private final ChatClient chatClient;
    private final VectorDocumentService vectorDocumentService;
    private final RagDocumentChunker ragDocumentChunker;
    private final String model;

    public RagService(ChatClient.Builder chatClientBuilder,
                      VectorDocumentService vectorDocumentService,
                      RagDocumentChunker ragDocumentChunker,
                      @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.vectorDocumentService = vectorDocumentService;
        this.ragDocumentChunker = ragDocumentChunker;
        this.model = model;
    }

    public RagDocumentImportResp importDocument(RagDocumentImportReq req) {
        try {
            String documentTitle = req.title().strip();
            int chunkSize = req.chunkSize() == null ? RagDocumentChunker.DEFAULT_CHUNK_SIZE : req.chunkSize();
            int overlap = req.overlap() == null ? RagDocumentChunker.DEFAULT_OVERLAP : req.overlap();
            boolean replaceExisting = Boolean.TRUE.equals(req.replaceExisting());

            // 文档导入阶段只做 RAG 的“知识入库”：
            // 1. 把一篇长文档切成多个 chunk。
            // 2. 每个 chunk 单独生成 embedding。
            // 3. 每个 chunk 单独写入 pgvector。
            // 当前切分器优先按句子/换行等自然边界组合 chunk。
            // 如果单个自然单元超过 chunkSize，才会退回按 Java 字符数硬切。
            List<RagDocumentChunker.Chunk> chunks = ragDocumentChunker.split(req.content(), chunkSize, overlap);
            int chunkCount = chunks.size();
            int deletedCount = deleteExistingChunksIfNecessary(documentTitle, replaceExisting);

            // 这里逐个 chunk 入库，而不是整篇文档只存一条向量。
            // 这样用户提问时，pgvector 可以命中更精确的片段，而不是返回整篇文档的“平均语义”。
            List<RagDocumentChunkResp> importedChunks = chunks.stream()
                    .map(chunk -> importChunk(documentTitle, chunk, chunkCount))
                    .toList();

            return new RagDocumentImportResp(
                    documentTitle,
                    req.content().strip().length(),
                    chunkSize,
                    overlap,
                    replaceExisting,
                    deletedCount,
                    chunkCount,
                    importedChunks,
                    buildImportNote(replaceExisting, deletedCount)
            );
        } catch (RuntimeException ex) {
            throw new AiRagException("RAG 文档入库失败", ex);
        }
    }

    public RagChatResp chat(RagChatReq req) {
        try {
            int topK = req.topK() == null ? DEFAULT_TOP_K : req.topK();
            double maxDistance = req.maxDistance() == null ? DEFAULT_MAX_DISTANCE : req.maxDistance();

            // RAG 的第一步是 Retrieval(程序查资料)：
            // 先复用 vector 模块，把用户问题转成 query embedding，再用 pgvector 找相似资料。
            List<RagReferenceResp> retrievedReferences = vectorDocumentService.searchSimilarDocuments(req.question(), topK)
                    .stream()
                    .map(this::toReference)
                    .toList();

            // retrievedReferences 是 pgvector 按 topK 取回的原始候选。
            // references 是真正进入 Prompt 的资料；rejectedReferences 是因为距离超过 maxDistance 被过滤掉的资料。
            // 这样接口响应里能直接观察“检索到了什么”和“最终用了什么”。
            List<RagReferenceResp> references = filterUsedReferences(retrievedReferences, maxDistance);
            List<RagReferenceResp> rejectedReferences = filterRejectedReferences(retrievedReferences, maxDistance);
            List<RagCitationResp> citations = buildCitations(references);

            // RAG 的第二步是 Augmented Generation(模型根据资料生成回答)：
            // 把检索到的资料作为 context 放进 System Prompt，再让 Chat Model 回答用户问题。
            String content = chatClient.prompt()
                    .system(system -> system
                            .text(SYSTEM_PROMPT_TEMPLATE)
                            .param("context", buildContext(references)))
                    .user(req.question())
                    .advisors(SIMPLE_LOGGER_ADVISOR)
                    .options(OllamaChatOptions.builder()
                            .model(model)
                            .disableThinking()
                            .temperature(0.2))
                    .call()
                    .content();

            return new RagChatResp(
                    req.question(),
                    content == null ? "" : content,
                    topK,
                    maxDistance,
                    retrievedReferences.size(),
                    references.size(),
                    rejectedReferences.size(),
                    citations,
                    references,
                    rejectedReferences,
                    "citations 是适合展示的引用摘要；references 是实际进入 Prompt 的完整资料；rejectedReferences 是被 maxDistance 过滤掉的候选资料。"
            );
        } catch (RuntimeException ex) {
            throw new AiRagException("RAG 问答失败", ex);
        }
    }

    private List<RagReferenceResp> filterUsedReferences(List<RagReferenceResp> retrievedReferences,
                                                        double maxDistance) {
        return retrievedReferences.stream()
                .filter(reference -> reference.distance() <= maxDistance)
                .toList();
    }

    private List<RagReferenceResp> filterRejectedReferences(List<RagReferenceResp> retrievedReferences,
                                                            double maxDistance) {
        return retrievedReferences.stream()
                .filter(reference -> reference.distance() > maxDistance)
                .toList();
    }

    private RagReferenceResp toReference(VectorSearchItemResp item) {
        return new RagReferenceResp(
                item.id(),
                item.title(),
                item.content(),
                item.documentTitle(),
                item.chunkIndex(),
                item.chunkCount(),
                item.chunkStart(),
                item.chunkEnd(),
                item.distance(),
                item.similarity()
        );
    }

    private RagDocumentChunkResp importChunk(String documentTitle,
                                             RagDocumentChunker.Chunk chunk,
                                             int chunkCount) {
        String chunkTitle = buildChunkTitle(documentTitle, chunk.chunkIndex(), chunkCount);
        // createChunk(...) 内部会调用 EmbeddingModel.embed(chunk.text())。
        // 也就是说，每个 chunk 都会得到一个独立的 2560 维向量，并保存 chunk 的起止位置元数据。
        Long id = vectorDocumentService.createChunk(
                documentTitle,
                chunkTitle,
                chunk.text(),
                chunk.chunkIndex(),
                chunkCount,
                chunk.start(),
                chunk.end()
        );

        return new RagDocumentChunkResp(
                id,
                chunk.chunkIndex(),
                chunkCount,
                chunk.start(),
                chunk.end(),
                chunk.text().length(),
                chunkTitle
        );
    }

    private int deleteExistingChunksIfNecessary(String documentTitle, boolean replaceExisting) {
        if (!replaceExisting) {
            return 0;
        }
        // 只在调用方明确传 replaceExisting=true 时清理同名旧 chunk。
        // 这适合学习阶段反复导入同一份资料，避免检索结果里出现重复内容。
        return vectorDocumentService.deleteChunksByDocumentTitle(documentTitle);
    }

    private String buildImportNote(boolean replaceExisting, int deletedCount) {
        if (!replaceExisting) {
            return "长文档已按 chunk 切分；每个 chunk 已生成 embedding 并写入 pgvector。";
        }
        return "已先删除相同文档标题的旧 chunk %d 条，再写入本次新切分的 chunk。".formatted(deletedCount);
    }

    private String buildChunkTitle(String documentTitle, int chunkIndex, int chunkCount) {
        return "%s - chunk %d/%d".formatted(documentTitle, chunkIndex, chunkCount);
    }

    private List<RagCitationResp> buildCitations(List<RagReferenceResp> references) {
        // citations 和 buildContext(...) 使用同一个顺序生成“资料 N”。
        // 这样模型回答里如果提到“根据资料 1”，接口返回的 citations[0].label 也是“资料 1”。
        List<RagCitationResp> citations = new ArrayList<>();
        for (int i = 0; i < references.size(); i++) {
            RagReferenceResp reference = references.get(i);
            citations.add(new RagCitationResp(
                    buildReferenceLabel(i),
                    reference.id(),
                    reference.title(),
                    reference.documentTitle(),
                    reference.chunkIndex(),
                    reference.chunkCount(),
                    reference.chunkStart(),
                    reference.chunkEnd(),
                    reference.distance(),
                    reference.similarity()
            ));
        }
        return citations;
    }

    private String buildContext(List<RagReferenceResp> references) {
        if (references.isEmpty()) {
            return "没有检索到满足相似度阈值的参考资料。";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < references.size(); i++) {
            RagReferenceResp reference = references.get(i);
            context.append(buildReferenceLabel(i)).append("：\n")
                    .append("id: ").append(reference.id()).append("\n")
                    .append("title: ").append(reference.title()).append("\n");
            appendChunkMetadata(context, reference);
            context.append("distance: ").append(reference.distance()).append("\n")
                    .append("content: ").append(reference.content()).append("\n\n");
        }
        return context.toString();
    }

    private String buildReferenceLabel(int zeroBasedIndex) {
        return "资料 " + (zeroBasedIndex + 1);
    }

    private void appendChunkMetadata(StringBuilder context, RagReferenceResp reference) {
        if (reference.documentTitle() == null || reference.chunkIndex() == null) {
            return;
        }

        context.append("documentTitle: ").append(reference.documentTitle()).append("\n")
                .append("chunk: ").append(reference.chunkIndex()).append("/")
                .append(reference.chunkCount()).append("\n")
                .append("range: ").append(reference.chunkStart()).append("-")
                .append(reference.chunkEnd()).append("\n");
    }
}
