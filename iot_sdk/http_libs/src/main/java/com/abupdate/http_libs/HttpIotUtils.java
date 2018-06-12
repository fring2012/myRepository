package com.abupdate.http_libs;

import com.abupdate.http_libs.engine.HttpManager;
import com.abupdate.http_libs.request.GetRequest;
import com.abupdate.http_libs.request.PostFileRequest;
import com.abupdate.http_libs.request.PostJsonRequest;
import com.abupdate.http_libs.request.PostFormRequest;
import com.abupdate.http_libs.request.base.Request;
import com.abupdate.http_libs.response.Response;

/**
 * 目前所有https请求，使用post
 * post的content-type有form表单，json
 * Created by raise.yang on 17/09/05.
 */

public class HttpIotUtils {

    private static HttpIotUtils s_instance;
    private static HttpManager httpManager;

    public static HttpIotUtils getInstance() {
        if (s_instance == null) {
            synchronized (HttpIotUtils.class) {
                s_instance = new HttpIotUtils();
            }
        }
        return s_instance;
    }

    /**
     * 设置http管理器
     *
     * @param manager
     */
    public static void init(HttpManager manager) {
        httpManager = manager;
    }

    public Response exec(Request request) {
        if (request.getHttpListener() != null) {
            //异步
            httpManager.enqueue(request);
            return null;
        } else {
            //同步
            return httpManager.execute(request);
        }
    }

    public static PostJsonRequest postJson(String url) {
        return new PostJsonRequest(url);
    }

    public static PostFileRequest postFile(String url) {
        return new PostFileRequest(url);
    }

    public static GetRequest get(String url) {
        return new GetRequest(url);
    }
    public static PostFormRequest postForm(String url) {
        return new PostFormRequest(url);
    }


}
