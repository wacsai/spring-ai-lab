package com.yytnet.fms.ailab.tool.dto.resp;

public record OrderStatusResp(
        boolean found,
        String orderNo,
        String status,
        String logistics,
        String estimatedDelivery,
        String message,
        String source
) {
}
