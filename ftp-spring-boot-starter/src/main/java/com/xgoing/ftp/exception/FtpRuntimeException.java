package com.xgoing.ftp.exception;

/**
 * 自定义异常，运行时异常.
 */
public class FtpRuntimeException extends RuntimeException {
    public FtpRuntimeException() {
        super();
    }

    public FtpRuntimeException(String message) {
        super(message);
    }

    public FtpRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public FtpRuntimeException(Throwable cause) {
        super(cause);
    }
}
