package com.yytnet.fms.ailab.common.exception;

public class AiMcpException extends RuntimeException {

    public AiMcpException(String message) {
        super(message);
    }

    public AiMcpException(String message, Throwable cause) {
        super(message, cause);
    }
}
