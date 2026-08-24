package com.yytnet.fms.ailab.mcp.dto.resp;

public record McpChatResp(
        String content,
        int mcpToolProviderCount,
        int mcpToolCount,
        String note
) {
}
