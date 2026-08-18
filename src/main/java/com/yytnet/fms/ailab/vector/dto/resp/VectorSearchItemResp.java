package com.yytnet.fms.ailab.vector.dto.resp;

public record VectorSearchItemResp(
        Long id,
        String title,
        String content,
        double distance,
        double similarity
) {
}
