package com.yytnet.fms.ailab.rag.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagDocumentChunkerTest {

    private final RagDocumentChunker chunker = new RagDocumentChunker();

    @Test
    void splitShouldPreferSentenceBoundaries() {
        String content = "AAAA。BBBB。CCCC。DDDD。";

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 10, 0);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly("AAAA。BBBB。", "CCCC。DDDD。");
    }

    @Test
    void splitShouldUseCompletePreviousUnitAsOverlapWhenPossible() {
        String content = "AAAA。BBBB。CCCC。DDDD。";

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 10, 5);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly("AAAA。BBBB。", "BBBB。CCCC。", "CCCC。DDDD。");
    }

    @Test
    void splitShouldFallbackToCharacterSplitForLongUnit() {
        String content = "abcdefghijklmnopqrst";

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 8, 2);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly("abcdefgh", "ghijklmn", "mnopqrst");
    }
}
