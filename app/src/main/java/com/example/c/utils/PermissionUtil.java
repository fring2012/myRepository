package com.example.c.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.example.c.ui.activity.activity.VersionManagerActivity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

public class PermissionUtil {
    public static String[] PERMISSION = {Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE};


    public static boolean isLacksOfPermission(Context context,String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断版本是否在6.0之上
            //ActivityCompat.shouldShowRequestPermissionRationale如果用户已经选择了不授予权限，这个方法将放回true
            if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,permission)){
                return  false;
            }
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
        }
        return false;
    }





}
