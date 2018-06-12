package com.abupdate.iot_libs.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import com.abupdate.iot_libs.MqttAgentPolicy;
import com.abupdate.iot_libs.constant.BroadcastConsts;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.engine.LogManager;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.UpgradeParamInfo;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;

import java.io.File;

/**
 * 监听开机广播和网络切换广播<br/>
 * 开机后，检测是否是升级引起，则是写上报数据进数据库<br/>
 * 网络切换后，若能上网，则上报数据库中的上报数据<br/>
 * 若升级成功，会删除升级文件<br/>
 */
public class UpgradeReceiver extends BroadcastReceiver {

    private static final String TAG = "UpgradeReceiver";

    private Context mCtx;

    @Override
    public void onReceive(Context context, Intent intent) {
        mCtx = context;
        String action = intent.getAction();
        Trace.d(TAG, "action: " + action);
        dispatch_action(action);
    }

    private void dispatch_action(String action) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            network_process();
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            boot_process();
        }
    }

    private void boot_process() {
        SPFTool.putBoolean(SPFTool.KEY_SHOULD_REPORT, true);
        String version_name = SPFTool.getString(SPFTool.KEY_VERSION_NAME, "");
        final String deltaId = SPFTool.getString(SPFTool.KEY_DELTAID, "");
        if (!TextUtils.isEmpty(version_name) && !TextUtils.isEmpty(deltaId)) {
            //表明之前升级过
            SPFTool.putString(SPFTool.KEY_VERSION_NAME, "");
            SPFTool.putString(SPFTool.KEY_DELTAID, "");
            Trace.i(TAG, "mobile version:" + DeviceInfo.getInstance().version + " update version " + version_name);
            boolean is_success = version_name.equals(DeviceInfo.getInstance().version);
            // 发送广播通知客户，手机升级成功与否

            Intent intent = new Intent(BroadcastConsts.ACTION_FOTA_UPDATE_RESULT);
            intent.putExtra(BroadcastConsts.KEY_FOTA_UPDATE_RESULT, is_success);
            mCtx.sendBroadcast(intent,BroadcastConsts.PERMISSION_FOTA_UPDATE);
            // 若升级成功，执行一次删除升级包
            if (is_success) {
                Trace.d(TAG, "boot_process() update success!");
                String path = SPFTool.getString(SPFTool.KEY_UPDATE_FILE_PATH, "");
                if (!TextUtils.isEmpty(path)) {
                    new File(path).delete();
                }
            } else {
                Trace.d(TAG, "boot_process() update failed!");
            }
            //存储升级上报数据
            String mid = TextUtils.isEmpty(DeviceInfo.getInstance().mid) ? SPFTool.getString(DeviceInfo.KEY_MID_BACK, "abupdate-MID-ERROR-COLLECT") : DeviceInfo.getInstance().mid;
            UpgradeParamInfo info = new UpgradeParamInfo(mid,
                    deltaId,
                    String.valueOf(is_success ? 1 : 99)
            );
            ReportManager.getInstance(mCtx).saveReportData(info);

            //存储错误日志文件上报数据
            if (!is_success) {
                LogManager.getInstance().saveRecoveryLog(deltaId);
            }
            if (isNetWorkAvailable(mCtx)) {
                Trace.d(TAG, "boot_process() boot complete upgrade report");
                report();
            }
        }
    }

    private void network_process() {
        //5.0以上使用jobScheduler的方式，5.0以下使用网络切换广播
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (isNetWorkAvailable(mCtx)) {
                Trace.d(TAG, "network_process() should_report = " + SPFTool.getBoolean(SPFTool.KEY_SHOULD_REPORT, false));
                //上报数据
                if (SPFTool.getBoolean(SPFTool.KEY_SHOULD_REPORT, false)) {
                    report();
                }

                //定期与服务器通讯一次
                if (System.currentTimeMillis() - SPFTool.getLong(OtaConstants.SPF_STATIC_CHECK_VERSION_CYCLE, -1) >= OtaConstants.STATIC_CHECK_VERSION_CYCLE) {
                    //check version
                    OtaService.startByAction(OtaService.ACTION_STATIC_CHECK_VERSION);
                    SPFTool.putLong(OtaConstants.SPF_STATIC_CHECK_VERSION_CYCLE, System.currentTimeMillis());
                }

                //mqtt 周期连接检测
                if (SPFTool.getBoolean(MqttAgentPolicy.CONFIG_MQTT_CONNECT, false)) {
                    if (!MqttAgentPolicy.isConnected()) {
                        MqttAgentPolicy.connect();
                    }
                }
            }

        }
    }

    //检测是否有未上报的数据
    private void report() {
        if (ReportManager.getInstance(mCtx).queryReport() == 0) {
            SPFTool.putBoolean(SPFTool.KEY_SHOULD_REPORT, false);//没有需要上报的数据，置为false
            Trace.d(TAG, "report() do not have data to be reported!");
            return;
        }
        OtaService.startByAction(OtaService.ACTION_REPORT);
    }

    private boolean isNetWorkAvailable(Context context) {

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
            Trace.d(TAG, "==Util:isNetWorkAvailable Exception");
        }
        return ret;
    }
}
