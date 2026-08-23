package com.yytnet.fms.ailab.memory.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemoryChatReq(
        // 会话 ID 是 Memory 的隔离边界。
        // 同一个 conversationId 会读取同一组历史消息；不同 conversationId 互相看不到对方的上下文。
        @NotBlank(message = "conversationId不能为空")
        @Size(max = 100, message = "conversationId不能超过100个字符")
        String conversationId,

        // 用户本轮消息。
        // Memory 并不是模型永久记住了内容，而是应用把同一个 conversationId 下的历史消息重新放进本次 Prompt。
        @NotBlank(message = "消息不能为空")
        @Size(max = 2000, message = "消息不能超过2000个字符")
        String message
) {
}
