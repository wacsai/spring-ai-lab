package com.yytnet.fms.ailab.tool.dto.resp;

import java.util.List;

public record LearningProgressResp(
        String currentStage,
        List<String> completedMilestones,
        String nextStage,
        String verification,
        String source
) {
}
