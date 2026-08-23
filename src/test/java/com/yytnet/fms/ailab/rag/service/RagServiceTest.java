package com.yytnet.fms.ailab.rag.service;

import com.yytnet.fms.ailab.rag.dto.req.RagDocumentImportReq;
import com.yytnet.fms.ailab.rag.dto.resp.RagDocumentImportResp;
import com.yytnet.fms.ailab.vector.service.VectorDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RagServiceTest {

    private VectorDocumentService vectorDocumentService;
    private RagService ragService;

    @BeforeEach
    void setUp() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        vectorDocumentService = mock(VectorDocumentService.class);
        when(vectorDocumentService.createChunk(
                anyString(),
                anyString(),
                anyString(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString(),
                nullable(String.class)
        )).thenReturn(1L);

        RagDocumentChunker ragDocumentChunker = new RagDocumentChunker();
        ragService = new RagService(
                chatClientBuilder,
                vectorDocumentService,
                ragDocumentChunker,
                new RagMarkdownDocumentChunker(ragDocumentChunker),
                "qwen3.5:4b"
        );
    }

    @Test
    void importDocumentShouldReplaceBySourceIdentityWhenExternalIdExists() {
        when(vectorDocumentService.deleteChunksBySourceIdentity("MARKDOWN", "spring-ai-rag-sample.md"))
                .thenReturn(6);

        RagDocumentImportResp resp = ragService.importDocument(new RagDocumentImportReq(
                "Spring AI RAG 样例文档新标题",
                """
                        # Spring AI RAG 测试资料

                        ## RAG
                        RAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。
                        """,
                200,
                0,
                true,
                "MARKDOWN",
                "spring-ai-rag-sample.md",
                "spring-ai-rag-sample.md"
        ));

        assertThat(resp.deletedCount()).isEqualTo(6);
        assertThat(resp.replaceScope()).isEqualTo("SOURCE_IDENTITY");
        verify(vectorDocumentService).deleteChunksBySourceIdentity("MARKDOWN", "spring-ai-rag-sample.md");
        verify(vectorDocumentService, never()).deleteChunksByDocumentTitle(anyString());
    }

    @Test
    void importDocumentShouldFallbackToDocumentTitleWhenExternalIdIsMissing() {
        when(vectorDocumentService.deleteChunksByDocumentTitle("Spring AI 手动笔记"))
                .thenReturn(2);

        RagDocumentImportResp resp = ragService.importDocument(new RagDocumentImportReq(
                "Spring AI 手动笔记",
                "RAG 的核心流程是先检索资料，再把资料作为上下文交给 ChatClient 生成回答。",
                200,
                0,
                true,
                null,
                null,
                null
        ));

        assertThat(resp.deletedCount()).isEqualTo(2);
        assertThat(resp.replaceScope()).isEqualTo("DOCUMENT_TITLE");
        verify(vectorDocumentService).deleteChunksByDocumentTitle("Spring AI 手动笔记");
        verify(vectorDocumentService, never()).deleteChunksBySourceIdentity(anyString(), anyString());
    }
}
