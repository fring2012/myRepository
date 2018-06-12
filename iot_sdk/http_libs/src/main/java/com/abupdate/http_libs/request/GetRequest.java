package com.abupdate.http_libs.request;

import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.request.base.AbstractRequest;

/**
 * 对Get做简单支持，拼接
 * Created by fighter_lee on 2017/7/19.
 */

public class GetRequest extends AbstractRequest {

    public GetRequest(String url) {
        super();
        setUrl(url);
        setMethod(HttpMethods.Get);
    }
}
