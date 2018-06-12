package com.abupdate.sota.network;

import android.text.TextUtils;

import com.abupdate.http_libs.response.Response;
import com.abupdate.sota.info.local.SDKConfig;
import com.abupdate.sota.info.remote.SotaDeviceInfo;
import com.abupdate.sota.info.remote.SotaProductInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.inter.multi.CheckAllAppTask;
import com.abupdate.sota.utils.JsonAnalyseUtil;
import com.abupdate.sota.utils.TelephoneUtils;
import com.abupdate.trace.Trace;

import org.json.JSONObject;

import java.util.List;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class CheckAllAppRequest extends PostJsonRequest<CheckAllAppTask.CheckAllAppResponse,List<String>> {

    private static final String TAG = "CheckAllAppRequest";

    CheckAllAppRequest() {
        this(new CheckAllAppTask.CheckAllAppResponse());
    }

    CheckAllAppRequest(CheckAllAppTask.CheckAllAppResponse checkAllAppResponse) {
        super(checkAllAppResponse);
    }

    @Override
    protected Response doRequest() {
        String url = String.format("%s/product/%s/%s/app/checkAllApp", SDKConfig.HTTP_BASE_URL, SotaProductInfo.getInstance().productId, SotaRegisterInfo.getInstance().deviceId);
        JSONObject jsonObject = new JSONObject();
        try {
            SotaDeviceInfo sotaDeviceInfo = SotaDeviceInfo.getInstance();
            String cid = TelephoneUtils.build().getCid();
            String lac = TelephoneUtils.build().getLac();
            String mcc = TelephoneUtils.build().getMcc();
            String mnc = TelephoneUtils.build().getMnc();
            jsonObject.put("mid", sotaDeviceInfo.mid);
            if (!TextUtils.isEmpty(cid) && cid.length() <= 100) {
                jsonObject.put("cid", cid);
            }
            if (!TextUtils.isEmpty(lac) && lac.length() <= 100) {
                jsonObject.put("lac", lac);
            }
            if (!TextUtils.isEmpty(mcc) && mcc.length() <= 100) {
                jsonObject.put("mcc", mcc);
            }
            if (!TextUtils.isEmpty(mnc) && mnc.length() <= 100) {
                jsonObject.put("mnc", mnc);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Trace.e(TAG, e);
        }
        setUrl(url).setRequestParams(jsonObject);
        return super.doRequest();
    }

    @Override
    public List<String> parseSuccessResult(Response response) {
        return JsonAnalyseUtil.getAllApp(response.getContent());
    }

}
