package com.yytnet.fms.ailab.vector.repository.projection;

/**
 * Native SQL 查询结果投影。
 *
 * <p>VectorDocumentQueryRepository 的 SELECT 使用了 id/title/content/distance 这些别名，
 * Spring Data JPA 会按 getter 名称把查询结果映射到这个 interface 上。</p>
 */
public interface VectorSearchProjection {

    Long getId();

    String getTitle();

    String getContent();

    Double getDistance();
}
