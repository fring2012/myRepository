package com.abupdate.http_libs.request;

import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.data.RequestConfig;
import com.abupdate.http_libs.request.base.AbstractRequest;
import com.abupdate.http_libs.request.content.JsonBody;

import org.json.JSONObject;

/**
 * Created by raise.yang on 17/09/05.
 */

public class PostJsonRequest extends AbstractRequest {
    public PostJsonRequest(String url) {
        super();
        setUrl(url);
        setMethod(HttpMethods.Post);
        setHeaderContentType(RequestConfig.JSON);
    }

    public PostJsonRequest json(JSONObject json) {
        return (PostJsonRequest) setHttpBody(new JsonBody(json.toString()));
    }
}
