package com.yytnet.fms.ailab.rag.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RagMarkdownDocumentChunker {

    private final RagDocumentChunker ragDocumentChunker;

    public RagMarkdownDocumentChunker(RagDocumentChunker ragDocumentChunker) {
        this.ragDocumentChunker = ragDocumentChunker;
    }

    public List<RagDocumentChunker.Chunk> split(String content, Integer chunkSize, Integer overlap) {
        String normalizedContent = content.strip();
        List<MarkdownSection> sections = splitToSections(normalizedContent);
        List<RagDocumentChunker.Chunk> chunks = new ArrayList<>();

        for (MarkdownSection section : sections) {
            // Markdown 先按标题切成 section；每个 section 内部继续复用通用 chunker。
            // 这样可以保留“按句子/换行自然边界切分 + 超长内容字符兜底”的已有行为。
            List<RagDocumentChunker.Chunk> sectionChunks = ragDocumentChunker.split(section.text(), chunkSize, overlap);
            for (RagDocumentChunker.Chunk chunk : sectionChunks) {
                chunks.add(new RagDocumentChunker.Chunk(
                        chunks.size() + 1,
                        section.start() + chunk.start(),
                        section.start() + chunk.end(),
                        addHeadingContextIfMissing(section.heading(), chunk.text())
                ));
            }
        }

        return chunks;
    }

    private List<MarkdownSection> splitToSections(String content) {
        List<MarkdownSection> sections = new ArrayList<>();
        int sectionStart = 0;
        String sectionHeading = null;
        boolean inFencedCodeBlock = false;

        int lineStart = 0;
        while (lineStart < content.length()) {
            int lineEnd = findLineEnd(content, lineStart);
            String line = content.substring(lineStart, lineEnd);
            String trimmedLine = line.stripLeading();

            if (trimmedLine.startsWith("```")) {
                inFencedCodeBlock = !inFencedCodeBlock;
            } else if (!inFencedCodeBlock && isMarkdownHeading(line)) {
                if (sectionStart < lineStart) {
                    addSection(sections, content, sectionStart, lineStart, sectionHeading);
                }
                sectionStart = lineStart;
                sectionHeading = line.strip();
            }

            lineStart = nextLineStart(content, lineEnd);
        }

        addSection(sections, content, sectionStart, content.length(), sectionHeading);
        return sections;
    }

    private void addSection(List<MarkdownSection> sections,
                            String content,
                            int start,
                            int end,
                            String heading) {
        if (start >= end) {
            return;
        }

        String text = content.substring(start, end).strip();
        if (text.isBlank()) {
            return;
        }
        sections.add(new MarkdownSection(start, end, heading, text));
    }

    private boolean isMarkdownHeading(String line) {
        int leadingSpaces = countLeadingSpaces(line);
        if (leadingSpaces > 3 || leadingSpaces >= line.length()) {
            return false;
        }

        int index = leadingSpaces;
        int level = 0;
        while (index < line.length() && line.charAt(index) == '#' && level < 6) {
            level++;
            index++;
        }

        if (level == 0) {
            return false;
        }

        return index == line.length() || Character.isWhitespace(line.charAt(index));
    }

    private int countLeadingSpaces(String line) {
        int count = 0;
        while (count < line.length() && line.charAt(count) == ' ') {
            count++;
        }
        return count;
    }

    private int findLineEnd(String content, int lineStart) {
        int lineEnd = content.indexOf('\n', lineStart);
        if (lineEnd < 0) {
            return content.length();
        }
        return lineEnd;
    }

    private int nextLineStart(String content, int lineEnd) {
        if (lineEnd >= content.length()) {
            return content.length();
        }
        return lineEnd + 1;
    }

    private String addHeadingContextIfMissing(String heading, String text) {
        if (heading == null || text.stripLeading().startsWith(heading)) {
            return text;
        }
        // 同一个 Markdown section 被切成多个 chunk 时，后续 chunk 可能不再包含标题行。
        // 这里把 section 标题补回 chunk 内容，方便模型理解这段资料属于哪个标题。
        return heading + "\n\n" + text;
    }

    private record MarkdownSection(
            int start,
            int end,
            String heading,
            String text
    ) {
    }
}
