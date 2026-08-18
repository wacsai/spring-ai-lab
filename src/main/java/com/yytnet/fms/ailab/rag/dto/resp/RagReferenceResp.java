package com.yytnet.fms.ailab.rag.dto.resp;

public record RagReferenceResp(
        Long id,
        String title,
        String content,
        double distance,
        double similarity
) {
}
