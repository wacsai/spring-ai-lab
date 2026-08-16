package com.yytnet.fms.ailab.chat.service;

import com.yytnet.fms.ailab.chat.dto.req.ChatReq;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    // 系统提示词用于定义模型的长期角色和回答边界，用户输入只负责表达本次问题。
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是一个面向 Java / Spring Boot 开发者的 Spring AI 学习助手。
            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 聚焦当前学习阶段，不主动扩展到 RAG、Agent、MCP 等后续阶段
            用户额外要求：{customSystemPrompt}
            """;

    private final ChatClient chatClient;
    private final String model;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        // 请求级 options 会覆盖一部分默认配置，因此这里保存默认模型名，后面显式写回 options。
        this.model = model;
    }

    /**
     * 聊天
     *
     * @param req 请求参数
     * @return 聊天内容
     */
    public String chat(ChatReq req) {
        // 构建 ChatClientRequestSpec
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt()
                .system(system -> system
                        .text(SYSTEM_PROMPT_TEMPLATE)
                        .param("customSystemPrompt", resolveCustomSystemPrompt(req.systemPrompt())))
                .user(req.msg());

        // 构建 OllamaChatOptions 构建器
        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder()
                // 请求级 options 会参与覆盖默认配置；显式设置 model 可避免只传 temperature 等参数时丢失模型名。
                .model(model)
                // qwen thinking 输出可能不会进入 content；当前阶段先关闭，保证接口稳定返回文本。
                .disableThinking();
        
        // 应用请求参数到 OllamaChatOptions 中
        boolean hasOptions = applyOptions(req, optionsBuilder);
        if (hasOptions) {
            requestSpec = requestSpec.options(optionsBuilder);
        }

        // 调用聊天接口
        String content = requestSpec
                .call()
                .content();
        return content == null ? "" : content;
    }

    /**
     * 应用请求参数到 OllamaChatOptions 中
     *
     * @param req            请求参数
     * @param optionsBuilder OllamaChatOptions 构建器
     * @return 是否应用了请求参数
     */
    private boolean applyOptions(ChatReq req, OllamaChatOptions.Builder optionsBuilder) {
        // 只设置调用方传入的参数；未传的参数继续使用 application.yaml 中的默认配置。
        boolean hasOptions = false;

        // 设置温度
        if (req.temperature() != null) {
            optionsBuilder.temperature(req.temperature());
            hasOptions = true;
        }
        // 设置 topP
        if (req.topP() != null) {
            optionsBuilder.topP(req.topP());
            hasOptions = true;
        }
        // 设置 topK
        if (req.topK() != null) {
            optionsBuilder.topK(req.topK());
            hasOptions = true;
        }
        // 设置 numPredict
        if (req.numPredict() != null) {
            optionsBuilder.numPredict(req.numPredict());
            hasOptions = true;
        }

        return hasOptions;
    }

    /**
     * 解析自定义系统提示词
     *
     * @param systemPrompt 系统提示词
     * @return 自定义系统提示词
     */
    private String resolveCustomSystemPrompt(String systemPrompt) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            return "无";
        }
        return systemPrompt;
    }
}
