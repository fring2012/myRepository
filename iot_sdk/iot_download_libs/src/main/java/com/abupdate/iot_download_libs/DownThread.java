package com.abupdate.iot_download_libs;

import android.text.TextUtils;

import com.abupdate.trace.Trace;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 最小的下载任务线程类
 * Created by raise.yang on 17/07/03.
 */

public class DownThread implements Runnable {

    private static final String TAG = "DownThread";
    public final DownEntity downEntity;
    private final long start;//下载起点
    public final long length;//下载长度
    private int segmentDownloadNum;//块下载次数
    private int segmentNum;//第n块
    private String md5;//md5256

    private File temp_folder;//临时文件夹
    private String temp_record_name;//临时记录文件名
    private String temp_file_name;//块文件名

    public long downing_length;//已经下载的长度

    private boolean is_finished;
    private boolean downWithSegment = false;

    public DownThread(DownEntity downEntity, long start, long length, int num, String md5) {
        this.downEntity = downEntity;
        this.start = start;
        this.length = length;
        this.md5 = md5;
        this.segmentNum = num;
        temp_folder = DownUtils.get_temp_folder(downEntity);
        temp_folder.mkdirs();
        //文件名+起点
        temp_record_name = new File(downEntity.file_path).getName() + "record" + +start;
        if (!TextUtils.isEmpty(md5) && downEntity.getSegmentDownInfos().size() > 0) {
            temp_file_name = new File(downEntity.file_path).getName() + segmentNum;
            downWithSegment = true;
            segmentDownloadNum = DownConfig.SEGMENT_DOWNLOAD_RETRY_TIME;
        }

        //读取已经下载的长度
        downing_length = DownUtils.retrieve_down_length(temp_folder, temp_record_name);
        if (downing_length >= length) {
            is_finished = true;
            return;
        }
    }

    public DownThread(DownEntity downEntity, long start, long length) {
        this(downEntity, start, length, -1, "");
        //        Trace.d("DownThread", "下载长度start = " + start + " length = " + length + " 长度已下载 = " + downing_length);
    }

    @Override
    public void run() {
        if (is_finished || downEntity.download_cancel || downing_length == length || downEntity.download_status != DownError.NO_ERROR) {
            is_finished = true;
            return;
        }
        //下载重试机制
        int max_retry = DownConfig.RETRY_TIMES_MAX;
        for (int i = 0; i < max_retry; i++) {
            if (downloadTask()) {
                break;
            }
            if (i == max_retry - 1) {
                //如果是最后一次重试
//                if (DownError.NO_ERROR == downEntity.download_status) {
//                    downEntity.download_status = DownError.ERROR_NET_WORK;
//                }
                break;
            }
            try {
                Thread.sleep(DownConfig.RETRY_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        is_finished = true;
        //        if (downEntity.download_status!= DownError.NO_ERROR){
        //            //当前线程下载失败，请求其他线程停止下载
        //            DLManager.getInstance().cancel_all();
        //        }
//        Trace.d("DownThread", "down end.file_size=" + downEntity.file_size + ",start=" + start + ",length=" + length + ",downing_length=" + downing_length);
    }

    private boolean downloadTask() {
        try {
            download();
            return true;
        } catch (IOException e) {
            Trace.e(TAG, "downloadTask() e = ", e);
            downEntity.download_status = DownError.ERROR_NET_WORK;
        } catch (DownloadException e) {
            Trace.e(TAG, "downloadTask() e = ", e);
            if (e.getReasonCode() == DownError.ERROR_BLOCK_VERIFY_FAIL) {
                delete_temp_record_file();
                delete_temp_block_file();
                if (segmentDownloadNum >= 1) {
                    //单块下载失败，单块重置重新下载
                    downing_length = 0;
                    return downloadTask();
                } else {
                    downEntity.download_status = DownError.ERROR_BLOCK_VERIFY_FAIL;
                }
            }
        } catch (Exception e) {
            Trace.e(TAG, "downloadTask() e = ", e);
            downEntity.download_status = DownError.ERROR_NET_WORK;
        }
//        Trace.d(TAG, "downloadTask() start:"+start);
        return false;
    }

    private void download() throws IOException, DownloadException {

        URL downloadUrl = null;
        HttpURLConnection conn = null;
        downloadUrl = new URL(downEntity.url);
        conn = (HttpURLConnection) downloadUrl.openConnection();
        conn.setConnectTimeout(DownConfig.CONNECT_TIMEOUT);
        conn.setReadTimeout(DownConfig.READ_TIMEOUT);
        if (DownConfig.SSL != null)//设置https
        {
            HttpsURLConnection.setDefaultSSLSocketFactory(DownConfig.SSL);
        }

        long block_offset = start + downing_length;
        long block_size = length - downing_length;

        //注意range是包头包尾的，所以这里需要-1
        conn.setRequestProperty("Range", "bytes=" + block_offset + "-" + (block_offset + block_size - 1));
        int response_code = conn.getResponseCode();
        if (response_code == HttpURLConnection.HTTP_OK ||
                response_code == HttpURLConnection.HTTP_PARTIAL) {

            if (downWithSegment) {
                //分块下载
                InputStream is = conn.getInputStream();
                writeBlockFile(is, block_offset);
            } else {
                InputStream is = conn.getInputStream();
                writeDownloadFile(new File(downEntity.file_path),is, block_offset);
            }
        }else{
            Trace.e(TAG, "download() response code error:"+response_code);
        }
    }

    private void writeDownloadFile(File desFile,InputStream is, long block_offset) throws IOException {
        //直接写
        RandomAccessFile raf = null;
        raf = new RandomAccessFile(desFile, "rw");
        //定位写入起点
        raf.seek(block_offset);

        byte[] buffer = new byte[100 * 1024];//100kb
        int len = -1;
        try {
            while ((len = is.read(buffer)) != -1) {
                if (downEntity.download_cancel) {
//                    Trace.i(TAG, "线程%s取消下载,已下载: %s", Thread.currentThread().getName(),downing_length);
                    break;
                }

                raf.write(buffer, 0, len);
                downing_length += len;

                if (downing_length > length){
                    break;
                }
            }
        } finally {
            DownUtils.save_down_length(temp_folder, temp_record_name, downing_length);
            FileDescriptor fd = raf.getFD();
            fd.sync();
            DownUtils.closeIO(is, raf);
        }
    }

    private void writeBlockFile(InputStream is, long block_offset) throws IOException, DownloadException {
        //将文件下入临时文件
        File file = new File(temp_folder, temp_file_name);
        writeDownloadFile(file,is,downing_length);

        //校验 --> 合并
        if (!downEntity.download_cancel) {
            Trace.i(TAG, "writeBlockFile() file md5:" + DownUtils.getMd5ByFile(new File(temp_folder, temp_file_name)) + ",md5:" + md5);
            if (TextUtils.equals(DownUtils.getMd5ByFile(new File(temp_folder, temp_file_name)), md5)) {
                //校验通过，正式写文件
                FileInputStream fis = new FileInputStream(new File(temp_folder, temp_file_name));
                insertToUpdateFile(fis);
                delete_temp_block_file();//合入文件后，删除块文件
            } else {
                //校验失败
                segmentDownloadNum--;
                throw new DownloadException(DownError.ERROR_BLOCK_VERIFY_FAIL);
            }

        }
    }

    //将块合并
    private void insertToUpdateFile(InputStream is) throws IOException {
        //直接写
        RandomAccessFile raf = null;
        raf = new RandomAccessFile(downEntity.file_path, "rw");
        //定位写入起点
        raf.seek(start);

        byte[] buffer = new byte[100 * 1024];//100kb
        int len = -1;
        try {
            while ((len = is.read(buffer)) != -1) {
                raf.write(buffer, 0, len);
            }
        } finally {
            FileDescriptor fd = raf.getFD();
            fd.sync();
            DownUtils.closeIO(is, raf);
        }
    }

    public boolean is_finished() {
        return is_finished || downEntity.download_cancel;
    }

    public void set_download_finished(boolean finished) {
        if (finished) {
            downing_length = length;
            downEntity.download_status = DownError.NO_ERROR;
            is_finished = true;
        } else {
            //            downing_length = 0;
            downEntity.download_status = DownError.NO_ERROR;
            is_finished = false;
        }
    }

    //删除临时断点记录文件
    public void delete_temp_record_file() {
        File temp_file = new File(temp_folder, temp_record_name);
        temp_file.delete();
    }

    //删除块文件
    public void delete_temp_block_file() {
        File file = new File(temp_folder, temp_file_name);
        file.delete();
    }
}
