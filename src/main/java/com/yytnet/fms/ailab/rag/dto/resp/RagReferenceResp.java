package com.yytnet.fms.ailab.rag.dto.resp;

public record RagReferenceResp(
        Long id,
        String title,
        String content,
        String documentTitle,
        Integer chunkIndex,
        Integer chunkCount,
        Integer chunkStart,
        Integer chunkEnd,
        String sourceType,
        String sourceName,
        String externalId,
        double distance,
        double similarity
) {
}
