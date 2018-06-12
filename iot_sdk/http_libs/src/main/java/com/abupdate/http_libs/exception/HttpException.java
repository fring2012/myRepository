package com.abupdate.http_libs.exception;


public class HttpException extends Exception {

    public static final int HTTP_ERROR_UNREACHABLE = 1;

    private int reasonCode;
    private Throwable cause;

    public HttpException(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public HttpException(int reason, Throwable cause) {
        this.reasonCode = reason;
        this.cause = cause;
    }

    public HttpException(Throwable cause) {
        this.cause = cause;
    }

    public int getReasonCode() {
        return this.reasonCode;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String toString() {
        String result = this.getMessage() + " (" + this.reasonCode + ")";
        if (this.cause != null) {
            result = result + " - " + this.cause.toString();
        }

        return result;
    }

    public static String getErrorMessage(int error) {
        switch (error) {
            case HTTP_ERROR_UNREACHABLE:
                return "网络无法访问";
            default:
                return "Unknown error";
        }
    }
}
