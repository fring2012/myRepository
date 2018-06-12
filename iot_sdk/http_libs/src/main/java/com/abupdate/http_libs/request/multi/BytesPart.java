package com.abupdate.http_libs.request.multi;


import com.abupdate.http_libs.data.Consts;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author fighter_lee
 * @date 2018/1/18
 */
public class BytesPart extends AbstractPart {
    public byte[] bytes;
    public static final String TAG = BytesPart.class.getSimpleName();
    //protected           String type = Consts.MIME_TYPE_OCTET_STREAM;

    public BytesPart(String key, byte[] bytes) {
        this(key, bytes, null);
        this.bytes = bytes;
    }

    public BytesPart(String key, byte[] bytes, String mimeType) {
        super(key, mimeType);
        this.bytes = bytes;
    }

    @Override
    protected byte[] createContentType() {
        return  (Consts.CONTENT_TYPE + ": " + mimeType + "\r\n").getBytes(infoCharset);
    }

    @Override
    protected byte[] createContentDisposition() {
        return ("Content-Disposition: form-data; name=\"" + key + "\"\r\n").getBytes(infoCharset);
    }

    @Override
    public long getTotalLength() {
        return header.length + bytes.length + CR_LF.length;
    }

    @Override
    public byte[] getTransferEncoding() {
        return TRANSFER_ENCODING_BINARY;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(bytes);
        out.write(CR_LF);
        updateProgress(bytes.length + CR_LF.length);
    }
}
