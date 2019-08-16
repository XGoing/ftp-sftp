package com.xgoing.ftp.exception;

/**
 * 自定义异常，配置异常.
 */
public class FtpException extends Exception {
    public FtpException(String message) {
        super(message);
    }

    public FtpException(Throwable cause) {
        super(cause);
    }

    public FtpException(String message, Throwable cause) {
        super(message, cause);
    }
}
