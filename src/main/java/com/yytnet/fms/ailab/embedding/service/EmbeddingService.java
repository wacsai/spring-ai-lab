package com.yytnet.fms.ailab.embedding.service;

import com.yytnet.fms.ailab.common.exception.AiEmbeddingException;
import com.yytnet.fms.ailab.embedding.dto.req.EmbeddingReq;
import com.yytnet.fms.ailab.embedding.dto.resp.EmbeddingResp;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class EmbeddingService {

    private static final int SAMPLE_SIZE = 8;

    private final EmbeddingModel embeddingModel;
    private final String model;

    public EmbeddingService(EmbeddingModel embeddingModel,
                            @Value("${spring.ai.ollama.embedding.model}") String model) {
        this.embeddingModel = embeddingModel;
        this.model = model;
    }

    public EmbeddingResp embed(EmbeddingReq req) {
        try {
            // EmbeddingModel 和 ChatClient/ChatModel 的职责不同：
            // - ChatClient/ChatModel 负责生成自然语言回答。
            // - EmbeddingModel 负责把文本转换成 float[] 向量，后续用于相似度检索和 RAG。
            float[] vector = embeddingModel.embed(req.text());
            if (vector.length == 0) {
                throw new AiEmbeddingException("模型返回了空的文本向量", null);
            }

            return new EmbeddingResp(
                    req.text(),
                    model,
                    vector.length,
                    Math.min(SAMPLE_SIZE, vector.length),
                    sample(vector),
                    "当前接口只返回向量维度和前几个数值样例；完整向量后续会存入 pgvector。"
            );
        } catch (AiEmbeddingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new AiEmbeddingException("文本向量化失败", ex);
        }
    }

    private List<Float> sample(float[] vector) {
        // 真实向量通常有上千维，不适合完整返回给前端；学习阶段取前 8 个值方便观察。
        return IntStream.range(0, Math.min(SAMPLE_SIZE, vector.length))
                .mapToObj(index -> vector[index])
                .toList();
    }
}
