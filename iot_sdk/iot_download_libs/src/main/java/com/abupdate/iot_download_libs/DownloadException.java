package com.abupdate.iot_download_libs;


public class DownloadException extends Exception {

    public static final int HTTP_ERROR_UNREACHABLE = 1;

    private int reasonCode;
    private Throwable cause;

    public DownloadException(int reasonCode) {
        this.reasonCode = reasonCode;
    }

    public DownloadException(int reason, Throwable cause) {
        this.reasonCode = reason;
        this.cause = cause;
    }

    public DownloadException(Throwable cause) {
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
            case DownError.ERROR_BLOCK_VERIFY_FAIL:
                return "block file verify fail";
            default:
                return "Unknown error";
        }
    }
}
