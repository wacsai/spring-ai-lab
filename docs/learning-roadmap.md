# Spring AI Learning Roadmap

## Current Baseline

当前仓库已完成 Phase 1A、Phase 1B、Phase 1C、Advisors 基础、Structured Output 和 Tool Calling 基础。

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
Ollama qwen3.5:4b
```

当前接口支持：

- `msg`
- `systemPrompt`
- `temperature`
- `topP`
- `topK`
- `numPredict`

当前接口：

- `POST /api/ai/chat`
- `POST /api/ai/chat/stream`
- `POST /api/ai/structured/movie/extract`
- `POST /api/ai/tool/chat`

后续按阶段推进，每个阶段只完成一个明确能力，并保持可运行、可验证。

## Phase 1A - Spring Boot + Spring AI + Ollama 最小闭环

目标：让 Spring Boot 通过 Spring AI 成功调用 Ollama。

学习内容：

- Spring AI Ollama Starter
- `spring.ai.ollama.base-url`
- `spring.ai.ollama.chat.model`
- `ChatModel`
- `ChatClient.Builder`
- Controller / Service / DTO 分层

验收：

```text
REST API
→ Service
→ ChatClient
→ Spring AI Ollama
→ http://192.168.0.50:11434
→ qwen3.5:4b
```

验收接口：

```text
POST /api/ai/chat
```

## Phase 1B - ChatClient / Prompt / Model Options

目标：掌握 ChatClient 的基础使用方式，而不是只完成一次模型调用。

学习内容：

- System Prompt
- User Prompt
- Prompt Template
- Temperature
- TopK / TopP
- Max Tokens / num-predict
- 请求级 Options 覆盖默认配置

验收：

- 可以通过不同接口或参数测试不同 Prompt
- 可以解释 System Prompt 和 User Prompt 的职责差异
- 可以在一次请求中覆盖默认模型参数

## Phase 1C - Streaming

目标：支持流式返回，理解普通调用和流式调用的差异。

学习内容：

- `ChatClient.stream()`
- SSE
- Servlet 与 Reactive 依赖边界
- 流式返回的错误处理

验收：

- 提供一个流式聊天接口
- 客户端可以逐段收到模型输出

## Phase 2 - Structured Output

目标：让模型输出可直接进入 Java 业务逻辑的数据结构。

学习内容：

- JSON Schema
- Java Record / DTO
- `.entity(...)`
- Output Validation
- `validateSchema()`
- JSON 反序列化失败处理

注意：

- 需要验证当前 Ollama 模型是否能稳定输出 JSON。
- 如果 reasoning / thinking 内容干扰 JSON 输出，应在本阶段解决，而不是拖到业务阶段。

## Phase 3 - Advisors 基础

目标：理解 Spring AI 中可复用 AI 能力的挂载方式。

学习内容：

- Advisor Chain
- Advisor 顺序
- `SimpleLoggerAdvisor`
- `ToolCallingAdvisor`
- `QuestionAnswerAdvisor`
- `MessageChatMemoryAdvisor`

验收：

- 可以解释 Advisor 与普通 Service 调用的区别
- 可以说明 Tool Calling、RAG、Memory 为什么都会用到 Advisor

## Phase 4 - Tool Calling

目标：让模型调用真实 Java 方法。

示例工具：

- GPU 状态
- Docker 容器状态
- 订单查询
- 数据库查询

重点：

- Tool Definition
- Tool Selection
- Tool Result
- Error Handling
- Permission Boundary
- Tool 调用日志
- Tool 调用失败时的模型反馈

验收：

- 至少提供一个只读、安全、可重复验证的工具
- 模型可以根据用户问题决定是否调用工具
- 工具执行失败时有明确错误返回

当前实现：

- 使用 `@Tool` 暴露 `LearningProgressTool#getSpringAiLearningProgress`
- 使用 `ChatClient.tools(learningProgressTool)` 在请求级注册工具
- 提供 `POST /api/ai/tool/chat`
- 当前工具只读，不访问数据库、不写文件、不调用外部系统

## Phase 5 - Embedding

目标：理解文本向量化。

重点：

- Embedding Model
- Vector Dimension
- Cosine Similarity
- TopK
- Chunking

注意：

- Chat Model 和 Embedding Model 是两类模型，不要默认复用 `qwen3.5:4b`。
- 进入 pgvector 前，必须先记录所选 Embedding Model 的向量维度。

## Phase 6 - PostgreSQL + pgvector

目标：完成第一套 VectorStore。

重点：

- pgvector
- Vector Index
- Metadata Filtering
- HNSW
- schema 初始化策略
- 文档 id 与 metadata 设计

## Phase 7 - RAG

目标：构建知识库问答。

流程：

```text
Document
→ Chunk
→ Embedding
→ pgvector
→ Retrieve
→ Prompt Context
→ Qwen
```

重点：

- Document Loader
- Text Splitter
- Embedding
- VectorStore
- Retriever
- Prompt Context
- 引用来源与回答边界

## Phase 8 - Chat Memory

目标：实现多轮上下文。

重点：

- Conversation ID
- ChatMemory
- Message Window
- Chat History vs Chat Memory
- Memory Repository
- Memory 与 Tool Calling 的边界

## Phase 9 - Agent

目标：实现第一个企业型 Agent。

建议项目：Server Ops Agent

Tools:

- getCpuStatus
- getMemoryStatus
- getGpuStatus
- getDiskStatus
- getDockerContainers
- getDockerLogs

目标：让 LLM 根据状态自主决定下一步工具调用。

边界：

- Agent 必须建立在已经跑通的 Tool Calling、Memory、状态管理之上。
- 不在前置阶段提前实现 Agent Loop。

## Phase 10 - MCP

目标：理解并实践标准化 Tool / Resource 接入。

重点：

- MCP Server
- MCP Client
- Tool / Resource 暴露
- 与本地 Java Tool Calling 的差异

## Phase 11 - Observability / Evaluation

目标：让 AI 功能可观测、可测试、可评估。

指标：

- Latency
- Token Usage
- Tool Call Accuracy
- Retrieval Accuracy
- Answer Quality
- Failure Rate

推进方式：

- 基础日志从 Phase 1 就开始保留。
- 完整评估体系放到本阶段集中建设。
