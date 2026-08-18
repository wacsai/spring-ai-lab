package com.yytnet.fms.ailab.embedding.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmbeddingReq(
        // 待向量化的文本。Embedding 阶段只验证“文本 -> 向量”，暂不接入 pgvector / RAG。
        @NotBlank(message = "文本不能为空")
        @Size(max = 2000, message = "文本不能超过2000个字符")
        String text
) {
}
