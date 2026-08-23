package com.yytnet.fms.ailab.rag.dto.resp;

import java.util.List;

public record RagDocumentImportResp(
        String title,
        int contentLength,
        int chunkSize,
        int overlap,
        boolean replaceExisting,
        int deletedCount,
        int chunkCount,
        List<RagDocumentChunkResp> chunks,
        String note
) {
}
