package com.yytnet.fms.ailab.embedding.dto.resp;

import java.util.List;

public record EmbeddingResp(
        String text,
        String model,
        int dimension,
        int sampleSize,
        List<Float> sample,
        String note
) {
}
