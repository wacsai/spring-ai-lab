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

**RAG Markdown 标题感知切分已实现**

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
代码结构 + 编译验证 + 真实接口调用验证 + System Prompt + 请求级 Model Options + Streaming + SimpleLoggerAdvisor + Structured Output + Tool Calling + Embedding + JPA/PostgreSQL 配置 + pgvector 最小存取接口 + RAG 最小闭环 + RAG 文档切分入库 + RAG 检索诊断 + RAG 自然切分策略 + RAG 引用摘要 + RAG 同名文档替换导入 + RAG 文档来源元数据 + TXT/Markdown 文件导入 + Markdown 标题感知切分
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
→ 模型按需调用 LearningProgressTool 或 OrderStatusTool
→ 基于工具结果返回回答

POST /api/ai/embedding
→ EmbeddingModel
→ qwen3-embedding:4b
→ 返回 2560 维向量

POST /api/ai/vector/documents
→ content 生成 embedding
→ JPA EntityManager 原生 SQL
→ 写入 ai_document_embedding

POST /api/ai/vector/search
→ query 生成 embedding
→ Spring Data JPA `@Query(nativeQuery = true)`
→ pgvector cosine distance `<=>`
→ 返回最相似文档

POST /api/ai/rag/chat
→ question 生成 embedding
→ pgvector 检索相似文档
→ 按 maxDistance 过滤参考资料
→ 把参考资料放入 System Prompt
→ ChatClient 基于资料生成回答
→ 返回 answer + citations + references + rejectedReferences + source 元数据 + 检索计数

POST /api/ai/rag/documents
→ 长文本 content
→ sourceType/sourceName/externalId 记录资料来源
→ replaceExisting=true 时先删除相同 documentTitle 的旧 chunk
→ 优先按句子/换行自然边界切分
→ 超长自然单元按字符兜底切分
→ 按 chunkSize + overlap 组合多个 chunk
→ 每个 chunk 生成 embedding
→ 每个 chunk 作为独立向量记录写入 pgvector
→ 返回 chunk id / chunkIndex / chunkStart / chunkEnd

POST /api/ai/rag/documents/files
→ 上传 UTF-8 .txt / .md 文件
→ .txt 自动 sourceType=TEXT
→ .md/.markdown 自动 sourceType=MARKDOWN
→ sourceName/externalId 默认使用原始文件名
→ Markdown 先按标题拆成 section
→ 每个 section 再复用通用 chunker
→ 读取文本后复用 POST /api/ai/rag/documents 的入库流程
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

当前 Embedding 接口：

```text
POST /api/ai/embedding
```

当前 Embedding 配置：

```text
spring.ai.ollama.embedding.model = qwen3-embedding:4b
实际向量维度 = 2560
```

当前 PostgreSQL / pgvector 配置：

```text
spring.datasource.url = jdbc:postgresql://localhost:5432/spring_ai_lab
spring.datasource.username = spring_ai_lab
spring.datasource.password = spring_ai_lab_pwd
schema.sql = CREATE EXTENSION IF NOT EXISTS vector + ai_document_embedding vector(2560)
```

当前 RAG 接口：

```text
POST /api/ai/rag/chat
POST /api/ai/rag/documents
```

```text
POST /api/ai/vector/documents
POST /api/ai/vector/search
```

说明：`POST /api/ai/vector/search` 已通过 `@Query(nativeQuery = true)` 真实接口验证。

当前 Tool Calling 已验证：

```text
用户问题包含订单号 A1001
→ 模型生成 toolCalls: getOrderStatus({"orderNo":"A1001"})
→ Spring AI 执行 OrderStatusTool#getOrderStatus
→ ToolResponseMessage 带回订单状态
→ 模型生成最终回答
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
