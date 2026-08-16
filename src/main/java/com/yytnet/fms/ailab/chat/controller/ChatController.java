package com.yytnet.fms.ailab.chat.controller;

import com.yytnet.fms.ailab.chat.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/message")
    public String chat() {
        String msg = "你是谁";
        return chatService.chat(msg);
    }

}
