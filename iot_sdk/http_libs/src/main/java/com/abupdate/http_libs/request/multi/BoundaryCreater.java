package com.abupdate.http_libs.request.multi;

import com.abupdate.http_libs.data.Consts;

import java.nio.charset.Charset;
import java.util.Random;

/**
 * 生成Boundary
 * Created by fighter_lee on 2018/1/17.
 */
public class BoundaryCreater {
    public static final Charset charset         = Charset.forName(Consts.DEFAULT_CHARSET);
    private final static char[]  MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private String boundary;
    private byte[] boundaryLine;
    private byte[] boundaryEnd;

    public BoundaryCreater() {
        final StringBuilder buf = new StringBuilder();
        final Random rand = new Random();
        for (int i = 0; i < 30; i++) {
            buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        boundary = buf.toString();
        boundaryLine =  ("--" + boundary + "\r\n").getBytes(charset);
        boundaryEnd = ("--" + boundary + "--\r\n").getBytes(charset);
    }

    public String getBoundary() {
        return boundary;
    }

    public byte[] getBoundaryLine() {
        return boundaryLine;
    }

    public byte[] getBoundaryEnd() {
        return boundaryEnd;
    }
}
