# Architecture Decision Log

## ADR-001: Java AI 主线使用 Spring AI

Status: Accepted

### Decision

以 Spring Boot + Spring AI 作为 Java AI 应用开发主线。

### Reason

- 与现有 Java / Spring 技术栈一致
- 适合企业业务系统集成
- 支持 Chat、Embedding、VectorStore、RAG、Tool Calling、MCP 等能力

### Alternatives

- LangChain
- LangGraph

### Follow-up

后续学习 LangChain / LangGraph，用于理解 Python AI 生态和复杂 Agent 编排。

---

## ADR-002: 当前 LLM Runtime 使用 Ollama

Status: Accepted

### Decision

本地模型通过 Docker Ollama 运行。

### Current Model

```text
qwen3.5:4b
```

### Endpoint

```text
http://192.168.0.50:11434
```

### Reason

- 本地部署简单
- GPU 已验证
- 便于 Spring AI / ComfyUI / Mac 开发环境共同调用

---

## ADR-003: 第一套向量存储使用 PostgreSQL + pgvector

Status: Planned

### Decision

先使用 PostgreSQL + pgvector 完成 RAG。

### Reason

- 已熟悉 PostgreSQL
- 与企业 Java 应用集成简单
- 便于学习向量存储本质

### Follow-up

第二阶段增加 Qdrant，对比专业 Vector DB 与 pgvector 的差异。

---

## ADR-004: DeepSeek Harness 暂不纳入当前架构

Status: Accepted

### Decision

当前项目不引入 DeepSeek Harness。

### Reason

当前目标是自行使用 Spring AI 构建企业 Agent，而不是使用独立 Agent Harness 作为主要运行时。

---

## ADR-005: 学习路线按最小闭环逐阶段推进

Status: Accepted

### Decision

当前仓库先从 Spring Boot + Spring AI + Ollama 的最小闭环开始，不提前实现 Structured Output、Tool Calling、Embedding、RAG、Memory、Agent 或 MCP。

第一阶段只完成：

```text
REST API
→ Service
→ ChatClient
→ Ollama
→ qwen3.5:4b
```

### Reason

- 当前代码只有一个 Controller demo，正式 Spring AI 集成尚未开始。
- 先完成可运行、可验证的最小链路，便于定位依赖、配置、网络和模型调用问题。
- 后续能力之间存在前置关系，例如 RAG 依赖 Embedding 和 VectorStore，Agent 依赖 Tool Calling、Memory 和状态管理。

### Impact

- 每个阶段只引入当前阶段必需的依赖和配置。
- 每完成一个阶段更新 `docs/current-status.md`。
- 后续阶段可以预留包结构或接口，但不提前大规模实现。

---

## ADR-006: Structured Output 使用固定任务接口

Status: Accepted

### Decision

Structured Output 阶段不改造开放聊天接口，而是新增固定任务接口：

```text
POST /api/ai/structured/movie/extract
```

该接口只负责从自然语言文本中提取电影信息，并返回 `MovieExtractResp`。

### Reason

- Structured Output 适合业务目标明确、输出结构固定的任务。
- 开放聊天问题是随机的，不应强行套入电影对象。
- `MovieExtractResp` 使用 `movieRelated` 表达输入是否与电影信息相关，避免非电影输入时硬编字段。

### Impact

- `POST /api/ai/chat` 继续返回自然语言文本。
- `POST /api/ai/structured/movie/extract` 返回 Java record 可直接表达的结构化结果。
- 当前阶段不提前抽象多模型 Provider 的通用结构化输出层。

---

## ADR-007: Tool Calling 先使用只读学习进度工具

Status: Accepted

### Decision

Tool Calling 阶段新增独立接口：

```text
POST /api/ai/tool/chat
```

该接口通过 `ChatClient.tools(learningProgressTool)` 注册一个带 `@Tool` 注解的 Java 本地方法：

```text
LearningProgressTool#getSpringAiLearningProgress
```

### Reason

- 当前目标是先理解模型选择工具、Spring AI 执行 Java 方法、模型基于工具结果回答的基本链路。
- 学习进度查询是只读、无副作用、可重复验证的工具，适合作为第一个 Tool Calling demo。
- 暂不接入数据库、订单、服务器状态等真实业务能力，避免把 Tool Calling 和后续业务集成复杂度混在一起。

### Alternatives

- 直接接入数据库查询工具
- 直接接入 Linux / Docker / GPU 状态工具
- 在原 `POST /api/ai/chat` 中直接注册工具

### Impact

- `POST /api/ai/chat` 继续保持普通聊天职责。
- `POST /api/ai/tool/chat` 专门用于学习 Tool Calling。
- 后续真实业务工具可以按相同模式扩展，但需要增加权限边界、参数校验、审计日志和失败处理。

---

## ADR-008: Tool Calling 使用内存订单工具学习参数提取

Status: Accepted

### Decision

在 `POST /api/ai/tool/chat` 中继续注册第二个只读工具：

```text
OrderStatusTool#getOrderStatus(orderNo)
```

该工具通过 `@ToolParam` 声明 `orderNo` 参数，并使用内存 `Map` 模拟订单数据。

### Reason

- 无参数 Tool 只能证明模型可以调用 Java 方法，不能证明模型可以从用户问题中提取参数。
- 企业业务 Tool 通常都需要参数，例如订单号、用户 ID、时间范围、商品编码。
- 使用内存订单数据可以聚焦学习 `@ToolParam`、`toolCalls.arguments` 和 ToolResponseMessage，不提前引入数据库。

### Impact

- 当前 Tool Calling 阶段已覆盖无参数工具和带参数工具。
- `pom.xml` 中 Maven Compiler 开启 `parameters=true`，用于保留 Java 方法参数名，方便 Spring AI 生成工具参数 schema。
- 后续接入真实订单、数据库或外部 API 时，可以沿用该模式，但必须补充权限校验、参数校验和审计日志。
