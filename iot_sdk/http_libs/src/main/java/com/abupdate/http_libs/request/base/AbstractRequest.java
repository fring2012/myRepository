package com.abupdate.http_libs.request.base;

import com.abupdate.http_libs.HttpIotUtils;
import com.abupdate.http_libs.data.HttpConfig;
import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.inter.HttpListener;
import com.abupdate.http_libs.request.content.HttpBody;
import com.abupdate.http_libs.response.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by fighter_lee on 2017/7/18.
 */

public abstract class AbstractRequest implements Request {

    private static final String TAG = Request.class.getSimpleName();
    private String url;
    private HttpMethods method;
    private String charset;//默认UTF—8
    private int connectTimeout = -1;
    private int socketTimeout = -1;
    private int maxRetryTimes = -1;//重试次数
    private int maxRedirectTimes = -1;//重定向重试次数
    private HttpListener httpListener;
    private String type;
    public static final String CR_LF = "\r\n";
    public static final String TRANSFER_ENCODING_BINARY = ("Content-Transfer-Encoding: binary\r\n");
    private HttpBody httpBody;
    private SSLSocketFactory sslSocketFactory;
    private HostnameVerifier hostnameVerifier;

    public AbstractRequest() {
        charset = HttpConfig.DEFAULT_CHARSET;
    }

    @Override
    public Request setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public HttpMethods getMethod() {
        return method;
    }

    @Override
    public Request setMethod(HttpMethods method) {
        this.method = method;
        return this;
    }

    /**
     * 设置请求中的媒体类型信息
     *
     * @param type
     * @return
     */
    @Override
    public Request setHeaderContentType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public String getContentType() {
        return type;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    /**
     * 设置请求编码
     *
     * @param charset
     * @return
     */
    @Override
    public Request setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    @Override
    public Request setHttpBody(HttpBody body) {
        this.httpBody = body;
        return this;
    }

    @Override
    public HttpBody getHttpBody() {
        return httpBody;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * 连接超时时间
     *
     * @param connectTimeout
     * @return
     */
    @Override
    public Request setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public int getSocketTimeout() {
        return socketTimeout;
    }

    /**
     * 读取超时时间
     *
     * @param socketTimeout
     * @return
     */
    @Override
    public Request setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    @Override
    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    /**
     * 设置重试次数(第一次请求不算入重试次数中)
     *
     * @param maxRetryTimes
     * @return
     */
    @Override
    public Request setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
        return this;
    }

    @Override
    public int getMaxRedirectTimes() {
        return maxRedirectTimes;
    }

    /**
     * 设置重定向次数(第一次请求不算入重试次数中)
     *
     * @param maxRedirectTimes
     * @return
     */
    @Override
    public Request setMaxRedirectTimes(int maxRedirectTimes) {
        this.maxRedirectTimes = maxRedirectTimes;
        return this;
    }

    @Override
    public HttpListener getHttpListener() {
        return httpListener;
    }

    @Override
    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    @Override
    public Request setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
        return this;
    }

    @Override
    public Request setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    @Override
    public Request build() {
        return this;
    }

    @Override
    public Response exec() {
        return HttpIotUtils.getInstance().exec(this);
    }

    @Override
    public void exec(HttpListener httpListener) {
        this.httpListener = httpListener;
        HttpIotUtils.getInstance().exec(this);
    }
}