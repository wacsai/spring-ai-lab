package com.yytnet.fms.ailab.rag.dto.resp;

/**
 * RAG 引用摘要。
 * <p>
 * references 会返回完整 chunk 内容，适合调试 Prompt；
 * citations 只返回展示答案依据时常用的摘要信息，适合前端渲染“资料 1 / 资料 2”。
 */
public record RagCitationResp(
        // 和 System Prompt 中的“资料 1”“资料 2”保持一致，用于把模型回答和引用来源对齐。
        String label,
        // 对应 ai_document_embedding 表的 id，方便需要时回查完整 chunk。
        Long referenceId,
        // 当前 chunk 的标题，例如“xxx - chunk 2/3”。
        String title,
        // 原始文档标题。普通短文本入库可能没有该字段，所以允许为 null。
        String documentTitle,
        // 当前 chunk 在原始文档中的序号。普通短文本入库可能没有该字段，所以允许为 null。
        Integer chunkIndex,
        // 原始文档一共切成多少个 chunk。普通短文本入库可能没有该字段，所以允许为 null。
        Integer chunkCount,
        // 当前 chunk 在清洗后原文中的起始字符位置。
        Integer chunkStart,
        // 当前 chunk 在清洗后原文中的结束字符位置。
        Integer chunkEnd,
        // pgvector cosine distance，值越小越相似。
        double distance,
        // 为了更符合人的阅读习惯，接口额外返回 similarity = 1 - distance，值越大越相似。
        double similarity
) {
}
