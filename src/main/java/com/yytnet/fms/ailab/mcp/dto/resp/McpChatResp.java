package com.yytnet.fms.ailab.mcp.dto.resp;

import java.util.List;

public record McpChatResp(
        String content,
        String feature,
        String model,
        long durationMs,
        int mcpToolProviderCount,
        int mcpToolCount,
        List<String> mcpToolNames,
        String note
) {
}
