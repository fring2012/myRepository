package com.example.c.utils;

import com.google.gson.Gson;

public class GsonUtil {
    private static GsonUtil gsonUtil;
    private Gson mGson;
    private GsonUtil(){
        mGson = new Gson();
    }
    public static GsonUtil getInstance(){
        if(gsonUtil == null)
            gsonUtil = new GsonUtil();
        return gsonUtil;
    }


}
