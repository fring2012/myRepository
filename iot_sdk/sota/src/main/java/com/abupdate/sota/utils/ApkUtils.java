package com.abupdate.sota.utils;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.abupdate.sota.SotaControler;
import com.abupdate.sota.info.remote.ApkInfo;
import com.abupdate.trace.Trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by raise.yang on 17/11/02.
 */

public class ApkUtils {
    private static final String TAG = "ApkUtils";

    /**
     * 获取本地apk信息
     *
     * @return
     */
    public static ApkInfo getLocalApkInfo(String packageName) {
        Trace.d(TAG, "getLocalApkInfo() "+packageName);
        ApkInfo apkInfo = new ApkInfo();
        apkInfo.setPackageName(packageName);
        PackageManager manager = SotaControler.sContext.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Trace.i(TAG, "getLocalApkInfo() NameNotFoundException:%s", packageName);
        }
        if (info != null) {
            apkInfo.setAppName(info.packageName);
            apkInfo.setVersionCode(info.versionCode);
            apkInfo.setVersionName(info.versionName);
        }
        return apkInfo;
    }

    /**
     * 安装apk,静默
     * http://www.trinea.cn/android/android-install-silent/
     *
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int installBySilent(String filePath) {
        if (filePath == null
                || !new File(filePath).exists()) {
            return 1;
        }

        String[] args = {"pm", "install", "-r", filePath};
        ProcessBuilder processBuilder = new ProcessBuilder(args);

        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;

            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Trace.d("ApkUtils", "installBySilent() successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }

    /**
     * 打开App
     *
     * @param packageName 包名
     */
    public static void launchApp(String packageName) {
        if (isSpace(packageName)) return;
        SotaControler.sContext.startActivity(SotaControler.sContext.getPackageManager().getLaunchIntentForPackage(packageName));
    }

    public static Intent getLaunchAppIntent(String packageName) {
        return SotaControler.sContext.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    private static boolean isSpace(String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
