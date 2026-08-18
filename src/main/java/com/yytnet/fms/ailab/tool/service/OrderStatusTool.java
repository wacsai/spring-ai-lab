package com.yytnet.fms.ailab.tool.service;

import com.yytnet.fms.ailab.tool.dto.resp.OrderStatusResp;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderStatusTool {

    private static final Map<String, OrderStatusResp> MOCK_ORDERS = Map.of(
            "A1001", new OrderStatusResp(
                    true,
                    "A1001",
                    "已发货",
                    "运输中，当前到达上海转运中心",
                    "2026-08-20",
                    "订单 A1001 已发货，正在运输中。",
                    "OrderStatusTool#getOrderStatus"
            ),
            "A1002", new OrderStatusResp(
                    true,
                    "A1002",
                    "待付款",
                    "暂无物流信息",
                    null,
                    "订单 A1002 还未付款，暂时不会发货。",
                    "OrderStatusTool#getOrderStatus"
            ),
            "A1003", new OrderStatusResp(
                    true,
                    "A1003",
                    "已签收",
                    "已由本人签收",
                    "2026-08-15",
                    "订单 A1003 已完成签收。",
                    "OrderStatusTool#getOrderStatus"
            )
    );

    /**
     * 这是一个带参数的 Tool。
     * 模型需要先从用户问题中提取订单号，再把订单号放到 toolCalls.arguments 里交给 Spring AI。
     */
    @Tool(
            name = "getOrderStatus",
            description = "根据订单号查询模拟订单状态、物流信息和预计送达时间。只有用户询问订单状态、物流、发货、签收、付款情况时使用。"
    )
    public OrderStatusResp getOrderStatus(
            @ToolParam(description = "订单号，例如 A1001、A1002、A1003") String orderNo) {
        String normalizedOrderNo = normalizeOrderNo(orderNo);
        if (normalizedOrderNo == null) {
            return notFound("", "没有识别到订单号，请让用户提供订单号。");
        }

        OrderStatusResp order = MOCK_ORDERS.get(normalizedOrderNo);
        if (order == null) {
            return notFound(normalizedOrderNo, "没有查询到该订单，请确认订单号是否正确。");
        }
        return order;
    }

    private String normalizeOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return null;
        }
        return orderNo.trim().toUpperCase();
    }

    private OrderStatusResp notFound(String orderNo, String message) {
        return new OrderStatusResp(
                false,
                orderNo,
                null,
                null,
                null,
                message,
                "OrderStatusTool#getOrderStatus"
        );
    }
}
