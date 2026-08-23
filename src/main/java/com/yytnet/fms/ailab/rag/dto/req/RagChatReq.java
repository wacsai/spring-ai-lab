package com.yytnet.fms.ailab.rag.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RagChatReq(
        @NotBlank(message = "问题不能为空")
        @Size(max = 2000, message = "问题不能超过2000个字符")
        String question,

        // 先从向量库取最相似的前 N 条，再按 maxDistance 过滤。
        @Min(value = 1, message = "topK不能小于1")
        @Max(value = 20, message = "topK不能大于20")
        Integer topK,

        // pgvector cosine distance 越小越相似。
        // 这里的阈值用于避免把明显无关的文档塞给 Chat Model。
        @DecimalMin(value = "0.0", message = "maxDistance不能小于0")
        @DecimalMax(value = "2.0", message = "maxDistance不能大于2")
        Double maxDistance,

        // 可选来源过滤。
        // 不传时从所有向量记录里检索；传 MARKDOWN 时只检索 Markdown 文件导入的 chunk。
        // 这一步发生在数据库相似度排序之前，所以不会先从全库 topK 里拿到无关来源后再过滤。
        @Pattern(regexp = "MANUAL|TEXT|MARKDOWN|PDF|URL|", message = "sourceType只支持MANUAL、TEXT、MARKDOWN、PDF、URL")
        String sourceType,

        // 可选外部来源 ID 过滤。
        // 对文件导入来说，当前 externalId 默认就是原始文件名，例如 spring-ai-rag-sample.md。
        // 后续接入业务系统时，它可以是文件路径、对象存储 key、业务表主键或 URL。
        @Size(max = 500, message = "externalId不能超过500个字符")
        String externalId
) {
}
