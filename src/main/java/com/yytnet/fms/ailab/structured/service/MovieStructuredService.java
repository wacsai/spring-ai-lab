package com.yytnet.fms.ailab.structured.service;

import com.yytnet.fms.ailab.common.exception.AiStructuredOutputException;
import com.yytnet.fms.ailab.structured.dto.req.MovieExtractReq;
import com.yytnet.fms.ailab.structured.dto.resp.MovieExtractResp;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MovieStructuredService {

    // 结构化输出适合固定任务：这个 Service 只做“电影信息提取”，不承担开放聊天职责。
    // Prompt 里明确输出规则，是为了让模型更稳定地生成可被 Java record 接收的 JSON。
    private static final String SYSTEM_PROMPT = """
            你是一个电影信息提取器。
            任务：从用户输入中提取电影相关信息，并返回结构化对象。
            规则：
            - 为了通过 JSON Schema 校验，不要在 JSON 中返回 null。
            - 字符串字段缺失时返回空字符串 ""。
            - releaseYear 缺失时返回 0。
            - 如果输入与电影无关，movieRelated=false，title/director 返回空字符串，releaseYear 返回 0，reason 说明“输入内容与电影信息无关”。
            - 如果输入与电影有关，movieRelated=true。
            - 只能提取输入中明确出现或可高度确定的信息，不能编造。
            - 如果电影相关但某个字段缺失，该字段按上面的缺失规则返回，并在 reason 中说明缺失字段。
            - 如果电影相关且字段完整，reason 返回空字符串。
            - 必须使用中文填写 reason。
            """;

    private final ChatClient chatClient;
    private final String model;

    public MovieStructuredService(ChatClient.Builder chatClientBuilder,
                                  @Value("${spring.ai.ollama.chat.model}") String model) {
        this.chatClient = chatClientBuilder.build();
        this.model = model;
    }

    public MovieExtractResp extract(MovieExtractReq req) {
        try {
            // .entity(MovieExtractResp.class) 会要求 Spring AI 把模型输出转换成 Java record。
            // 和 .content() 只取字符串不同，这里一旦模型输出不是合法结构，就会进入异常处理。
            MovieExtractResp resp = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(req.text())
                    .options(OllamaChatOptions.builder()
                            // 请求级 options 需要显式带上模型名，避免覆盖默认配置时丢失 model。
                            .model(model)
                            // Ollama 的 JSON 模式会约束模型尽量返回 JSON，而不是普通自然语言。
                            .format("json")
                            // reasoning/thinking 内容可能干扰 JSON 输出，当前结构化输出阶段先关闭。
                            .disableThinking())
                    .call()
                    // validateSchema() 会按 MovieExtractResp 生成的 JSON Schema 校验模型输出。
                    // 大模型负责“抽取和填 JSON”,Spring AI 负责“把 JSON 变成 Java 对象”
                    .entity(MovieExtractResp.class, ChatClient.EntityParamSpec::validateSchema);
            return normalize(requireStructuredOutput(resp));
        } catch (AiStructuredOutputException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            // 结构化输出失败通常表示模型没有按 schema 返回，或 JSON 无法反序列化成目标对象。
            throw new AiStructuredOutputException("模型未能返回合法的电影结构化结果", ex);
        }
    }

    private MovieExtractResp requireStructuredOutput(MovieExtractResp resp) {
        if (resp == null) {
            throw new AiStructuredOutputException("模型返回了空的电影结构化结果", null);
        }
        return resp;
    }

    private MovieExtractResp normalize(MovieExtractResp resp) {
        // 模型为了通过 schema 校验会用 "" / 0 表达缺失；对外接口再归一化成更自然的 null。
        boolean movieRelated = Boolean.TRUE.equals(resp.movieRelated());
        String title = cleanNullableText(resp.title());
        String director = cleanNullableText(resp.director());
        Integer releaseYear = normalizeReleaseYear(resp.releaseYear());
        String reason = cleanNullableText(resp.reason());

        if (!movieRelated) {
            return new MovieExtractResp(false, null, null, null,
                    reason == null ? "输入内容与电影信息无关" : reason);
        }

        if (reason == null && (title == null || director == null || releaseYear == null)) {
            reason = "输入内容与电影相关，但部分字段缺失";
        }

        return new MovieExtractResp(true, title, director, releaseYear, reason);
    }

    private String cleanNullableText(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
            return null;
        }
        return value;
    }

    private Integer normalizeReleaseYear(Integer releaseYear) {
        if (releaseYear == null || releaseYear <= 0) {
            return null;
        }
        return releaseYear;
    }
}
