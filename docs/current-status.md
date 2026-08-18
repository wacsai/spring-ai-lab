# Current Status

Last Updated: 2026-08-18

## Completed

### Project Skeleton

- [x] Spring Boot 项目骨架已创建
- [x] 已接入 Spring AI Ollama Starter
- [x] 已配置 Ollama base-url 和 chat model
- [x] 已建立 Controller / Service / DTO 基础分层
- [x] 已提供 `POST /api/ai/chat`
- [x] `mvn -q -DskipTests compile` 已通过
- [x] Spring Boot 已启动验证
- [x] `POST /api/ai/chat` 已调用 Ollama 并返回模型回答
- [x] 已支持 System Prompt 模板
- [x] 已支持请求级 `temperature` / `topP` / `topK` / `numPredict`
- [x] 已完成请求级参数校验
- [x] 已通过全局异常处理返回字段级校验错误消息
- [x] 已提供 `POST /api/ai/chat/stream`
- [x] 已通过 SSE 验证流式输出
- [x] 已挂载 `SimpleLoggerAdvisor`
- [x] 已验证普通调用和流式调用均输出 Advisor 请求/响应日志
- [x] 已提供 `POST /api/ai/structured/movie/extract`
- [x] 已验证 `.entity(MovieExtractResp.class)` 结构化输出
- [x] 已处理非电影输入、字段缺失归一化和结构化输出失败响应
- [x] 已提供 `POST /api/ai/tool/chat`
- [x] 已通过 `@Tool` 暴露只读学习进度查询工具
- [x] 已通过 `@ToolParam` 暴露带参数订单状态查询工具
- [x] 已在 ChatClient 请求中注册 Java Tool
- [x] 已通过真实接口验证模型生成 `toolCalls` 并收到 `ToolResponseMessage`
- [x] 已通过真实接口验证模型可以从用户问题中提取 `orderNo` 并传入 Java Tool
- [x] 已提供 Tool Calling 失败的统一错误响应
- [x] 已配置 Ollama Embedding 模型 `qwen3-embedding:4b`
- [x] 已提供 `POST /api/ai/embedding`
- [x] 已通过真实接口验证文本向量化
- [x] 已确认 `qwen3-embedding:4b` 返回向量维度为 `2560`
- [x] 已提供 Embedding 失败的统一错误响应
- [x] 已加入 Spring Data JPA 和 PostgreSQL JDBC 依赖
- [x] 已配置本地 PostgreSQL datasource
- [x] 已准备 pgvector 初始化脚本 `db/schema.sql`
- [x] 已移除 `vector(2560)` 的 HNSW 索引初始化，避免超过 pgvector HNSW 维度限制
- [x] 已提供 `POST /api/ai/vector/documents`
- [x] 已提供 `POST /api/ai/vector/search`
- [x] 已实现 Embedding + pgvector 精确相似度检索代码
- [x] 已将 pgvector 相似度检索改为 Spring Data JPA `@Query(nativeQuery = true)`
- [x] 已验证 `@Query` 检索接口可返回语义相近文档

### GPU / Docker

- [x] NVIDIA Driver 正常
- [x] CUDA 正常
- [x] NVIDIA Container Toolkit 正常
- [x] Docker GPU 已验证

### Ollama

- [x] 裸机 Ollama 安装测试完成
- [x] 裸机版本已停止并禁用 systemd 自启动
- [x] Docker Ollama 部署完成
- [x] LAN API 已开放
- [x] `qwen3.5:4b` 已下载
- [x] API `/api/chat` 已验证
- [x] `ollama ps` 已验证 `100% GPU`

### ComfyUI

- [x] Linux 源码安装完成
- [x] Python venv 正常
- [x] GPU 正常
- [x] ComfyUI Manager 正常
- [x] Ollama Custom Node 已安装

## Current Stage

**PostgreSQL + pgvector 最小存取接口已实现，待真实接口验证**

当前真实状态：

```text
Client
↓
VectorDocumentController
↓
VectorDocumentService
↓
EmbeddingModel
↓
qwen3-embedding:4b
↓
float[] vector(2560)
↓
JPA EntityManager
↓
PostgreSQL 18 + pgvector
↓
ai_document_embedding
```

当前代码已经完成最小闭环、System Prompt 模板、请求级模型参数覆盖、普通调用、流式调用、基础 Advisor 挂载、结构化输出、Tool Calling 基础接口、带参数 Tool、Embedding 最小接口、JPA/PostgreSQL 依赖接入、pgvector 初始化脚本、文档向量入库接口和精确相似度检索接口。

当前数据库配置已启动到执行 schema 阶段；`vector(2560)` 字段可保留，但当前 pgvector HNSW 索引最多支持 2000 维，因此初始化脚本暂不创建 HNSW 索引。向量入库和检索代码已编译通过，检索接口已通过真实请求验证。

## Next Task

验证 **PostgreSQL + pgvector 最小存取接口**：

```text
Text
→ EmbeddingModel
→ Vector(2560)
→ PostgreSQL + pgvector
→ Similarity Search
```

验收标准：

- 将检索结果放入 Prompt
- 让 Chat Model 基于检索出的文档回答
- 进入 RAG 最小闭环

## Pending

- [ ] PostgreSQL + pgvector
- [ ] RAG
- [ ] Chat Memory
- [ ] Agent
- [ ] MCP
- [ ] Observability
- [ ] Evaluation
- [ ] Qdrant 对比实验
