# Phase Summary

本文总结 `spring-ai-lab` 第一轮 Spring AI 学习主线。

## 总体结论

第一轮学习主线已经完成。

当前项目已经从最初的 Controller demo，推进到一套覆盖企业 AI 应用核心能力的 Spring Boot 学习工程：

```text
模型调用
→ 结构化输出
→ 本地工具调用
→ 文本向量化
→ pgvector 检索
→ RAG
→ 多轮记忆
→ Agent Loop
→ MCP 远程工具
→ 轻量观测字段
```

当前仍属于学习 demo，不是生产级 AI 平台。生产级能力还需要权限、审计、限流、成本统计、Trace、评估集、回归报告和上线治理。

## Phase 1 - ChatClient

目标：

```text
REST API
→ Service
→ ChatClient
→ Ollama
→ qwen3.5:4b
```

关键代码：

```text
chat/controller/ChatController.java
chat/service/ChatService.java
chat/dto/req/ChatReq.java
```

学习重点：

- ChatClient 比 ChatModel 更适合企业开发中的 prompt、options、advisor、tools 组合。
- Controller 不直接调用 ChatClient，AI 调用放在 Service 层。
- 请求级 options 可以覆盖默认模型参数。

## Phase 2 - Structured Output

目标：

```text
自然语言输入
→ 模型输出 JSON
→ Spring AI 反序列化成 Java record
```

关键代码：

```text
structured/controller/MovieStructuredController.java
structured/service/MovieStructuredService.java
structured/dto/resp/MovieExtractResp.java
```

学习重点：

- Structured Output 适合固定任务，不适合所有随机用户问题。
- “导演是谁”“标题是什么”的判断主要由模型完成。
- Spring AI 负责 schema 约束、JSON 解析和对象映射。

## Phase 3 - Tool Calling

目标：

```text
模型判断是否需要工具
→ Spring AI 执行 Java @Tool 方法
→ 工具结果回到模型
→ 模型组织最终回答
```

关键代码：

```text
tool/service/LearningProgressTool.java
tool/service/OrderStatusTool.java
tool/service/ToolCallingService.java
```

学习重点：

- `@Tool` 的 `name` 和 `description` 是给模型看的工具定义。
- `@ToolParam` 用于描述工具参数。
- `parameters=true` 用于保留 Java 方法参数名，方便生成工具 schema。
- 模型决定是否调用工具，Java 负责真正执行工具。

## Phase 4 - Embedding / pgvector

目标：

```text
文本
→ EmbeddingModel
→ 2560 维向量
→ PostgreSQL + pgvector 存储和相似度检索
```

关键代码：

```text
embedding/service/EmbeddingService.java
vector/service/VectorDocumentService.java
vector/repository/VectorDocumentQueryRepository.java
vector/repository/VectorDocumentCommandRepository.java
```

学习重点：

- Embedding 把文本转换成固定维度向量。
- `qwen3-embedding:4b` 当前返回 2560 维。
- pgvector 的 `<=>` 表示 cosine distance，值越小越相似。
- `similarity = 1 - distance` 只是为了更直观展示。

## Phase 5 - RAG

目标：

```text
用户问题
→ query embedding
→ pgvector 检索相关 chunk
→ references 放入 Prompt
→ ChatClient 基于资料回答
```

关键代码：

```text
rag/controller/RagController.java
rag/service/RagService.java
rag/service/RagDocumentChunker.java
rag/service/RagMarkdownDocumentChunker.java
```

学习重点：

- RAG = Retrieval + Generation。
- 比较的是 query 向量和 chunk 向量的相似度。
- 当前支持长文档切分、Markdown 标题感知切分、来源过滤、引用摘要。
- 当前 chunking 是学习版，真实企业中会更重视文档结构、标题层级、表格、代码块和业务语义边界。

## Phase 6 - Memory

目标：

```text
conversationId
→ MessageChatMemoryAdvisor
→ JVM 内存消息窗口
→ 多轮对话上下文隔离
```

关键代码：

```text
memory/config/MemoryChatConfig.java
memory/service/MemoryChatService.java
memory/controller/MemoryChatController.java
```

学习重点：

- 当前 Memory 使用 JVM 内存，应用重启后丢失。
- 不同 `conversationId` 互相隔离。
- 企业中通常会把记忆放到数据库、Redis、对象存储或专门的会话存储中。

## Phase 7 - Agent

目标：

```text
Goal
→ State
→ Action
→ Observation
→ Loop
→ Stop Condition
```

关键代码：

```text
agent/service/StudyAgentService.java
agent/dto/model/StudyAgentDecision.java
agent/dto/resp/StudyAgentLoopResp.java
```

学习重点：

- Agent 不是单纯 LLM 调用，而是 LLM + Tools + State + Loop + Goal。
- 模型负责决定下一步 action，Java 负责执行 action 和安全边界。
- 当前实现了 `GET_LEARNING_PROGRESS`、`RAG_SEARCH`、`ASK_USER`、`FINISH`。
- 当前 Agent 是学习 demo，不继续深挖复杂编排和 prompt 调优。

## Phase 8 - MCP

目标：

```text
spring-ai-lab
→ MCP Client
→ HTTP + MCP
→ spring-ai-mcp-server-demo
→ 远程 Java @Tool
```

关键代码：

```text
mcp/controller/McpClientController.java
mcp/service/McpClientChatService.java
mcp/dto/resp/McpChatResp.java
```

配套项目：

```text
/Users/wacsai/Data/yytnet/1221/spring-ai-mcp-server-demo
```

学习重点：

- MCP 可以先理解成把 Tool Calling 的工具能力移动到外部服务。
- 主项目只配置 MCP Server 地址，不配置具体工具。
- 工具通过 MCP 协议自动发现。
- ChatClient 仍然通过 `.tools(...)` 注册工具，只是工具来源变成远程 `ToolCallbackProvider`。
- 已验证无参数工具和带参数订单工具。

## Phase 9 - 轻量 Observability / Evaluation

目标：

```text
不用复杂平台
→ 先让接口响应携带关键诊断字段
→ 形成最小可排查能力
```

当前实现：

```text
POST /api/ai/mcp/chat
→ feature
→ model
→ durationMs
→ mcpToolProviderCount
→ mcpToolCount
→ mcpToolNames
```

学习重点：

- Observability 是看清楚 AI 调用发生了什么。
- Evaluation 是判断回答质量是否稳定、是否退化。
- 当前阶段不接 ELK、Prometheus、Grafana、OpenTelemetry、Langfuse、Ragas。
- 后续 Evaluation 可从固定问题、关键字断言和结构性断言开始。

## 当前状态

第一轮主线已经完成。

已覆盖：

```text
ChatClient
Structured Output
Tool Calling
Embedding
pgvector
RAG
Memory
Agent
MCP
Lightweight Observability
```

后续增强方向：

```text
Evaluation 最小回归测试
Qdrant 对比实验
生产级权限 / 审计 / Trace / 成本统计
```
