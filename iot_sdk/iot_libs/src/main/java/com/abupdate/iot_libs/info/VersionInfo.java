package com.abupdate.iot_libs.info;


import android.text.TextUtils;

import com.abupdate.iot_download_libs.segmentDownload.SegmentDownInfo;
import com.abupdate.iot_libs.OtaAgentPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class VersionInfo {

    private static VersionInfo mInstance;

    private VersionInfo() {
    }

    public static VersionInfo getInstance() {
        if (mInstance == null) {
            synchronized (VersionInfo.class) {
                mInstance = new VersionInfo();
            }
        }
        return mInstance;
    }

    /**
     * 策略信息map
     */
    public HashMap<String, PolicyMapInfo> policyHashMap = new HashMap<String, PolicyMapInfo>();

    /**
     * 升级版本号
     */
    public String versionName;
    /**
     * 升级包大小
     */
    public long fileSize;
    /**
     * 升级包ID
     */
    public String deltaID;
    /**
     * 升级包MD5检验值，校验升级包是否下载正确
     */
    public String md5sum;
    /**
     * 升级包下载地址
     */
    public String deltaUrl;

    /**
     * 发布日期
     */
    public String publishDate;
    /**
     * 日志内容
     */
    public String content;

    /**
     * 分段下载
     */
    public List<SegmentDownInfo> segmentSha;

    /**
     * 版本别名
     */
    public String versionAlias;

    @Override
    public String toString() {
        return "VersionInfo{" + "\n" +
                "versionName='" + versionName + '\'' + "\n" +
                "versionAlias='" + versionAlias + '\'' + "\n" +
                "fileSize=" + fileSize + "\n" +
                "deltaID='" + deltaID + '\'' + "\n" +
                "md5sum='" + md5sum + '\'' + "\n" +
                "deltaUrl='" + deltaUrl + '\'' + "\n" +
                "publishDate='" + publishDate + '\'' + "\n" +
                "content='" + content + '\'' + "\n" +
                '}';
    }

    /**
     * 数据重置
     */
    public void reset() {
        policyHashMap.clear();
        versionName = null;
        versionAlias = null;
        fileSize = 0;
        deltaID = null;
        md5sum = null;
        deltaUrl = null;
        publishDate = null;
        content = null;
        segmentSha = null;
    }

    /**
     * 获取当前设备的语言的更新提示
     * @return
     */
    public String getReleaseNoteByCurrentLanguage() {
        String l_country = OtaAgentPolicy.sCx.getResources().getConfiguration().locale.getCountry();
        String l_language = OtaAgentPolicy.sCx.getResources().getConfiguration().locale.getLanguage();
        return getReleaseNoteByLanguage(l_country,l_language);
    }

    /**
     * 获取指定国家和语言的更新提示
     * @param country
     * @param language
     * @return
     */
    public String getReleaseNoteByLanguage(String country,String language) {
        String l_language_country = language + "_" + country;
        String content_backup = "";
        String release_note = "";
        try {
            JSONArray ja = new JSONArray(content);
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                if (jo.has("country")) {
                    String countryName = jo.getString("country");
                    if (l_language_country.equalsIgnoreCase(countryName)) {
                        release_note = jo.getString("content");
                        break;
                    } else if (countryName.contains(language)) {
                        content_backup = jo.getString("content");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            release_note = "";
        }

        if (TextUtils.isEmpty(release_note)) {
            if (!TextUtils.isEmpty(content_backup)) {
                release_note = content_backup;
            } else {
                release_note = content;
            }
        }
        return release_note;
    }
}
