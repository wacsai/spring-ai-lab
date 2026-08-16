package com.yytnet.fms.ailab.chat.dto.req;

import jakarta.validation.constraints.NotBlank;

public record ChatReq(
        @NotBlank(message = "信息不能为空")
        String message
) {
}
