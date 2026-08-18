package com.yytnet.fms.ailab.vector.dto.resp;

import java.util.List;

public record VectorSearchResp(
        String query,
        int queryDimension,
        int topK,
        List<VectorSearchItemResp> results,
        String note
) {
}
