package com.abupdate.http_libs.request.content;

import com.abupdate.http_libs.data.HttpConfig;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by fighter_lee on 2018/1/17.
 */
public abstract class HttpBody {
    public static final int OUTPUT_BUFFER_SIZE = HttpConfig.DEFAULT_BUFFER_SIZE;
    protected String contentType;
    protected String contentEncoding;

    public String getContentType() {
        return contentType;
    }

    public abstract long getContentLength();

    public abstract void writeTo(OutputStream outstream) throws IOException;

    public HttpBody setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public HttpBody setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }
}