package com.abupdate.iot_download_libs;

import android.text.TextUtils;

import com.abupdate.iot_download_libs.segmentDownload.SegmentDownInfo;

import java.util.List;

/**
 * # 下载文件实体
 * Created by raise.yang on 17/07/03.
 */

public class DownEntity {

    public String url;
    public String file_path;//绝对路径
    public String md5;
    public long file_size;

    public long downloaded_size;//已下载大小

    public String str_extra;//用于扩展

    public int download_status;//下载状态码
    public volatile boolean download_cancel;//取消下载

    private List<SegmentDownInfo> segmentDownInfos;//分段下载信息

    public DownEntity(String url, String file_path, long file_size, String md5) {
        this.url = url;
        this.file_path = file_path;
        this.file_size = file_size;
        this.md5 = md5;
    }

    public DownEntity(String url, String file_path, long file_size) {
        this.url = url;
        this.file_path = file_path;
        this.file_size = file_size;
    }

    public DownEntity(String url, String file_path) {
        this.url = url;
        this.file_path = file_path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DownEntity) {
            DownEntity obj1 = (DownEntity) obj;
            if (TextUtils.isEmpty(obj1.url) && TextUtils.isEmpty(this.url)){
                return true;
            }
            if (obj1.url.equals(this.url)) {
                return true;
            }
        }
        return false;
    }

    public DownEntity setSegmentDownInfo(List<SegmentDownInfo> segmentDownInfos) {
        this.segmentDownInfos = segmentDownInfos;
        return this;
    }

    public List<SegmentDownInfo> getSegmentDownInfos() {
        return segmentDownInfos;
    }

    @Override
    public String toString() {
        return "DownEntity{" +
                "url='" + url + '\'' +
                ", file_path='" + file_path + '\'' +
                ", md5='" + md5 + '\'' +
                ", file_size=" + file_size +
                ", downloaded_size=" + downloaded_size +
                ", str_extra='" + str_extra + '\'' +
                ", download_status=" + download_status +
                ", download_cancel=" + download_cancel +
                '}';
    }
}
