package com.abupdate.iot_libs.engine;

import android.content.Context;
import android.os.DropBoxManager;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.ErrorFileParamInfo;
import com.abupdate.iot_libs.report.ReportDBManager;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.FileUtil;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/1/16
 */
public class LogManager {
    private static final String TAG = "LogManager";
    private static LogManager mInstance;
    public static final int TASK_REPORT_RECOVERY_LOG = 1;
    public static final int TASK_REPORT_TRACE_LOG = 2;
    public static final String FILE_KEY = "uploadFile";
    //日志压缩文件存储目录
    String log_dir_s = OtaAgentPolicy.sCx.getFilesDir() + "/Log";

    public static LogManager getInstance() {
        if (mInstance == null) {
            synchronized (LogManager.class) {
                if (mInstance == null) {
                    mInstance = new LogManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 将存储的日志上报
     */
    public void report() {
        OtaService.startByAction(OtaService.ACTION_REPORT);
    }

    /**
     * 上报SDK执行日志<br/>
     * SDK执行日志路径请见{@link OtaAgentPolicy.Builder#tracePath} <br/>
     *
     */
    public void saveTraceLog() {
        if (!OtaAgentPolicy.getConfig().reportLog){
            return;
        }
        String tracePath = OtaAgentPolicy.getConfig().tracePath;
        File file = zipTraceLog(tracePath);
        if (null == file || !file.exists() || file.length() == 0){
            return;
        }
        saveTraceLog(file);
    }

    /**
     * 上报由{@link LogManager#zipTraceLog}方法压缩的日志文件
     * @param customLog
     */
    public void saveTraceLog(File customLog) {
        clearLog();
        ErrorFileParamInfo errorFileParamInfo = new ErrorFileParamInfo(
                DeviceInfo.getInstance().mid,
                String.valueOf(LogManager.TASK_REPORT_TRACE_LOG),
                customLog.getAbsolutePath());
        ReportManager.getInstance(OtaAgentPolicy.sCx).saveErrorFileData(errorFileParamInfo);
        Trace.d(TAG, "saveTraceLog() finish");
    }

    /**
     * 存储升级失败日志文件
     * @param deltaId
     */
    public void saveRecoveryLog(final String deltaId) {
        if (!OtaAgentPolicy.getConfig().reportLog){
            return;
        }
        clearLog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                File recoveryLog = LogManager.getInstance().zipRecoveryLog();
                if (null != recoveryLog && recoveryLog.exists() && recoveryLog.length() > 0) {
                    ErrorFileParamInfo errorFileParamInfo = new ErrorFileParamInfo(
                            DeviceInfo.getInstance().mid,
                            deltaId,
                            String.valueOf(LogManager.TASK_REPORT_RECOVERY_LOG),
                            recoveryLog.getAbsolutePath());
                    ReportManager.getInstance(OtaAgentPolicy.sCx).saveErrorFileData(errorFileParamInfo);
                    Trace.d(TAG, "saveRecoveryLog() finish");
                }
            }
        }).start();
    }

    /**
     * 指定格式压缩日志文件
     * @param tracePath
     * @return
     */
    public File zipTraceLog(String tracePath) {
        if (!new File(tracePath).exists() || new File(tracePath).length() <= 0) {
            Trace.e(TAG, "Trace file not exist!");
            return null;
        }
        String fota_zip_log = log_dir_s + File.separator + formatTime(System.currentTimeMillis()) + ".zip";
        if (!new File(log_dir_s).exists()) {
            if (!FileUtil.createOrExistsDir(log_dir_s)) {
                Trace.e(TAG, "fota log dir create failed");
                return null;
            }
        }
        try {
            if (FileUtil.zipFile(new File(tracePath), new File(fota_zip_log))) {
                return new File(fota_zip_log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public File zipRecoveryLog() {
        long last_recovery_time = SPFTool.getLong(SPFTool.KEY_LAST_RECOVERY_TIME, -1);
        if (-1 == last_recovery_time) {
            return null;
        }

        File recovery_log_dir = new File(log_dir_s);
        if (!recovery_log_dir.exists()) {
            if (!FileUtil.createOrExistsDir(log_dir_s)) {
                return null;
            }
        }
        String recovery_log = "";

        DropBoxManager dropBoxManager = (DropBoxManager) OtaAgentPolicy.sCx.getSystemService(Context.DROPBOX_SERVICE);
        DropBoxManager.Entry system_recovery_log = dropBoxManager.getNextEntry("SYSTEM_RECOVERY_LOG", last_recovery_time);
        if (system_recovery_log != null && dropBoxManager != null && dropBoxManager.isTagEnabled("SYSTEM_RECOVERY_LOG")) {
            recovery_log = log_dir_s + File.separator + formatTime(system_recovery_log.getTimeMillis())+".zip";
            InputStream inputStream = null;
            try {
                inputStream = system_recovery_log.getInputStream();
                if (!FileUtil.zipFile(inputStream, new File(recovery_log),getLogName(system_recovery_log.getTag(),system_recovery_log.getTimeMillis()))){
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    FileUtil.closeIO(inputStream);
                }
            }
        }

        if (new File(recovery_log).exists() && new File(recovery_log).length() > 0) {
            return new File(recovery_log);
        } else {
            return null;
        }
    }

    public String formatTime(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String fTime = simpleDateFormat.format(time);
        return fTime;
    }

    public String getLogName(String tag,Long timeMillis) {
        return tag+"@"+timeMillis+".txt";
    }

    private void clearLog() {
        //如果日志数大于5，清除无用的数据。
        ReportDBManager db = ReportManager.getInstance(OtaAgentPolicy.sCx).getDB();
        List<ErrorFileParamInfo> infos = db.query_error_log_data();
        if (infos.size() > 5){
            for (ErrorFileParamInfo info : infos) {
                new File(info.uploadFile).delete();
                db.delete(info);
            }
        }
    }

}
