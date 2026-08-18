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

**Spring AI Tool Calling 基础已完成**

当前真实状态：

```text
Client
↓
ToolCallingController
↓
ToolCallingService
↓
ChatClient
↓
System Prompt + User Prompt + Model Options + Java Tool
↓
SimpleLoggerAdvisor
↓
call()
↓
Spring AI Tool Calling
↓
LearningProgressTool#getSpringAiLearningProgress / OrderStatusTool#getOrderStatus(orderNo)
↓
Spring AI Ollama
↓
qwen3.5:4b
```

当前代码已经完成最小闭环、System Prompt 模板、请求级模型参数覆盖、普通调用、流式调用、基础 Advisor 挂载、结构化输出、Tool Calling 基础接口、带参数 Tool、编译验证和真实接口调用验证。

## Next Task

进入 **Embedding**：

```text
Text
→ EmbeddingModel
→ Vector
→ Similarity
```

验收标准：

- 选定一个 Embedding 模型
- 记录向量维度
- 提供最小文本向量化接口
- 可以解释 Chat Model 和 Embedding Model 的区别

## Pending

- [ ] Embedding
- [ ] PostgreSQL + pgvector
- [ ] RAG
- [ ] Chat Memory
- [ ] Agent
- [ ] MCP
- [ ] Observability
- [ ] Evaluation
- [ ] Qdrant 对比实验
