package com.yytnet.fms.ailab.mcp.service;

import com.yytnet.fms.ailab.chat.dto.req.ChatReq;
import com.yytnet.fms.ailab.common.exception.AiMcpException;
import com.yytnet.fms.ailab.mcp.dto.resp.McpChatResp;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class McpClientChatService {

    // MCP 阶段继续打开 SimpleLoggerAdvisor，方便从日志里看到远程 MCP tool 的调用过程和最终响应。
    private static final SimpleLoggerAdvisor SIMPLE_LOGGER_ADVISOR = SimpleLoggerAdvisor.builder()
            .order(0)
            .build();

    private static final String SYSTEM_PROMPT = """
            你是一个面向 Java / Spring Boot 开发者的 Spring AI 学习助手。
            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 当前接口用于学习 Spring AI MCP Client
            - 当用户询问当前学习进度、已完成内容、当前阶段、下一步学习内容时，必须调用 MCP Server 提供的远程工具获取真实结果
            - 当用户询问订单状态、物流、发货、签收、付款情况，并且提供了订单号时，必须调用 MCP Server 提供的远程订单查询工具
            - 如果用户想查订单但没有提供订单号，先要求用户补充订单号
            - 当前 MCP Server 是 spring-ai-mcp-server-demo，它运行在独立 Spring Boot 进程中
            - MCP 工具不是当前进程里的本地 @Tool 对象，而是通过 HTTP + MCP 协议从远程 Server 暴露出来的 tool
            - 如果使用了 MCP 工具结果，回答末尾必须包含一行：数据来源：MCP Server 工具返回的 source 字段
            """;

    private final ChatClient chatClient;
    private final List<ToolCallbackProvider> mcpToolCallbackProviders;
    private final String model;

    public McpClientChatService(ChatClient.Builder chatClientBuilder,
                                List<ToolCallbackProvider> mcpToolCallbackProviders,
                                @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.mcpToolCallbackProviders = mcpToolCallbackProviders;
        this.model = model;
    }

    public McpChatResp chat(ChatReq req) {
        ToolCallbackProvider[] providers = mcpToolCallbackProviders.toArray(ToolCallbackProvider[]::new);
        List<String> toolNames = getToolNames(providers);
        int toolCount = toolNames.size();

        if (toolCount == 0) {
            throw new AiMcpException("没有发现可用的 MCP 工具，请先启动 spring-ai-mcp-server-demo 并确认 8081 端口可访问");
        }

        try {
            long startNanos = System.nanoTime();
            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(req.msg())
                    // tools(...) 这里注册的是 MCP Client 自动发现的远程工具。
                    // 调用链路是：ChatClient -> MCP Client -> HTTP /mcp -> MCP Server -> 远程 Java @Tool 方法。
                    // Spring AI 2.0 起推荐统一使用 tools(Object...)，它既能接收本地 @Tool 对象，也能接收 ToolCallbackProvider。
                    .tools((Object[]) providers)
                    .advisors(SIMPLE_LOGGER_ADVISOR)
                    .options(buildOptions(req))
                    .call()
                    .content();
            long durationMs = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();

            return new McpChatResp(
                    content == null ? "" : content,
                    "MCP_CHAT",
                    model,
                    durationMs,
                    providers.length,
                    toolCount,
                    toolNames,
                    "Phase 11 轻量观测字段：feature/model/durationMs/mcpToolNames 用于学习阶段排查本次 AI 调用。"
            );
        } catch (RuntimeException ex) {
            throw new AiMcpException("MCP 远程工具调用失败，请确认 spring-ai-mcp-server-demo 已启动且端口为 8081", ex);
        }
    }

    private List<String> getToolNames(ToolCallbackProvider[] providers) {
        List<String> toolNames = new ArrayList<>();
        for (ToolCallbackProvider provider : providers) {
            for (ToolCallback callback : provider.getToolCallbacks()) {
                toolNames.add(callback.getToolDefinition().name());
            }
        }
        return toolNames;
    }

    private OllamaChatOptions.Builder buildOptions(ChatReq req) {
        OllamaChatOptions.Builder optionsBuilder = OllamaChatOptions.builder()
                // 请求级 options 显式带上模型名，避免覆盖默认配置时丢失 model。
                .model(model)
                // MCP 阶段先关闭 thinking，降低额外推理文本干扰工具调用协议的概率。
                .disableThinking();

        applyOptions(req, optionsBuilder);
        return optionsBuilder;
    }

    private void applyOptions(ChatReq req, OllamaChatOptions.Builder optionsBuilder) {
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
