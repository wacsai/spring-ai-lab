package com.yytnet.fms.ailab.vector.repository;

import com.yytnet.fms.ailab.vector.domain.AiDocumentEmbeddingEntity;
import com.yytnet.fms.ailab.vector.repository.projection.VectorSearchProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VectorDocumentQueryRepository extends Repository<AiDocumentEmbeddingEntity, Long> {

    // 这是 Spring Data JPA 的原生 SQL 查询，不是 JPQL。
    // <=> 是 pgvector 的 cosine distance 运算符：
    // - 左边是表里已经存好的文档向量 embedding
    // - 右边是本次查询文本生成的 query embedding
    // - distance 越小，代表语义越接近
    // queryEmbedding 同样以字符串参数传入，所以这里也显式 CAST 成 vector。
    @Query(value = """
            SELECT
                id AS id,
                title AS title,
                content AS content,
                document_title AS documentTitle,
                chunk_index AS chunkIndex,
                chunk_count AS chunkCount,
                chunk_start AS chunkStart,
                chunk_end AS chunkEnd,
                source_type AS sourceType,
                source_name AS sourceName,
                external_id AS externalId,
                embedding <=> CAST(:queryEmbedding AS vector) AS distance
            FROM ai_document_embedding
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :topK
            """, nativeQuery = true)
    List<VectorSearchProjection> searchByCosineDistance(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") int topK
    );
}
