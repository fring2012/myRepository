package com.abupdate.iot_libs.utils;

import android.content.Context;
import android.text.TextUtils;

import com.abupdate.iot_download_libs.segmentDownload.SegmentDownInfo;
import com.abupdate.iot_libs.info.PolicyMapInfo;
import com.abupdate.iot_libs.info.RegisterInfo;
import com.abupdate.iot_libs.info.SafeInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.trace.Trace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fighter_lee on 2017/9/15.
 */

public class BeanUtils {

    //设置register信息
    public static void setRegisterInfo(String jsonStr, Context sCx) {
        JSONObject json = null;
        try {
            json = new JSONObject(jsonStr);
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.has("deviceSecret")) {
                    String deviceSecret = data.getString("deviceSecret");
                    RegisterInfo.getInstance().deviceSecret = deviceSecret;
                    SPFTool.putString(SPFTool.KEY_DEVICE_SECRET, deviceSecret);
                }
                if (data.has("deviceId")) {
                    String deviceId = data.getString("deviceId");
                    RegisterInfo.getInstance().deviceId = deviceId;
                    SPFTool.putString(SPFTool.KEY_DEVICE_ID, deviceId);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 版本检测回来后，设置数据
     */
    public static void setVersionInfo(String jsonStr) {
        VersionInfo.getInstance().reset();
        JSONObject jobj = null;
        try {
            jobj = new JSONObject(jsonStr);

            if (jobj.has("data")) {
                JSONObject data = jobj.getJSONObject("data");
                if (data.has("version")) {
                    JSONObject verJob = new JSONObject(data.getString("version"));
                    if (verJob.has("versionName")) {
                        VersionInfo.getInstance().versionName = verJob.getString("versionName");
                    }
                    if (verJob.has("fileSize")) {
                        VersionInfo.getInstance().fileSize = verJob.getInt("fileSize");
                    }
                    if (verJob.has("deltaID")) {
                        VersionInfo.getInstance().deltaID = verJob.getString("deltaID");
                    }
                    if (verJob.has("md5sum")) {
                        //解决md5起始位少个'0'
                        String tmpMd5 = verJob.getString("md5sum");
                        int fix_num = 32 - tmpMd5.length();
                        for (int i = 0; i < fix_num; i++) {
                            tmpMd5 = "0" + tmpMd5;
                        }
                        VersionInfo.getInstance().md5sum = tmpMd5;
                    }
                    if (verJob.has("versionAlias")) {
                        VersionInfo.getInstance().versionAlias = verJob.getString("versionAlias");
                        if (TextUtils.isEmpty(VersionInfo.getInstance().versionAlias)) {
                            VersionInfo.getInstance().versionAlias = VersionInfo.getInstance().versionName;
                        }
                    }
                    if (verJob.has("deltaUrl")) {
                        VersionInfo.getInstance().deltaUrl = verJob.getString("deltaUrl");
                    }
                    if (verJob.has("segmentMd5")) {
                        String segment = verJob.getString("segmentMd5");
                        if (segment.length() > 0 && !TextUtils.equals(segment, "null")) {
                            JSONArray segmentSha = new JSONArray(segment);
                            List<SegmentDownInfo> segmentDownInfos = new ArrayList<>();
                            for (int i = 0; i < segmentSha.length(); i++) {
                                JSONObject jsonObject = segmentSha.getJSONObject(i);
                                SegmentDownInfo segmentDownInfo = new SegmentDownInfo();
                                if (jsonObject.has("num")) {
                                    int num = jsonObject.getInt("num");
                                    segmentDownInfo.setNum(num);
                                }
                                if (jsonObject.has("md5")) {
                                    String md5 = jsonObject.getString("md5");
                                    segmentDownInfo.setMd5(md5);
                                }
                                if (jsonObject.has("startpos")) {
                                    long startpos = jsonObject.getLong("startpos");
                                    segmentDownInfo.setStartpos(startpos);
                                }
                                if (jsonObject.has("endpos")) {
                                    long endpos = jsonObject.getLong("endpos");
                                    segmentDownInfo.setEndpos(endpos);
                                }
                                segmentDownInfos.add(segmentDownInfo);
                            }
                            VersionInfo.getInstance().segmentSha = segmentDownInfos;
                        }
                    }
                }
                if (data.has("releaseNotes")) {
                    JSONObject relJob = new JSONObject(data.getString("releaseNotes"));
                    if (relJob.has("version")) {
                        VersionInfo.getInstance().versionName = relJob.getString("version");
                    }
                    if (relJob.has("publishDate")) {
                        VersionInfo.getInstance().publishDate = relJob.getString("publishDate");
                    }
                    if (relJob.has("content")) {
                        VersionInfo.getInstance().content = relJob.getString("content");
                    }
                }
                if (data.has("policy")) {
                    JSONObject polJob = new JSONObject(data.getString("policy"));
                    if (polJob.has("download")) {
                        String jsonData = polJob.getString("download");
                        getPolicyMapInfo(jsonData, "download");
                    }
                    if (polJob.has("install")) {
                        String jsonData = polJob.getString("install");
                        getPolicyMapInfo(jsonData, "install");
                    }
                    if (polJob.has("check")) {
                        String jsonData = polJob.getString("check");
                        getPolicyMapInfo(jsonData, "check");
                    }
                }
                if (data.has("safe")) {
                    JSONObject safeJob = new JSONObject(data.getString("safe"));
                    if (safeJob.has("isEncrypt")) {
                        int isEncrypt = safeJob.getInt("isEncrypt");
                        SafeInfo.getInstance().isEncrypt = isEncrypt;
                    }
                    if (safeJob.has("encKey")) {
                        String encKey = safeJob.getString("encKey");
                        SafeInfo.getInstance().encKey = encKey;
                    }
                }
            }
            SPFTool.putString(SPFTool.KEY_VERSION_INFO, jsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setVersionInfoFromLocal() {
        String versionInfo = SPFTool.getString(SPFTool.KEY_VERSION_INFO, "");
        if (!TextUtils.isEmpty(versionInfo)) {
            Trace.d("BeanUtils", "setVersionInfoFromLocal() set version info");
            setVersionInfo(versionInfo);
        }
    }

    private static void getPolicyMapInfo(String jsonData, String keyTag) {
        PolicyMapInfo policyMapInfo;

        try {
            JSONArray array = new JSONArray(jsonData);
            int len = array.length();

            for (int i = 0; i < len; i++) {
                JSONObject jsonPolicy = array.getJSONObject(i);
                policyMapInfo = new PolicyMapInfo();

                if (jsonPolicy.has("key_name")) {
                    policyMapInfo.key_name = jsonPolicy
                            .getString("key_name");
                }
                if (jsonPolicy.has("key_value")) {
                    policyMapInfo.key_value = jsonPolicy
                            .getString("key_value");
                }
                if (jsonPolicy.has("key_message")) {
                    policyMapInfo.key_message = jsonPolicy
                            .getString("key_message");
                }

                VersionInfo.getInstance().policyHashMap.put(keyTag + "_" + policyMapInfo.key_name, policyMapInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
