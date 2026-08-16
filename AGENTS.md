# AGENTS.md

本文件面向 Codex / Coding Agent，用于约束本仓库中的自动化开发行为。

## Project Goal

构建一个 Java / Spring Boot / Spring AI 的企业级 AI 应用学习项目。

当前优先目标是逐步完成：

```text
ChatClient
→ Structured Output
→ Tool Calling
→ Embedding
→ pgvector
→ RAG
→ Memory4
→ Agent
→ MCP
→ Observability / Evaluation
```

## Working Rules

1. 开始任务前先阅读：
   - `AI_CONTEXT.md`
   - `docs/current-status.md`
   - `docs/architecture.md`
   - `docs/decisions.md`

2. 不要一次性实现后续多个阶段。

3. 当前阶段之外的功能只允许预留接口，不要提前大规模实现。

4. 代码保持标准 Spring Boot 风格：
   - controller
   - service
   - dto
   - config
   - repository
   - domain
   - ai

5. AI 相关代码优先放在 `ai` 包下，并按能力拆分：

```text
ai/
├── chat
├── prompt
├── structured
├── tool
├── embedding
├── rag
├── memory
├── agent
└── mcp
```

6. 不要把 `ChatClient` 调用直接写入 Controller。

7. 所有外部地址、模型名称、Token、数据库连接均通过配置管理，不硬编码。

8. 默认 Ollama 地址：

```text
http://192.168.0.50:11434
```

9. 默认模型：

```text
qwen3.5:4b
```

10. 修改依赖或配置前：
    - 优先确认当前 Spring Boot / Spring AI 官方兼容关系
    - 避免使用已废弃 Starter / API

11. 每完成一个阶段：
    - 确保项目可编译
    - 运行测试或最小验证
    - 更新 `docs/current-status.md`

12. 重要技术选择写入 `docs/decisions.md`，包括：
    - 选择原因
    - 替代方案
    - 影响

## Safety / Change Discipline

- 不删除现有业务代码，除非任务明确要求
- 不执行危险系统命令
- 不直接修改生产配置
- 不提交真实密码、Token、API Key
- 不将 Ollama / Database 密钥写入 Git

## Expected Agent Behavior

当任务描述不完整时：

- 优先从仓库上下文判断
- 若仍无法确定，再提出最少量澄清问题
- 不要为了“完整”而扩展到未要求的阶段
