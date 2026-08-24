# Spring AI Lab

这是一个面向 Java / Spring Boot 开发者的 Spring AI 学习项目。

项目目标不是训练模型，而是用 Spring Boot + Spring AI 跑通企业 AI 应用常见能力：

```text
ChatClient
→ Structured Output
→ Tool Calling
→ Embedding
→ PostgreSQL + pgvector
→ RAG
→ Chat Memory
→ Agent
→ MCP
→ 轻量 Observability / Evaluation
```

当前第一轮学习主线已经完成。项目保留了各阶段的独立接口，便于回顾和对比。

## 技术栈

- Java 25
- Spring Boot 4.1.0
- Spring AI 2.0.0
- Ollama
- PostgreSQL 18 + pgvector
- Maven

默认模型：

```text
Chat Model: qwen3.5:4b
Embedding Model: qwen3-embedding:4b
Ollama Base URL: http://192.168.0.50:11434
```

## 配套项目

MCP 阶段使用了一个独立 MCP Server：

```text
/Users/wacsai/Data/yytnet/1221/spring-ai-mcp-server-demo
```

当前主项目作为 MCP Client，默认连接：

```text
http://localhost:8081/mcp
```

## 启动前配置

本项目需要 PostgreSQL + pgvector。数据库连接通过环境变量提供：

```text
SPRING_DATASOURCE_URL
SPRING_DATASOURCE_USERNAME
SPRING_DATASOURCE_PASSWORD
```

如果使用 IDEA，可以通过 `.env` 或 Run Configuration 环境变量注入。不要把真实密码提交到 Git。

## 启动

先启动 MCP Server：

```bash
cd /Users/wacsai/Data/yytnet/1221/spring-ai-mcp-server-demo
mvn spring-boot:run
```

再启动主项目：

```bash
cd /Users/wacsai/Data/yytnet/1221/spring-ai-lab
mvn spring-boot:run
```

本项目默认端口：

```text
8080
```

MCP Server 默认端口：

```text
8081
```

## 主要接口

基础聊天：

```text
POST /api/ai/chat
POST /api/ai/chat/stream
```

结构化输出：

```text
POST /api/ai/structured/movie/extract
```

Tool Calling：

```text
POST /api/ai/tool/chat
```

Embedding / pgvector：

```text
POST /api/ai/embedding
POST /api/ai/vector/documents
POST /api/ai/vector/search
```

RAG：

```text
POST /api/ai/rag/chat
POST /api/ai/rag/documents
POST /api/ai/rag/documents/files
```

Memory：

```text
POST /api/ai/memory/chat
DELETE /api/ai/memory/conversations/{conversationId}
```

Agent：

```text
POST /api/ai/agent/study
POST /api/ai/agent/study/steps
POST /api/ai/agent/study/loop
```

MCP：

```text
POST /api/ai/mcp/chat
```

## MCP 验证示例

```http
POST http://localhost:8080/api/ai/mcp/chat
Content-Type: application/json

{
  "msg": "帮我查一下订单 A1001 的物流状态"
}
```

预期可以看到：

```text
mcpToolProviderCount = 1
mcpToolCount = 2
mcpToolNames 包含 getMcpOrderStatus 和 getSpringAiMcpLearningProgress
```

## 当前观测方式

当前不接 ELK / Prometheus / Grafana / OpenTelemetry 等复杂平台。

学习阶段使用轻量方式观察 AI 调用：

- `SimpleLoggerAdvisor` 输出 ChatClient 请求和响应
- RAG 接口返回 `references` / `rejectedReferences` / `citations`
- Agent 接口返回 `steps` / `action` / `observation` / `stopReason`
- MCP 接口返回 `feature` / `model` / `durationMs` / `mcpToolNames`

## 文档

- `AI_CONTEXT.md`：长期项目背景和当前阶段
- `docs/current-status.md`：当前进度与下一步
- `docs/architecture.md`：架构说明
- `docs/decisions.md`：架构决策记录
- `docs/learning-roadmap.md`：学习路线
- `docs/phase-summary.md`：第一轮学习阶段总结

## 验证

```bash
mvn -q -DskipTests compile
mvn -q test
```
