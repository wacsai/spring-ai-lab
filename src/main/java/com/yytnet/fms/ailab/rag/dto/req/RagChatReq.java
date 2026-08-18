package com.yytnet.fms.ailab.rag.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RagChatReq(
        @NotBlank(message = "问题不能为空")
        @Size(max = 2000, message = "问题不能超过2000个字符")
        String question,

        // 先从向量库取最相似的前 N 条，再按 maxDistance 过滤。
        @Min(value = 1, message = "topK不能小于1")
        @Max(value = 20, message = "topK不能大于20")
        Integer topK,

        // pgvector cosine distance 越小越相似。
        // 这里的阈值用于避免把明显无关的文档塞给 Chat Model。
        @DecimalMin(value = "0.0", message = "maxDistance不能小于0")
        @DecimalMax(value = "2.0", message = "maxDistance不能大于2")
        Double maxDistance
) {
}
