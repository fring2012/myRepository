package com.example.c.utils;

import android.Manifest;
import android.os.Build;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {
    public static String[] PERMISSION = {Manifest.permission.READ_PHONE_STATE};

    public static boolean isLacksOfPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(
                    PinkeApplication.getInstance().getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED;
        }
        return false;
    }

}
