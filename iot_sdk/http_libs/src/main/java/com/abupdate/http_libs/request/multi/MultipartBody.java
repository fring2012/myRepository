package com.abupdate.http_libs.request.multi;


import com.abupdate.http_libs.data.Consts;
import com.abupdate.http_libs.request.content.HttpBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

/**
 * @author fighter_lee
 * @date 2018/1/18
 */
public class MultipartBody extends HttpBody {
    private LinkedList<AbstractPart> httpParts = new LinkedList<AbstractPart>();
    private long totalSize;
    private long bytesWritten;
    BoundaryCreater boundaryCreater;

    public MultipartBody() {
        boundaryCreater = new BoundaryCreater();
        contentType = Consts.MIME_TYPE_FORM_DATA + Consts.BOUNDARY_PARAM + boundaryCreater.getBoundary();
    }

    public LinkedList<AbstractPart> getHttpParts() {
        return httpParts;
    }

    public MultipartBody setHttpParts(LinkedList<AbstractPart> httpParts) {
        this.httpParts = httpParts;
        return this;
    }

    public MultipartBody addPart(String key, String string, String charset, String mimeType) throws
            UnsupportedEncodingException {
        return addPart(new StringPart(key, string, charset, mimeType));
    }

    public MultipartBody addPart(String key, byte[] bytes, String mimeType) {
        return addPart(new BytesPart(key, bytes, mimeType));
    }

    public MultipartBody addPart(String key, File file, String mimeType) throws FileNotFoundException {
        return addPart(new FilePart(key, file, mimeType));
    }

    public MultipartBody addPart(AbstractPart part) {
        if (part == null) {
            return this;
        }
        // note that: set multibody to every part, so that we can get progress of these part.
        part.setMultipartBody(this);
        part.createHeader(boundaryCreater.getBoundaryLine());
        httpParts.add(part);
        return this;
    }

    @Override
    public long getContentLength() {
        long contentLen = -1;
        try {
            for (AbstractPart part : httpParts) {
                long len = 0;
                len = part.getTotalLength();
                if (len < 0) {
                    return -1;
                }
                contentLen += len;
            }
            contentLen += boundaryCreater.getBoundaryEnd().length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentLen;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        bytesWritten = 0;
        totalSize = (int) getContentLength();
        for (AbstractPart part : httpParts) {
            part.writeToServer(outstream);
        }
        outstream.write(boundaryCreater.getBoundaryEnd());
        updateProgress(boundaryCreater.getBoundaryEnd().length);
    }

    @SuppressWarnings("unchecked")
    protected void updateProgress(long count) {
        bytesWritten += count;
    }

    public BoundaryCreater getBoundary() {
        return boundaryCreater;
    }
}
