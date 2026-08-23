package com.yytnet.fms.ailab.vector.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_document_embedding")
public class AiDocumentEmbeddingEntity {

    // 这个 Entity 目前只用于 Spring Data JPA Repository 识别表和主键。
    // 暂时不映射 embedding 列，是为了避免在学习阶段引入 Hibernate 自定义 pgvector 类型映射。
    // 真正涉及 embedding 的写入和检索，都放在 native SQL 里通过 CAST(... AS vector) 完成。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @Column(name = "document_title")
    private String documentTitle;

    @Column(name = "chunk_index")
    private Integer chunkIndex;

    @Column(name = "chunk_count")
    private Integer chunkCount;

    @Column(name = "chunk_start")
    private Integer chunkStart;

    @Column(name = "chunk_end")
    private Integer chunkEnd;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    protected AiDocumentEmbeddingEntity() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public Integer getChunkCount() {
        return chunkCount;
    }

    public Integer getChunkStart() {
        return chunkStart;
    }

    public Integer getChunkEnd() {
        return chunkEnd;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getExternalId() {
        return externalId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
