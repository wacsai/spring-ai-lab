# Architecture

## 1. 当前目标架构

```text
Client / Frontend
        │
        ▼
Spring Boot Application
        │
        ├── Controller
        ├── Service
        ├── Repository
        │
        └── Spring AI
              │
              ├── ChatClient / ChatModel
              ├── Structured Output
              ├── Tool Calling
              ├── EmbeddingModel
              ├── VectorStore
              ├── RAG
              ├── Chat Memory
              ├── Agent
              └── MCP
                    │
          ┌─────────┼──────────┬──────────────────────────────┐
          ▼         ▼          ▼                              ▼
       Ollama    PostgreSQL   Local Tools              MCP Server Demo
          │       + pgvector
          ▼
     qwen3.5:4b
```

## 2. 基础设施

### Model Runtime

```text
Ubuntu Server
└── Docker
    └── Ollama
        └── qwen3.5:4b
```

API:

```text
http://192.168.0.50:11434
```

### Vector Storage

第一阶段使用：

```text
PostgreSQL 18 + pgvector
```

当前 Embedding 模型：

```text
qwen3-embedding:4b -> vector(2560)
```

后续对比：

```text
Qdrant
```

### Multimedia

```text
ComfyUI
→ Image / Video Generation

TTS
→ Voice

Lip Sync
→ Audio / Mouth Alignment

FFmpeg
→ Final Processing
```

## 3. 设计原则

- Spring Boot 是业务宿主
- Spring AI 是 AI 能力层
- Ollama 是模型运行时
- Qwen 是当前 LLM
- Embedding Model 与 Chat Model 分离
- pgvector / Qdrant 负责 Vector Search
- Tool Calling 负责连接真实业务能力
- MCP 用于标准化外部工具/资源接入
- Agent 建立在 Tool + State + Loop + Model 之上

## 4. MCP 当前最小架构

```text
Client
↓
spring-ai-lab:8080
↓
McpClientController
↓
McpClientChatService
↓
ChatClient
↓
Spring AI MCP Client
↓ HTTP + MCP /mcp
spring-ai-mcp-server-demo:8081
↓
McpLearningTool#getSpringAiMcpLearningProgress
McpOrderTool#getMcpOrderStatus(orderNo)
```

当前 MCP 阶段只验证远程工具调用闭环，不接数据库、不提前实现 Resource / Prompt / Sampling / Elicitation。
