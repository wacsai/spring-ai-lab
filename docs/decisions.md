# Architecture Decision Log

## ADR-001: Java AI 主线使用 Spring AI

Status: Accepted

### Decision

以 Spring Boot + Spring AI 作为 Java AI 应用开发主线。

### Reason

- 与现有 Java / Spring 技术栈一致
- 适合企业业务系统集成
- 支持 Chat、Embedding、VectorStore、RAG、Tool Calling、MCP 等能力

### Alternatives

- LangChain
- LangGraph

### Follow-up

后续学习 LangChain / LangGraph，用于理解 Python AI 生态和复杂 Agent 编排。

---

## ADR-002: 当前 LLM Runtime 使用 Ollama

Status: Accepted

### Decision

本地模型通过 Docker Ollama 运行。

### Current Model

```text
qwen3.5:4b
```

### Endpoint

```text
http://192.168.0.50:11434
```

### Reason

- 本地部署简单
- GPU 已验证
- 便于 Spring AI / ComfyUI / Mac 开发环境共同调用

---

## ADR-003: 第一套向量存储使用 PostgreSQL + pgvector

Status: Planned

### Decision

先使用 PostgreSQL + pgvector 完成 RAG。

### Reason

- 已熟悉 PostgreSQL
- 与企业 Java 应用集成简单
- 便于学习向量存储本质

### Follow-up

第二阶段增加 Qdrant，对比专业 Vector DB 与 pgvector 的差异。

---

## ADR-004: DeepSeek Harness 暂不纳入当前架构

Status: Accepted

### Decision

当前项目不引入 DeepSeek Harness。

### Reason

当前目标是自行使用 Spring AI 构建企业 Agent，而不是使用独立 Agent Harness 作为主要运行时。

---

## ADR-005: 学习路线按最小闭环逐阶段推进

Status: Accepted

### Decision

当前仓库先从 Spring Boot + Spring AI + Ollama 的最小闭环开始，不提前实现 Structured Output、Tool Calling、Embedding、RAG、Memory、Agent 或 MCP。

第一阶段只完成：

```text
REST API
→ Service
→ ChatClient
→ Ollama
→ qwen3.5:4b
```

### Reason

- 当前代码只有一个 Controller demo，正式 Spring AI 集成尚未开始。
- 先完成可运行、可验证的最小链路，便于定位依赖、配置、网络和模型调用问题。
- 后续能力之间存在前置关系，例如 RAG 依赖 Embedding 和 VectorStore，Agent 依赖 Tool Calling、Memory 和状态管理。

### Impact

- 每个阶段只引入当前阶段必需的依赖和配置。
- 每完成一个阶段更新 `docs/current-status.md`。
- 后续阶段可以预留包结构或接口，但不提前大规模实现。

---

## ADR-006: Structured Output 使用固定任务接口

Status: Accepted

### Decision

Structured Output 阶段不改造开放聊天接口，而是新增固定任务接口：

```text
POST /api/ai/structured/movie/extract
```

该接口只负责从自然语言文本中提取电影信息，并返回 `MovieExtractResp`。

### Reason

- Structured Output 适合业务目标明确、输出结构固定的任务。
- 开放聊天问题是随机的，不应强行套入电影对象。
- `MovieExtractResp` 使用 `movieRelated` 表达输入是否与电影信息相关，避免非电影输入时硬编字段。

### Impact

- `POST /api/ai/chat` 继续返回自然语言文本。
- `POST /api/ai/structured/movie/extract` 返回 Java record 可直接表达的结构化结果。
- 当前阶段不提前抽象多模型 Provider 的通用结构化输出层。

---

## ADR-007: Tool Calling 先使用只读学习进度工具

Status: Accepted

### Decision

Tool Calling 阶段新增独立接口：

```text
POST /api/ai/tool/chat
```

该接口通过 `ChatClient.tools(learningProgressTool)` 注册一个带 `@Tool` 注解的 Java 本地方法：

```text
LearningProgressTool#getSpringAiLearningProgress
```

### Reason

- 当前目标是先理解模型选择工具、Spring AI 执行 Java 方法、模型基于工具结果回答的基本链路。
- 学习进度查询是只读、无副作用、可重复验证的工具，适合作为第一个 Tool Calling demo。
- 暂不接入数据库、订单、服务器状态等真实业务能力，避免把 Tool Calling 和后续业务集成复杂度混在一起。

### Alternatives

- 直接接入数据库查询工具
- 直接接入 Linux / Docker / GPU 状态工具
- 在原 `POST /api/ai/chat` 中直接注册工具

### Impact

- `POST /api/ai/chat` 继续保持普通聊天职责。
- `POST /api/ai/tool/chat` 专门用于学习 Tool Calling。
- 后续真实业务工具可以按相同模式扩展，但需要增加权限边界、参数校验、审计日志和失败处理。

---

## ADR-008: Tool Calling 使用内存订单工具学习参数提取

Status: Accepted

### Decision

在 `POST /api/ai/tool/chat` 中继续注册第二个只读工具：

```text
OrderStatusTool#getOrderStatus(orderNo)
```

该工具通过 `@ToolParam` 声明 `orderNo` 参数，并使用内存 `Map` 模拟订单数据。

### Reason

- 无参数 Tool 只能证明模型可以调用 Java 方法，不能证明模型可以从用户问题中提取参数。
- 企业业务 Tool 通常都需要参数，例如订单号、用户 ID、时间范围、商品编码。
- 使用内存订单数据可以聚焦学习 `@ToolParam`、`toolCalls.arguments` 和 ToolResponseMessage，不提前引入数据库。

### Impact

- 当前 Tool Calling 阶段已覆盖无参数工具和带参数工具。
- `pom.xml` 中 Maven Compiler 开启 `parameters=true`，用于保留 Java 方法参数名，方便 Spring AI 生成工具参数 schema。
- 后续接入真实订单、数据库或外部 API 时，可以沿用该模式，但必须补充权限校验、参数校验和审计日志。

---

## ADR-009: Embedding 阶段先做最小向量化接口

Status: Accepted

### Decision

Embedding 阶段先使用 Ollama 模型：

```text
qwen3-embedding:4b
```

新增最小接口：

```text
POST /api/ai/embedding
```

该接口只完成：

```text
text -> EmbeddingModel -> float[] vector
```

并返回模型名、向量维度和前 8 个向量样例。

### Reason

- Embedding 是 pgvector 和 RAG 的前置能力，需要先确认模型能正常返回向量。
- 不同 Embedding 模型的向量维度不同，建 pgvector 表之前必须先确认维度。
- 当前阶段不引入数据库，避免把向量化、存储、索引和检索混在同一步。

### Result

真实接口验证确认：

```text
qwen3-embedding:4b -> 2560 维向量
```

### Impact

- 后续 pgvector 表字段应按当前模型使用 `vector(2560)`。
- 如果后续更换 Embedding 模型，需要重新确认维度，并评估是否重建向量数据。

---

## ADR-010: PostgreSQL + pgvector 先接入 JPA 和初始化脚本

Status: Accepted

### Decision

PostgreSQL + pgvector 阶段先做数据库环境准备：

```text
Spring Data JPA
PostgreSQL JDBC Driver
spring.datasource.*
db/schema.sql
```

当前默认连接配置：

```text
jdbc:postgresql://localhost:5432/spring_ai_lab
username = spring_ai_lab
password = spring_ai_lab_pwd
```

初始化脚本先创建：

```text
CREATE EXTENSION IF NOT EXISTS vector
ai_document_embedding.embedding vector(2560)
```

### Reason

- 用户已在本地准备 PostgreSQL 18 + pgvector，希望先把 Spring Boot 数据库环境接起来。
- 当前 Embedding 模型 `qwen3-embedding:4b` 已真实验证为 2560 维，因此表结构先按 `vector(2560)` 准备。
- 本阶段先不实现 RAG，不引入复杂文档切分、召回、Prompt 拼接。

### Impact

- 应用启动时会尝试连接 datasource，并执行 `db/schema.sql`。
- 如果本地库名、用户名或密码不同，需要修改 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` 或 `application.yaml`。
- 后续可以在该表基础上实现最小文本入库和相似度检索。
- 当前暂不创建 HNSW 索引，因为 `qwen3-embedding:4b` 是 2560 维，已超过当前 pgvector HNSW 索引 2000 维限制。

---

## ADR-011: pgvector 最小检索使用 JPA 原生 SQL 和 @Query

Status: Accepted

### Decision

向量入库先使用 Spring Data JPA 提供的 `EntityManager` 执行原生 SQL：

```text
INSERT ... CAST(:embedding AS vector)
```

相似度检索使用 Spring Data JPA Repository 的 `@Query(nativeQuery = true)`：

```text
ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
```

新增接口：

```text
POST /api/ai/vector/documents
POST /api/ai/vector/search
```

### Reason

- 当前目标是先跑通 Embedding + pgvector 的最小闭环。
- 插入需要 `INSERT ... RETURNING id`，用 `EntityManager` 更直接。
- 检索是标准查询，适合使用 `@Query(nativeQuery = true)` 和 interface projection。
- 当前阶段暂不深入 Hibernate 对 pgvector 字段的实体类型映射。

### Impact

- 当前检索使用精确 cosine distance，没有 HNSW 索引，适合小数据量学习验证。
- 后续数据量变大时，需要重新评估索引策略、模型维度或向量降维。
- RAG 阶段可以复用 `POST /api/ai/vector/search` 的检索逻辑，把返回文档作为 Prompt 上下文。

---

## ADR-012: RAG 先实现最小闭环接口

Status: Accepted

### Decision

新增独立 RAG 接口：

```text
POST /api/ai/rag/chat
```

当前最小流程：

```text
question
→ VectorDocumentService.searchSimilarDocuments()
→ pgvector 返回相似文档
→ maxDistance 过滤明显无关资料
→ references 拼入 System Prompt
→ ChatClient 生成回答
```

### Reason

- RAG 的核心是“先检索，再生成”，当前阶段应先把 Embedding、pgvector、ChatClient 串起来。
- 继续复用已有向量检索逻辑，避免在 RAG 阶段重复实现 embedding 和 SQL 查询。
- 增加 `maxDistance` 阈值，避免数据库里总是返回 topK 导致无关资料被塞进 Prompt。
- 当前阶段暂不引入文档切分、批量导入、复杂 rerank、Spring AI VectorStore 抽象或 Memory。

### Impact

- `POST /api/ai/vector/search` 继续用于观察底层向量检索结果。
- `POST /api/ai/rag/chat` 用于观察“检索结果如何影响模型回答”。
- response 返回 `references`，便于学习阶段确认模型回答前实际拿到了哪些资料。
- 后续真实 RAG 阶段需要补充文档切分、数据导入、召回阈值调优和引用格式优化。

---

## ADR-013: RAG 文档入库使用自然边界优先切分

Status: Accepted

### Decision

新增文档入库接口：

```text
POST /api/ai/rag/documents
```

当前切分策略：

```text
chunkSize 默认 500 字符
overlap 默认 80 字符
优先按句号、问号、感叹号、分号、换行等自然边界拆分单元
多个自然单元组合成 chunk，直到接近 chunkSize
相邻 chunk 尽量用上一段末尾的完整自然单元做 overlap
如果单个自然单元超过 chunkSize，再使用字符切分兜底
```

每个 chunk 都作为独立向量记录写入 `ai_document_embedding`，并保存以下元数据：

```text
document_title
chunk_index
chunk_count
chunk_start
chunk_end
```

### Reason

- 真实 RAG 不应只存短句；长文档需要拆成更小片段，检索时才能命中更精确的上下文。
- 自然边界优先切分可以减少英文单词、中文句子和 Markdown 结构被硬截断的情况。
- 保留字符兜底，避免长代码块、长表格行、无标点文本无法切分。
- 保存 chunk 元数据后，RAG references 可以定位到原文档中的具体片段。
- 当前阶段先不引入 PDF/Word 解析、Markdown 结构解析、tokenizer、rerank 或权限过滤。

### Impact

- 旧的短文本向量记录仍然可用，新增 chunk 元数据列均为可空字段。
- `POST /api/ai/rag/chat` 的 references 会返回 document/chunk 元数据。
- 后续可以继续升级为 Markdown 标题切分、段落层级切分或 token 切分，而不影响 pgvector 检索主流程。

---

## ADR-014: RAG 问答响应区分检索候选和实际使用资料

Status: Accepted

### Decision

`POST /api/ai/rag/chat` 响应中新增检索诊断字段：

```text
retrievedCount
usedCount
rejectedCount
references
rejectedReferences
```

含义：

```text
references = distance <= maxDistance，实际进入 Prompt 的资料
rejectedReferences = distance > maxDistance，只用于调试观察，不进入 Prompt
```

### Reason

- 学习阶段需要看清楚 pgvector 返回了哪些候选，以及 maxDistance 过滤掉了哪些候选。
- 只看最终 answer 不容易判断模型到底参考了什么资料。
- 区分 used/rejected 后，可以更直观地调试 topK 和 maxDistance。

### Impact

- `references` 继续表示模型实际使用的资料。
- `rejectedReferences` 可能包含语义较弱的候选，不会进入 Prompt。
- 后续可以基于这些诊断数据继续调优默认阈值、召回数量和切分策略。

---

## ADR-015: RAG 问答响应增加引用摘要 citations

Status: Accepted

### Decision

`POST /api/ai/rag/chat` 响应中保留完整 `references`，同时新增更适合前端展示的 `citations`：

```text
citations
references
rejectedReferences
```

其中：

```text
citations = 实际进入 Prompt 的资料摘要，只包含 label、文档标题、chunk 位置、distance、similarity 等展示字段
references = 实际进入 Prompt 的完整资料，包含 content，适合学习和调试
rejectedReferences = 被 maxDistance 过滤掉的候选资料，不进入 Prompt
```

`citations.label` 和 System Prompt 中的“资料 1”“资料 2”使用同一套编号逻辑。

### Reason

- 企业 RAG 场景需要回答“答案依据是什么”。
- 完整 `references` 适合调试，但直接给前端展示会过重，也容易把长 chunk 内容暴露到普通引用区域。
- 单独返回 `citations` 可以让前端展示引用来源，同时继续保留学习阶段需要的完整诊断信息。
- 引用编号和 Prompt 资料编号对齐后，可以更容易判断模型回答里提到的“资料 N”对应哪条数据库记录。

### Impact

- `/api/ai/rag/chat` 响应结构新增 `citations` 字段。
- 旧的 `references` 和 `rejectedReferences` 语义不变。
- 后续可以继续在 `citations` 中增加 sourceType、sourceName、externalId、pageNumber 等真实文档来源字段。

---

## ADR-016: RAG 文档导入支持同名文档替换

Status: Accepted

### Decision

`POST /api/ai/rag/documents` 请求新增：

```text
replaceExisting
```

行为：

```text
replaceExisting != true
→ 保持原行为，直接追加本次导入的 chunk

replaceExisting = true
→ 先删除 document_title 等于当前 title 的旧 chunk
→ 再切分当前 content
→ 每个新 chunk 重新生成 embedding 并写入 pgvector
```

响应新增：

```text
replaceExisting
deletedCount
```

### Reason

- 学习阶段会反复导入同一篇测试文档，容易产生重复 chunk。
- 重复 chunk 会让 RAG 检索结果出现内容相同、distance 相同的多条记录，影响观察和调参。
- 默认仍然追加，避免一次普通导入意外删除旧数据；只有显式传 `replaceExisting=true` 才执行替换。

### Impact

- 删除范围只限定在 `document_title = title` 的 RAG chunk。
- 早期 `/api/ai/vector/documents` 写入的普通向量 demo 记录没有 `document_title`，不会被该功能删除。
- 当前实现是学习阶段的最小替换能力；后续真实业务应增加 documentId、sourceId、版本号、权限边界和导入事务策略。

---

## ADR-017: RAG chunk 增加文档来源元数据

Status: Accepted

### Decision

`ai_document_embedding` 增加来源元数据列：

```text
source_type
source_name
external_id
```

`POST /api/ai/rag/documents` 请求新增：

```text
sourceType
sourceName
externalId
```

默认值：

```text
sourceType = MANUAL
sourceName = title
externalId = null
```

`POST /api/ai/rag/chat` 的 `references` 和 `citations` 都返回这些来源字段。

### Reason

- 真实 RAG 需要追踪 chunk 来自哪里，而不只是知道来自哪个 documentTitle。
- `sourceType` 用于区分手动 API 导入、TXT、Markdown、PDF、URL 等不同来源。
- `sourceName` 用于展示来源名称，例如文件名、网页标题或知识库名称。
- `externalId` 用于绑定外部系统里的唯一标识，例如文件路径、对象存储 key、业务表主键或 URL。
- 当前阶段先只保存和返回元数据，不提前实现 PDF/Markdown/URL 解析。

### Impact

- 旧数据的来源字段允许为 null，不影响现有检索。
- 新导入的 RAG chunk 默认会带 `MANUAL` 来源信息。
- 后续实现文档解析、去重、删除、版本管理、权限过滤时，可以基于这些来源字段继续扩展。

---

## ADR-018: RAG 文件导入先支持 TXT 和 Markdown

Status: Accepted

### Decision

新增文件上传接口：

```text
POST /api/ai/rag/documents/files
```

使用 Spring Web 自带的 `MultipartFile` 接收 `multipart/form-data`，不新增额外依赖。

当前只支持 UTF-8 文本文件：

```text
.txt -> sourceType = TEXT
.md / .markdown -> sourceType = MARKDOWN
```

文件导入读取文本后，会构造 `RagDocumentImportReq` 并复用已有 `importDocument(...)`：

```text
读取上传文件文本
→ 自动填 sourceType/sourceName/externalId
→ 复用文档切分
→ 每个 chunk 生成 embedding
→ 写入 pgvector
```

### Reason

- 真实 RAG 资料通常来自文件，而不是每次手写 JSON content。
- TXT 和 Markdown 都是纯文本，能复用现有 chunk、embedding、pgvector 入库链路。
- 当前阶段不引入 PDF/Word 解析，避免分页、表格、页眉页脚、乱码、OCR 等问题干扰 RAG 主线。
- 不新增依赖，保持阶段改动最小。

### Impact

- `POST /api/ai/rag/documents` 仍保留，适合手动 JSON 导入。
- `POST /api/ai/rag/documents/files` 适合上传真实 `.txt` / `.md` 学习资料。
- `TEXT` 被加入合法 `sourceType`。
- 后续可以在 Markdown 文件导入基础上继续做标题层级切分，而不是只按标点和长度切分。

---

## ADR-019: Markdown 文档先使用标题感知切分

Status: Accepted

### Decision

当导入文档的 `sourceType = MARKDOWN` 时，RAG 入库不再直接把整篇 Markdown 交给通用 chunker，而是先执行：

```text
Markdown content
→ 按 # / ## / ### 等标题行拆成 section
→ 每个 section 内部复用 RagDocumentChunker
→ section 被拆成多个 chunk 时，为后续 chunk 补回标题上下文
```

普通 `MANUAL` / `TEXT` 文本仍然走原来的自然边界切分。

### Reason

- Markdown 自带标题结构，标题通常表达当前段落主题。
- 直接按长度或标点切分可能让 chunk 缺少标题上下文，模型只看到正文时不容易判断资料主题。
- 先按标题拆 section，再复用通用 chunker，可以在不引入 Markdown AST 解析库的前提下提升 chunk 语义完整性。
- 当前阶段仍保持最小实现，不提前处理表格、链接、图片、Front Matter、复杂嵌套列表等完整 Markdown 语法。

### Impact

- `.md` / `.markdown` 上传后，chunk 更倾向于保留 `## RAG`、`## Embedding` 这类标题上下文。
- 代码块中的 `##` 不会被当成标题切分。
- 后续可以继续升级为 Markdown AST 解析，或者在 chunk 元数据中保存 headingPath。

---

## ADR-020: RAG 问答支持来源范围过滤

Status: Accepted

### Decision

`POST /api/ai/rag/chat` 请求新增可选过滤条件：

```text
sourceType
externalId
```

检索流程调整为：

```text
question
→ 生成 query embedding
→ SQL WHERE 按 sourceType/externalId 缩小候选 chunk
→ pgvector cosine distance 排序
→ topK
→ maxDistance 过滤
→ references 进入 Prompt
```

### Reason

- 真实 RAG 通常不会永远从整个知识库里检索，而是会按来源、文件、业务对象或权限范围缩小候选集合。
- 当前已经保存 `sourceType` / `sourceName` / `externalId` 元数据，可以先用来源过滤理解 metadata filtering 的作用。
- 过滤条件必须放在 SQL 查询阶段，而不是拿到全库 topK 后再在 Java 内存中过滤，否则可能漏掉指定来源内真正相关的 chunk。

### Impact

- 不传 `sourceType` / `externalId` 时，旧的全库检索行为保持不变。
- 传 `sourceType=MARKDOWN` 时，只检索 Markdown 文件导入的 chunk。
- 传 `externalId=spring-ai-rag-sample.md` 时，只检索这个外部来源对应的 chunk。
- 当前只是来源范围过滤，不等同于完整权限系统；后续真实业务可以继续扩展 tenantId、userId、role、visibility 等权限字段。

---

## ADR-021: Markdown 切分合并低价值 chunk

Status: Accepted

### Decision

Markdown 标题感知切分后，额外合并低价值 chunk：

```text
纯标题 chunk
正文长度很短的 chunk
→ 合并到相邻 chunk
→ 重新生成连续 chunkIndex
```

### Reason

- Markdown 文档开头的一级标题经常只是文档名，单独生成 embedding 后容易被 RAG 检索召回，但不能提供可回答问题的正文依据。
- 正文过短的 chunk 信息量不足，会浪费 Prompt 空间，也会干扰 references 观察。
- 这个问题属于入库前的切分质量问题，应优先在 chunker 内修正，而不是在检索或 Prompt 阶段临时过滤。

### Impact

- 重新导入 Markdown 文件后，纯标题不再单独成为一条向量记录。
- chunk 数量可能比之前减少，例如原来 7 个 chunk 可能变成 6 个。
- 旧数据不会自动变化，需要使用 `replaceExisting=true` 重新导入同一文档。

---

## ADR-022: replaceExisting 优先使用稳定来源身份

Status: Accepted

### Decision

RAG 文档重新导入时，`replaceExisting=true` 的删除范围调整为：

```text
有 externalId
→ 按 sourceType + externalId 删除旧 chunk

没有 externalId
→ 回退按 documentTitle 删除旧 chunk
```

响应新增：

```text
replaceScope
```

用于说明本次替换旧 chunk 使用的范围：

```text
NONE
SOURCE_IDENTITY
DOCUMENT_TITLE
```

### Reason

- `documentTitle` 更像展示标题，用户可能会修改，不适合作为外部文档的稳定身份。
- `externalId` 更适合表示外部来源唯一标识，例如文件名、文件路径、对象存储 key、URL 或业务主键。
- 对文件导入来说，当前 `externalId` 默认使用原始文件名；重新上传同一个文件时，即使 title 改了，也应该替换旧 chunk，而不是留下两个版本。

### Impact

- 文件导入时，`replaceExisting=true` 会优先删除相同 `sourceType + externalId` 的旧 chunk。
- 手动 JSON 导入如果没有传 `externalId`，仍然保持旧行为：按 `documentTitle` 删除。
- 这是学习阶段的“单一当前版本”策略，不保留历史版本；真实业务后续可以增加 document 表、version、contentHash、status 等字段。

---

## ADR-023: Chat Memory 先使用 JVM 内存窗口

Status: Accepted

### Decision

新增 Chat Memory 最小接口：

```text
POST /api/ai/memory/chat
```

请求使用：

```text
conversationId
message
```

当前 Memory 实现：

```text
MessageChatMemoryAdvisor
MessageWindowChatMemory
InMemoryChatMemoryRepository
maxMessages = 20
```

调用链路：

```text
conversationId
→ ChatMemory.CONVERSATION_ID / chat_memory_conversation_id
→ MessageChatMemoryAdvisor 读取历史消息
→ ChatClient 调用模型
→ MessageChatMemoryAdvisor 写回用户消息和模型回答
```

### Reason

- Chat Memory 的核心不是模型永久记住信息，而是应用按会话保存历史消息，并在下一次请求时重新提供给模型。
- 当前阶段先使用 Spring AI 官方 Memory Advisor，学习方式更接近真实企业代码。
- JVM 内存版不需要新增数据库表和持久化逻辑，适合先验证同一个 `conversationId` 下的多轮上下文保留。

### Impact

- 同一个 `conversationId` 会共享一组最近消息窗口。
- 不同 `conversationId` 之间上下文隔离。
- Spring Boot 重启后内存消息全部丢失。
- 多实例部署时不同实例之间不共享 Memory。
- 后续可以替换为 JDBC、Redis 或其他持久化 ChatMemoryRepository。

---

## ADR-024: Chat Memory 增加会话清理接口

Status: Accepted

### Decision

新增接口：

```text
DELETE /api/ai/memory/conversations/{conversationId}
```

该接口调用：

```text
ChatMemory.clear(conversationId)
```

响应返回：

```text
conversationId
cleared
memoryMessageCount
note
```

### Reason

- Chat Memory 需要能主动清理指定会话，否则学习阶段无法方便验证“清理前后上下文是否变化”。
- 真实聊天系统通常需要新建会话、清空会话、切换会话等能力。
- 当前阶段只清理 JVM 内存中的指定 conversationId，不引入数据库和持久化历史管理。

### Impact

- 清理一个 `conversationId` 不影响其他会话。
- 清理后再次使用同一个 `conversationId`，会从空上下文开始。
- 该接口只对当前 Spring Boot 进程内的 Memory 生效；应用重启后本来就没有旧内存消息。
