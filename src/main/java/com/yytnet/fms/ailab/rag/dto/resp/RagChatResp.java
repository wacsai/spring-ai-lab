package com.yytnet.fms.ailab.rag.dto.resp;

import java.util.List;

public record RagChatResp(
        String question,
        String answer,
        int topK,
        double maxDistance,
        List<RagReferenceResp> references,
        String note
) {
}
