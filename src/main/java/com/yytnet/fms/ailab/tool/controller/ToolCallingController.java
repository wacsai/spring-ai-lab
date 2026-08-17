package com.yytnet.fms.ailab.tool.controller;

import com.yytnet.fms.ailab.chat.dto.req.ChatReq;
import com.yytnet.fms.ailab.chat.dto.resp.ChatResp;
import com.yytnet.fms.ailab.tool.service.ToolCallingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/tool")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    /**
     * Tool Calling 聊天接口。
     * Controller 只负责 HTTP 入参和出参，真正的 ChatClient + Tool 注册放在 Service 中。
     */
    @PostMapping("/chat")
    public ChatResp chat(@Valid @RequestBody ChatReq req) {
        return new ChatResp(toolCallingService.chat(req));
    }
}
