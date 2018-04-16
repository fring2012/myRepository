package com.example.c.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

public class DownloadManagerUtil {

    //利用android自带的下载器下载
    public static void downloadAPK(String url,Context context,String filename){
        Uri uri = Uri.parse(url);
        String[] bakUrlSplit = url.split("/");
        //https://dl.pstmn.io/download/latest/win64
        DownloadManager.Request request = new DownloadManager.Request(uri);;
        request.setDestinationInExternalPublicDir("/download/",bakUrlSplit[bakUrlSplit.length-1]);
        //获取下载管理器
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载任务加入下载队列，否则不会进行下载
        downloadManager.enqueue(request);

    }

    //使用OkHttp下载文件
    public static void downloadFile(final String url, Context context, String fileName){
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
                File dest = new File(mSDCardPath,url.substring(url.lastIndexOf("/")+1));
                sink = Okio.sink(dest);
                bufferedSink = Okio.buffer(sink);
                bufferedSink.writeAll(response.body().source());

                bufferedSink.close();

            }
        });
    }
}
