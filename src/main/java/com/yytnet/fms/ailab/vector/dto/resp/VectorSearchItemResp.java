package com.yytnet.fms.ailab.vector.dto.resp;

public record VectorSearchItemResp(
        Long id,
        String title,
        String content,
        String documentTitle,
        Integer chunkIndex,
        Integer chunkCount,
        Integer chunkStart,
        Integer chunkEnd,
        double distance,
        double similarity
) {
}
