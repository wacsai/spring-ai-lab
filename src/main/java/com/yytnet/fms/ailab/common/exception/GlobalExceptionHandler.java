package com.yytnet.fms.ailab.common.exception;

import com.yytnet.fms.ailab.common.dto.ErrorResp;
import com.yytnet.fms.ailab.common.dto.FieldErrorResp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求参数不合法异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResp> handleValidationException(MethodArgumentNotValidException ex) {
        // 获取请求参数不合法的错误信息
        List<FieldErrorResp> fields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldErrorResp(error.getField(), error.getDefaultMessage()))
                .toList();

        String message = fields.isEmpty() ? "请求参数不合法" : fields.getFirst().message();
        return ResponseEntity.badRequest()
                .body(new ErrorResp(HttpStatus.BAD_REQUEST.value(), message, fields));
    }

    /**
     * 处理请求体格式不正确异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResp> handleHttpMessageNotReadableException() {
        return ResponseEntity.badRequest()
                .body(new ErrorResp(HttpStatus.BAD_REQUEST.value(), "请求体格式不正确", List.of()));
    }

    /**
     * 处理业务层主动抛出的 400 请求错误，例如上传空文件或不支持的文件类型。
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResp> handleBadRequestException(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResp(HttpStatus.BAD_REQUEST.value(), ex.getMessage(), List.of()));
    }

    /**
     * 处理模型结构化输出失败异常
     */
    @ExceptionHandler(AiStructuredOutputException.class)
    public ResponseEntity<ErrorResp> handleAiStructuredOutputException(AiStructuredOutputException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ErrorResp(HttpStatus.UNPROCESSABLE_CONTENT.value(), ex.getMessage(), List.of()));
    }

    /**
     * 处理模型工具调用失败异常
     */
    @ExceptionHandler(AiToolCallingException.class)
    public ResponseEntity<ErrorResp> handleAiToolCallingException(AiToolCallingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResp(HttpStatus.BAD_GATEWAY.value(), ex.getMessage(), List.of()));
    }

    /**
     * 处理文本向量化失败异常
     */
    @ExceptionHandler(AiEmbeddingException.class)
    public ResponseEntity<ErrorResp> handleAiEmbeddingException(AiEmbeddingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResp(HttpStatus.BAD_GATEWAY.value(), ex.getMessage(), List.of()));
    }

    /**
     * 处理向量存储或相似度检索失败异常
     */
    @ExceptionHandler(AiVectorStoreException.class)
    public ResponseEntity<ErrorResp> handleAiVectorStoreException(AiVectorStoreException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResp(HttpStatus.BAD_GATEWAY.value(), ex.getMessage(), List.of()));
    }

    /**
     * 处理 RAG 检索增强生成失败异常
     */
    @ExceptionHandler(AiRagException.class)
    public ResponseEntity<ErrorResp> handleAiRagException(AiRagException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResp(HttpStatus.BAD_GATEWAY.value(), ex.getMessage(), List.of()));
    }
}
