package com.yytnet.fms.ailab.rag.dto.resp;

import java.util.List;

public record RagChatResp(
        String question,
        String answer,
        int topK,
        double maxDistance,
        int retrievedCount,
        int usedCount,
        int rejectedCount,
        List<RagCitationResp> citations,
        List<RagReferenceResp> references,
        List<RagReferenceResp> rejectedReferences,
        String note
) {
}
