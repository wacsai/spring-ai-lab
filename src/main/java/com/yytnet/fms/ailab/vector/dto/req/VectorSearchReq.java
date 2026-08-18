package com.yytnet.fms.ailab.vector.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VectorSearchReq(
        @NotBlank(message = "查询内容不能为空")
        @Size(max = 2000, message = "查询内容不能超过2000个字符")
        String query,

        // 返回最相似的前 N 条。学习阶段限制小一些，避免无索引查询返回过多数据。
        @Min(value = 1, message = "topK不能小于1")
        @Max(value = 20, message = "topK不能大于20")
        Integer topK
) {
}
