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
}
