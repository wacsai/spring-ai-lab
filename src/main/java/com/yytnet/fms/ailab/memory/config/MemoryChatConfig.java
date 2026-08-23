package com.yytnet.fms.ailab.memory.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryChatConfig {

    // 内存版 Memory 只保留最近 N 条消息。
    // 消息包括 UserMessage 和 AssistantMessage；超过窗口后旧消息会被丢弃，避免 Prompt 无限增长。
    public static final int MAX_MEMORY_MESSAGES = 20;

    @Bean
    public ChatMemory chatMemory() {
        // InMemoryChatMemoryRepository 底层就是 JVM 内存里的 Map。
        // Spring Boot 进程重启后，这些对话历史会全部丢失；当前阶段只用于理解 Memory 调用链。
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(MAX_MEMORY_MESSAGES)
                .build();
    }
}
