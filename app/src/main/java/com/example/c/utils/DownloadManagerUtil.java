package com.example.c.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import com.example.c.been.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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





    public static String FILE_PATH = Environment.getExternalStorageDirectory() + "/azhong";//文件下载保存路径
    private DbHelper helper;//数据库帮助类
    private SQLiteDatabase db;
    private OnProgressListener listener;//进度回调监听
    private Map<String, FileInfo> map = new HashMap<>();//保存正在下载的任务信息
    private static DownloadManagerUtil manger;

    private DownloadManagerUtil(DbHelper helper, OnProgressListener listener) {
        this.helper = helper;
        this.listener = listener;
        db = helper.getReadableDatabase();
    }

    /**
     * 单例模式
     *
     * @param helper   数据库帮助类
     * @param listener 下载进度回调接口
     * @return
     */
    public static synchronized DownloadManagerUtil getInstance(DbHelper helper, OnProgressListener listener) {
        if (manger == null) {
            synchronized (DownloadManagerUtil.class) {
                if (manger == null) {
                    manger = new DownloadManagerUtil(helper, listener);
                }
            }
        }
        return manger;
    }

    /**
     * 开始下载任务
     */
    public void start(String url) {
        db = helper.getReadableDatabase();
        FileInfo info = helper.queryData(db, url);
        map.put(url, info);
        //开始任务下载
        new DownLoadTask(map.get(url), helper, listener).start();
    }

    /**
     * 停止下载任务
     */
    public void stop(String url) {
        map.get(url).setStop(true);
    }

    /**
     * 重新下载任务
     */
    public void restart(String url) {
        stop(url);
        try {
            File file = new File(FILE_PATH, map.get(url).getFileName());
            if (file.exists()) {
                file.delete();
            }
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        db = helper.getWritableDatabase();
        helper.resetData(db, url);
        start(url);
    }

    /**
     * 获取当前任务状态
     */
    public boolean getCurrentState(String url) {
        return map.get(url).isDownLoading();
    }

    /**
     * 添加下载任务
     *
     * @param info 下载文件信息
     */
    public void addTask(FileInfo info) {
        //判断数据库是否已经存在这条下载信息
        if (!helper.isExist(db, info)) {
            db = helper.getWritableDatabase();
            helper.insertData(db, info);
            map.put(info.getUrl(), info);
        } else {
            //从数据库获取最新的下载信息
            db = helper.getReadableDatabase();
            FileInfo o = helper.queryData(db, info.getUrl());
            if (!map.containsKey(info.getUrl())) {
                map.put(info.getUrl(), o);
            }
        }
    }
}
