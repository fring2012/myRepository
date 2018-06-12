package com.abupdate.mqtt_libs.connect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.abupdate.trace.Trace;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by fighter_lee on 2017/9/28.
 */

public class Utils {
    private static final String TAG = "Network is avaliable";
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final String IP = "119.29.29.29";

    public static boolean isNetWorkAvailable(Context context) {

        boolean ret = false;
        if (context == null) {
            return ret;
        }
        try {
            ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectManager == null) {
                return ret;
            }
            NetworkInfo[] infos = connectManager.getAllNetworkInfo();
            if (infos == null) {
                return ret;
            }
            for (int i = 0; i < infos.length && infos[i] != null; i++) {
                if (infos[i].isConnected() && infos[i].isAvailable()) {
                    ret = true;
                    break;
                }
            }
        } catch (Exception e) {

        }
        Trace.d(TAG, "old Network Avaliable (): " + ret);
        return ret;
    }

    public static boolean isAvaliableByPing() {
        boolean result = false;
        try {
            Process process = Runtime.getRuntime().exec("sh");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            os.write(String.format("ping -c 1 %s", IP).getBytes());
            os.writeBytes(LINE_SEP);
            os.flush();
            os.writeBytes("exit" + LINE_SEP);
            os.flush();
            result = process.waitFor() == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Trace.d(TAG, "new Network Avaliable (): " + result);
        return result;
    }

}
