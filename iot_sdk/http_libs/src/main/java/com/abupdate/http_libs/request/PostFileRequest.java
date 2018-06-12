package com.abupdate.http_libs.request;

import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.request.base.AbstractRequest;
import com.abupdate.http_libs.request.multi.FilePart;
import com.abupdate.http_libs.request.multi.MultipartBody;
import com.abupdate.http_libs.request.multi.StringPart;

import java.io.File;
import java.util.Map;

/**
 * Created by raise.yang on 17/09/05.
 */

public class PostFileRequest extends AbstractRequest {

    private String filePath;

    private String disName;
    private MultipartBody multipartBody;

    public String getDisName() {
        return disName;
    }

    public PostFileRequest setDisName(String name) {
        this.disName = name;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public PostFileRequest(String url) {
        super();
        multipartBody = new MultipartBody();
        setUrl(url);
        setMethod(HttpMethods.Post);
    }

    public PostFileRequest map(Map<String, String> params) {
        addParams(multipartBody, params);
        return (PostFileRequest) setHttpBody(multipartBody);
    }

    public PostFileRequest addFile(String key, File file) {
        multipartBody.addPart(new FilePart(key,file));
        return (PostFileRequest) setHttpBody(multipartBody);
    }

    private void addParams(MultipartBody multipartBody, Map<String, String> params) {
        for (Map.Entry<String, String> paramsEntry : params.entrySet()) {
            StringPart stringPart = new StringPart(paramsEntry.getKey(), paramsEntry.getValue());
            multipartBody.addPart(stringPart);
        }
    }
}
