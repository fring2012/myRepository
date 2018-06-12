package com.abupdate.sota.network;

import com.abupdate.sota.engine.DefaultLocalApkInfoGen;
import com.abupdate.sota.engine.Dispatcher;
import com.abupdate.sota.info.local.ReportResult;
import com.abupdate.sota.info.remote.ApkInfo;
import com.abupdate.sota.info.remote.NewAppInfo;
import com.abupdate.sota.info.remote.ReportInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.inter.GenLocalApkInfoInter;
import com.abupdate.sota.inter.multi.CheckAllAppTask;
import com.abupdate.sota.inter.multi.CheckNewVersionTask;
import com.abupdate.sota.network.base.BaseRequestStack;
import com.abupdate.sota.network.base.CommonResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class RequestStack extends BaseRequestStack {

    private static RequestStack mInstance;
    private Dispatcher dispatcher;

    public static RequestStack getInstance() {
        if (mInstance == null) {
            synchronized (RequestStack.class) {
                if (mInstance == null) {
                    mInstance = new RequestStack();
                }
            }
        }
        return mInstance;
    }

    public Dispatcher getDispatcher() {
        if (dispatcher == null) {
            dispatcher = new Dispatcher();
        }
        return dispatcher;
    }

    public CommonResponse<SotaRegisterInfo> doRegister() {
        RegisterRequest registerRequest = new RegisterRequest();
        return (CommonResponse<SotaRegisterInfo>) doRequest(registerRequest);
    }

    public CheckAllAppTask.CheckAllAppResponse checkAllAppFlow() {
        if (!SotaRegisterInfo.getInstance().isValid()) {
            //注册信息不可用
            RegisterRequest registerRequest = new RegisterRequest();
            CommonResponse<SotaRegisterInfo> registerResponse = (CommonResponse<SotaRegisterInfo>) doRequest(registerRequest);
            if (!registerResponse.isOK) {
                return (CheckAllAppTask.CheckAllAppResponse) registerResponse.transErrorResult(new CheckAllAppTask.CheckAllAppResponse());
            } else {
                return doCheckAllApp();
            }
        } else {
            return doCheckAllApp();
        }
    }

    public CheckNewVersionTask.CheckNewVersionResponse checkNewAppFlow(GenLocalApkInfoInter infoInter) {
        CheckAllAppTask.CheckAllAppResponse response = checkAllAppFlow();
        if (!response.isOK) {
            return (CheckNewVersionTask.CheckNewVersionResponse) response.transErrorResult(new CheckNewVersionTask.CheckNewVersionResponse());
        } else {
            List<String> packageNames = response.getResult();
            if (null != packageNames && packageNames.size() > 0) {
                List<ApkInfo> apkInfos = new ArrayList<>();
                for (String packageName : packageNames) {
                    ApkInfo apkInfo = null;
                    if (null != infoInter){
                        apkInfo = infoInter.genlocalApkInfo(packageName);
                    }else{
                        apkInfo = new DefaultLocalApkInfoGen().genlocalApkInfo(packageName);
                    }
                    apkInfos.add(apkInfo);
                }
                CommonResponse<List<NewAppInfo>> checkNewAppResponse = doCheckNewApp(apkInfos);
                if (!checkNewAppResponse.isOK){
                    return (CheckNewVersionTask.CheckNewVersionResponse) checkNewAppResponse.transErrorResult(new CheckNewVersionTask.CheckNewVersionResponse());
                }else{
                    return (CheckNewVersionTask.CheckNewVersionResponse) checkNewAppResponse.transSuccessResult(new CheckNewVersionTask.CheckNewVersionResponse());
                }
            }
        }
        return null;
    }

    public CommonResponse<List<NewAppInfo>> doCheckNewApp(List<ApkInfo> apkInfos) {
        CheckNewAppRequest checkNewAppRequest = new CheckNewAppRequest();
        try {
            JSONArray jsonArray = new JSONArray();
            for (ApkInfo apkInfo : apkInfos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appName", apkInfo.getAppName());
                jsonObject.put("packageName", apkInfo.getPackageName());
                jsonObject.put("versionCode", apkInfo.getVersionCode());
                jsonObject.put("versionName", apkInfo.getVersionName());
                jsonArray.put(jsonObject);
            }
            checkNewAppRequest.addJson("content", jsonArray.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (CommonResponse<List<NewAppInfo>>) doRequest(checkNewAppRequest);
    }

    public CommonResponse<ReportResult> report(List<ReportInfo> infos) {
        ReportRequest reportRequest = new ReportRequest()
                .addReportInfo(infos);
        return (CommonResponse<ReportResult>) doRequest(reportRequest);
    }

    public CheckAllAppTask.CheckAllAppResponse doCheckAllApp() {
        CheckAllAppRequest checkAllAppRequest = new CheckAllAppRequest();
        return (CheckAllAppTask.CheckAllAppResponse) doRequest(checkAllAppRequest);
    }

}
