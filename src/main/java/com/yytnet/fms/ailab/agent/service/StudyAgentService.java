package com.yytnet.fms.ailab.agent.service;

import com.yytnet.fms.ailab.agent.dto.req.StudyAgentReq;
import com.yytnet.fms.ailab.agent.dto.resp.StudyAgentResp;
import com.yytnet.fms.ailab.agent.dto.resp.StudyAgentStateResp;
import com.yytnet.fms.ailab.agent.dto.resp.StudyAgentStepResp;
import com.yytnet.fms.ailab.common.exception.AiAgentException;
import com.yytnet.fms.ailab.memory.config.MemoryChatConfig;
import com.yytnet.fms.ailab.tool.dto.resp.LearningProgressResp;
import com.yytnet.fms.ailab.tool.service.LearningProgressTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudyAgentService {

    private static final String AGENT_TYPE = "study-agent-v1";
    private static final String STEPS_AGENT_TYPE = "study-agent-state-v1";

    // Memory 先处理历史上下文，Logger 再记录最终请求，方便观察 Agent 看到的上下文。
    private static final SimpleLoggerAdvisor SIMPLE_LOGGER_ADVISOR = SimpleLoggerAdvisor.builder()
            .order(1)
            .build();

    private static final String SYSTEM_PROMPT = """
            你是 spring-ai-lab 项目的学习助手 Agent。

            当前 Agent 的目标：
            - 帮助用户判断 Spring AI 学习项目的当前阶段和下一步
            - 必要时调用学习进度工具获取当前项目状态
            - 结合当前 conversationId 下的历史对话给出建议

            Agent 边界：
            - 当前是最小 Agent 闭环，只做学习规划和解释
            - 不执行文件修改、数据库写入、系统命令或外部请求
            - 不扩展到 MCP、Observability、Evaluation 的实现细节
            - 用户询问当前阶段、学习进度、下一步、Agent 怎么学时，必须调用学习进度工具

            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 明确说明建议来自“工具返回的学习进度”和“当前对话上下文”
            - 如果使用了工具结果，回答末尾包含：数据来源：LearningProgressTool
            """;

    private static final String STEPS_SYSTEM_PROMPT = """
            你是 spring-ai-lab 项目的学习助手 Agent。

            当前接口用于学习显式 Agent State + Step 记录。
            服务层已经完成 Step 1：调用 LearningProgressTool 获取学习进度。

            当前 Agent State：
            - goal: {goal}
            - toolObservation:
            {toolObservation}

            回答要求：
            - 使用中文回答
            - 先给结论，再给必要解释
            - 只基于当前 Agent State 和当前对话上下文回答
            - 明确说明这是“显式 State + Step 记录版 Agent”
            - 回答末尾包含：数据来源：LearningProgressTool
            """;

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final MessageChatMemoryAdvisor memoryAdvisor;
    private final LearningProgressTool learningProgressTool;
    private final String model;

    public StudyAgentService(ChatClient.Builder chatClientBuilder,
                             ChatMemory chatMemory,
                             LearningProgressTool learningProgressTool,
                             @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.chatMemory = chatMemory;
        this.memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .order(0)
                .build();
        this.learningProgressTool = learningProgressTool;
        this.model = model;
    }

    public StudyAgentResp chat(StudyAgentReq req) {
        try {
            String conversationId = req.conversationId().strip();

            String content = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(req.message())
                    // tools(...) 给 Agent 暴露可调用能力；是否调用由模型基于目标和工具描述决定。
                    .tools(learningProgressTool)
                    .advisors(advisor -> advisor
                            // Memory 给 Agent 提供会话状态；没有 conversationId，Agent 就无法区分不同学习会话。
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                            .advisors(memoryAdvisor, SIMPLE_LOGGER_ADVISOR))
                    .options(OllamaChatOptions.builder()
                            .model(model)
                            .disableThinking()
                            .temperature(0.2))
                    .call()
                    .content();

            return new StudyAgentResp(
                    conversationId,
                    content == null ? "" : content,
                    AGENT_TYPE,
                    chatMemory.get(conversationId).size(),
                    MemoryChatConfig.MAX_MEMORY_MESSAGES,
                    "这是最小 Agent 闭环：ChatClient + LearningProgressTool + Chat Memory + 固定目标边界；尚未实现多步执行循环。"
            );
        } catch (RuntimeException ex) {
            throw new AiAgentException("学习助手 Agent 调用失败", ex);
        }
    }

    public StudyAgentStateResp chatWithSteps(StudyAgentReq req) {
        try {
            String conversationId = req.conversationId().strip();
            String goal = req.message().strip();
            List<StudyAgentStepResp> steps = new ArrayList<>();

            // Step 1：显式调用工具并记录 observation。
            // 旧 /study 接口把工具调用交给模型自动决定；这里由服务层明确执行，方便学习 Agent State。
            LearningProgressResp progress = learningProgressTool.getSpringAiLearningProgress();
            steps.add(new StudyAgentStepResp(
                    1,
                    "TOOL_CALL: LearningProgressTool#getSpringAiLearningProgress",
                    formatProgressObservation(progress)
            ));

            // Step 2：把工具 observation 作为 Agent State 的一部分交给模型，生成最终回答。
            String answer = chatClient.prompt()
                    .system(system -> system
                            .text(STEPS_SYSTEM_PROMPT)
                            .param("goal", goal)
                            .param("toolObservation", formatProgressObservation(progress)))
                    .user(goal)
                    .advisors(advisor -> advisor
                            .param(ChatMemory.CONVERSATION_ID, conversationId)
                            .advisors(memoryAdvisor, SIMPLE_LOGGER_ADVISOR))
                    .options(OllamaChatOptions.builder()
                            .model(model)
                            .disableThinking()
                            .temperature(0.2))
                    .call()
                    .content();

            String normalizedAnswer = answer == null ? "" : answer;
            steps.add(new StudyAgentStepResp(
                    2,
                    "MODEL_CALL: ChatClient 基于 Agent State 生成最终学习建议",
                    summarizeAnswer(normalizedAnswer)
            ));

            return new StudyAgentStateResp(
                    conversationId,
                    STEPS_AGENT_TYPE,
                    goal,
                    true,
                    steps.size(),
                    steps,
                    normalizedAnswer,
                    chatMemory.get(conversationId).size(),
                    MemoryChatConfig.MAX_MEMORY_MESSAGES,
                    "这是显式 State + Step 记录版 Agent：当前固定两步执行，尚未实现 while 循环、动态规划和停止条件。"
            );
        } catch (RuntimeException ex) {
            throw new AiAgentException("学习助手 Agent Step 调用失败", ex);
        }
    }

    private String formatProgressObservation(LearningProgressResp progress) {
        return """
                currentStage: %s
                completedMilestones: %s
                nextStage: %s
                verification: %s
                source: %s
                """.formatted(
                progress.currentStage(),
                String.join("、", progress.completedMilestones()),
                progress.nextStage(),
                progress.verification(),
                progress.source()
        ).strip();
    }

    private String summarizeAnswer(String answer) {
        if (answer.isBlank()) {
            return "模型返回了空回答。";
        }
        int maxLength = 120;
        if (answer.length() <= maxLength) {
            return answer;
        }
        return answer.substring(0, maxLength) + "...";
    }
}
