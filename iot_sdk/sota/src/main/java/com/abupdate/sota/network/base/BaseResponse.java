package com.abupdate.sota.network.base;

import com.abupdate.sota.info.local.Error;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public abstract class BaseResponse<T> {

    public boolean isOK;

    public int errorCode;

    public T result;

    public boolean isNetError() {
        return errorCode == Error.NET_ERROR;
    }

    public T getResult(){
        return result;
    }

    public BaseResponse transErrorResult(BaseResponse response){
        if (!isOK){
            response.isOK = false;
            response.errorCode = errorCode;
        }
        return response;
    }

    public BaseResponse<T> transSuccessResult(BaseResponse<T> response) {
        if (isOK){
            response.isOK = true;
            response.result = result;
        }
        return response;
    }

}
