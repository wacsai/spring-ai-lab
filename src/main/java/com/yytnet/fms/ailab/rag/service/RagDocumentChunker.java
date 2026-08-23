package com.yytnet.fms.ailab.rag.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RagDocumentChunker {

    // 学习阶段先使用最简单的“字符数切分”：
    // - chunkSize 表示每段最多多少个 Java 字符，不是 token，也不是英文单词数。
    // - overlap 表示相邻两段保留多少个重复字符，用来减少边界处上下文丢失。
    public static final int DEFAULT_CHUNK_SIZE = 500;
    public static final int DEFAULT_OVERLAP = 80;

    public List<Chunk> split(String content, Integer chunkSize, Integer overlap) {
        int resolvedChunkSize = chunkSize == null ? DEFAULT_CHUNK_SIZE : chunkSize;
        int resolvedOverlap = overlap == null ? DEFAULT_OVERLAP : overlap;

        // strip() 去掉首尾空白，避免文档开头或结尾的大量空格也被当成 chunk 内容入库。
        String normalizedContent = content.strip();
        List<Chunk> chunks = new ArrayList<>();

        int start = 0;
        while (start < normalizedContent.length()) {
            // 当前实现按 String 下标直接截取，因此可能把一句话、一个英文单词、甚至一段 Markdown 结构切开。
            // 这不是企业级最佳切分策略，只是为了先观察“长文档 -> 多个 chunk -> 多条 embedding”的最小闭环。
            int end = Math.min(start + resolvedChunkSize, normalizedContent.length());
            String text = normalizedContent.substring(start, end);
            chunks.add(new Chunk(chunks.size() + 1, start, end, text));

            if (end == normalizedContent.length()) {
                break;
            }

            // 下一个 chunk 从“当前结尾 - overlap”开始，保留边界上下文。
            // 例：chunkSize=120, overlap=20，则区间是 0-120、100-220、200-...
            start = end - resolvedOverlap;
        }

        return chunks;
    }

    public record Chunk(
            int chunkIndex,
            int start,
            int end,
            String text
    ) {
    }
}
