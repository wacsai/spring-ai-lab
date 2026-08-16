# Spring AI Lab Context Pack

这套 Markdown 用于让 ChatGPT / Work / Codex 围绕同一套项目上下文协作。

建议放到 Spring AI 项目仓库根目录。

## Files

- `AI_CONTEXT.md`：长期项目背景、环境、总体目标
- `AGENTS.md`：Codex / Coding Agent 开发约束
- `docs/architecture.md`：当前架构设计
- `docs/current-status.md`：当前进度与下一步
- `docs/learning-roadmap.md`：Spring AI 学习路线
- `docs/decisions.md`：架构决策记录

## Usage

每次开始新的开发任务：

1. 先读取 `AI_CONTEXT.md`
2. 查看 `docs/current-status.md`
3. 按 `docs/learning-roadmap.md` 推进
4. 重大技术选择写入 `docs/decisions.md`
5. Codex 遵循 `AGENTS.md`
