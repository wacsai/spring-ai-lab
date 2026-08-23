package com.yytnet.fms.ailab.agent.dto.resp;

public record StudyAgentResp(
        String conversationId,
        String content,
        String agentType,
        int memoryMessageCount,
        int maxMemoryMessages,
        String note
) {
}
