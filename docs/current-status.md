# Current Status

Last Updated: 2026-08-17

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

**Spring AI Stage 1B: ChatClient / Prompt / Model Options 已完成**

当前真实状态：

```text
Client
↓
ChatController
↓
ChatService
↓
ChatClient
↓
System Prompt + User Prompt + Model Options
↓
Spring AI Ollama
↓
qwen3.5:4b
```

当前代码已经完成最小闭环、System Prompt 模板、请求级模型参数覆盖、编译验证和真实接口调用验证。

## Next Task

进入 **Phase 1C - Streaming**：

```text
ChatClient.stream()
→ SSE
→ 客户端逐段接收模型输出
```

验收标准：

- 提供一个流式聊天接口
- 客户端可以逐段收到模型输出
- 普通调用与流式调用边界清晰

## Pending

- [ ] Phase 1C: Streaming
- [ ] Advisors 基础
- [ ] Structured Output
- [ ] Tool Calling
- [ ] Embedding
- [ ] PostgreSQL + pgvector
- [ ] RAG
- [ ] Chat Memory
- [ ] Agent
- [ ] MCP
- [ ] Observability
- [ ] Evaluation
- [ ] Qdrant 对比实验
