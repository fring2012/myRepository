package com.abupdate.http_libs.request.multi;


import com.abupdate.http_libs.data.Consts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author fighter_lee
 * @date 2018/1/18
 */
public abstract class AbstractPart {

    protected static final Charset infoCharset = BoundaryCreater.charset;
    public static final byte[] CR_LF = "\r\n".getBytes(infoCharset);
    public static final byte[] TRANSFER_ENCODING_BINARY = "Content-Transfer-Encoding: binary\r\n".getBytes(infoCharset);
    public static final byte[] TRANSFER_ENCODING_8BIT = "Content-Transfer-Encoding: 8bit\r\n".getBytes(infoCharset);


    protected String key;
    public byte[] header;
    protected String mimeType = Consts.MIME_TYPE_OCTET_STREAM;
    protected MultipartBody multipartBody;

    protected AbstractPart(String key, String mimeType) {
        this.key = key;
        if (mimeType != null) {
            this.mimeType = mimeType;
        }
    }

    //此方法需要被调用以产生header（开发者无需自己调用，Entity会调用它）
    public byte[] createHeader(byte[] boundaryLine) {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        try {
            headerStream.write(boundaryLine);
            headerStream.write(createContentDisposition());
            headerStream.write(createContentType());
            headerStream.write(getTransferEncoding());
            headerStream.write(CR_LF);
            header = headerStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return header;
    }

    protected abstract byte[] createContentType();

    protected abstract byte[] createContentDisposition();

    public abstract long getTotalLength() throws IOException;

    public abstract byte[] getTransferEncoding();

    public abstract void writeTo(OutputStream out) throws IOException;

    public void writeToServer(OutputStream out) throws IOException {
        if (header == null) {
            throw new RuntimeException("Not call createHeader()，未调用createHeader方法");
        }
        out.write(header);
        updateProgress(header.length);
        writeTo(out);
    }

    public MultipartBody getMultipartBody() {
        return multipartBody;
    }

    public void setMultipartBody(MultipartBody multipartBody) {
        this.multipartBody = multipartBody;
    }

    protected void updateProgress(int length) {
        if (multipartBody != null) {
            multipartBody.updateProgress(length);
        }
    }
}
