package com.yytnet.fms.ailab.memory.dto.resp;

public record MemoryClearResp(
        String conversationId,
        boolean cleared,
        int memoryMessageCount,
        String note
) {
}
