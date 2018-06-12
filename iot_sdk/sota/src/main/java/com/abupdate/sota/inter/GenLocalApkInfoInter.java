package com.abupdate.sota.inter;

import com.abupdate.sota.info.remote.ApkInfo;

/**
 * @author fighter_lee
 * @date 2018/3/26
 */
public interface GenLocalApkInfoInter {

    ApkInfo genlocalApkInfo(String packageName);

}
