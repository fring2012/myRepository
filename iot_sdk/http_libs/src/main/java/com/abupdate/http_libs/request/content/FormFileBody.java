package com.abupdate.http_libs.request.content;

import com.abupdate.http_libs.data.Consts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author fighter_lee
 * @date 2018/1/17
 */
public class FormFileBody extends HttpBody {
    private File file;

    public FormFileBody(File file) {
        this(file, Consts.MIME_TYPE_OCTET_STREAM);
    }

    public FormFileBody(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
    }

    public File getFile() {
        return file;
    }

    @Override
    public long getContentLength() {
        return file.length();
    }

    @Override
    public void writeTo(OutputStream outstream) throws IOException {
        final InputStream instream = new FileInputStream(this.file);
        try {
            final byte[] tmp = new byte[OUTPUT_BUFFER_SIZE];
            int l;
            while ((l = instream.read(tmp)) != -1) {
                outstream.write(tmp, 0, l);
            }
            outstream.flush();
        } finally {
            instream.close();
        }
    }

}
