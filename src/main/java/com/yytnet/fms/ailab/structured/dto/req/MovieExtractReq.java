package com.yytnet.fms.ailab.structured.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MovieExtractReq(
        // 待分析的自然语言文本。这个接口只做电影信息提取，不承担开放聊天职责。
        @NotBlank(message = "文本不能为空")
        @Size(max = 2000, message = "文本不能超过2000个字符")
        String text
) {
}
