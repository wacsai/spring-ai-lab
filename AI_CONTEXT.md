# Spring AI Lab - AI Context

## 1. 项目目标

这是一个面向 **Java / Spring Boot AI 应用层开发** 的学习与实践项目。

目标不是训练基础模型，而是掌握企业级 AI 应用开发，包括：

- Spring AI
- LLM 应用开发
- Structured Output
- Tool Calling
- Embedding
- Vector Database
- RAG
- Chat Memory
- Agent
- MCP
- Observability
- Evaluation
- 私有化模型部署

最终目标：形成一个可用于实际项目与求职展示的 Java AI 应用工程。

## 2. 当前技术背景

- Java / Spring Boot 后端开发
- 熟悉 Spring Boot、JPA / MyBatis
- 熟悉 PostgreSQL、Redis
- 熟悉 Docker、Linux
- 熟悉常规企业后端架构与服务治理

## 3. 当前 AI 基础设施

### Linux AI Server

- OS: Ubuntu
- GPU: NVIDIA GeForce RTX 2080 Ti 22GB
- NVIDIA Driver / CUDA: 已正常
- NVIDIA Container Toolkit: 已正常
- Docker GPU: 已验证正常

### Ollama

- 部署方式: Docker
- LAN API: `http://192.168.0.50:11434`
- 当前模型: `qwen3.5:4b`
- GPU 状态: 已验证 `100% GPU`

### ComfyUI

- 部署方式: Linux 源码 + Python venv
- 运行正常
- 与 Ollama 位于同一台 Linux 服务器

## 4. 当前架构理解

```text
Spring Boot
    ↓
Spring AI
    ↓
┌──────────────┬──────────────┬──────────────┐
│              │              │              │
LLM           RAG          Tools / MCP      Agent
│              │
Ollama       Embedding
│              ↓
Qwen        pgvector / Qdrant
```

Spring Boot 负责：

- Controller / API
- Service / Domain Logic
- Persistence
- Security
- Task / Workflow
- Configuration
- Observability

Spring AI 负责：

- ChatModel / ChatClient
- Prompt
- Structured Output
- EmbeddingModel
- VectorStore
- RAG
- Chat Memory
- Tool Calling
- MCP
- Agentic Patterns

## 5. Agent 基础认知

- LLM != Agent
- Agent = LLM + Tools + State/Memory + Execution Loop + Goal
- Tool Calling 是 Agent 的基础能力之一
- Workflow: 主要流程由程序预定义
- Agent: 下一步更多由 LLM 根据当前状态动态决定
- Harness: 模型外的执行环境，例如 Tools、Agent Loop、Context、State、Sandbox、Permission、Session 等
- DeepSeek Harness 当前不是本项目的必需组件

## 6. 当前阶段

当前真实状态：

**Spring AI Tool Calling 基础已完成**

当前代码已经从 Controller demo 推进到基础 AI 调用链：

```text
POST /api/ai/chat
↓
ChatController
↓
ChatService
↓
ChatClient
↓
System Prompt + User Prompt + Model Options
↓
SimpleLoggerAdvisor
↓
call() / stream()
↓
Spring AI Ollama
↓
qwen3.5:4b
```

当前已完成：

```text
代码结构 + 编译验证 + 真实接口调用验证 + System Prompt + 请求级 Model Options + Streaming + SimpleLoggerAdvisor + Structured Output + Tool Calling
```

已验证：

```text
POST /api/ai/chat
→ Ollama
→ qwen3.5:4b
→ 返回模型回答

POST /api/ai/chat/stream
→ SSE
→ 逐段返回模型输出

POST /api/ai/structured/movie/extract
→ MovieExtractResp
→ 返回电影信息提取结构化对象

POST /api/ai/tool/chat
→ ChatClient 注册 Java Tool
→ 模型按需调用 LearningProgressTool
→ 基于工具结果返回回答
```

当前接口支持：

```text
msg
systemPrompt
temperature
topP
topK
numPredict
```

当前结构化输出接口：

```text
POST /api/ai/structured/movie/extract
```

当前 Tool Calling 接口：

```text
POST /api/ai/tool/chat
```

目标链路：

```text
Mac Spring Boot
    ↓
Spring AI
    ↓
http://192.168.0.50:11434
    ↓
Docker Ollama
    ↓
qwen3.5:4b
    ↓
RTX 2080 Ti
```

## 7. 学习顺序

1. Spring Boot + Spring AI + Ollama 最小闭环
2. ChatClient / Prompt / Model Options
3. Streaming
4. Structured Output
5. Advisors 基础
6. Tool Calling
7. Embedding
8. PostgreSQL + pgvector
9. RAG
10. Chat Memory
11. Agent
12. MCP
13. Observability / Evaluation

## 8. 开发原则

- 一次只完成一个阶段
- 每个阶段都要可运行、可验证
- 优先使用官方 Spring AI 能力
- 版本和 Starter 名称需要以当前官方文档为准
- 保持标准 Spring Boot 分层
- 不把 AI 调用直接堆在 Controller
- 每个阶段完成后更新 `docs/current-status.md`
- 重要架构决策写入 `docs/decisions.md`
