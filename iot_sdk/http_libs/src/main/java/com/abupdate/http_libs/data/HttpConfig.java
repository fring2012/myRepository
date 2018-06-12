package com.abupdate.http_libs.data;

import android.content.Context;

import com.abupdate.http_libs.HttpIotUtils;
import com.abupdate.http_libs.engine.HttpManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by fighter_lee on 2017/7/18.
 * <p>
 * 默认的http请求参数
 */

public class HttpConfig {
    private Context context;

    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final int DEFAULT_TIMEOUT = 20 * 1000;
    public static final String DEFAULT_CHARSET = "UTF-8";
    private static final int DEFAULT_RETRYTIMES = 3;
    private static final int DEFAULT_REDIRECTTIMES = 3;
    public static final String CONTENT_TYPE = RequestConfig.FORM;

    private int socketTimeout = DEFAULT_TIMEOUT;

    private int connectTimeout = DEFAULT_TIMEOUT;

    private int retryTimes = DEFAULT_RETRYTIMES;

    private int redirectTimes = DEFAULT_REDIRECTTIMES;

    public HttpConfig(Context context) {
        if (context != null) {
            this.context = context.getApplicationContext();
        }
    }

    /**
     * 设置https请求证书
     *
     * @param password
     * @param keyStorePath
     * @return
     */
    public HttpConfig setSSL(String password, String keyStorePath) {
        initSSL(password, keyStorePath, defaultVerifier);
        return this;
    }

    /**
     * 设置https请求证书以及域名校验
     *
     * @param password
     * @param keyStorePath
     * @param verifier
     * @return
     */
    public HttpConfig setSSL(String password, String keyStorePath, HostnameVerifier verifier) {
        initSSL(password, keyStorePath, verifier);
        return this;
    }

    /**
     * 设置请求重试次数
     *
     * @param retryTimes
     * @return
     */
    public HttpConfig setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
        return this;
    }

    /**
     * 设置重定向重试次数
     *
     * @param redirectTimes
     * @return
     */
    public HttpConfig setRedirectTimes(int redirectTimes) {
        this.redirectTimes = redirectTimes;
        return this;
    }

    /**
     * 读取超时（毫秒）
     *
     * @param socketTimeout
     * @return
     */
    public HttpConfig setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    /**
     * 连接超时（毫秒）
     *
     * @param connectTimeout
     * @return
     */
    public HttpConfig setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public void create() {
        HttpIotUtils.init(new HttpManager(this));
    }

    public int getRedirectTimes() {
        return redirectTimes;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    private void initSSL(String password, String keyStorePath, HostnameVerifier verifier) {
        // 声明SSL上下文
        SSLContext sslContext = null;
        // 实例化主机名验证接口
        HostnameVerifier hnv = verifier;
        try {
            sslContext = getSSLContext(password, keyStorePath);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sslContext != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
                    .getSocketFactory());
        }
        HttpsURLConnection.setDefaultHostnameVerifier(hnv);
    }

    private SSLContext getSSLContext(String password, String keyStorePath) throws Exception {
        // 实例化密钥库
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // 获得密钥库
        KeyStore keyStore = getKeyStore(password, keyStorePath);
        // 初始化密钥工厂
        keyManagerFactory.init(keyStore, password.toCharArray());

        // 实例化信任库
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // 获得信任库
        //        KeyStore trustStore = getKeyStore(password, trustStorePath);
        // 初始化信任库
        trustManagerFactory.init(keyStore);
        // 实例化SSL上下文
        SSLContext ctx = SSLContext.getInstance("TLS");
        // 初始化SSL上下文
        ctx.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(), null);
        // 获得SSLSocketFactory
        return ctx;
    }

    private KeyStore getKeyStore(String password, String keyPath) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore ks = KeyStore.getInstance("BKS");
        InputStream keyin = this.getClass().getResourceAsStream(keyPath);
        ks.load(keyin, password.toCharArray());
        keyin.close();
        return ks;
    }

    private HostnameVerifier defaultVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //默认校验通过
            return true;
        }
    };

    @Override
    public String toString() {
        return "HttpConfig{" +
                "context=" + context +
                ", connectTimeout=" + connectTimeout +
                ", socketTimeout=" + socketTimeout +
                '}';
    }
}
