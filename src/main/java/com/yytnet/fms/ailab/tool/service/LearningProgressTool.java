package com.yytnet.fms.ailab.tool.service;

import com.yytnet.fms.ailab.tool.dto.resp.LearningProgressResp;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LearningProgressTool {

    /**
     * @Tool 会把这个 Java 方法声明为一个“模型可调用工具”。
     * 但它不会自动暴露给所有模型请求，只有在 ChatClient 调用链中注册这个 bean，模型才可能调用它。
     */
    @Tool(
            name = "getSpringAiLearningProgress",
            description = "查询 spring-ai-lab 当前 Spring AI 学习进度、已完成能力和下一步学习内容。用户询问当前阶段、学习进度、下一步时使用。"
    )
    public LearningProgressResp getSpringAiLearningProgress() {
        // 这个工具是只读的：不查数据库、不写文件、不调用外部系统，方便先验证 Tool Calling 的核心链路。
        return new LearningProgressResp(
                "Agent 基础阶段已完成，准备进入 MCP 基础学习",
                List.of(
                        "ChatClient 普通调用",
                        "System Prompt",
                        "请求级模型参数",
                        "Streaming SSE",
                        "SimpleLoggerAdvisor",
                        "Structured Output 电影信息提取",
                        "Tool Calling 学习进度查询工具",
                        "Tool Calling 带参数订单查询工具",
                        "Embedding 文本向量化",
                        "PostgreSQL + pgvector 向量入库和相似度检索",
                        "RAG 最小闭环",
                        "RAG 文档切分、文件导入、来源过滤和引用摘要",
                        "Chat Memory JVM 内存版多轮会话",
                        "Chat Memory 会话清理接口",
                        "Agent 学习助手最小闭环",
                        "Agent 显式 State + Step 记录",
                        "Agent 动态 Loop + Stop Condition",
                        "Agent Loop RAG_SEARCH 检索动作",
                        "Agent Loop ASK_USER 澄清动作",
                        "Agent Loop 重复 action 保护",
                        "Agent Loop RAG_SEARCH query 保守归一化",
                        "Agent 基础阶段收口"
                ),
                "MCP",
                "当前 Agent 是学习 demo，已覆盖 Goal、State、Action、Observation、Loop、Stop Condition、Memory 和 Java 安全边界；模型 action 选择仍存在不稳定性，暂不继续做 Prompt 调优，后续在 Evaluation / Observability 阶段再系统评估。",
                "LearningProgressTool#getSpringAiLearningProgress"
        );
    }
}
