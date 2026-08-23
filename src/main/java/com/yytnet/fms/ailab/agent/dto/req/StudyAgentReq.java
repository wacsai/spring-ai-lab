package com.yytnet.fms.ailab.agent.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudyAgentReq(
        // Agent 也复用 Chat Memory 的 conversationId。
        // 同一个 conversationId 可以让学习助手记住你前面问过什么、已经确认过什么。
        @NotBlank(message = "conversationId不能为空")
        @Size(max = 100, message = "conversationId不能超过100个字符")
        String conversationId,

        // 用户给 Agent 的目标或问题。
        // 第一版 Agent 只围绕“Spring AI 学习规划”工作，不做通用任务执行。
        @NotBlank(message = "消息不能为空")
        @Size(max = 2000, message = "消息不能超过2000个字符")
        String message
) {
}
