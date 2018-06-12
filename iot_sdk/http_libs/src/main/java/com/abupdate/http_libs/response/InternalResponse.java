package com.abupdate.http_libs.response;


import com.abupdate.http_libs.data.HttpStatus;
import com.abupdate.http_libs.data.NameValuePair;
import com.abupdate.http_libs.exception.HttpException;
import com.abupdate.http_libs.request.base.Request;

import java.util.ArrayList;

public class InternalResponse implements Response {
    private static final String TAG = InternalResponse.class.getSimpleName();
    protected String charset;
    protected HttpStatus httpStatus;
    protected int retryTimes;
    protected int redirectTimes;
    protected long contentLength;
    protected String contentEncoding;
    protected String contentType;
    protected ArrayList<NameValuePair> headers;
    protected Request request;
    protected HttpException exception;
    protected String result;

    public InternalResponse(Request request) {
        this.request = request;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Request getRequest() {
        return request;
    }

    public Request setRequest(Request request) {
        this.request = request;
        return request;
    }

    @Override
    public HttpException getException() {
        return exception;
    }

    public void setException(HttpException e) {
        this.exception = e;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        if (charset != null) {
            this.charset = charset;
        }
    }

    @Override
    public ArrayList<NameValuePair> getHeaders() {
        return headers;
    }

    public void setHeaders(ArrayList<NameValuePair> headers) {
        this.headers = headers;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    public long setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this.contentLength;
    }

    @Override
    public String getContentEncoding() {
        return contentEncoding;
    }

    public InternalResponse setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public InternalResponse setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public void setContent(String result) {
        this.result = result;
    }

    @Override
    public String getContent() {
        return result;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    @Override
    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public int getRedirectTimes() {
        return redirectTimes;
    }

    public void setRedirectTimes(int redirectTimes) {
        this.redirectTimes = redirectTimes;
    }

    @Override
    public boolean isConnectSuccess() {
        return httpStatus != null && httpStatus.isSuccess();
    }

    @Override
    public boolean isResultOk() {
        return result != null;
    }

    @Override
    public String toString() {
        return resToString();
    }

    @Override
    public String resToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("^_^\n")
                .append("____________________________ IOT http response info start ____________________________")
                .append("\n url            : ").append(request.getUrl())
                .append("\n status         : ").append(httpStatus)
                .append("\n charset        : ").append(charset)
                .append("\n retryTimes     : ").append(retryTimes)
                .append("\n redirectTimes  : ").append(redirectTimes)
                .append("\n contentLength  : ").append(contentLength)
                .append("\n contentEncoding: ").append(contentEncoding)
                .append("\n contentType    : ").append(contentType)
                .append("\n header         ");
        if (headers == null) {
            sb.append(": null");
        } else {
            for (NameValuePair nv : headers) {
                sb.append("\n|    ").append(nv);
            }
        }
        sb.append("\n ").append(request)
                .append("\n exception      : ").append(exception)
                .append("\n.")
                .append("\n _________________ data-start _________________")
                .append("\n ").append(result)
                .append("\n _________________ data-over _________________")
                .append("\n____________________________ IOT http response info end ____________________________");
        return sb.toString();
    }
}
