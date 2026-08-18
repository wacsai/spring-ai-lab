package com.yytnet.fms.ailab.vector.repository;

import com.yytnet.fms.ailab.vector.domain.AiDocumentEmbeddingEntity;
import com.yytnet.fms.ailab.vector.repository.projection.VectorSearchProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VectorDocumentQueryRepository extends Repository<AiDocumentEmbeddingEntity, Long> {

    @Query(value = """
            SELECT
                id AS id,
                title AS title,
                content AS content,
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
