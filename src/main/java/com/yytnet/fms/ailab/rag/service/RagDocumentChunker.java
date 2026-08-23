package com.yytnet.fms.ailab.rag.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RagDocumentChunker {

    // chunkSize 表示每段最多多少个 Java 字符，不是 token，也不是英文单词数。
    // overlap 表示相邻两段尽量保留多少个重复字符，用来减少边界处上下文丢失。
    public static final int DEFAULT_CHUNK_SIZE = 500;
    public static final int DEFAULT_OVERLAP = 80;

    public List<Chunk> split(String content, Integer chunkSize, Integer overlap) {
        int resolvedChunkSize = chunkSize == null ? DEFAULT_CHUNK_SIZE : chunkSize;
        int resolvedOverlap = overlap == null ? DEFAULT_OVERLAP : overlap;

        // strip() 去掉首尾空白，避免文档开头或结尾的大量空格也被当成 chunk 内容入库。
        String normalizedContent = content.strip();
        List<Chunk> chunks = new ArrayList<>();
        List<Unit> units = splitToUnits(normalizedContent);
        List<Unit> currentUnits = new ArrayList<>();

        for (Unit unit : units) {
            // 如果一个自然单元本身已经超过 chunkSize，就只能退回字符兜底切分。
            // 这种情况常见于没有标点的大段文本、长代码块、长表格行等。
            if (unit.length() > resolvedChunkSize) {
                addCurrentChunk(chunks, normalizedContent, currentUnits);
                currentUnits.clear();
                addHardSplitChunks(chunks, normalizedContent, unit, resolvedChunkSize, resolvedOverlap);
                continue;
            }

            if (currentUnits.isEmpty()) {
                currentUnits.add(unit);
                continue;
            }

            // 优先把完整句子/段落单元放进同一个 chunk；只有超过 chunkSize 时才换新 chunk。
            if (unit.end() - currentUnits.getFirst().start() <= resolvedChunkSize) {
                currentUnits.add(unit);
                continue;
            }

            List<Unit> previousUnits = List.copyOf(currentUnits);
            addCurrentChunk(chunks, normalizedContent, currentUnits);
            currentUnits = selectOverlapUnits(previousUnits, resolvedOverlap, resolvedChunkSize, unit);
            currentUnits.add(unit);
        }

        addCurrentChunk(chunks, normalizedContent, currentUnits);
        return chunks;
    }

    private List<Unit> splitToUnits(String content) {
        List<Unit> units = new ArrayList<>();
        int start = 0;
        int index = 0;

        while (index < content.length()) {
            char current = content.charAt(index);
            if (isNaturalBoundary(current)) {
                addUnit(units, content, start, index + 1);
                start = skipWhitespace(content, index + 1);
                index = start;
                continue;
            }
            index++;
        }

        addUnit(units, content, start, content.length());
        return units;
    }

    private boolean isNaturalBoundary(char value) {
        return value == '。'
                || value == '！'
                || value == '？'
                || value == '!'
                || value == '?'
                || value == '；'
                || value == ';'
                || value == '.'
                || value == '\n';
    }

    private int skipWhitespace(String content, int start) {
        int index = start;
        while (index < content.length() && Character.isWhitespace(content.charAt(index))) {
            index++;
        }
        return index;
    }

    private void addUnit(List<Unit> units, String content, int start, int end) {
        if (start >= end) {
            return;
        }
        String text = content.substring(start, end);
        if (text.isBlank()) {
            return;
        }
        units.add(new Unit(start, end));
    }

    private void addCurrentChunk(List<Chunk> chunks, String content, List<Unit> units) {
        if (units.isEmpty()) {
            return;
        }

        int start = units.getFirst().start();
        int end = units.getLast().end();
        chunks.add(new Chunk(chunks.size() + 1, start, end, content.substring(start, end)));
    }

    private List<Unit> selectOverlapUnits(List<Unit> previousUnits,
                                          int overlap,
                                          int chunkSize,
                                          Unit nextUnit) {
        if (overlap <= 0) {
            return new ArrayList<>();
        }

        List<Unit> selected = new ArrayList<>();
        int selectedLength = 0;
        for (int index = previousUnits.size() - 1; index >= 0 && selectedLength < overlap; index--) {
            Unit unit = previousUnits.get(index);
            selected.addFirst(unit);
            selectedLength = selected.getLast().end() - selected.getFirst().start();
        }

        // 如果“完整单元 overlap + 下一个单元”超过 chunkSize，就逐步减少 overlap。
        while (!selected.isEmpty() && nextUnit.end() - selected.getFirst().start() > chunkSize) {
            selected.removeFirst();
        }

        return selected;
    }

    private void addHardSplitChunks(List<Chunk> chunks,
                                    String content,
                                    Unit unit,
                                    int chunkSize,
                                    int overlap) {
        int start = unit.start();
        while (start < unit.end()) {
            int end = Math.min(start + chunkSize, unit.end());
            chunks.add(new Chunk(chunks.size() + 1, start, end, content.substring(start, end)));
            if (end == unit.end()) {
                break;
            }
            start = Math.max(start + 1, end - overlap);
        }
    }

    public record Chunk(
            int chunkIndex,
            int start,
            int end,
            String text
    ) {
    }

    private record Unit(
            int start,
            int end
    ) {
        int length() {
            return end - start;
        }
    }
}
