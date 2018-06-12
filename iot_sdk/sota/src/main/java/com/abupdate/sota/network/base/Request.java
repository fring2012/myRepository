package com.abupdate.sota.network.base;

import com.abupdate.http_libs.response.Response;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public abstract class Request<T> {

    protected abstract Response doRequest();

    protected abstract T parseNetworkResponse(Response response);

}
