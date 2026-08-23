package com.yytnet.fms.ailab.memory.controller;

import com.yytnet.fms.ailab.memory.dto.req.MemoryChatReq;
import com.yytnet.fms.ailab.memory.dto.resp.MemoryChatResp;
import com.yytnet.fms.ailab.memory.service.MemoryChatService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/memory")
public class MemoryChatController {

    private final MemoryChatService memoryChatService;

    public MemoryChatController(MemoryChatService memoryChatService) {
        this.memoryChatService = memoryChatService;
    }

    /**
     * 最小 Chat Memory 闭环：
     * 同一个 conversationId 下，Memory Advisor 会把历史消息带入本次模型调用。
     */
    @PostMapping("/chat")
    public MemoryChatResp chat(@Valid @RequestBody MemoryChatReq req) {
        return memoryChatService.chat(req);
    }
}
