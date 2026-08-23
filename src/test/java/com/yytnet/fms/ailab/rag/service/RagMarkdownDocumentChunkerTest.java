package com.yytnet.fms.ailab.rag.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagMarkdownDocumentChunkerTest {

    private final RagMarkdownDocumentChunker chunker = new RagMarkdownDocumentChunker(new RagDocumentChunker());

    @Test
    void splitShouldPreferMarkdownHeadingSections() {
        String content = """
                # Spring AI
                ChatClient 负责组织 Prompt。

                ## RAG
                RAG 的核心流程是先检索资料，再生成回答。

                ## Embedding
                Embedding 负责把文本转换成向量。
                """;

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 200, 0);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly(
                        "# Spring AI\nChatClient 负责组织 Prompt。",
                        "## RAG\nRAG 的核心流程是先检索资料，再生成回答。",
                        "## Embedding\nEmbedding 负责把文本转换成向量。"
                );
    }

    @Test
    void splitShouldKeepHeadingContextWhenSectionHasMultipleChunks() {
        String content = """
                ## RAG
                abcdefghijklmnopqrstuvwxyz
                """;

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 12, 0);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .allSatisfy(text -> assertThat(text).startsWith("## RAG"));
    }

    @Test
    void splitShouldIgnoreHeadingsInsideFencedCodeBlocks() {
        String content = """
                ## Real
                这是正文。
                ```
                ## Not Heading
                ```
                这是代码块后的正文。
                """;

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 200, 0);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly("""
                        ## Real
                        这是正文。
                        ```
                        ## Not Heading
                        ```
                        这是代码块后的正文。""");
    }

    @Test
    void splitShouldMergeHeadingOnlyChunkIntoNextChunk() {
        String content = """
                # Spring AI RAG 测试资料

                ## RAG
                RAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。

                ## Citations
                citations 用于展示回答引用了哪些资料。
                """;

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 200, 0);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly(
                        "# Spring AI RAG 测试资料\n\n## RAG\nRAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。",
                        "## Citations\ncitations 用于展示回答引用了哪些资料。"
                );
    }

    @Test
    void splitShouldMergeVeryShortBodyChunkIntoNextChunk() {
        String content = """
                ## 摘要
                RAG。

                ## 详细说明
                RAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。
                """;

        List<RagDocumentChunker.Chunk> chunks = chunker.split(content, 200, 0);

        assertThat(chunks)
                .extracting(RagDocumentChunker.Chunk::text)
                .containsExactly("""
                        ## 摘要
                        RAG。

                        ## 详细说明
                        RAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。""");
    }
}
