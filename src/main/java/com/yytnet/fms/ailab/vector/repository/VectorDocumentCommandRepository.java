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
                          int chunkEnd,
                          String sourceType,
                          String sourceName,
                          String externalId) {
        // RAG 文档切分后，每个 chunk 都是一条独立向量记录。
        // document_title/chunk_index/chunk_start/chunk_end 用来让检索结果能定位回原文档中的具体片段。
        // source_type/source_name/external_id 用来追踪资料来自手动输入、文件、网页或外部业务系统。
        Query query = entityManager.createNativeQuery("""
                INSERT INTO ai_document_embedding (
                    title,
                    content,
                    embedding,
                    document_title,
                    chunk_index,
                    chunk_count,
                    chunk_start,
                    chunk_end,
                    source_type,
                    source_name,
                    external_id
                )
                VALUES (
                    :title,
                    :content,
                    CAST(:embedding AS vector),
                    :documentTitle,
                    :chunkIndex,
                    :chunkCount,
                    :chunkStart,
                    :chunkEnd,
                    :sourceType,
                    :sourceName,
                    :externalId
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
        query.setParameter("sourceType", sourceType);
        query.setParameter("sourceName", sourceName);
        query.setParameter("externalId", externalId);

        Number id = (Number) query.getSingleResult();
        return id.longValue();
    }

    @Transactional
    public int deleteChunksByDocumentTitle(String documentTitle) {
        // 只删除 RAG 文档导入产生的 chunk 记录：
        // saveChunk(...) 会写 document_title，早期 /api/ai/vector/documents 写入的普通短文本没有 document_title。
        // 这样 replaceExisting 不会误删普通向量 demo 数据。
        Query query = entityManager.createNativeQuery("""
                DELETE FROM ai_document_embedding
                WHERE document_title = :documentTitle
                """);
        query.setParameter("documentTitle", documentTitle);
        return query.executeUpdate();
    }

    @Transactional
    public int deleteChunksBySourceIdentity(String sourceType, String externalId) {
        // source_type + external_id 表示一个外部来源的稳定身份。
        // 对文件导入来说 external_id 默认是原始文件名；后续可以换成文件路径、对象存储 key 或业务主键。
        Query query = entityManager.createNativeQuery("""
                DELETE FROM ai_document_embedding
                WHERE source_type = :sourceType
                  AND external_id = :externalId
                """);
        query.setParameter("sourceType", sourceType);
        query.setParameter("externalId", externalId);
        return query.executeUpdate();
    }
}
