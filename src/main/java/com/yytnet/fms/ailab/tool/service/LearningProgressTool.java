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
                "Tool Calling 基础已完成",
                List.of(
                        "ChatClient 普通调用",
                        "System Prompt",
                        "请求级模型参数",
                        "Streaming SSE",
                        "SimpleLoggerAdvisor",
                        "Structured Output 电影信息提取",
                        "Tool Calling 学习进度查询工具"
                ),
                "Embedding",
                "当前工具返回的是固定学习状态，用于验证模型能按需调用 Java 本地方法。",
                "LearningProgressTool#getSpringAiLearningProgress"
        );
    }
}
