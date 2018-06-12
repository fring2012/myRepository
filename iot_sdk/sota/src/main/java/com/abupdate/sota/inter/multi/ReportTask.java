package com.abupdate.sota.inter.multi;

import com.abupdate.sota.SotaControler;
import com.abupdate.sota.engine.report.ReportDBManager;
import com.abupdate.sota.info.local.ReportStatus;
import com.abupdate.sota.info.local.ReportType;
import com.abupdate.sota.info.remote.ReportInfo;
import com.abupdate.sota.inter.RealCall;
import com.abupdate.sota.network.RequestStack;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/26
 */
public class ReportTask {

    private static ReportTask mInstance;
    private ReportDBManager mDbManager;

    public static ReportTask getInstance() {
        if (mInstance == null) {
            synchronized (ReportTask.class) {
                if (mInstance == null) {
                    mInstance = new ReportTask();
                }
            }
        }
        return mInstance;
    }

    public ReportTask() {
        mDbManager = new ReportDBManager(SotaControler.sContext);
    }

    public void saveUpdate(String appName, String packageName, String versionName, int versionCode, ReportStatus status) {
        mDbManager.add(appName, packageName, versionName, versionCode, ReportType.UPDATE.getType(), status.getType());
    }

    public void saveDownload(String appName, String packageName, String versionName, int versionCode, ReportStatus status) {
        mDbManager.add(appName, packageName, versionName, versionCode, ReportType.DOWNLOAD.getType(), status.getType());
    }

    public int querySize() {
        List<ReportInfo> query = mDbManager.query();
        return query.size();
    }

    public int queryAndReport() {
        if (querySize() > 0) {
            RequestStack.getInstance().getDispatcher().enqueue(new RealCall().genReportAsy().addInfos(mDbManager.query()));
        }
        return 0;
    }

}
