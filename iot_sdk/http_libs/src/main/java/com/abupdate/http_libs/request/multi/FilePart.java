package com.abupdate.http_libs.request.multi;

import com.abupdate.http_libs.data.Consts;
import com.abupdate.http_libs.request.content.HttpBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author fighter_lee
 * @date 2018/1/18
 */
public class FilePart extends AbstractPart {

    public File file;
    public static final String TAG = FilePart.class.getSimpleName();

    public FilePart(String key, File file) {
        this(key, file, Consts.MIME_TYPE_OCTET_STREAM);
    }

    public FilePart(String key, File file, String mimeType) {
        super(key, mimeType);
        this.file = file;
    }

    @Override
    protected byte[] createContentType() {
        return (Consts.CONTENT_TYPE + ": " + mimeType + "\r\n").getBytes(infoCharset);
    }

    @Override
    protected byte[] createContentDisposition() {
        String dis = "Content-Disposition: form-data; name=\"" + key;
        return (dis + "\"; filename=\"" + file.getName() + "\"\r\n").getBytes(infoCharset);
    }


    @Override
    public long getTotalLength() {
        long len = file.length();
        return header.length + len + CR_LF.length;
    }

    @Override
    public byte[] getTransferEncoding() {
        return TRANSFER_ENCODING_BINARY;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        final InputStream instream = new FileInputStream(this.file);
        try {
            final byte[] tmp = new byte[HttpBody.OUTPUT_BUFFER_SIZE];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                out.write(tmp, 0, l);
                updateProgress(l);
            }
            out.write(CR_LF);
            updateProgress(CR_LF.length);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            instream.close();
        }
    }


}
