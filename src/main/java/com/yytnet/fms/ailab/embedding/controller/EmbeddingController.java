package com.yytnet.fms.ailab.embedding.controller;

import com.yytnet.fms.ailab.embedding.dto.req.EmbeddingReq;
import com.yytnet.fms.ailab.embedding.dto.resp.EmbeddingResp;
import com.yytnet.fms.ailab.embedding.service.EmbeddingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/embedding")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * Embedding 最小闭环接口。
     * 只负责把文本转成向量并返回维度和样例，不做向量数据库存储。
     */
    @PostMapping
    public EmbeddingResp embed(@Valid @RequestBody EmbeddingReq req) {
        return embeddingService.embed(req);
    }
}
