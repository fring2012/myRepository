package com.abupdate.http_libs.request.multi;

import com.abupdate.http_libs.data.Consts;

import java.io.UnsupportedEncodingException;

/**
 * @author fighter_lee
 * @date 2018/1/18
 */
public class StringPart extends BytesPart {

    protected String charset;
    protected String mimeType;

    public StringPart(String key, String string) {
        this(key, string, Consts.DEFAULT_CHARSET, Consts.MIME_TYPE_TEXT);
    }

    public StringPart(String key, String string, String charset, String mimeType) {
        super(key, getBytes(string, charset),mimeType);
        this.mimeType = mimeType != null ? mimeType : Consts.MIME_TYPE_TEXT;
        this.charset = charset;
    }

    public static byte[] getBytes(String string, String charset) {
        try {
            return string.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected byte[] createContentType() {
        return (Consts.CONTENT_TYPE + ": " + mimeType + " " + Consts.CHARSET_PARAM + charset + "\r\n").getBytes(infoCharset);
    }

    @Override
    public byte[] getTransferEncoding() {
        return TRANSFER_ENCODING_8BIT;
    }

}
