package com.abupdate.iot_libs.info;

/**
 * Created by lyb on 2017/2/28.
 *
 */

public class SafeInfo {

    /**
     * 是否加密(1代表加密,0代表不加密)
     */
    public int isEncrypt;

    /**
     * 公钥
     */
    public String encKey;

    private static SafeInfo mInstance;

    private SafeInfo() {
    }

    public static SafeInfo getInstance() {
        if (mInstance == null) {
            synchronized (SafeInfo.class) {
                if (mInstance == null) {
                    mInstance = new SafeInfo();
                }
            }
        }
        return mInstance;
    }

}
