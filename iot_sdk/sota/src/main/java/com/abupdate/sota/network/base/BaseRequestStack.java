package com.abupdate.sota.network.base;

import com.abupdate.http_libs.response.Response;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public abstract class BaseRequestStack {

    public Object doRequest(Request request) {
        Response response = request.doRequest();
        return request.parseNetworkResponse(response);
    }

}
