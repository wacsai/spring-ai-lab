package com.yytnet.fms.ailab.memory.controller;

import com.yytnet.fms.ailab.memory.dto.req.MemoryChatReq;
import com.yytnet.fms.ailab.memory.dto.resp.MemoryChatResp;
import com.yytnet.fms.ailab.memory.dto.resp.MemoryClearResp;
import com.yytnet.fms.ailab.memory.service.MemoryChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
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

    /**
     * 清理指定会话的 Chat Memory：
     * 只删除当前 conversationId 的 JVM 内存历史，不影响其他会话。
     */
    @DeleteMapping("/conversations/{conversationId}")
    public MemoryClearResp clear(@PathVariable
                                 @NotBlank(message = "conversationId不能为空")
                                 @Size(max = 100, message = "conversationId不能超过100个字符")
                                 String conversationId) {
        return memoryChatService.clear(conversationId);
    }
}
