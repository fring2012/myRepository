package com.abupdate.iot_libs.report;

import android.content.Context;
import android.text.TextUtils;

import com.abupdate.iot_download_libs.DownError;
import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.Error;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.DownParamInfo;
import com.abupdate.iot_libs.info.ErrorFileParamInfo;
import com.abupdate.iot_libs.info.PushMessageInfo;
import com.abupdate.iot_libs.info.UpgradeParamInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.IReportResultCallback;
import com.abupdate.iot_libs.interact.HttpTools;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.JsonAnalyticsUtil;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.trace.Trace;

import java.io.File;
import java.util.List;

/**
 * 在非UI线程中调用
 * Created by raise.yang on 2016/07/21.
 */
public class ReportManager {

    private static String TAG = "ReportManager";

    private static ReportManager m_instance;
    private final Context m_context;
    private final ReportDBManager m_dbManager;


    private ReportManager(Context context) {
        m_context = context.getApplicationContext();
        m_dbManager = new ReportDBManager(m_context);
    }

    public void reportDownParamInfo(int code, long startTime, String ip) {
        int downCode;
        switch (code) {
            case DownError.NO_ERROR:
                downCode = 1;
                break;
            case DownError.ERROR_MD5_VERIFY_FAILED:
                downCode = 8;
                break;
            case DownError.ERROR_FETCH_FILE_SIZE:
            case DownError.ERROR_NET_WORK:
                downCode = 7;
                break;
            default:
                downCode = 99;
        }
        int length;
        try {
            length = (int) (new File(OtaAgentPolicy.config.updatePath).length());
        } catch (Exception e) {
            length = 0;
        }
        DownParamInfo downParamInfo = new DownParamInfo(OtaAgentPolicy.getVersionInfo().deltaID,
                String.valueOf(downCode),
                startTime,
                Utils.getSecondTime(),
                length,
                ip);
        saveReportData(downParamInfo);
        OtaService.startByAction(OtaService.ACTION_REPORT);
    }

    public void reportUpdateParamInfo(int error) {
        if (null == VersionInfo.getInstance().deltaID) {
            return;
        }
        int error_status = 99;
        switch (error) {
            case Error.UPGRADE_BATTERY_NOT_ENOUGH:
                error_status = 2;
                break;
            case Error.UPGRADE_IOEXCEPTION:
                error_status = 3;
                break;
            case Error.UPGRADE_FILE_NOT_EXIST:
                error_status = 4;
                break;
            case Error.UPGRADE_VALIDATE_FILE_FAIL:
                error_status = 5;
                break;
            default:
                error_status = 99;
        }

        UpgradeParamInfo info = new UpgradeParamInfo(DeviceInfo.getInstance().mid,
                VersionInfo.getInstance().deltaID,
                String.valueOf(error_status)
        );
        saveReportData(info);
        OtaService.startByAction(OtaService.ACTION_REPORT);
    }

    public static ReportManager getInstance(Context context) {
        if (m_instance == null) {
            synchronized (ReportManager.class) {
                if (m_instance == null) {
                    m_instance = new ReportManager(context);
                }
            }
        }
        return m_instance;
    }

    /**
     * 开启上报服务
     *
     * @return 返回当前数据库中日志个数
     */
    public int queryReport() {
        List<UpgradeParamInfo> upgradeParamInfos = m_dbManager.query_upgrade();
        List<DownParamInfo> downInfos = m_dbManager.query_down();
        List<PushMessageInfo> pushMessageInfos = m_dbManager.query_push_data();
        List<ErrorFileParamInfo> errorFileParamInfos = m_dbManager.query_error_log_data();
        return upgradeParamInfos.size() + downInfos.size() + pushMessageInfos.size() + errorFileParamInfos.size();
    }

    public ReportDBManager getDB() {
        return m_dbManager;
    }

    /**
     * 保存上报数据
     *
     * @param reportData 下载上报 或者 升级上报
     */
    public void saveReportData(Object reportData) {
        if (reportData instanceof UpgradeParamInfo) {
            m_dbManager.add((UpgradeParamInfo) reportData);
        } else if (reportData instanceof DownParamInfo) {
            m_dbManager.add((DownParamInfo) reportData);
        }
    }

    public void savePushResponseData(PushMessageInfo info) {
        m_dbManager.addPushData(info);
    }

    public void saveErrorFileData(ErrorFileParamInfo info) {
        m_dbManager.addErrorFileData(info);
    }

    /**
     * 查询数据库上报
     */
    public void report() {
        List<DownParamInfo> downInfos = m_dbManager.query_down();
        int size = downInfos.size();
        if (size > 0) {
            Trace.d(TAG, "check the local report download: " + size);
        }
        for (final DownParamInfo info : downInfos) {
            reportDown(info, new IReportResultCallback() {
                @Override
                public void onReportSuccess() {
                    Trace.d(TAG, "onReportSuccess() report down.");
                    m_dbManager.delete(info);
                }

                @Override
                public void onReportFail() {
                    Trace.d(TAG, "onReportFail() report down.");
                    m_dbManager.delete(info);
                }

                @Override
                public void onReportNetFail() {
                    Trace.d(TAG, "onReportNetFail() report down.");
                }
            });
        }

        List<UpgradeParamInfo> upgradeInfos = m_dbManager.query_upgrade();
        size = upgradeInfos.size();
        if (size > 0) {
            Trace.d(TAG, "check the local report upgrade: " + size);
        }
        for (final UpgradeParamInfo info : upgradeInfos) {
            reportUpgrade(info, new IReportResultCallback() {
                @Override
                public void onReportSuccess() {
                    Trace.d(TAG, "onReportSuccess() upgrade");
                    m_dbManager.delete(info);
                }

                @Override
                public void onReportFail() {
                    Trace.d(TAG, "onReportFail() upgrade.");
                    m_dbManager.delete(info);
                }

                @Override
                public void onReportNetFail() {
                    Trace.d(TAG, "onReportNetFail() upgrade.");
                }
            });
        }

        final List<PushMessageInfo> pushMessageInfos = m_dbManager.query_push_data();
        size = pushMessageInfos.size();
        if (size > 0) {
            Trace.d(TAG, "check push message data:" + size);
        }
        for (final PushMessageInfo pushMessageInfo : pushMessageInfos) {
            reportPushData(pushMessageInfo, new IReportResultCallback() {
                @Override
                public void onReportSuccess() {
                    Trace.d(TAG, "onReportSuccess() push");
                    m_dbManager.delete(pushMessageInfo);
                }

                @Override
                public void onReportFail() {
                    Trace.d(TAG, "onReportFail() push");
                    m_dbManager.delete(pushMessageInfo);
                }

                @Override
                public void onReportNetFail() {
                    Trace.d(TAG, "onReportNetFail() push");
                }
            });
        }

        List<ErrorFileParamInfo> errorFileParamInfos = m_dbManager.query_error_log_data();
        size = errorFileParamInfos.size();
        if (size > 0) {
            Trace.d(TAG, "check error log report data:" + size);
            for (final ErrorFileParamInfo errorFileParamInfo : errorFileParamInfos) {
                reportErrorLog(errorFileParamInfo, new IReportResultCallback() {

                    @Override
                    public void onReportSuccess() {
                        Trace.d(TAG, "onReportSuccess() error log");
                        m_dbManager.delete(errorFileParamInfo);
                        new File(errorFileParamInfo.uploadFile).delete();
                    }

                    @Override
                    public void onReportFail() {
                        Trace.d(TAG, "onReportFail() error log");
                        m_dbManager.delete(errorFileParamInfo);
                        new File(errorFileParamInfo.uploadFile).delete();
                    }

                    @Override
                    public void onReportNetFail() {
                        Trace.d(TAG, "onReportNetFail() error log");
                        if (TextUtils.isEmpty(errorFileParamInfo.deltaID)){
                            //APP运行日志删除
                            m_dbManager.delete(errorFileParamInfo);
                            new File(errorFileParamInfo.uploadFile).delete();
                        }

                    }
                });
            }
        }
    }

    private void reportErrorLog(ErrorFileParamInfo errorFileParamInfo, IReportResultCallback iReportResultCallback) {
        try {
            String doPostErrorLog = HttpTools.getInstance().doPostErrorLog(errorFileParamInfo);
            Trace.d(TAG, "reportErrorLog(): "+doPostErrorLog);
            if (TextUtils.isEmpty(doPostErrorLog)) {
                iReportResultCallback.onReportNetFail();
            } else {
                if (JsonAnalyticsUtil.reportJson(doPostErrorLog)) {
                    iReportResultCallback.onReportSuccess();
                } else {
                    iReportResultCallback.onReportFail();
                }
            }
        } catch (FotaException e) {
            iReportResultCallback.onReportFail();
            e.printStackTrace();
        }
    }

    /**
     * 下载上报
     *
     * @param downParamInfo
     * @param iReportResultCallback
     */
    public void reportDown(final DownParamInfo downParamInfo, final IReportResultCallback iReportResultCallback) {
        try {
            String jsonStr = HttpTools.getInstance().doPostReportDownResult(downParamInfo);
            if (TextUtils.isEmpty(jsonStr)) {
                //json为空，则一般为网络错误或者服务器异常,不删除，保留至下次上报
                iReportResultCallback.onReportNetFail();
            } else {
                boolean reportJson = JsonAnalyticsUtil.reportJson(jsonStr);
                if (reportJson) {
                    iReportResultCallback.onReportSuccess();
                } else {
                    iReportResultCallback.onReportFail();
                }
            }
        } catch (Exception e) {
            iReportResultCallback.onReportFail();
            e.printStackTrace();
        }
    }

    /**
     * 升级上报
     *
     * @param upgradeParamInfo
     * @param iReportResultCallback
     */
    public void reportUpgrade(final UpgradeParamInfo upgradeParamInfo, final IReportResultCallback iReportResultCallback) {
        try {
            String jsonStr = HttpTools.getInstance().doPostUpgradeResult(upgradeParamInfo);
            if (TextUtils.isEmpty(jsonStr)) {
                iReportResultCallback.onReportNetFail();
            } else {
                boolean reportJson = JsonAnalyticsUtil.reportJson(jsonStr);
                if (reportJson) {
                    iReportResultCallback.onReportSuccess();
                } else {
                    iReportResultCallback.onReportFail();
                }
            }
        } catch (Exception e) {
            iReportResultCallback.onReportFail();
            e.printStackTrace();
        }
    }

    /**
     * 推送消息上报
     *
     * @param pushMessageInfo
     * @param iReportResultCallback
     */
    public void reportPushData(PushMessageInfo pushMessageInfo, IReportResultCallback iReportResultCallback) {
        try {
            String response = HttpTools.getInstance().doPostMsgPushResponse(pushMessageInfo.msgId);
            if (TextUtils.isEmpty(response)) {
                iReportResultCallback.onReportNetFail();
            } else {
                boolean reportJson = JsonAnalyticsUtil.reportJson(response);
                if (reportJson) {
                    iReportResultCallback.onReportSuccess();
                } else {
                    iReportResultCallback.onReportFail();
                }
            }
        } catch (Exception e) {
            iReportResultCallback.onReportFail();
            e.printStackTrace();
        }
    }
}
