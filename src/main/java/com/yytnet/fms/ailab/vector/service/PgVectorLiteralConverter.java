package com.yytnet.fms.ailab.vector.service;

import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class PgVectorLiteralConverter {

    public String toLiteral(float[] vector) {
        // pgvector 接收的字面量格式类似：[0.1,0.2,-0.3]。
        // 当前阶段用字符串 + CAST(:value AS vector) 规避 Hibernate 对 pgvector 类型映射的复杂度。
        return IntStream.range(0, vector.length)
                .mapToObj(index -> Float.toString(vector[index]))
                .collect(Collectors.joining(",", "[", "]"));
    }
}
