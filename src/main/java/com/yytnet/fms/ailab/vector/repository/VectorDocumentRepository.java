package com.yytnet.fms.ailab.vector.repository;

import com.yytnet.fms.ailab.vector.repository.projection.VectorSearchRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class VectorDocumentRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long save(String title, String content, String embeddingLiteral) {
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

    public List<VectorSearchRow> search(String queryEmbeddingLiteral, int topK) {
        Query query = entityManager.createNativeQuery("""
                SELECT
                    id,
                    title,
                    content,
                    embedding <=> CAST(:queryEmbedding AS vector) AS distance
                FROM ai_document_embedding
                ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
                """);
        query.setParameter("queryEmbedding", queryEmbeddingLiteral);
        query.setMaxResults(topK);

        return query.getResultList()
                .stream()
                .map(this::toVectorSearchRow)
                .toList();
    }

    private VectorSearchRow toVectorSearchRow(Object row) {
        Object[] values = (Object[]) row;
        Number id = (Number) values[0];
        String title = (String) values[1];
        String content = (String) values[2];
        Number distance = (Number) values[3];
        return new VectorSearchRow(id.longValue(), title, content, distance.doubleValue());
    }
}
