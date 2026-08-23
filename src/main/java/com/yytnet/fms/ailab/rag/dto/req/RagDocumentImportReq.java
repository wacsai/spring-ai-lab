package com.yytnet.fms.ailab.rag.dto.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagDocumentImportReq(
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题不能超过200个字符")
        String title,

        // 当前阶段先接收纯文本长文档；PDF/Word/Markdown 文件解析属于后续阶段。
        @NotBlank(message = "内容不能为空")
        @Size(max = 20000, message = "内容不能超过20000个字符")
        String content,

        // 每个 chunk 的最大字符数；不传时使用 RagDocumentChunker 的默认值 500。
        @Min(value = 100, message = "chunkSize不能小于100")
        @Max(value = 2000, message = "chunkSize不能大于2000")
        Integer chunkSize,

        // 相邻 chunk 重叠的字符数；不传时使用 RagDocumentChunker 的默认值 80。
        @Min(value = 0, message = "overlap不能小于0")
        @Max(value = 500, message = "overlap不能大于500")
        Integer overlap
) {

    @AssertTrue(message = "overlap必须小于chunkSize")
    public boolean isChunkConfigValid() {
        int resolvedChunkSize = chunkSize == null ? 500 : chunkSize;
        int resolvedOverlap = overlap == null ? 80 : overlap;
        return resolvedOverlap < resolvedChunkSize;
    }
}
