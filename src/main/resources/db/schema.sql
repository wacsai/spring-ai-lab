CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS ai_document_embedding (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    embedding vector(2560) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- qwen3-embedding:4b 返回 2560 维向量。
-- 当前 pgvector 的 hnsw 索引限制是最多 2000 维，因此这里先不创建 hnsw 索引。
-- 学习阶段先完成“存向量 + 精确相似度查询”，后续再根据模型维度决定索引策略。
