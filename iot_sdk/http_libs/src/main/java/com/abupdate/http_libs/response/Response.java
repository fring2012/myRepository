package com.abupdate.http_libs.response;

import com.abupdate.http_libs.data.HttpStatus;
import com.abupdate.http_libs.data.NameValuePair;
import com.abupdate.http_libs.exception.HttpException;
import com.abupdate.http_libs.request.base.Request;

import java.util.ArrayList;

public interface Response {

    ArrayList<NameValuePair> getHeaders();

    HttpStatus getHttpStatus();

    void setContent(String result);

    String getContent();

    Request getRequest();

    long getContentLength();

    String getContentEncoding();

    String getContentType();

    String getCharset();

    boolean isConnectSuccess();

    int getRetryTimes();

    int getRedirectTimes();

    HttpException getException();

    String resToString();

    boolean isResultOk();
}
