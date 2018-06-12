package com.abupdate.iot_libs.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.RecoverySystem;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.abupdate.iot_download_libs.DLManager;
import com.abupdate.iot_download_libs.DownError;
import com.abupdate.iot_libs.MqttAgentPolicy;
import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.Error;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.engine.IOTCallbackManager;
import com.abupdate.iot_libs.engine.OTAExecuteManager;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.RegisterInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.IDownSimpleListener;
import com.abupdate.iot_libs.inter.ILogoutCallback;
import com.abupdate.iot_libs.inter.OtaListener;
import com.abupdate.iot_libs.interact.OtaTools;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.utils.FileUtil;
import com.abupdate.iot_libs.utils.JsonAnalyticsUtil;
import com.abupdate.iot_libs.utils.NetUtils;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.mqtt_libs.connect.MqttManager;
import com.abupdate.mqtt_libs.mqttv3.MqttException;
import com.abupdate.trace.Trace;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Created by fighter_lee on 2017/5/15.
 */

public class OtaService extends IntentService {

    public static final String TAG = "OtaService";
    public static final String ACTION_REGISTER = "action_register";
    public static final String ACTION_CHECK_VERSION = "action_check_version";
    public static final String ACTION_CONNECT = "action_connect";
    public static final String ACTION_DISCONNECT = "action_disconnect";
    public static final String ACTION_DOWNLOAD = "action_download";
    public static final String ACTION_UPDATE = "action_update";
    public static final String ACTION_REPORT = "action_report";
    public static final String ACTION_STATIC_CHECK_VERSION = "action_static_check_version";
    private static final String KEY_ACTION = "key_action";
    private static final String KEY_EXTRA_INFO = "key_extra_info";
    private static Context sCx;
    private Handler handler;
    private static long mStartTime;
    private static String ip = "";

    public OtaService() {
        super("OtaService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        if (null == IOTCallbackManager.getInstance().handler) {
            IOTCallbackManager.getInstance().setCallbackOnUIThread(handler);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getStringExtra(KEY_ACTION);
        switch (action) {
            case ACTION_REGISTER:
                register_task();
                break;

            case ACTION_CHECK_VERSION:
                check_version_task();
                break;

            case ACTION_STATIC_CHECK_VERSION:
                static_check_version_task();
                break;

            case ACTION_CONNECT:
                connectMqtt();
                break;

            case ACTION_DISCONNECT:
                disConnectMqtt();
                break;

            case ACTION_DOWNLOAD:
                download_task();
                break;

            case ACTION_UPDATE:
                String local_reboot_path = intent.getStringExtra(KEY_EXTRA_INFO);
                if (null != local_reboot_path) {
                    //用户传入路径，本地升级，不会进行md5校验
                    rebootLocalUpgrade(local_reboot_path);
                } else {
                    //使用初始化的路径，并在升级前会进行md5校验
                    upgrade();
                }
                break;

            case ACTION_REPORT:
                task_report();
                break;
        }
    }

    private void static_check_version_task() {
        try {
            OTAExecuteManager.getInstance().checkVersion();
        } catch (FotaException e) {
            e.printStackTrace();
        }
    }

    private void task_report() {
        //上报
        if (!DeviceInfo.getInstance().isValid()) {
            return;
        }
        ReportManager.getInstance(sCx).report();
    }

    private void upgrade() {
        Trace.d(TAG, "upgrade() start.");
        String updatePath = OtaAgentPolicy.config.updatePath;
        Trace.d(TAG, "rebootUpgrade() path:" + updatePath);

        //进行MD5校验
        if (!FileUtil.validateFile(updatePath, VersionInfo.getInstance().md5sum)) {
            Trace.e(TAG, "onUpdateFail() . update validate file fail");
            IOTCallbackManager.getInstance().onUpdateFailed(Error.UPGRADE_VALIDATE_FILE_FAIL);
            ReportManager.getInstance(sCx).reportUpdateParamInfo(Error.UPGRADE_VALIDATE_FILE_FAIL);
            return;
        }

        startUpdate(new File(updatePath));
    }

    /**
     * 本地升级
     */
    public void rebootLocalUpgrade(String updatePath) {
        //        String updatePath = DLService.mPath;
        Trace.d(TAG, "rebootLocalUpgrade() path:" + updatePath);

        if (TextUtils.isEmpty(updatePath)) {
            Trace.e(TAG, "rebootLocalUpgrade() path is null");
            IOTCallbackManager.getInstance().onUpdateFailed(Error.UPGRADE_FILE_NOT_EXIST);
            ReportManager.getInstance(sCx).reportUpdateParamInfo(Error.UPGRADE_FILE_NOT_EXIST);
            return;
        }
        startUpdate(new File(updatePath));
    }

    private void startUpdate(File file) {
        //回调rebootConditionPrepare()方法判断
        if (!IOTCallbackManager.getIRebootUpgradeCallBack().rebootConditionPrepare()) {
            Trace.d(TAG, "startUpdate() update conditions does not meet");
            IOTCallbackManager.getIRebootUpgradeCallBack().onError(Error.UPGRADE_CONDITIONS_IS_NOT_SATISFIED);
            return;
        }

        //本地升级不存储记录
        if (file.getAbsoluteFile() != null &&
                OtaAgentPolicy.getVersionInfo().versionName != null &&
                OtaAgentPolicy.getVersionInfo().deltaID != null) {

            SPFTool.putString(SPFTool.KEY_UPDATE_FILE_PATH, file.getAbsolutePath());
            //存储升级标识
            SPFTool.putString(SPFTool.KEY_VERSION_NAME, OtaAgentPolicy.getVersionInfo().versionName);
            SPFTool.putString(SPFTool.KEY_DELTAID, OtaAgentPolicy.getVersionInfo().deltaID);
            SPFTool.putLong(SPFTool.KEY_LAST_RECOVERY_TIME, System.currentTimeMillis());
            Trace.d(TAG, "rebootUpgrade() version_name = " + SPFTool.getString(SPFTool.KEY_VERSION_NAME, "null") + ",deltaId:" + SPFTool.getString(SPFTool.KEY_DELTAID, "null"));
        }

        try {
            RecoverySystem.installPackage(sCx, file);
        } catch (IOException e) {
            Trace.e(TAG, "onUpdateFail() .", e);
            IOTCallbackManager.getInstance().onUpdateFailed(Error.UPGRADE_IOEXCEPTION);
            ReportManager.getInstance(sCx).reportUpdateParamInfo(Error.UPGRADE_IOEXCEPTION);
        }
    }

    private void download_task() {
        //下载前上报
        mStartTime = Utils.getSecondTime();

        //文件存在,且已经下载完成（文件校验成功）
        if (new File(OtaAgentPolicy.config.updatePath).exists() && FileUtil.validateFile(OtaAgentPolicy.config.updatePath, VersionInfo.getInstance().md5sum)) {
            ReportManager.getInstance(OtaAgentPolicy.sCx).reportDownParamInfo(0, mStartTime, "");
            IOTCallbackManager.getInstance().downloadCallback(OtaConstants.DOWNLOAD_CALLBACK_SUCCESS, 0, 0, 0);
            return;
        }

        DLManager.getInstance().add(OTAExecuteManager.getInstance().downloadPrepare());

        DLManager.getInstance().execAsync(new IDownSimpleListener() {
            @Override
            public void on_start() {
                IOTCallbackManager.getInstance().downloadCallback(OtaConstants.DOWNLOAD_CALLBACK_PREPARE, 0, 0, 0);
                getIp();
            }

            @Override
            public void onCompleted(File file) {
                ReportManager.getInstance(sCx).reportDownParamInfo(DownError.NO_ERROR, mStartTime, ip);
                IOTCallbackManager.getInstance().downloadCallback(OtaConstants.DOWNLOAD_CALLBACK_SUCCESS, 0, 0, 0);
            }

            @Override
            public void onDownloadProgress(long downSize, long totalSize, int progress) {
                IOTCallbackManager.getInstance().downloadCallback(OtaConstants.DOWNLOAD_CALLBACK_PROGRESS, downSize, totalSize, 0);
            }

            @Override
            public void onFailed(int error) {
                Trace.d(TAG, "onFailed() " + error);
                IOTCallbackManager.getInstance().downloadCallback(OtaConstants.DOWNLOAD_CALLBACK_FAILED, 0, 0, error);
                ReportManager.getInstance(sCx).reportDownParamInfo(error, mStartTime, ip);
            }

            @Override
            public void onCancel() {
                IOTCallbackManager.getInstance().downloadCallback(OtaConstants.DOWNLOAD_CALLBACK_CANCEL, 0, 0, 0);
            }
        });
    }

    private void check_version_task() {
        int checkVersionResult = OTAExecuteManager.getInstance().checkVersionExecute();

        if (JsonAnalyticsUtil.isSuccess(checkVersionResult)) {
            IOTCallbackManager.getInstance().onCheckVersionSuccess();
        } else {
            IOTCallbackManager.getInstance().onCheckVersionFailed(checkVersionResult);
        }
    }

    public static void selfDisconnect() {
        if (MqttManager.getInstance().isKeepConnect()) {
            MqttManager.getInstance().stopKeepConnect();
            OtaService.startByAction(OtaService.ACTION_DISCONNECT);
            return;
        }
        if (!MqttManager.getInstance().isConneect()) {
            Trace.d("MqttAgentPolicy", "disConnect() is disconnected");
            OtaListener.getInstance().disconnect(new MqttException(new Throwable("is disconnected")));
            return;
        }
        if (OtaTools.getInstance().getState() == OtaTools.State.Disconnecting) {
            Trace.d("MqttAgentPolicy", "disConnect() is disconnecting");
            OtaListener.getInstance().disconnect(new MqttException(new Throwable("is disconnecting")));
            return;
        }
        OtaService.startByAction(OtaService.ACTION_DISCONNECT);
    }

    private void disConnectMqtt() {
        if (MqttAgentPolicy.isConnected()) {
            //如果是登录状态，需要登出
            if (OtaTools.getInstance().getState() == OtaTools.State.Login) {
                OtaTools.getInstance().setState(OtaTools.State.Disconnecting);
                OtaTools.getInstance().logout(true, new ILogoutCallback() {
                    @Override
                    public void onLogoutSuccess() {
                        OtaTools.getInstance().disConnect(OtaListener.getInstance().setAction(OtaListener.Action.DISCONNECT));
                    }

                    @Override
                    public void onLogoutFail(int error) {
                        OtaTools.getInstance().disConnect(OtaListener.getInstance().setAction(OtaListener.Action.DISCONNECT));
                    }

                    @Override
                    public void onLogoutTimeout() {
                        OtaTools.getInstance().disConnect(OtaListener.getInstance().setAction(OtaListener.Action.DISCONNECT));
                    }
                });
            } else {
                OtaTools.getInstance().setState(OtaTools.State.Disconnecting);
                OtaTools.getInstance().disConnect(OtaListener.getInstance().setAction(OtaListener.Action.DISCONNECT));
            }
        } else if (MqttManager.getInstance().isKeepConnect()) {
            OtaTools.getInstance().disConnect(OtaListener.getInstance().setAction(OtaListener.Action.DISCONNECT));
        }
    }

    private void connectMqtt() {
        if (TextUtils.isEmpty(RegisterInfo.getInstance().deviceSecret) ||
                TextUtils.isEmpty(RegisterInfo.getInstance().deviceId)) {
            int registerCode = OTAExecuteManager.getInstance().registerExecute();
            if (JsonAnalyticsUtil.SUCCESS != registerCode
                    || !RegisterInfo.getInstance().isValid()) {
                Trace.e(TAG, "connectMqtt() failed");
                return;
            }
        }
        OtaTools.getInstance().setState(OtaTools.State.Connecting);
        OtaTools.getInstance().connect();
    }

    public static void startByAction(String action) {
        startByAction(action, null);
    }

    public static void startByAction(String action, @Nullable Object extraInfo) {
        synchronized (OtaService.class) {
            if (TextUtils.isEmpty(action)) {
                Trace.e(TAG, "startByAction() action is null");
                return;
            }

            if (null == sCx) {
                Trace.e(TAG, "startByAction() context is null,should call initContext();");
                return;
            }

            Intent intent = new Intent();
            intent.putExtra(KEY_ACTION, action);
            intent.setClass(sCx, OtaService.class);
            if (extraInfo != null) {
                if (extraInfo instanceof Boolean) {
                    intent.putExtra(KEY_EXTRA_INFO, (Boolean) extraInfo);
                }

                if (extraInfo instanceof Integer) {
                    intent.putExtra(KEY_EXTRA_INFO, (Integer) extraInfo);
                }

                if (extraInfo instanceof String) {
                    intent.putExtra(KEY_EXTRA_INFO, (String) extraInfo);
                }
            }

            sCx.startService(intent);
        }
    }

    private boolean register_task() {
        if (null == sCx) {
            Trace.e(TAG, "register_task() context is null,please call method initContext() in application!");
            return false;
        }
        int registerResult = OTAExecuteManager.getInstance().registerExecute();

        if (JsonAnalyticsUtil.isSuccess(registerResult)) {
            IOTCallbackManager.getInstance().onRegisterSuccess();
            return true;
        } else {
            IOTCallbackManager.getInstance().onCheckVersionFailed(registerResult);
            return false;
        }
    }

    public static void initContext(Context context) {
        sCx = context;
    }

    public static void setDownloadCancel() {
        DLManager.getInstance().cancel_all();
    }

    private void getIp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InetAddress address = null;
                try {
                    address = InetAddress.getByName(NetUtils.getHost(VersionInfo.getInstance().deltaUrl));
                    ip = address.getHostAddress();
                    Trace.d(TAG, "download_task() download IP:" + ip);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
