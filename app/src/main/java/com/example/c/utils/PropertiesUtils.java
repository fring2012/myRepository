package com.example.c.utils;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Created by C on 2018/4/3.
 */

public class PropertiesUtils {
    private static Properties properties;
    public static Properties getPropertes(Context context){
        if(properties == null) {
            properties = new Properties();
            try {
                InputStream in = context.getAssets().open("appConfig.properties");
                properties.load(in);
                in.close();
            } catch (IOException e) {

            }
        }
        return properties;
    }

    public static String getString(String key){
        String value = properties.getProperty(key);
        value = propertiesUrl(value);
        return value;
    }
    public static String propertiesUrl(String value){
        if (properties == null)
            throw new RuntimeException( "请先调用getPropertes(Context context)方法初始化properties");
        List<String> list = StringUtil.extractMessageByRegular(value);
        String str;
        for (String s: list) {
            String param = properties.getProperty(s);
            if(param == null)
                throw new RuntimeException(s + "参数不存在");
            value = value.replace("{" + s + "}",param);
        }
        return value;
    }
}
