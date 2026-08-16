package com.yytnet.fms.ailab.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String chat(String message) {
        String content = chatClient.prompt()
                .user(message)
                .call()
                .content();
        return content == null ? "" : content;
    }
}
