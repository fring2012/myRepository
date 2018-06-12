package com.abupdate.sota.inter;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public interface Callback {

    void onFailure(int code);

    void onSuccess();

}
