package com.abupdate.http_libs.engine;

import android.content.Context;
import android.os.AsyncTask;

import com.abupdate.http_libs.data.Consts;
import com.abupdate.http_libs.data.HttpConfig;
import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.data.HttpStatus;
import com.abupdate.http_libs.data.NameValuePair;
import com.abupdate.http_libs.exception.HttpException;
import com.abupdate.http_libs.inter.HttpListener;
import com.abupdate.http_libs.request.base.Request;
import com.abupdate.http_libs.request.content.HttpBody;
import com.abupdate.http_libs.response.InternalResponse;
import com.abupdate.trace.Trace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by fighter_lee on 2017/7/18.
 */

public class HttpManager {

    private static final String TAG = HttpManager.class.getSimpleName();
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private HttpConfig config;

    public static HttpConfig build(Context context) {
        return new HttpConfig(context);
    }

    public HttpManager(HttpConfig config) {
        initConfig(config);
    }

    private void initConfig(HttpConfig config) {
        this.config = config;
        Trace.d(TAG, "initConfig() " + config.toString());
    }

    public final HttpConfig getConfig() {
        return config;
    }

    /**
     * 异步请求
     *
     * @param request
     */
    public void enqueue(Request request) {
        ExecuteAsyncTask executeAsyncTask = new ExecuteAsyncTask();
        executeAsyncTask.execute(request);
    }

    /**
     * 同步请求
     *
     * @param request
     * @return
     */
    public InternalResponse execute(Request request) {
        InternalResponse response = handleRequest(request);
        if (request.getHttpListener() != null) {
            //取消回调
            request.getHttpListener().disableListener(true);
        }
        connectWithRetry(request, response);
        return response;
    }

    private class ExecuteAsyncTask extends AsyncTask<Request, Integer, InternalResponse> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected InternalResponse doInBackground(Request[] abstractRequests) {
            Request abstractRequest = abstractRequests[0];
            InternalResponse response = handleRequest(abstractRequest);
            HttpListener httpListener = abstractRequest.getHttpListener();

            if (httpListener != null) {
                httpListener.notifyCallStart(abstractRequest);
            }

            boolean b = connectWithRetry(abstractRequest, response);
            if (!b) {
                if (httpListener != null) {
                    httpListener.notifyCallFailure(response.getException(), response);
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(InternalResponse internalResponse) {
            HttpListener listener = internalResponse.getRequest().getHttpListener();
            listener.notifyCallEnd(internalResponse);
            super.onPostExecute(internalResponse);
        }
    }

    private boolean connectWithRetry(Request abstractRequest, InternalResponse response) {

        response.setRedirectTimes(0);//初始化重定向次数
        response.setRetryTimes(0);//初始化重试次数
        for (int i = 1; i <= abstractRequest.getMaxRetryTimes() + 1; i++) {
            if (i > 1) {
                //第二次请求回调重试
                response.setRetryTimes(response.getRetryTimes() + 1);
                if (abstractRequest.getHttpListener() != null) {
                    abstractRequest.getHttpListener().notifyCallRetry(abstractRequest, abstractRequest.getMaxRetryTimes(), response.getRetryTimes());
                }
            }
            boolean connect = false;
            try {
                connect = connect(abstractRequest, response);
            } catch (HttpException e) {
                e.printStackTrace();
                response.setException(e);
            } catch (IOException e) {
                e.printStackTrace();
                HttpException httpException = new HttpException(e);
                response.setException(httpException);
            }
            if (connect) {
                return true;
            }
        }
        return false;
    }

    private boolean connect(Request abstractRequest, InternalResponse response) throws HttpException, IOException {
        //        Trace.d(TAG, "connect() start.");
        String u = abstractRequest.getUrl();
        URL url = null;
        HttpURLConnection connection;
        InputStream inputStream = null;
        try {
            url = new URL(u);
            if (u.startsWith("https")) {
                connection = (HttpsURLConnection) url.openConnection();
                HttpsURLConnection httpsConn = (HttpsURLConnection)connection;
                if (null != abstractRequest.getHostnameVerifier()){
                    httpsConn.setHostnameVerifier(abstractRequest.getHostnameVerifier());
                }
                if (null != abstractRequest.getSslSocketFactory()){
                    httpsConn.setSSLSocketFactory(abstractRequest.getSslSocketFactory());
                }
            } else {
                connection = (HttpURLConnection) url.openConnection();
            }
            connection.setConnectTimeout(abstractRequest.getConnectTimeout());
            connection.setReadTimeout(abstractRequest.getSocketTimeout());
            connection.setRequestMethod(abstractRequest.getMethod().getMethodName());
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty(HEADER_CONTENT_TYPE, abstractRequest.getContentType());

            writeData(connection, abstractRequest);
            //            byte[] request_data = abstractRequest.getContent();
            //            if (null != request_data) {
            //                connection.setFixedLengthStreamingMode(request_data.length);
            //                connection.setRequestProperty(HEADER_CONTENT_LENGTH,
            //                        String.valueOf(request_data.length));
            //                OutputStream outputStream = connection.getOutputStream();
            //                outputStream.write(request_data);
            //            }

            int statusCode = connection.getResponseCode();
            //            statusCode = 302;
            if (statusCode != 200) {
                Trace.d(TAG, "connect() responseCode：" + statusCode);
            }

            HttpStatus httpStatus = new HttpStatus(statusCode, connection.getResponseMessage());
            response.setHttpStatus(httpStatus);

            inputStream = connection.getInputStream();
            if (inputStream == null) {
                throw new HttpException(HttpException.HTTP_ERROR_UNREACHABLE);
            }

            ArrayList<NameValuePair> headers = new ArrayList<NameValuePair>();
            for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
                if (header.getKey() != null) {
                    List<String> values = header.getValue();
                    if (values != null) {
                        for (String value : values) {
                            headers.add(new NameValuePair(header.getKey(), value));
                        }
                    }
                }
            }
            response.setHeaders(headers);
            response.setContentLength(connection.getContentLength());
            response.setContentEncoding(connection.getContentEncoding());
            response.setContentType(connection.getContentType());

            if (statusCode <= 299 || statusCode == 600) {
                String charSet = getCharsetByContentType(response.getContentType(), abstractRequest.getCharset());
                response.setCharset(charSet);
                String result = getResultString(inputStream, abstractRequest.getCharset());

                response.setContent(result);
                if (abstractRequest.getHttpListener() != null) {
                    abstractRequest.getHttpListener().notifyCallSuccess(result, response);
                }
                return true;

            } else if (statusCode <= 399) {
                //重定向
                if (response.getRedirectTimes() < abstractRequest.getMaxRedirectTimes()) {
                    //重新请求
                    response.setRedirectTimes(response.getRedirectTimes() + 1);
                    if (abstractRequest.getHttpListener() != null) {
                        abstractRequest.getHttpListener().notifyCallRedirect(
                                abstractRequest, abstractRequest.getMaxRedirectTimes(), response.getRedirectTimes());
                    }
                    URL redirect_url = connection.getURL();
                    Trace.d(TAG, "connect() 重定向:" + redirect_url.toString());
                    abstractRequest.setUrl(redirect_url.toString());
                    connect(abstractRequest, response);
                }

            } else if (statusCode <= 499) {
                // 客户端被拒
                throw new HttpException(httpStatus.getCode(), new Throwable(httpStatus.getDescriptionInChinese()));
            } else if (statusCode < 599) {
                // 服务器有误
                throw new HttpException(httpStatus.getCode(), new Throwable(httpStatus.getDescriptionInChinese()));
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return false;
    }

    private void writeData(HttpURLConnection conn, Request request) throws IOException {
        HttpBody httpBody = request.getHttpBody();
        if (httpBody != null && request.getMethod() == HttpMethods.Post) {
            conn.setDoOutput(true);
            conn.setRequestProperty(Consts.CONTENT_TYPE, httpBody.getContentType());
            OutputStream outStream = conn.getOutputStream();
            httpBody.writeTo(outStream);
            outStream.close();
        }
    }

    protected InternalResponse handleRequest(Request request) {
        //设置默认的数据
        if (request.getConnectTimeout() <= 0) {
            request.setConnectTimeout(config.getConnectTimeout());
        }
        if (request.getSocketTimeout() <= 0) {
            request.setSocketTimeout(config.getSocketTimeout());
        }
        if (request.getMethod() == null) {
            request.setMethod(HttpMethods.Get);
        }
        if (request.getCharset() == null) {
            request.setCharset(HttpConfig.DEFAULT_CHARSET);
        }
        if (request.getMaxRetryTimes() <= 0) {
            request.setMaxRetryTimes(config.getRetryTimes());
        }
        if (request.getMaxRedirectTimes() <= 0) {
            request.setMaxRedirectTimes(config.getRedirectTimes());
        }
        if (request.getContentType() == null) {
            request.setHeaderContentType(HttpConfig.CONTENT_TYPE);
        }

        return new InternalResponse(request);
    }

    private String getCharsetByContentType(String contentType, String defCharset) {
        if (contentType != null) {
            String[] values = contentType.split(";"); // values.length should be 2
            for (String value : values) {
                value = value.trim();
                if (value.toLowerCase().startsWith("charset=")) {
                    return value.substring("charset=".length());
                }
            }
        }
        return defCharset == null ? HttpConfig.DEFAULT_CHARSET : defCharset;
    }

    private static String getResultString(InputStream inputStream, String encode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result = "";
        if (inputStream != null) {
            try {
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                result = new String(outputStream.toByteArray(), encode);

            } catch (IOException e) {
                Trace.d(TAG, "getResultString() exception:" + e.toString());
                e.printStackTrace();
            }
        }
        return result;
    }
}
