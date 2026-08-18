package com.yytnet.fms.ailab.vector.repository.projection;

public record VectorSearchRow(
        Long id,
        String title,
        String content,
        double distance
) {
}
