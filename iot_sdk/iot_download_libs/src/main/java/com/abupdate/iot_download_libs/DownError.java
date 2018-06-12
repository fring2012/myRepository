package com.abupdate.iot_download_libs;

/**
 * Created by raise.yang on 17/07/10.
 */

public class DownError {
    /**
     * 下载成功
     */
    public static final int NO_ERROR = 0;
    /**
     * MD5校验失败
     */
    public static final int ERROR_MD5_VERIFY_FAILED = -1;
    /**
     * 网络异常
     */
    public static final int ERROR_NET_WORK = -2;
    /**
     * 内存不足,没有权限
     */
    public static final int ERROR_FILE_IO_EXCEPTION = -3;
    /**
     * 请求文件大小失败(在没有设置文件大小并且从网络中获取失败)
     */
    public static final int ERROR_FETCH_FILE_SIZE = -4;

    /**
     * 块校验失败
     */
    public static final int ERROR_BLOCK_VERIFY_FAIL = -5;

    /**
     * 未知错误
     */
    public static final int UNKNOWN_ERROR = 999;
}
