package com.yytnet.fms.ailab.memory.service;

import com.yytnet.fms.ailab.memory.dto.resp.MemoryClearResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemoryChatServiceTest {

    private ChatMemory chatMemory;
    private MemoryChatService memoryChatService;

    @BeforeEach
    void setUp() {
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));

        chatMemory = mock(ChatMemory.class);
        when(chatMemory.get("demo-001")).thenReturn(List.of());

        memoryChatService = new MemoryChatService(
                chatClientBuilder,
                chatMemory,
                "qwen3.5:4b"
        );
    }

    @Test
    void clearShouldRemoveConversationMemory() {
        MemoryClearResp resp = memoryChatService.clear(" demo-001 ");

        assertThat(resp.conversationId()).isEqualTo("demo-001");
        assertThat(resp.cleared()).isTrue();
        assertThat(resp.memoryMessageCount()).isZero();
        verify(chatMemory).clear("demo-001");
    }
}
