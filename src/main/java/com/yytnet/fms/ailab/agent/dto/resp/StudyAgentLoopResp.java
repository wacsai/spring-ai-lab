package com.yytnet.fms.ailab.agent.dto.resp;

import java.util.List;

public record StudyAgentLoopResp(
        String conversationId,
        String agentType,
        String goal,
        boolean completed,
        String stopReason,
        int maxSteps,
        int stepCount,
        List<StudyAgentStepResp> steps,
        String answer,
        int memoryMessageCount,
        int maxMemoryMessages,
        String note
) {
}
