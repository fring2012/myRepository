package com.abupdate.http_libs.request.base;

import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.inter.HttpListener;
import com.abupdate.http_libs.request.content.HttpBody;
import com.abupdate.http_libs.response.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * 请求抽象类
 * Created by raise.yang on 17/09/05.
 */

public interface Request {

    Request setUrl(String url);

    String getUrl();

    HttpMethods getMethod();

    Request setMethod(HttpMethods method);

    Request setHeaderContentType(String type);

    String getContentType();

    String getCharset();

    Request setCharset(String charset);

    Request setHttpBody(HttpBody body);

    HttpBody getHttpBody();

    int getConnectTimeout();

    int getSocketTimeout();

    int getMaxRetryTimes();

    int getMaxRedirectTimes();

    Request setConnectTimeout(int connectTimeout);

    Request setSocketTimeout(int socketTimeout);

    Request setMaxRetryTimes(int maxRetryTimes);

    Request setMaxRedirectTimes(int maxRedirectTimes);

    HttpListener getHttpListener();

    SSLSocketFactory getSslSocketFactory();

    Request setSslSocketFactory(SSLSocketFactory sslSocketFactory);

    HostnameVerifier getHostnameVerifier();

    Request setHostnameVerifier(HostnameVerifier hostnameVerifier);

    Request build();

    Response exec();

    void exec(HttpListener httpListener);
}
