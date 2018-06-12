package com.abupdate.sota.utils;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class TimeUtils {

    /**
     * 获取精确到秒的时间戳
     * @return
     */
    public static long getSecondTime() {
        return System.currentTimeMillis() / 1000;
    }

}
