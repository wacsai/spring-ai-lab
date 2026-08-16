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

**Spring AI Stage 1A: Spring Boot + Spring AI + Ollama 最小闭环已完成**

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
Spring AI Ollama
↓
qwen3.5:4b
```

当前代码已经完成最小闭环的代码结构、编译验证和真实接口调用验证。

## Next Task

进入 **Phase 1B - ChatClient / Prompt / Model Options**：

```text
System Prompt
→ User Prompt
→ Prompt Template
→ Model Options
→ 请求级参数覆盖
```

验收标准：

- 可以通过不同接口或参数测试不同 Prompt
- 可以解释 System Prompt 和 User Prompt 的职责差异
- 可以在一次请求中覆盖默认模型参数

## Pending

- [ ] Phase 1B: ChatClient / Prompt / Model Options
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
