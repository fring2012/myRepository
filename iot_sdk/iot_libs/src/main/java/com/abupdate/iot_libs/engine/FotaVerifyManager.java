package com.abupdate.iot_libs.engine;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;

import com.abupdate.trace.Trace;


/**
 * Created by fighter_lee on 2017/6/21.
 */

public class FotaVerifyManager {
    private static final String TAG = "FotaVerifyManager";

    private static final String[] PERMISSIONS = new String[]{
            "android.permission.INTERNET",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.WAKE_LOCK",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_WIFI_STATE",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.ACCESS_COARSE_LOCATION"
    };

    private static final String[] RECEIVER_ELEMENTS = new String[]{
            "com.abupdate.iot_libs.receiver.UpgradeReceiver"
    };

    private static final String[] SERVICE_ELEMENTS = new String[]{
            "com.abupdate.iot_libs.service.OtaService",
            "com.abupdate.mqtt_libs.mqtt_service.MqttService"
    };
    private static final String[] SERVICE_LOLLIPOP_ELEMENTS = new String[]{
            "com.abupdate.iot_libs.service.OtaService",
            "com.abupdate.mqtt_libs.mqtt_service.MqttService",
            "com.abupdate.iot_libs.service.JobSchedulerService"
    };

    public static boolean verify(Context context) {
        boolean permission = verifyPermissions(context);
        boolean element = verifyElement(context);
        return permission ? element : false;
    }

    private static boolean verifyPermissions(Context context) {

        PackageManager packageManager = context.getPackageManager();
        StringBuilder buffer = new StringBuilder();
        try {
            String[] requestedPermissions = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;

            for (int i = 0; i < PERMISSIONS.length; i++) {
                String needPermission = PERMISSIONS[i];

                boolean b = false;
                if (requestedPermissions != null) {
                    for (String requestedPermission : requestedPermissions) {
                        if (requestedPermission.equals(needPermission)) {
                            b = true;
                        }
                    }
                }
                if (!b) {
                    //当前的权限缺失了
                    if (buffer.length() <= 0) {
                        buffer.append("please add permissions in AndroidManifest:\n");
                    }
                    buffer.append(needPermission)
                            .append("\n");
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (buffer.length() == 0) {
//            Trace.d(TAG, "verifyPermissions() permission verify pass");
            return true;
        } else {
            Trace.e(TAG, buffer.toString());
            return false;
        }
    }

    private static boolean verifyElement(Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean service = verifyServiceElement(packageManager, context);
        boolean receiver = verifyReceiverElement(packageManager, context);
        return service && receiver;
    }

    private static boolean verifyReceiverElement(PackageManager packageManager, Context context) {
        StringBuilder buffer = new StringBuilder();
        try {
            ActivityInfo[] receivers = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_RECEIVERS).receivers;
            for (int i = 0; i < RECEIVER_ELEMENTS.length; i++) {
                String needReceiverElement = RECEIVER_ELEMENTS[i];
                boolean b = false;
                if (receivers != null) {
                    for (int j = 0; j < receivers.length; j++) {
                        ActivityInfo receiver = receivers[j];
                        String receiverName = receiver.name;
                        if (receiverName.equals(needReceiverElement)) {
                            b = true;
                        }
                    }
                }
                if (!b) {
                    if (buffer.length() == 0) {
                        buffer.append("please add Receiver in AndroidManifest:\n");
                    }
                    buffer.append(needReceiverElement)
                            .append("\n");
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (buffer.length() == 0) {
            //receiver 验证通过
            return true;
        } else {
            Trace.e(TAG, buffer.toString());
            return false;
        }
    }

    private static boolean verifyServiceElement(PackageManager packageManager, Context context) {
        StringBuilder buffer = new StringBuilder();
        try {
            ServiceInfo[] services = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SERVICES).services;
            String[] need_services;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                need_services = SERVICE_LOLLIPOP_ELEMENTS;
            } else {
                need_services = SERVICE_ELEMENTS;
            }
            for (int i = 0; i < need_services.length; i++) {
                String needReceiverElement = need_services[i];
                boolean b = false;
                if (services != null) {
                    for (int j = 0; j < services.length; j++) {
                        ServiceInfo service = services[j];
                        String receiverName = service.name;
                        if (receiverName.equals(needReceiverElement)) {
                            b = true;
                        }
                    }
                }
                if (!b) {
                    if (buffer.length() == 0) {
                        buffer.append("please add Service in AndroidManifest:\n");
                    }
                    buffer.append(needReceiverElement)
                            .append("\n");
                }
            }

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (buffer.length() == 0) {
            //service 验证通过
//            Trace.d(TAG, "verifyServiceElement() Service verify pass");
            return true;
        } else {
            Trace.e(TAG, buffer.toString());
            return false;
        }
    }
}
