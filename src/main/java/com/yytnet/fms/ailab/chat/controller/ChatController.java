package com.yytnet.fms.ailab.chat.controller;

import com.yytnet.fms.ailab.chat.dto.req.ChatReq;
import com.yytnet.fms.ailab.chat.dto.resp.ChatResp;
import com.yytnet.fms.ailab.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/ai")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResp chat(@Valid @RequestBody ChatReq req) {
        return new ChatResp(chatService.chat(req));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatReq req) {
        return chatService.stream(req);
    }

}
