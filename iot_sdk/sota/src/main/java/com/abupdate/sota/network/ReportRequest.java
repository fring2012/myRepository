package com.abupdate.sota.network;

import com.abupdate.http_libs.response.Response;
import com.abupdate.sota.info.local.ReportResult;
import com.abupdate.sota.info.local.SDKConfig;
import com.abupdate.sota.info.remote.SotaDeviceInfo;
import com.abupdate.sota.info.remote.SotaProductInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.info.remote.ReportInfo;
import com.abupdate.sota.network.base.CommonResponse;
import com.abupdate.trace.Trace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/8
 */
public class ReportRequest extends PostJsonRequest<CommonResponse<ReportResult>, Void> {
    private static final String TAG = "ReportRequest";
    private List<ReportInfo> infos = new ArrayList<>();

    ReportRequest() {
        this(new CommonResponse<ReportResult>());
    }

    ReportRequest(CommonResponse<ReportResult> reportStatusCommonResponse) {
        super(reportStatusCommonResponse);
    }

    public ReportRequest addReportInfo(List<ReportInfo> infos) {
        if (null != infos) {
            infos.addAll(infos);
        }
        return this;
    }

    @Override
    protected Response doRequest() {
        String url = String.format("%s/product/%s/%s/app/reportAppResult", SDKConfig.HTTP_BASE_URL, SotaProductInfo.getInstance().productId, SotaRegisterInfo.getInstance().deviceId);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = genReportJson();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setRequestParams(jsonObject)
                .setUrl(url);
        return super.doRequest();
    }

    @Override
    public Void parseSuccessResult(Response response) {
        return null;
    }

    private JSONObject genReportJson() throws JSONException {
        Trace.d(TAG, "genReportJson() ");
        JSONObject content = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (ReportInfo info : infos) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appName", info.getAppName());
            jsonObject.put("packageName", info.getPackageName());
            jsonObject.put("versionCode", info.getVersionCode());
            jsonObject.put("versionName", info.getVersionName());
            jsonObject.put("reportType", info.getReportType());
            jsonObject.put("status", info.getReportStatus());
            jsonArray.put(jsonObject);
        }
        content.put("content", jsonArray.toString());
        content.put("mid", SotaDeviceInfo.getInstance().mid);
        return content;
    }
}
