package com.example.c.data;

import android.content.Context;

import com.example.c.utils.PropertiesUtils;

import java.util.Properties;

public class ParamsConfig {
    private static ParamsConfig paramsConfig;

    public String registerDeviceUrl;  //注册设备接口
    public String checkVersionUrl;  //检查更新接口
    public String deviceSecret; //singn key
    public String deviceId;    //设备id
    public String mid;
    public String oem;
    public String models; //型号
    public String platform; //平台
    public String deviceType;
    public String sdkversion; //sdk版本
    public String appversion;
    public String version;  //当前版本号
    public String networkType; //网络类型 （WIFI 4G）
    public String productId;
    public String productSecret = "23dbc31a4ec941f0b546d16deeda1c61";

    private ParamsConfig(){}
    public static ParamsConfig getParamsConfig(){
        if (paramsConfig == null)
            paramsConfig = new ParamsConfig();
        return paramsConfig;
    }
    public void init(Context context){
        Properties properties = PropertiesUtils.getPropertes(context);
        registerDeviceUrl = PropertiesUtils.propertiesUrl(properties.getProperty("registerDeviceUrl"));// https://iotapi.adups.com/register/
        checkVersionUrl = PropertiesUtils.propertiesUrl(properties.getProperty("checkVersionUrl"));
        deviceId = properties.getProperty("deviceId");//252d1e9a82fdd0d64UC;
        mid = properties.getProperty("mid");//e0aee11a;
        oem = properties.getProperty("oem");//mi;
        models = properties.getProperty("models");//HM-Note4X;
        platform = properties.getProperty("platform");//MSM8625;
        deviceType = properties.getProperty("deviceType");//phone;
        sdkversion = properties.getProperty("sdkversion");//1.3.2_pre7;
        appversion = properties.getProperty("appversion");//1.3.2_pre7;
        version = properties.getProperty("version");//6.0.1;
        networkType = properties.getProperty("networkType");//WIFI;
        productId = properties.getProperty("productId");//1522029924;
    }
}
