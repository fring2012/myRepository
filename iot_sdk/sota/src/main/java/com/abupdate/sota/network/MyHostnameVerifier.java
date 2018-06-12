package com.abupdate.sota.network;


import com.abupdate.trace.Trace;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Created by lyb on 2017/1/6.
 * description  ${域名校验}
 */

public class MyHostnameVerifier implements HostnameVerifier {

    private static final String TAG = "MyHostnameVerifier";

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (hostname.endsWith("adups.com")) {
//            Trace.d(TAG, "host name verify success! ");
            return true;
        } else {
            Trace.e(TAG, "host name verify failed ! ");
            return false;
        }
    }
}
