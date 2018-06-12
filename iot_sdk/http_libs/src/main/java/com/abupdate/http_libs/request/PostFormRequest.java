package com.abupdate.http_libs.request;

import com.abupdate.http_libs.data.HttpMethods;
import com.abupdate.http_libs.data.RequestConfig;
import com.abupdate.http_libs.request.base.AbstractRequest;
import com.abupdate.http_libs.request.content.StringBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by raise.yang on 17/09/05.
 */

public class PostFormRequest extends AbstractRequest {
    public PostFormRequest(String url) {
        super();
        setUrl(url);
        setMethod(HttpMethods.Post);
        setHeaderContentType(RequestConfig.FORM);
    }

    public PostFormRequest map(Map<String, String> params) {
        return (PostFormRequest) setHttpBody(new StringBody(getParamsData(params)));
    }

    private String getParamsData(Map<String, String> params) {
        String data = "";
        try {
            if (null != params && !params.isEmpty()) {
                StringBuffer buffer = new StringBuffer();

                for (Map.Entry<String, String> entry : params.entrySet()) {

                    buffer.append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(),
                                    getCharset())).append("&");// 请求的参数之间使用&分割。

                }
                // 最后一个&要去掉
                buffer.deleteCharAt(buffer.length() - 1);
                data = buffer.toString();

            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }
}
