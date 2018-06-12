package com.abupdate.iot_libs.constant;

/**
 * Created by fighter_lee on 2017/5/11.
 */

public class OtaConstants {
    public static final int MQTT_LOGIN = 1000;
    public static final int MQTT_LOGOUT = 1001;
    public static final int MQTT_REPORT_DEVICE = 1002;

    public static final int DOWNLOAD_CALLBACK_PREPARE = 1;
    public static final int DOWNLOAD_CALLBACK_PROGRESS = 2;
    public static final int DOWNLOAD_CALLBACK_CANCEL = 3;
    public static final int DOWNLOAD_CALLBACK_SUCCESS = 4;
    public static final int DOWNLOAD_CALLBACK_FAILED = 5;

    public static final String SPF_STATIC_CHECK_VERSION_CYCLE = "spf_static_check_version_cycle";
    public static final String SPF_STATIC_MQTT_CHECK = "spf_static_mqtt_check";

    public static final String DOUBLE_LINE = "==========================";
    public static final String SINGLE_LINE = "--------------------------";

    public static final long STATIC_CHECK_VERSION_CYCLE = 3 * 24 * 60 * 60 * 1000;//三天
    public static final long STATIC_OTA_CYCLE_TASK = 1 * 24 * 60 * 60 * 1000;//一天
//        public static final long STATIC_CHECK_VERSION_CYCLE = 60 * 60 * 1000;//测试一小时

    public static final String KEY_CHECK_CYCLE = "check_cycle";
    public static final String KEY_REMIND_CYCLE = "check_remind";
    public static final String KEY_DOWNLOAD_WIFI = "download_wifi";
    public static final String KEY_DOWNLOAD_STORAGE_SIZE = "download_storageSize";
    public static final String KEY_DOWNLOAD_STORAGE_PATH = "download_storagePath";
    public static final String KEY_DOWNLOAD_FORCE = "download_forceDownload";
    public static final String KEY_INSTALL_BATTERY = "install_battery";
    public static final String KEY_INSTALL_FORCE = "install_force";
    public static final String KEY_REBOOT_UPDATE_FORCE = "install_rebootUpgrade";
    public static final String KEY_INSTALL_FREE_TIME = "install_freeInstall";


    public enum PolicyType{

        TYPE_DOWNLOAD_REQUEST_WIFI(KEY_DOWNLOAD_WIFI),
        TYPE_DOWNLOAD_FORCE(KEY_DOWNLOAD_FORCE),
        TYPE_DOWNLOAD_STORAGE_SIZE(KEY_DOWNLOAD_STORAGE_SIZE),
        TYPE_INSTALL_BATTERY(KEY_INSTALL_BATTERY),
        TYPE_INSTALL_FORCE(KEY_INSTALL_FORCE),
        TYPE_INSTALL_REBOOT_FORCE(KEY_REBOOT_UPDATE_FORCE),
        TYPE_INSTALL_FREE_TIME(KEY_INSTALL_FREE_TIME);

        String type;

        PolicyType(String type){
            this.type = type;
        }

        public String getType(){
            return type;
        }
    }

    public enum IntervalTimePolicy {
        type_install_force(KEY_INSTALL_FORCE),
        type_install_free_time(KEY_INSTALL_FREE_TIME);

        private String type;

        IntervalTimePolicy(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}
