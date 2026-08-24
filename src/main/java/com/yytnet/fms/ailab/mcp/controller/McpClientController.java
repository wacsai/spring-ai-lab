package com.yytnet.fms.ailab.mcp.controller;

import com.yytnet.fms.ailab.chat.dto.req.ChatReq;
import com.yytnet.fms.ailab.mcp.dto.resp.McpChatResp;
import com.yytnet.fms.ailab.mcp.service.McpClientChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/mcp")
public class McpClientController {

    private final McpClientChatService mcpClientChatService;

    public McpClientController(McpClientChatService mcpClientChatService) {
        this.mcpClientChatService = mcpClientChatService;
    }

    /**
     * MCP Client 聊天接口。
     * Controller 只负责 HTTP 入参和出参，真正的 MCP tool 注册与 ChatClient 调用放在 Service 中。
     */
    @PostMapping("/chat")
    public McpChatResp chat(@Valid @RequestBody ChatReq req) {
        return mcpClientChatService.chat(req);
    }
}
