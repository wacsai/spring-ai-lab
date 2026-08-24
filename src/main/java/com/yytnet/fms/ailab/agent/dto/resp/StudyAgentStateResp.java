package com.yytnet.fms.ailab.agent.dto.resp;

import java.util.List;

public record StudyAgentStateResp(
        String conversationId,
        String agentType,
        String goal,
        boolean completed,
        int stepCount,
        List<StudyAgentStepResp> steps,
        String answer,
        int memoryMessageCount,
        int maxMemoryMessages,
        String note
) {
}
