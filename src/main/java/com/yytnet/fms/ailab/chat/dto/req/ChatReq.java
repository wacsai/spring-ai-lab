package com.yytnet.fms.ailab.chat.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatReq(
        @NotBlank(message = "信息不能为空")
        String msg,

        // 可选的额外系统提示词，用来临时补充本次请求的模型行为约束。
        @Size(max = 1000, message = "系统提示词不能超过1000个字符")
        String systemPrompt,

        // 以下参数是请求级模型参数，只影响当前这一次模型调用。
        @DecimalMin(value = "0.0", message = "temperature不能小于0")
        @DecimalMax(value = "2.0", message = "temperature不能大于2")
        Double temperature,

        @DecimalMin(value = "0.0", message = "topP不能小于0")
        @DecimalMax(value = "1.0", message = "topP不能大于1")
        Double topP,

        @Min(value = 1, message = "topK不能小于1")
        @Max(value = 100, message = "topK不能大于100")
        Integer topK,

        @Min(value = 1, message = "numPredict不能小于1")
        @Max(value = 8192, message = "numPredict不能大于8192")
        Integer numPredict
) {
}
