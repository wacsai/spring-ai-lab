package com.yytnet.fms.ailab.chat.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    /**
     * 调用chatClient进行对话
     */
    public String chat(String msg) {
        String content = chatClient.prompt()
                .user(msg)
                .call()
                .content();
        return content == null ? "" : content;
    }
}
