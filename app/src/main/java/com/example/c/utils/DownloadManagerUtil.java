package com.example.c.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import com.example.c.been.FileInfo;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

public class DownloadManagerUtil {
    private int fileLength = 0;
    public final FileInfo fileInfo = new FileInfo();
    private long finished = 0;

    //利用android自带的下载器下载
    public static void downloadAPK(String url, Context context, String filename) {
        Uri uri = Uri.parse(url);
        String[] bakUrlSplit = url.split("/");
        //https://dl.pstmn.io/download/latest/win64
        DownloadManager.Request request = new DownloadManager.Request(uri);
        ;
        request.setDestinationInExternalPublicDir("/download/", bakUrlSplit[bakUrlSplit.length - 1]);
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);

    }

    //使用OkHttp下载文件
    public static void downloadFile(final String url, Context context, String fileName) {
        final long startTime = System.currentTimeMillis();
        Request request = new Request.Builder().url(url).build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //下载失败
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                String mSDCardPath = Environment.getExternalStorageDirectory().getPath();
                File dest = new File(mSDCardPath, url.substring(url.lastIndexOf("/") + 1));
                sink = Okio.sink(dest);
                bufferedSink = Okio.buffer(sink);
                bufferedSink.writeAll(response.body().source());

                bufferedSink.close();

            }
        });
    }

    /**
     * OkHttp设置请求头断点下载
     */
    public static void downloadPontFile(FileInfo fileInfo) {
        final String url = fileInfo.getUrl();
        final FileInfo _fileInfo = fileInfo;
        Logger.d("开始下载文件：" + fileInfo.getFileName() + url);

        Request request = new Request.Builder().url(url).header("Range", "bytes=" + fileInfo.getProgress() + "-" + fileInfo.getLength()).build();
        Logger.d("设置请求头Range：" + "bytes=" + fileInfo.getProgress() + "-" + fileInfo.getLength());
        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);

        ResponseBody body = null;
        try {
            body = call.execute().body();
            Source source = null;
            BufferedSource bufferedSource = null;
            String mSDCardPath = Environment.getExternalStorageDirectory().getPath();
            File dest = new File(mSDCardPath, url.substring(url.lastIndexOf("/") + 1));

            RandomAccessFile raf = new RandomAccessFile(dest, "rwd");
            raf.seek(_fileInfo.getProgress());//从文件已经下载完成处开始读取
            /*
            hanel NIO中的用法，由于RandomAccessFile没有使用缓存策略，直接使用会使得下载速度变慢，亲测缓存下载3.3秒的文件，用普通的RandomAccessFile需要20多秒。
            FileChannel channelOut = raf.getChannel();
             内存映射，直接使用RandomAccessFile，是用其seek方法指定下载的起始位置，使用缓存下载，在这里指定下载位置。
            MappedByteBuffer mappedBuffer = channelOut.map(FileChannel.MapMode.READ_WRITE, _fileInfo.getFinished(), body.contentLength());
           **/
            // sink = Okio.sink(raf.);
            // bufferedSink = Okio.buffer;
            source = body.source();
            bufferedSource = Okio.buffer(source);
            byte[] b = new byte[1024 * 8];
            int len = 0;
            while ((len = bufferedSource.read(b)) != -1) {
                if (_fileInfo.isStop())
                    break;
                raf.write(b, 0, len);
                _fileInfo.addProgress(len);
            }
            _fileInfo.setStop(true);
            Logger.d("源文件大小/下载字节数：" + dest.length() + "/" + _fileInfo.getProgress());
            Logger.d("写入结束！！！！！");
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 另外开辟一条连接获取文件长度
     */
    public static void getFileLength(final FileInfo fileInfo) {
        Request request = new Request.Builder().url(fileInfo.getUrl()).build();
        OkHttpClient client = new OkHttpClient();
        ResponseBody responseBody = null;
        try {
            responseBody = client.newCall(request).execute().body();
            fileInfo.setLength((int) responseBody.contentLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



