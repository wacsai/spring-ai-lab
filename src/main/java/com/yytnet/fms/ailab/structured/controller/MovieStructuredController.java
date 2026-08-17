package com.yytnet.fms.ailab.structured.controller;

import com.yytnet.fms.ailab.structured.dto.req.MovieExtractReq;
import com.yytnet.fms.ailab.structured.dto.resp.MovieExtractResp;
import com.yytnet.fms.ailab.structured.service.MovieStructuredService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/structured/movie")
public class MovieStructuredController {

    private final MovieStructuredService movieStructuredService;

    public MovieStructuredController(MovieStructuredService movieStructuredService) {
        this.movieStructuredService = movieStructuredService;
    }

    @PostMapping("/extract")
    public MovieExtractResp extract(@Valid @RequestBody MovieExtractReq req) {
        // Controller 只负责 HTTP 入参和返回；结构化输出的 Prompt、Options、entity 转换都放在 Service。
        return movieStructuredService.extract(req);
    }
}
