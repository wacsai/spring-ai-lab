package com.yytnet.fms.ailab.memory.dto.resp;

public record MemoryChatResp(
        String conversationId,
        String content,
        int memoryMessageCount,
        int maxMemoryMessages,
        String note
) {
}
