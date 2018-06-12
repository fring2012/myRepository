package com.abupdate.iot_libs.constant;


/**
 * 检测，下载，升级接口的回调错误码和说明
 */
public class Error {

    //**************************公共部分**************************
    /**
     * productId 不合法
     */
    public static final int PRODUCTID_IS_INVALID = 1001;

    /**
     * 项目不存在
     */
    public static final int PROJECT_NOT_EXIST = 1002;

    /**
     * 参数不合法
     */
    public static final int PARAM_IS_INVALID = 1003;

    /**
     * 参数缺失
     */
    public static final int PARAM_IS_LACK = 1004;

    /**
     * 系统异常
     */
    public static final int SERVICE_SYSTEM_ERROR = 1005;

    /**
     * JSON解析异常
     */
    public static final int JSON_PARSING_EXCEPTION = 1006;

    /**
     * 参数不合法
     */
    public static final int PARAM_IS_NOT_CONFORM_TO_RULE = 1007;

    /**
     * 获取项目信息失败
     */
    public static final int OBTAIN_PRODUCT_FAILED = 1009;

    /**
     * 服务器返回数据异常
     */
    public static final int SERVER_DATA_ERROR = 3003;

    /**
     * 设备参数初始化失败<br/>
     */
    public static final int DEVICE_INFO_INIT_FAILED = 3001;


    //**************************注册接口**************************
    /**
     * sign错误do
     */
    public static final int REGISTER_SIGN_ERROR = 2001;

    /**
     * 没有注册
     */
    public static final int WITHOUT_REGISTER_ERROR = 2003;

    public static final int RESPONSE_ERROR = 2002;


    //**************************检测接口**************************

    /**
     * 当前为最新版本
     */
    public static final int CHECK_CURRENT_IS_LAST_VERSION = 2101;

    /**
     * 没有配置相应的版本号
     */
    public static final int CHECK_VERSION_NOT_CONFIGURE = 2102;
    /**
     * 设备未注册
     */
    public static final int CHECK_DEVICE_IS_NOT_REGISTER = 2103;

    //**************************下载接口**************************
    /**
     * 未定义错误 8003
     */
    public static final int ERROR = 8003;
    /**
     * 下载文件期间，网络访问异常
     */
    public static final int DOWNLOADING_NET_EXCEPTION = -2;
    /**
     * 文件md5校验失败
     */
    public static final int DOWNLOADING_INVALID_FILE = -1;

    /**
     * 内存不足,没有权限
     */
    public static final int ERROR_FILE_IO_EXCEPTION = -3;

    /**
     * 请求文件大小失败(在没有设置文件大小并且从网络中获取失败)
     */
    public static final int ERROR_FETCH_FILE_SIZE = -4;

    /**
     * 块校验错误
     */
    public static final int ERROR_BLOCK_VERIFY_FAIL = -5;

    /**
     * 配置了wifi网络下载，但不在wifi下
     */
    public static final int DOWNLOADING_NOT_WIFI = 8009;

    /**
     * 配置了最小内存空间，但空间不足
     */
    public static final int DOWNLOADING_MEMORY_NOT_ENOUGH = 8010;

    /**
     * 下载上报状态值非法
     */
    public static final int DOWNLOAD_STATUS_ILLEGAL = 2201;

    /**
     * 下载上报deltaId不存在
     */
    public static final int DOWNLOAD_DELTAID_NOT_EXIST = 2202;

    //**************************升级接口**************************
    /**
     * 升级文件不存在
     */
    public static final int UPGRADE_FILE_NOT_EXIST = 7002;

    /**
     * 配置了最小电量并且手机电量小于配置的最小电量
     */
    public static final int UPGRADE_BATTERY_NOT_ENOUGH = 7003;

    /**
     * 升级文件不存在或者文件长度为0
     */
    public static final int UPGRADE_IOEXCEPTION = 7004;

    /**
     * 升级文件校验失败
     */
    public static final int UPGRADE_VALIDATE_FILE_FAIL = 7005;

    /**
     * 自定义升级条件不满足
     */
    public static final int UPGRADE_CONDITIONS_IS_NOT_SATISFIED = 7006;

    /**
     * 升级上报时deltaId 不存在
     */
    public static final int UPGRADE_DELTA_NOT_EXIST = 2301;

    /**
     * 升级上报状态值非法
     */
    public static final int UPGRADE_STATUS_ILLEGAL = 2302;


    /**
     * 解析错误信息
     *
     * @param code
     * @return
     */
    public static String getErrorMessage(int code) {
        switch (code) {
            case 0:
                return "client exception";
            case 1:
                return "Invalid protocol version";
            case 2:
                return "Invalid client ID";
            case 3:
                return "Broker unavailable";
            case 4:
                return "Bad user name or password";
            case 5:
                return "Not authorized to connect";
            case 6:
                return "Unexpected error";
            case 32000:
                return "Timed out waiting for a response from the server";
            case 32001:
                return "Internal error, caused by no new message IDs being available";
            case 32002:
                return "Timed out while waiting to write messages to the server";
            case 32100:
                return "Client is connected";
            case 32101:
                return "Client is disconnected";
            case 32102:
                return "Client is currently disconnecting";
            case 32103:
                return "Unable to connect to server";
            case 32104:
                return "Client is not connected";
            case 32105:
                return "The specified SocketFactory type does not match the broker URI";
            case 32106:
                return "SSL configuration error";
            case 32107:
                return "Disconnecting is not allowed from a callback method";
            case 32108:
                return "Unrecognized packet";
            case 32109:
                return "Connection lost";
            case 32110:
                return "Connect already in progress";
            case 32111:
                return "Client is closed";
            case 32200:
                return "Persistence already in use";
            case 32201:
                return "Token already in use";
            case 32202:
                return "Too many publishes in progress";

            case PRODUCTID_IS_INVALID:
                return "product id is invalid";
            case PROJECT_NOT_EXIST:
                return "project not exist";
            case PARAM_IS_INVALID:
                return "param is invalid";
            case PARAM_IS_LACK:
                return "param is lack";
            case SERVICE_SYSTEM_ERROR:
                return "service system error";
            case JSON_PARSING_EXCEPTION:
                return "json parse exception";
            case OBTAIN_PRODUCT_FAILED:
                return "obtain product info failed";
            case SERVER_DATA_ERROR:
                return "network error or server data error";
            case REGISTER_SIGN_ERROR:
                return "register sign error";
            case CHECK_VERSION_NOT_CONFIGURE:
                return "version not configure";
            case CHECK_DEVICE_IS_NOT_REGISTER:
                return "device is not register";
            case PARAM_IS_NOT_CONFORM_TO_RULE:
                return "param is not conform to rule";
            case ERROR:
                return "unknown error";
            case ERROR_FILE_IO_EXCEPTION:
                return "memory is not enough or permission denied";
            case ERROR_FETCH_FILE_SIZE:
                return "fetch download length fail";
            case DOWNLOADING_NET_EXCEPTION:
                return "download net exception";
            case DOWNLOADING_INVALID_FILE:
                return "download finished. but the file is invalid.";
            case DOWNLOADING_MEMORY_NOT_ENOUGH:
                return "download memory is not enough!";
            case DOWNLOADING_NOT_WIFI:
                return "download is request wifi!";
            case ERROR_BLOCK_VERIFY_FAIL:
                return "download block file verify fail";
            case DOWNLOAD_STATUS_ILLEGAL:
                return "report download status illegal";
            case DOWNLOAD_DELTAID_NOT_EXIST:
                return "report download deltaId not exist";
            case CHECK_CURRENT_IS_LAST_VERSION:
                return "current version is the last version!";
            case DEVICE_INFO_INIT_FAILED:
                return "device info is not completed!";
            case UPGRADE_BATTERY_NOT_ENOUGH:
                return "update battery is not enough!";
            case UPGRADE_FILE_NOT_EXIST:
                return "update file is not exist!";
            case UPGRADE_VALIDATE_FILE_FAIL:
                return "update verify file failed";
            case UPGRADE_IOEXCEPTION:
                return "file error or Permission denied ";
            case WITHOUT_REGISTER_ERROR:
                return "without register!";
            case UPGRADE_DELTA_NOT_EXIST:
                return "report update delta not exist";
            case UPGRADE_STATUS_ILLEGAL:
                return "report upgrade status illegal";
            default:
                return "UNKNOWN ERROR";
        }
    }

}
