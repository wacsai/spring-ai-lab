# Current Status

Last Updated: 2026-08-24

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
- [x] 已提供 `POST /api/ai/rag/chat`
- [x] 已实现最小 RAG 闭环：向量检索参考资料后交给 ChatClient 回答
- [x] 已为 RAG 增加 `maxDistance` 阈值，避免明显无关资料进入 Prompt
- [x] 已通过真实接口验证 `POST /api/ai/rag/chat` 可返回 answer + references
- [x] 已提供 `POST /api/ai/rag/documents`
- [x] 已实现按 `chunkSize + overlap` 切分长文档
- [x] 已实现 chunk 批量生成 embedding 并写入 pgvector
- [x] 已为向量记录增加 document/chunk 元数据，便于 references 定位具体片段
- [x] 已为 `POST /api/ai/rag/chat` 增加检索诊断字段
- [x] 已区分 `references` 和 `rejectedReferences`
- [x] 已将 RAG 文档切分升级为句子/换行自然边界优先，超长单元再字符兜底
- [x] 已为 `POST /api/ai/rag/chat` 增加 `citations` 引用摘要，便于前端展示答案依据
- [x] 已为 `POST /api/ai/rag/documents` 增加 `replaceExisting`，支持同名文档重新导入前删除旧 chunk
- [x] 已为 RAG chunk 增加 `sourceType` / `sourceName` / `externalId` 来源元数据
- [x] 已提供 `POST /api/ai/rag/documents/files`
- [x] 已支持 UTF-8 `.txt` / `.md` 文件上传导入并复用现有 RAG 文档入库流程
- [x] 已实现 Markdown 标题感知切分：Markdown 先按标题拆 section，再在 section 内复用通用 chunker
- [x] 已为 `POST /api/ai/rag/chat` 增加 `sourceType` / `externalId` 可选来源过滤
- [x] 已优化 Markdown 切分，纯标题或正文过短的低价值 chunk 会合并到相邻 chunk
- [x] 已优化 `replaceExisting` 删除范围，优先按 `sourceType + externalId` 替换旧 chunk
- [x] 已提供 `POST /api/ai/memory/chat`
- [x] 已使用 Spring AI `MessageChatMemoryAdvisor` 接入 Chat Memory
- [x] 已使用 `MessageWindowChatMemory + InMemoryChatMemoryRepository` 保存 JVM 内存会话消息
- [x] 已通过 `conversationId` 实现会话级上下文隔离
- [x] 已提供 `DELETE /api/ai/memory/conversations/{conversationId}` 清理指定会话 Memory
- [x] 已提供 `POST /api/ai/agent/study`
- [x] 已实现学习助手 Agent 最小闭环：ChatClient + LearningProgressTool + Chat Memory + 固定目标边界
- [x] 已更新 `LearningProgressTool` 的固定学习进度到当前阶段
- [x] 已提供 `POST /api/ai/agent/study/steps`
- [x] 已实现显式 Agent State + Step 记录：goal、steps、observation、completed、answer
- [x] 已提供 `POST /api/ai/agent/study/loop`
- [x] 已实现动态 Agent Loop + Stop Condition：模型决定 action，Java 执行动作，记录 observation，并在 FINISH 或 maxSteps 时停止
- [x] 已为 Agent Loop 增加 `RAG_SEARCH` action
- [x] 已在 `RagService` 暴露只检索不生成回答的 `retrieveReferences(...)`，供 Agent 将 RAG 结果作为 observation
- [x] 已为 Agent Loop 增加 `ASK_USER` action
- [x] 已支持信息不足时返回 `stopReason=WAITING_USER_INPUT` 并等待用户补充
- [x] 已为 Agent Loop 增加重复 action 保护，避免模型反复选择 `RAG_SEARCH` / `GET_LEARNING_PROGRESS` 导致 `MAX_STEPS_REACHED`
- [x] 已为 Agent Loop 增加 `RAG_SEARCH` query 保守归一化，避免模型扩写过泛 query 影响 pgvector 召回
- [x] 已完成 Agent 基础阶段收口，明确当前为学习 demo，不继续深挖 Prompt 调优和复杂 Agent 编排
- [x] 已创建独立 `spring-ai-mcp-server-demo` 最小 MCP Server
- [x] 已在 `spring-ai-lab` 接入 Spring AI MCP Client
- [x] 已配置 MCP Client 通过 Streamable HTTP 连接 `http://localhost:8081/mcp`
- [x] 已提供 `POST /api/ai/mcp/chat`
- [x] 已在 ChatClient 请求中注册 MCP Client 自动发现的远程 `ToolCallbackProvider`

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

**MCP Server + MCP Client 最小接入已完成**

当前真实状态：

```text
Client
↓
RagController
↓
MultipartFile(.txt/.md) 或 JSON content
↓
RagService
↓
Markdown sourceType 先按标题拆 section
↓
RagDocumentChunker
↓
纯标题/正文过短的低价值 chunk 合并到相邻 chunk
↓
replaceExisting=true 时按 sourceType + externalId 替换旧 chunk
↓
sourceType/sourceName/externalId 记录资料来源
↓
VectorDocumentService.createChunk()
↓
EmbeddingModel + PostgreSQL 18 + pgvector
↓
多条 chunk 向量记录
```

RAG 问答链路：

```text
Client
↓
RagController
↓
RagService
↓
sourceType/externalId 可选过滤检索范围
↓
VectorDocumentService.searchSimilarDocuments()
↓
EmbeddingModel + PostgreSQL 18 + pgvector
↓
相似文档 references
↓
maxDistance 过滤
↓
references / rejectedReferences
↓
citations 引用摘要 + source 元数据
↓
ChatClient
↓
qwen3.5:4b
↓
基于参考资料回答
```

Chat Memory 链路：

```text
Client
↓
MemoryChatController
↓
MemoryChatService
↓
conversationId 写入 Advisor 参数 chat_memory_conversation_id
↓
MessageChatMemoryAdvisor
↓
MessageWindowChatMemory + InMemoryChatMemoryRepository
↓
ChatClient
↓
qwen3.5:4b
↓
模型回答后写回当前 conversationId 的 JVM 内存消息窗口
```

Chat Memory 清理链路：

```text
Client
↓
DELETE /api/ai/memory/conversations/{conversationId}
↓
MemoryChatController
↓
MemoryChatService.clear()
↓
ChatMemory.clear(conversationId)
↓
清空当前 conversationId 的 JVM 内存消息窗口
```

Agent 学习助手链路：

```text
Client
↓
POST /api/ai/agent/study
↓
StudyAgentController
↓
StudyAgentService
↓
MessageChatMemoryAdvisor 读取 conversationId 历史消息
↓
LearningProgressTool 提供当前学习状态
↓
ChatClient
↓
qwen3.5:4b
↓
基于工具结果 + 会话上下文回答学习下一步
```

Agent State + Step 链路：

```text
Client
↓
POST /api/ai/agent/study/steps
↓
StudyAgentController
↓
StudyAgentService.chatWithSteps()
↓
Step 1: LearningProgressTool#getSpringAiLearningProgress
↓
Observation 写入 steps[0]
↓
Step 2: ChatClient 基于 goal + toolObservation 生成 answer
↓
Observation 写入 steps[1]
↓
返回 goal + completed + steps + answer
```

Agent Loop 链路：

```text
Client
↓
POST /api/ai/agent/study/loop
↓
StudyAgentController
↓
StudyAgentService.chatWithLoop()
↓
读取 conversationId 对应的历史 Memory 作为 memoryContext
↓
Loop Step N: ChatClient 结构化输出 StudyAgentDecision
↓
action=GET_LEARNING_PROGRESS 时，Java 服务层执行 LearningProgressTool
↓
action=RAG_SEARCH 时，Java 服务层调用 RagService.retrieveReferences 只检索资料
↓
action=ASK_USER 时，停止循环并返回 WAITING_USER_INPUT
↓
如果模型重复选择已经执行过的 RAG_SEARCH / GET_LEARNING_PROGRESS，Java 服务层阻止重复 action 并基于现有 steps 生成最终回答
↓
RAG_SEARCH 执行前会在 Java 服务层归一化 query；例如“我想重点学习 RAG”或基于 Memory 的“继续”会转成“RAG 的核心流程是什么？”
↓
工具返回结果作为 observation 写入 steps
↓
下一轮把 goal + memoryContext + steps 再交给模型判断
↓
action=FINISH 时停止循环并返回 answer
↓
达到 maxSteps 仍未 FINISH 时按 MAX_STEPS_REACHED 停止
↓
只把用户 goal 和最终 answer 写回 ChatMemory，内部决策步骤不写入 Memory
```

Agent 阶段收口：

```text
当前 Agent 是学习 demo，已经覆盖：
Goal
State
Action
Observation
Loop
Stop Condition
Memory
Java Safety Boundary
```

当前不继续深挖：

```text
Prompt 微调
RAG query rewrite
rerank
复杂工具编排
权限审批
状态持久化
多 Agent 协作
```

模型 action 选择存在不稳定性，已通过 Java 侧重复 action 保护和保守 query 归一化做最小兜底；后续进入 Observability / Evaluation 阶段再系统评估。

MCP Client 链路：

```text
Client
↓
POST /api/ai/mcp/chat
↓
McpClientController
↓
McpClientChatService
↓
ChatClient
↓
Spring AI MCP Client 自动发现远程 ToolCallbackProvider
↓
HTTP + MCP 协议访问 spring-ai-mcp-server-demo:8081/mcp
↓
远程 MCP Server 执行 McpLearningTool#getSpringAiMcpLearningProgress
↓
工具结果返回 ChatClient
↓
模型基于远程工具结果生成最终回答
```

当前代码已经完成最小闭环、System Prompt 模板、请求级模型参数覆盖、普通调用、流式调用、基础 Advisor 挂载、结构化输出、Tool Calling 基础接口、带参数 Tool、Embedding 最小接口、JPA/PostgreSQL 依赖接入、pgvector 初始化脚本、文档向量入库接口、精确相似度检索接口、最小 RAG 问答接口、RAG 文档切分入库接口、RAG 检索诊断字段、RAG 引用摘要、同名文档替换导入、来源元数据、TXT/Markdown 文件上传导入、Markdown 标题感知切分、RAG 来源过滤检索、Markdown 低价值 chunk 合并、RAG 稳定来源身份替换、Chat Memory JVM 内存版最小闭环、Chat Memory 会话清理接口、Agent 学习助手最小闭环、Agent 显式 State + Step 记录、Agent 动态 Loop + Stop Condition、Agent Loop RAG_SEARCH 检索动作、Agent Loop ASK_USER 澄清动作、Agent Loop 重复 action 保护、RAG_SEARCH query 保守归一化、Agent 基础阶段收口和 MCP Server + Client 最小接入。

当前数据库配置已启动到执行 schema 阶段；`vector(2560)` 字段可保留，但当前 pgvector HNSW 索引最多支持 2000 维，因此初始化脚本暂不创建 HNSW 索引。向量入库、检索代码、最小 RAG 接口、RAG 文档切分入库接口、RAG 检索诊断字段和 RAG 自然切分策略已编译通过，并已通过真实请求验证。RAG 引用摘要、同名文档替换导入、来源元数据、TXT/Markdown 文件上传导入、Markdown 标题感知切分、RAG 来源过滤检索、Markdown 低价值 chunk 合并和 RAG 稳定来源身份替换已完成代码实现。Chat Memory JVM 内存版和会话清理接口已完成代码实现。Agent 学习助手最小闭环、显式 State + Step 记录、动态 Loop + Stop Condition、RAG_SEARCH action 和 ASK_USER action 已完成代码实现，待真实接口调用验证。

## Next Task

进入 **MCP 真实接口验证**：

```text
启动 spring-ai-mcp-server-demo:8081
→ 启动 spring-ai-lab:8080
→ POST /api/ai/mcp/chat
→ 验证模型能否调用远程 MCP tool
→ 从日志确认 toolCalls / MCP 工具返回 / 最终回答
```

验收标准：

- 理解 MCP 和 Tool Calling 的区别
- 理解 MCP Server 与 Spring Boot 应用的关系
- 验证主项目能通过 MCP Client 使用另一个 Spring Boot 服务里的 Java 方法
- 不提前实现复杂 Resource / Prompt / Sampling / Elicitation

## Pending

- [x] PostgreSQL + pgvector
- [x] RAG 最小闭环
- [x] RAG 文档切分真实接口验证
- [x] RAG 检索诊断真实接口验证
- [x] RAG 自然切分策略真实接口验证
- [x] RAG 引用摘要
- [x] RAG 同名文档替换导入
- [x] RAG 文档来源元数据
- [x] RAG TXT / Markdown 文件导入
- [x] RAG Markdown 标题感知切分
- [x] RAG 来源过滤检索
- [x] RAG Markdown 低价值 chunk 合并
- [x] RAG 稳定来源身份替换
- [x] Chat Memory JVM 内存版最小闭环
- [x] Chat Memory 会话清理接口
- [x] Agent 学习助手最小闭环
- [x] Agent 显式 State + Step 记录
- [x] Agent 动态 Loop + Stop Condition
- [x] Agent Loop RAG_SEARCH 检索动作
- [x] Agent Loop ASK_USER 澄清动作
- [x] Agent Loop 重复 action 保护
- [x] Agent Loop RAG_SEARCH query 保守归一化
- [x] Agent 基础阶段收口
- [x] MCP Server + Client 最小接入
- [ ] MCP 真实接口验证
- [ ] Observability
- [ ] Evaluation
- [ ] Qdrant 对比实验
