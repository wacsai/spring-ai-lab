package com.yytnet.fms.ailab.agent.dto.model;

/**
 * 模型在 Agent Loop 中输出的“下一步决策”。
 *
 * 注意：这不是直接返回给前端的业务结果，而是服务层让模型按固定结构回答：
 * - action：下一步要做什么
 * - reason：为什么这么做
 * - answer：如果 action=FINISH，这里放最终回答
 *
 * 当前阶段只开放 GET_LEARNING_PROGRESS 和 FINISH 两个动作，便于学习 Agent Loop 的本质。
 */
public record StudyAgentDecision(
        String action,
        String reason,
        String answer
) {
}
