package com.abupdate.http_libs.request.content;

import com.abupdate.http_libs.data.Consts;

/**
 * @author fighter_lee
 * @date 2018/1/17
 */
public class JsonBody extends StringBody{

    public JsonBody(String param) {
        this(param, Consts.DEFAULT_CHARSET);
    }

    public JsonBody(String json, String charset) {
        super(json, Consts.MIME_TYPE_JSON, charset);
    }

    @Override
    public String toString() {
        return "JsonBody{} " + super.toString();
    }
}
