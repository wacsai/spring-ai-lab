package com.yytnet.fms.ailab.vector.dto.resp;

public record VectorDocumentCreateResp(
        Long id,
        String title,
        int dimension,
        String message
) {
}
