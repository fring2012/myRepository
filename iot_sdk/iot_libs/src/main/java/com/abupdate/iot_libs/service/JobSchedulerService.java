package com.abupdate.iot_libs.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.abupdate.iot_libs.MqttAgentPolicy;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by fighter_lee on 2017/7/5.
 * 7.0以上设备接受不到网络切换广播，那么使用JobService方式来上报，提升上报成功率
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {

    @Override
    public void onCreate() {
        super.onCreate();
        Trace.d("JobSchedulerService", "onCreate() ");
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        Trace.d("JobSchedulerService", "onStartJob() ");
        //多次jobfinish 以首次为准
        //mqtt任务
        if (SPFTool.getBoolean(MqttAgentPolicy.CONFIG_MQTT_CONNECT, false)) {
            if (!MqttAgentPolicy.isConnected()) {
                MqttAgentPolicy.connect();
            }
        }

        //查询上报
        int i = queryReport();
        if (i != 0) {
            OtaService.startByAction(OtaService.ACTION_REPORT);
        }

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                if (SPFTool.getBoolean(MqttAgentPolicy.CONFIG_MQTT_CONNECT, false)) {
                    if (!MqttAgentPolicy.isConnected()) {
                        //继续此任务
                        jobFinished(params, true);
                    }
                }
                int i1 = queryReport();
                if (i1 == 0) {
                    //没有需要上报的数据，置为false
                    jobFinished(params, false);
                } else {
                    jobFinished(params, true);
                }
            }
        }, 30, TimeUnit.SECONDS);

        //周期check
        if (System.currentTimeMillis() - SPFTool.getLong(OtaConstants.SPF_STATIC_CHECK_VERSION_CYCLE, -1) >= OtaConstants.STATIC_CHECK_VERSION_CYCLE) {
            SPFTool.putLong(OtaConstants.SPF_STATIC_CHECK_VERSION_CYCLE, System.currentTimeMillis());
            //check version（周期和服务器通讯一次）
            OtaService.startByAction(OtaService.ACTION_STATIC_CHECK_VERSION);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Trace.d("JobSchedulerService", "onStopJob() ");
        return false;
    }

    public int queryReport() {
        int i = ReportManager.getInstance(this).queryReport();
        return i;
    }

}
