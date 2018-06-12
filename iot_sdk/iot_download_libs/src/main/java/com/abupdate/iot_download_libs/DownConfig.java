package com.abupdate.iot_download_libs;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * 下载任务配置文件
 * Created by y2222 on 17/07/03.
 */
public class DownConfig {

    public static int CONNECT_TIMEOUT = 30 * 1000;
    public static int READ_TIMEOUT = 30 * 1000;
    public static int RETRY_TIMES_MAX = 1;
    public static int SEGMENT_DOWNLOAD_RETRY_TIME = 5;//块下载重试次数
    public static int RETRY_INTERVAL_TIME = 1 * 1000;//单位MS
    //每一个线程单元的最小值，比如配置100K,下载文件大小为360k,cpu个数是8，将会被分为4个cell线程下载
    public static long THREAD_BLOCK_CELL_MIN = 1024 * 1024;//1M
    //每一个线程单元的最大值，比如配置2M,下载文件大小为50M,cpu个数是8，将会被分为25个cell线程下载
    public static long THREAD_BLOCK_CELL_MAX = 20 * 1024 * 1024;//20M
    //是否分段下载
    public static boolean sSegmentDownload = true;

    public static SSLSocketFactory SSL;

    /**
     * 下载请求超时时间
     *
     * @param timeout
     */
    public static void setConnectTimeout(int timeout) {
        CONNECT_TIMEOUT = timeout;
    }

    /**
     * 下载block的最小值
     *
     * @param blockCellMin
     */
    public static void setThreadBlockCellMin(long blockCellMin) {
        THREAD_BLOCK_CELL_MIN = blockCellMin;
    }

    /**
     * 下载block的最大值
     *
     * @param blockCellMax
     */
    public static void setThreadBlockCellMax(long blockCellMax) {
        THREAD_BLOCK_CELL_MAX = blockCellMax;
    }

    /**
     * 设置crt证书
     *
     * @param certificates
     */
    public static void setCertificates(InputStream... certificates) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            int index = 0;
            for (InputStream certificate : certificates) {
                String certificateAlias = Integer.toString(index++);
                keyStore.setCertificateEntry(certificateAlias, certificateFactory.generateCertificate(certificate));

                try {
                    if (certificate != null) {
                        certificate.close();
                    }
                } catch (IOException e) {
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");

            TrustManagerFactory trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            trustManagerFactory.init(keyStore);
            sslContext.init
                    (
                            null,
                            trustManagerFactory.getTrustManagers(),
                            new SecureRandom()
                    );
            SSL = sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置分段下载
     *
     * @param segmentDownload
     */
    public static void setSegmentDownload(boolean segmentDownload) {
        sSegmentDownload = segmentDownload;
    }
}
