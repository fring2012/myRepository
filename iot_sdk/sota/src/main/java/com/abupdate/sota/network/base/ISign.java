package com.abupdate.sota.network.base;

import org.json.JSONObject;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public interface ISign {

    JSONObject genSign(JSONObject json);

}
