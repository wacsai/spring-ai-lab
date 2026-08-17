package com.yytnet.fms.ailab.tool.service;

import com.yytnet.fms.ailab.chat.dto.req.ChatReq;
import com.yytnet.fms.ailab.common.exception.AiToolCallingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ToolCallingService {

    // Tool Calling 阶段继续挂 SimpleLoggerAdvisor，方便从日志里观察模型请求、工具调用结果和最终响应。
    private static final SimpleLoggerAdvisor SIMPLE_LOGGER_ADVISOR = SimpleLoggerAdvisor.builder()
            .order(0)
            .build();

    private static final String SYSTEM_PROMPT = """
            你是一个面向 Java / Spring Boot 开发者的 Spring AI 学习助手。
            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 当前接口用于学习 Spring AI Tool Calling
            - 当用户询问当前学习进度、已完成内容、当前阶段、下一步学习内容时，必须调用工具获取真实结果，不要凭记忆回答
            - 如果问题不需要工具，直接回答即可
            - 如果使用了工具结果，回答末尾必须包含一行：数据来源：工具返回的 source 字段
            """;

    private final ChatClient chatClient;
    private final LearningProgressTool learningProgressTool;
    private final String model;

    public ToolCallingService(ChatClient.Builder chatClientBuilder,
                              LearningProgressTool learningProgressTool,
                              @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.learningProgressTool = learningProgressTool;
        this.model = model;
    }

    public String chat(ChatReq req) {
        try {
            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(req.msg())
                    // tools(...) 把带有 @Tool 方法的对象注册到本次请求中。
                    // Spring AI 会把工具名、描述、参数 schema 发给模型；模型决定是否调用。
                    .tools(learningProgressTool)
                    .advisors(SIMPLE_LOGGER_ADVISOR)
                    .options(buildOptions(req))
                    .call()
                    .content();
            return content == null ? "" : content;
        } catch (RuntimeException ex) {
            throw new AiToolCallingException("模型工具调用失败", ex);
        }
    }

    private OllamaChatOptions.Builder buildOptions(ChatReq req) {
        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder()
                // 和前面的阶段一样，请求级 options 显式带上模型名，避免覆盖默认配置时丢失 model。
                .model(model)
                // 当前工具调用阶段先关闭 thinking，降低额外推理文本干扰工具调用协议的概率。
                .disableThinking();

        applyOptions(req, optionsBuilder);
        return optionsBuilder;
    }

    private void applyOptions(ChatReq req, OllamaChatOptions.Builder optionsBuilder) {
        // 这里保留 ChatReq 已有的模型参数，便于对比同一批参数在普通聊天和工具调用中的效果。
        if (req.temperature() != null) {
            optionsBuilder.temperature(req.temperature());
        }
        if (req.topP() != null) {
            optionsBuilder.topP(req.topP());
        }
        if (req.topK() != null) {
            optionsBuilder.topK(req.topK());
        }
        if (req.numPredict() != null) {
            optionsBuilder.numPredict(req.numPredict());
        }
    }
}
