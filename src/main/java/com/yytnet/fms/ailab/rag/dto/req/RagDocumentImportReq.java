package com.yytnet.fms.ailab.rag.dto.req;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        Integer overlap,

        // 学习阶段经常会反复导入同一篇文档。
        // true 表示先删除相同 title 对应的旧 chunk，再写入本次新切分出来的 chunk，避免检索时命中重复资料。
        Boolean replaceExisting,

        // 资料来源类型。不传时默认 MANUAL，表示通过当前 API 手动传入文本。
        // 后续接入 Markdown/PDF/URL 导入时，可以复用这个字段区分来源。
        @Pattern(regexp = "MANUAL|MARKDOWN|PDF|URL|", message = "sourceType只支持MANUAL、MARKDOWN、PDF、URL")
        String sourceType,

        // 来源名称，通常是文件名、网页标题、知识库名称；不传时默认使用 title。
        @Size(max = 200, message = "sourceName不能超过200个字符")
        String sourceName,

        // 外部系统里的唯一标识，例如文件路径、对象存储 key、业务表主键或 URL；手动输入可以不传。
        @Size(max = 500, message = "externalId不能超过500个字符")
        String externalId
) {

    @AssertTrue(message = "overlap必须小于chunkSize")
    public boolean isChunkConfigValid() {
        int resolvedChunkSize = chunkSize == null ? 500 : chunkSize;
        int resolvedOverlap = overlap == null ? 80 : overlap;
        return resolvedOverlap < resolvedChunkSize;
    }
}
