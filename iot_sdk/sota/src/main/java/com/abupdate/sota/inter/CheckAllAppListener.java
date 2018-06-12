package com.abupdate.sota.inter;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public interface CheckAllAppListener extends BaseListener{

    void onSuccess(List<String> packageNames);

}
