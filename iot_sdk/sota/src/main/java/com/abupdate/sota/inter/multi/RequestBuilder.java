package com.abupdate.sota.inter.multi;

import com.abupdate.sota.inter.BaseListener;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public abstract class RequestBuilder<T> {

    public abstract T executed();

    public abstract void enqueue(BaseListener listener);

}
