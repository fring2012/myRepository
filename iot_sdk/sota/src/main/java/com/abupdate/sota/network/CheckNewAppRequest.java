package com.abupdate.sota.network;

import com.abupdate.http_libs.response.Response;
import com.abupdate.sota.info.local.SDKConfig;
import com.abupdate.sota.info.remote.SotaDeviceInfo;
import com.abupdate.sota.info.remote.NewAppInfo;
import com.abupdate.sota.info.remote.SotaProductInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.network.base.CommonResponse;
import com.abupdate.sota.utils.JsonAnalyseUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class CheckNewAppRequest extends PostJsonRequest<CommonResponse<List<NewAppInfo>>,List<NewAppInfo>> {

    private Map<String, String> jsonObjects = new HashMap<>();

    CheckNewAppRequest(CommonResponse<List<NewAppInfo>> listCommonResponse) {
        super(listCommonResponse);
    }

    CheckNewAppRequest() {
        this(new CommonResponse<List<NewAppInfo>>());
    }

    public CheckNewAppRequest addJson(String key, String jsonObject) {
        jsonObjects.put(key, jsonObject);
        return this;
    }

    @Override
    protected Response doRequest() {
        String url = String.format("%s/product/%s/%s/app/checkNewApp", SDKConfig.HTTP_BASE_URL, SotaProductInfo.getInstance().productId, SotaRegisterInfo.getInstance().deviceId);
        JSONObject js = new JSONObject();
        try {
            js.put("mid", SotaDeviceInfo.getInstance().mid);
            for (String s : jsonObjects.keySet()) {
                js.put(s, jsonObjects.get(s));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setUrl(url).setRequestParams(js);
        return super.doRequest();
    }

    @Override
    public List<NewAppInfo> parseSuccessResult(Response response) {
        return JsonAnalyseUtil.getNewApp(response.getContent());
    }

}
