package com.example.c.utils;

import android.os.Handler;
import android.os.Looper;

import com.example.c.been.ResultData;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpManager {
    private  static OkHttpManager mOkHttpManager;
    private OkHttpClient mOkHttpClient;
    private Gson mGson;
    private OkHttpManager(){
        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.newBuilder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
        mGson = new Gson();
    }

    public static OkHttpManager getInstance(){
        if (mOkHttpManager == null) {
            mOkHttpManager = new OkHttpManager();
        }
        return mOkHttpManager;
    }


    /**
     * 同步post请求
     * @param url
     * @param params
     * @return Response
     */
    public Response getResponseDoPostJsonSync(String url, Map<String, Object> params)  {
        if(params == null){
            params = new HashMap<>();
        }
        Response response = null;
        try {
            String jsonParams = mGson.toJson(params);
            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(mediaType,jsonParams);
            Request request = new Request.Builder().url(url).post(requestBody).build();
            response = mOkHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     *
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public ResponseBody getResponseBodyDoPostJsonSync(String url, Map<String, Object> params){
        return getResponseDoPostJsonSync(url,params).body();
    }

    /**
     * @param url
     * @param params
     * @return
     * @throws IOException
     */
    public String getStringDoPostJsonSync(String url, Map<String, Object> params){
        try {
            return getResponseBodyDoPostJsonSync(url,params).string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
