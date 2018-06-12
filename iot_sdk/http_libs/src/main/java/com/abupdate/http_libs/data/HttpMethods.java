package com.abupdate.http_libs.data;

/**
 * Created by fighter_lee on 2017/7/18.
 */
public enum HttpMethods {
	/* ******************* Http Get(Query) Request *************/
    /**
     * get
     */
    Get("GET"),
    /**
     * get http header only
     */
    Head("HEAD"),
    /**
     * debug
     */
    Trace("TRACE"),
    /**
     * query
     */
    Options("OPTIONS"),
    /**
     * delete
     */
    Delete("DELETE"),
	/* ******************* Http Upate(Entity Enclosing) Request *************/
    /**
     * update
     */
    Put("PUT"),
    /**
     * add
     */
    Post("POST"),
    /**
     * incremental update
     */
    Patch("PATCH");

    private String methodName;

    HttpMethods(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
