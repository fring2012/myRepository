package com.abupdate.iot_libs.constant;

/**
 * Created by fighter_lee on 2017/7/21.
 */

public class SDKConfig {

    private static boolean isTest = false;
    private static final String TEST_BASE_URL = "https://iottest.adups.com";
    private static final String OFFICIAL_BASE_URL = "https://iotapi.adups.com";
    private static final String TEST_MQTT_HOST = "iottest.adups.com";
    private static final String OFFICIAL_MQTT_HOST = "iotmqtt.adups.com";
    public static final byte[] KEY = {49,50,51,52,53,54,98};

    /**
     * mqtt port
     */
    public static final int MQTT_TCP_PORT = 1883;
    public static final int MQTT_SSL_PORT = 1884;

    /**
     * http url
     */
    public static String HTTP_BASE_URL;

    /**
     * mqtt host
     */
    public static String MQTT_HOST;

    public static void gen() {
        HTTP_BASE_URL = isTest ? TEST_BASE_URL : OFFICIAL_BASE_URL;
        MQTT_HOST = isTest?TEST_MQTT_HOST:OFFICIAL_MQTT_HOST;
    }

}
