package com.yytnet.fms.ailab.vector.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class VectorDocumentCommandRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long save(String title, String content, String embeddingLiteral) {
        // 这里没有使用 JpaRepository.save(entity)，是因为当前阶段暂不把 pgvector 字段映射成 Java 类型。
        // embeddingLiteral 是 Java 字符串参数，例如 "[0.1,0.2,...]"。
        // CAST(:embedding AS vector) 明确告诉 PostgreSQL：请把这个字符串参数按 pgvector 类型解析。
        // RETURNING id 用来在插入成功后直接拿到数据库生成的主键，方便接口返回和后续 RAG 引用文档片段。
        Query query = entityManager.createNativeQuery("""
                INSERT INTO ai_document_embedding (title, content, embedding)
                VALUES (:title, :content, CAST(:embedding AS vector))
                RETURNING id
                """);
        query.setParameter("title", title);
        query.setParameter("content", content);
        query.setParameter("embedding", embeddingLiteral);

        Number id = (Number) query.getSingleResult();
        return id.longValue();
    }

    @Transactional
    public Long saveChunk(String title,
                          String content,
                          String embeddingLiteral,
                          String documentTitle,
                          int chunkIndex,
                          int chunkCount,
                          int chunkStart,
                          int chunkEnd) {
        // RAG 文档切分后，每个 chunk 都是一条独立向量记录。
        // document_title/chunk_index/chunk_start/chunk_end 用来让检索结果能定位回原文档中的具体片段。
        Query query = entityManager.createNativeQuery("""
                INSERT INTO ai_document_embedding (
                    title,
                    content,
                    embedding,
                    document_title,
                    chunk_index,
                    chunk_count,
                    chunk_start,
                    chunk_end
                )
                VALUES (
                    :title,
                    :content,
                    CAST(:embedding AS vector),
                    :documentTitle,
                    :chunkIndex,
                    :chunkCount,
                    :chunkStart,
                    :chunkEnd
                )
                RETURNING id
                """);
        query.setParameter("title", title);
        query.setParameter("content", content);
        query.setParameter("embedding", embeddingLiteral);
        query.setParameter("documentTitle", documentTitle);
        query.setParameter("chunkIndex", chunkIndex);
        query.setParameter("chunkCount", chunkCount);
        query.setParameter("chunkStart", chunkStart);
        query.setParameter("chunkEnd", chunkEnd);

        Number id = (Number) query.getSingleResult();
        return id.longValue();
    }
}
