package com.yytnet.fms.ailab.rag.service;

import com.yytnet.fms.ailab.common.exception.AiRagException;
import com.yytnet.fms.ailab.rag.dto.req.RagChatReq;
import com.yytnet.fms.ailab.rag.dto.resp.RagChatResp;
import com.yytnet.fms.ailab.rag.dto.resp.RagReferenceResp;
import com.yytnet.fms.ailab.vector.dto.resp.VectorSearchItemResp;
import com.yytnet.fms.ailab.vector.service.VectorDocumentService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private final String model;

    public RagService(ChatClient.Builder chatClientBuilder,
                      VectorDocumentService vectorDocumentService,
                      @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.vectorDocumentService = vectorDocumentService;
        this.model = model;
    }

    public RagChatResp chat(RagChatReq req) {
        try {
            int topK = req.topK() == null ? DEFAULT_TOP_K : req.topK();
            double maxDistance = req.maxDistance() == null ? DEFAULT_MAX_DISTANCE : req.maxDistance();

            // RAG 的第一步是 Retrieval：
            // 先复用 vector 模块，把用户问题转成 query embedding，再用 pgvector 找相似资料。
            List<RagReferenceResp> references = vectorDocumentService.searchSimilarDocuments(req.question(), topK)
                    .stream()
                    // 当前最小 RAG 增加一个距离阈值，避免“不相关但排名靠前”的资料被塞给模型。
                    .filter(item -> item.distance() <= maxDistance)
                    .map(this::toReference)
                    .toList();

            // RAG 的第二步是 Augmented Generation：
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
                    references,
                    "RAG = 先用 pgvector 检索参考资料，再把参考资料交给 ChatClient 生成回答。"
            );
        } catch (RuntimeException ex) {
            throw new AiRagException("RAG 问答失败", ex);
        }
    }

    private RagReferenceResp toReference(VectorSearchItemResp item) {
        return new RagReferenceResp(
                item.id(),
                item.title(),
                item.content(),
                item.distance(),
                item.similarity()
        );
    }

    private String buildContext(List<RagReferenceResp> references) {
        if (references.isEmpty()) {
            return "没有检索到满足相似度阈值的参考资料。";
        }

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < references.size(); i++) {
            RagReferenceResp reference = references.get(i);
            context.append("资料 ").append(i + 1).append("：\n")
                    .append("id: ").append(reference.id()).append("\n")
                    .append("title: ").append(reference.title()).append("\n")
                    .append("distance: ").append(reference.distance()).append("\n")
                    .append("content: ").append(reference.content()).append("\n\n");
        }
        return context.toString();
    }
}
