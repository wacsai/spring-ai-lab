package com.yytnet.fms.ailab.common.dto;

import java.util.List;

public record ErrorResp(
        int code,
        String message,
        List<FieldErrorResp> fields
) {
}
