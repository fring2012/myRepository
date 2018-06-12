package com.abupdate.sota.inter;

import com.abupdate.sota.info.remote.NewAppInfo;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public interface CheckNewVersionListener extends BaseListener{

    void onSuccess(List<NewAppInfo> newAppInfos);

}
