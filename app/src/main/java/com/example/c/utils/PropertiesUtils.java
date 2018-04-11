package com.example.c.utils;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

}
