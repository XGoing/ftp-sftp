package com.xgoing.ftp.exception;

/**
 * 自定义异常，配置异常.
 */
public class FtpNoConnectException extends RuntimeException {
    public FtpNoConnectException(String message) {
        super(message);
    }

    public FtpNoConnectException(Throwable cause) {
        super(cause);
    }

    public FtpNoConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
