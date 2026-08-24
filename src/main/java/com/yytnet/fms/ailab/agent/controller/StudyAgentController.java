package com.yytnet.fms.ailab.agent.controller;

import com.yytnet.fms.ailab.agent.dto.req.StudyAgentReq;
import com.yytnet.fms.ailab.agent.dto.resp.StudyAgentLoopResp;
import com.yytnet.fms.ailab.agent.dto.resp.StudyAgentResp;
import com.yytnet.fms.ailab.agent.dto.resp.StudyAgentStateResp;
import com.yytnet.fms.ailab.agent.service.StudyAgentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/agent")
public class StudyAgentController {

    private final StudyAgentService studyAgentService;

    public StudyAgentController(StudyAgentService studyAgentService) {
        this.studyAgentService = studyAgentService;
    }

    /**
     * 学习助手 Agent 最小闭环：
     * 把模型、学习进度工具、Chat Memory 和固定目标边界组合起来。
     */
    @PostMapping("/study")
    public StudyAgentResp chat(@Valid @RequestBody StudyAgentReq req) {
        return studyAgentService.chat(req);
    }

    /**
     * 显式 State + Step 版学习助手 Agent：
     * 先由程序记录工具观察结果，再让模型基于 State 生成最终回答。
     */
    @PostMapping("/study/steps")
    public StudyAgentStateResp chatWithSteps(@Valid @RequestBody StudyAgentReq req) {
        return studyAgentService.chatWithSteps(req);
    }

    /**
     * 动态 Loop 版学习助手 Agent：
     * 模型先决定下一步 action，服务层执行 action，再把 observation 放回 state，直到 FINISH 或达到最大步数。
     */
    @PostMapping("/study/loop")
    public StudyAgentLoopResp chatWithLoop(@Valid @RequestBody StudyAgentReq req) {
        return studyAgentService.chatWithLoop(req);
    }
}
