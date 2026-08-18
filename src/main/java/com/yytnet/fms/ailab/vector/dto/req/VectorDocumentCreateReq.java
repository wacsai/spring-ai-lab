package com.yytnet.fms.ailab.vector.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VectorDocumentCreateReq(
        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题不能超过200个字符")
        String title,

        // 当前阶段每次只写入一段短文本；长文档切块属于后续 RAG 阶段。
        @NotBlank(message = "内容不能为空")
        @Size(max = 2000, message = "内容不能超过2000个字符")
        String content
) {
}
