# Current Status

Last Updated: 2026-08-17

## Completed

### Project Skeleton

- [x] Spring Boot 项目骨架已创建
- [x] 当前只有一个 Controller demo: `POST /chat/test`
- [x] 当前代码还没有接入 Spring AI
- [x] 当前代码还没有调用 Ollama
- [x] 当前代码还没有 Service / DTO 分层

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

**Spring AI Stage 0: 项目骨架已创建，正式 Spring AI 学习尚未开始**

当前真实状态：

```text
Client
↓
ChatController
↓
"test"
```

目标不是在当前阶段补齐所有 AI 能力，而是先进入最小可运行闭环。

## Next Task

进入 **Phase 1A - Spring Boot + Spring AI + Ollama 最小闭环**，完成：

```text
REST API
↓
Service
↓
Spring AI ChatClient
↓
Ollama
↓
qwen3.5:4b
```

验收标准：

- Spring Boot 可正常启动
- Spring AI Ollama Starter 配置正确
- 可调用 `http://192.168.0.50:11434`
- `/api/ai/chat` 能返回模型回答
- Controller / Service / DTO 分层完成

## Pending

- [ ] Phase 1A: Spring Boot + Spring AI + Ollama 最小闭环
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
