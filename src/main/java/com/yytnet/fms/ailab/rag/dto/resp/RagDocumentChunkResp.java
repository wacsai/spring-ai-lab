package com.yytnet.fms.ailab.rag.dto.resp;

public record RagDocumentChunkResp(
        Long id,
        int chunkIndex,
        int chunkCount,
        int chunkStart,
        int chunkEnd,
        int length,
        String title
) {
}
