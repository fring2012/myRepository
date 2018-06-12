package com.abupdate.iot_download_libs;

import android.os.Build;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.abupdate.trace.Trace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by raise.yang on 17/07/04.
 */
public class DownUtils {
    private static String TAG = "DownUtils";

    /**
     * 请求文件大小
     *
     * @param url
     * @return
     */
    public static long fetch_file_size(final String url) {
        if (TextUtils.isEmpty(url)) {
            return -1;
        }
        Integer length = 0;

        FutureTask futureTask = new FutureTask(new Callable() {
            @Override
            public Object call() throws Exception {

                URL downloadUrl = null;
                HttpURLConnection conn = null;
                try {
                    downloadUrl = new URL(url);
                    conn = (HttpURLConnection) downloadUrl.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                    Trace.e(TAG, e);
                    return -1;
                }
                conn.setConnectTimeout(DownConfig.CONNECT_TIMEOUT);
                conn.setReadTimeout(DownConfig.READ_TIMEOUT);
                return conn.getContentLength();
            }
        });
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(futureTask);
        try {
            length = (Integer) futureTask.get();
            Log.d(TAG, "fetch_file_size: "+length);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return length;
    }


    /**
     * 获得临时文件夹路径
     *
     * @param downEntity
     * @return
     */
    public static File get_temp_folder(DownEntity downEntity) {
        return new File(DLManager.getInstance().mCx.getFilesDir(), "temp_folder");
    }

    /**
     * 获得剩余磁盘空间大小;单位 B
     *
     * @param path
     * @return
     */
    public static long get_storage_free_size(String path) {
        File file = new File(path);
        if (!file.isDirectory()) {
            file = file.getParentFile();
        }
        StatFs statFs = null;
        try {
            statFs = new StatFs(file.getAbsolutePath());
        } catch (Exception e) {
            Trace.e(TAG, "get_storage_free_size() e = " + e);
            return -1;
        }
        long availableBlocksLong;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            availableBlocksLong = statFs.getAvailableBlocksLong();
        } else {
            availableBlocksLong = statFs.getAvailableBlocks();
        }

        long blockSizeLong;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSizeLong = statFs.getBlockSizeLong();
        } else {
            blockSizeLong = statFs.getBlockSize();
        }
        return availableBlocksLong * blockSizeLong;
    }

    //计算文件的md5
    public static String getMd5ByFile(File file) {
        if (file == null ||
                !file.exists())
            return "";
        FileInputStream fis = null;
        StringBuffer buf = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 256];
            int length = -1;
            // Trace.d(logTag, "getFileMD5, GenMd5 start");
            if (md == null) {
                return "";
            }
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] bytes = md.digest();
            if (bytes == null) {
                return "";
            }
            for (int i = 0; i < bytes.length; i++) {
                String md5s = Integer.toHexString(bytes[i] & 0xff);
                if (md5s == null) {
                    return "";
                }
                if (md5s.length() == 1) {
                    buf.append("0");
                }
                buf.append(md5s);
            }
            // Trace.d(logTag, "getFileMD5, GenMd5 success! spend the time: "+ (System.currentTimeMillis() - s) + "ms");
            String value = buf.toString();
            int fix_num = 32 - value.length();
            for (int i = 0; i < fix_num; i++) {
                value = "0" + value;
            }
            return value;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param dirPath 目录路径
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(String dirPath) {
        if (isSpace(dirPath)) {
            return false;
        } else {
            File path = new File(dirPath);
            return path != null && (path.exists() ? path.isDirectory() : path.mkdirs());
        }
    }

    /**
     * 用于判断指定字符是否为空白字符，空白符包含：空格、tab键、换行符。
     *
     * @param s
     * @return
     */
    private static boolean isSpace(String s) {
        if (s == null) {
            return true;
        }
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //获取下载进度
    public static long retrieve_down_length(File dir, String fileName) {
        File temp_file = new File(dir, fileName);
        if (!temp_file.exists()) {
            return 0;
        }
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(temp_file));
            return Long.parseLong(bufferedReader.readLine());
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //保存下载进度
    public static void save_down_length(File dir, String fileName, long length) {
        File temp_file = new File(dir, fileName);
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(temp_file));
            writer.write(String.valueOf(length));
            //            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //        RandomAccessFile raf2 = null;
        //        try {
        //            raf2 = new RandomAccessFile(
        //                    temp_file.getAbsolutePath(),
        //                    "rwd");
        //            raf2.write(String.valueOf(downing_length)
        //                    .getBytes());
        //            raf2.close();
        //        } catch (Exception e) {
        //            e.printStackTrace();
        //        }
    }

    /**
     * 递归删除文件夹 要利用File类的delete()方法删除目录时， 必须保证该目录下没有文件或者子目录，否则删除失败，
     * 因此在实际应用中，我们要删除目录， 必须利用递归删除该目录下的所有子目录和文件， 然后再删除该目录。
     *
     * @param path
     */
    public static boolean delDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] tmp = dir.listFiles();
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].isDirectory()) {
                    delDir(path + "/" + tmp[i].getName());
                } else {
                    tmp[i].delete();
                }
            }
           return dir.delete();
        }
        return false;
    }

    public static void closeIO(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
