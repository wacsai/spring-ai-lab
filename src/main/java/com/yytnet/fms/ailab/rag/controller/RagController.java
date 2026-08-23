package com.yytnet.fms.ailab.rag.controller;

import com.yytnet.fms.ailab.rag.dto.req.RagDocumentImportReq;
import com.yytnet.fms.ailab.rag.dto.req.RagChatReq;
import com.yytnet.fms.ailab.rag.dto.resp.RagChatResp;
import com.yytnet.fms.ailab.rag.dto.resp.RagDocumentImportResp;
import com.yytnet.fms.ailab.rag.service.RagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 最小 RAG 闭环：
     * 先从 pgvector 检索相关资料，再把资料交给 ChatClient 生成回答。
     */
    @PostMapping("/chat")
    public RagChatResp chat(@Valid @RequestBody RagChatReq req) {
        return ragService.chat(req);
    }

    /**
     * RAG 文档入库：
     * 把一篇较长文本切成多个 chunk，并逐个生成 embedding 写入 pgvector。
     */
    @PostMapping("/documents")
    public RagDocumentImportResp importDocument(@Valid @RequestBody RagDocumentImportReq req) {
        return ragService.importDocument(req);
    }
}
