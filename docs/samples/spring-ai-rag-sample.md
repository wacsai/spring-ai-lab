# Spring AI RAG 测试资料

## ChatClient

ChatClient 是 Spring AI 推荐使用的高级聊天客户端。它适合组织 System Prompt、User Prompt、请求级模型参数、Advisor、Tool Calling 和结构化输出。

## Embedding

Embedding 会把文本转换成固定维度的向量。当前项目使用 qwen3-embedding:4b，生成的向量维度是 2560。只有查询文本和文档文本使用同一个 Embedding 模型生成向量，后续的相似度检索才有意义。

## pgvector

pgvector 可以把文档向量保存到 PostgreSQL 中，并通过 cosine distance 计算查询向量和文档向量之间的距离。distance 越小，表示语义越相似；当前接口为了便于阅读，也返回 similarity。

## RAG

RAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。当前项目会先把用户问题转换成 query embedding，再从 pgvector 中检索相似 chunk，然后把满足 maxDistance 阈值的 references 放入 Prompt。

## Citations

citations 是适合前端展示的引用摘要。它不会包含完整 chunk 正文，而是返回资料编号、文档标题、chunk 位置、sourceType、sourceName、externalId、distance 和 similarity，方便判断模型回答依据了哪些资料。

## 文件导入

当前文件导入接口支持 UTF-8 编码的 txt 和 Markdown 文件。.txt 文件会记录为 sourceType=TEXT，.md 或 .markdown 文件会记录为 sourceType=MARKDOWN。文件内容读取后会复用原来的文档入库流程，也就是切分 chunk、生成 embedding、写入 pgvector。
