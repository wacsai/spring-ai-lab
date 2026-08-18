package com.yytnet.fms.ailab.vector.controller;

import com.yytnet.fms.ailab.vector.dto.req.VectorDocumentCreateReq;
import com.yytnet.fms.ailab.vector.dto.req.VectorSearchReq;
import com.yytnet.fms.ailab.vector.dto.resp.VectorDocumentCreateResp;
import com.yytnet.fms.ailab.vector.dto.resp.VectorSearchResp;
import com.yytnet.fms.ailab.vector.service.VectorDocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/vector")
public class VectorDocumentController {

    private final VectorDocumentService vectorDocumentService;

    public VectorDocumentController(VectorDocumentService vectorDocumentService) {
        this.vectorDocumentService = vectorDocumentService;
    }

    /**
     * 将一段文本生成 embedding 并写入 pgvector。
     */
    @PostMapping("/documents")
    public VectorDocumentCreateResp create(@Valid @RequestBody VectorDocumentCreateReq req) {
        return vectorDocumentService.create(req);
    }

    /**
     * 将查询文本生成 embedding，并用 pgvector 做精确相似度检索。
     */
    @PostMapping("/search")
    public VectorSearchResp search(@Valid @RequestBody VectorSearchReq req) {
        return vectorDocumentService.search(req);
    }
}
