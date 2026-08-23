package com.yytnet.fms.ailab.memory.service;

import com.yytnet.fms.ailab.common.exception.AiMemoryException;
import com.yytnet.fms.ailab.memory.config.MemoryChatConfig;
import com.yytnet.fms.ailab.memory.dto.req.MemoryChatReq;
import com.yytnet.fms.ailab.memory.dto.resp.MemoryChatResp;
import com.yytnet.fms.ailab.memory.dto.resp.MemoryClearResp;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MemoryChatService {

    // Logger 放在 Memory Advisor 后面，便于在日志里看到 Memory 参与后的请求内容。
    private static final SimpleLoggerAdvisor SIMPLE_LOGGER_ADVISOR = SimpleLoggerAdvisor.builder()
            .order(1)
            .build();

    private static final String SYSTEM_PROMPT = """
            你是一个面向 Java / Spring Boot 开发者的 Spring AI 学习助手。
            当前接口用于学习 Chat Memory。

            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 如果用户询问前文信息，优先根据当前 conversationId 下的历史对话回答
            - 不要主动扩展到 RAG、Agent、MCP 等后续阶段
            """;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final String model;

    public MemoryChatService(ChatClient.Builder chatClientBuilder,
                             ChatMemory chatMemory,
                             @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        // MessageChatMemoryAdvisor 会在调用模型前读取历史消息，在模型返回后写入 AssistantMessage。
        this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .order(0)
                .build();
        this.model = model;
    }

    public MemoryChatResp chat(MemoryChatReq req) {
        try {
            String conversationId = req.conversationId().strip();

            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(req.message())
                    .advisors(advisor -> advisor
                            // 这个参数是 Spring AI Memory Advisor 识别会话的关键。
                            // 同一个 conversationId 会复用同一份 JVM 内存消息窗口。
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                            .advisors(memoryAdvisor, SIMPLE_LOGGER_ADVISOR))
                    .options(OllamaChatOptions.builder()
                            .model(model)
                            .disableThinking()
                            .temperature(0.2))
                    .call()
                    .content();

            return new MemoryChatResp(
                    conversationId,
                    content == null ? "" : content,
                    chatMemory.get(conversationId).size(),
                    MemoryChatConfig.MAX_MEMORY_MESSAGES,
                    "当前 Memory 使用 JVM 内存保存最近若干条消息；应用重启后会丢失，不同 conversationId 互相隔离。"
            );
        } catch (RuntimeException ex) {
            throw new AiMemoryException("Chat Memory 调用失败", ex);
        }
    }

    public MemoryClearResp clear(String conversationId) {
        try {
            String normalizedConversationId = conversationId.strip();
            // clear(...) 只清理当前 conversationId 对应的 JVM 内存消息窗口。
            // 这不会影响其他 conversationId，也不会删除任何数据库数据。
            chatMemory.clear(normalizedConversationId);
            return new MemoryClearResp(
                    normalizedConversationId,
                    true,
                    chatMemory.get(normalizedConversationId).size(),
                    "已清空当前 conversationId 的 JVM 内存消息；再次使用该 conversationId 时会从空上下文开始。"
            );
        } catch (RuntimeException ex) {
            throw new AiMemoryException("清理 Chat Memory 失败", ex);
        }
    }
}
