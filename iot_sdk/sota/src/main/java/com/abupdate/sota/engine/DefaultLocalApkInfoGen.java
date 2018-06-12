package com.abupdate.sota.engine;

import com.abupdate.sota.info.remote.ApkInfo;
import com.abupdate.sota.inter.GenLocalApkInfoInter;
import com.abupdate.sota.utils.ApkUtils;

/**
 * @author fighter_lee
 * @date 2018/3/26
 */
public class DefaultLocalApkInfoGen implements GenLocalApkInfoInter {
    @Override
    public ApkInfo genlocalApkInfo(String packageName) {
        return ApkUtils.getLocalApkInfo(packageName);
    }
}
