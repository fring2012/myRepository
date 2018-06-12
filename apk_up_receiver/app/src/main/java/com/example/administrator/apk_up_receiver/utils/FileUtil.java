package com.example.administrator.apk_up_receiver.utils;


import android.util.Log;

import com.abupdate.trace.Trace;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;



public class FileUtil {

    private static final String TAG = "FileUtil";

    /**
     * 解压缩文件
     * @param zipFile
     * @param targetDir
     * @return
     */
    public static String UnzipSingleFile(String zipFile ,  String targetDir){
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        String apkPath = targetDir + "update.apk";
        deleteFile(apkPath);
        try {
            BufferedOutputStream dest = null; //缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

            ZipEntry  entry = zis.getNextEntry();
            if (entry != null) {
                try {
                    int count;
                    byte data[] = new byte[BUFFER];
                    File entryFile = new File(apkPath);
                    File entryDir = new File(entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
        }
        return apkPath;
    }


    public static List<String> Unzip(String zipFile, String targetDir) {
        int BUFFER = 4096; //这里缓冲区我们使用4KB，
        String strEntry; //保存每个zip的条目名称
        List<String> fileNameList = new ArrayList<String>();
        String apkPath = targetDir + "update.apk";
        deleteFile(apkPath);
        try {
            BufferedOutputStream dest = null; //缓冲输出流
            FileInputStream fis = new FileInputStream(zipFile);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry; //每个zip条目的实例

            if ((entry = zis.getNextEntry()) != null) {

                try {
//                    Trace.d(TAG,"Unzip="+ entry);
                    int count;
                    byte data[] = new byte[BUFFER];
                    strEntry = entry.getName();

                    File entryFile = new File(targetDir + strEntry);
                    fileNameList.add(strEntry);
     //               Trace.d(TAG, targetDir + strEntry);
                    File entryDir = new File(entryFile.getParent());
         //           Trace.d(TAG, entryFile.getParent());
                    if (!entryDir.exists()) {
                        entryDir.mkdirs();
                    }

                    FileOutputStream fos = new FileOutputStream(entryFile);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = zis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            zis.close();
        } catch (Exception cwj) {
            cwj.printStackTrace();
        }
        return fileNameList;
    }
    public static  String getHashByFile(File file) {
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
     * 设置文件的读写权限
     * @param permission
     * @param filePath
     */
    public static void setFileRWX(String permission,String filePath){
        String[] command = {"chmod", permission, filePath };
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文件是否存在
     * @param file
     * @return
     */
    public static boolean isExists(File file){
        if (file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return false;
        }
        return true;
    }



    /**
     * 文件路径是否合法
     * @param filePath
     * @return
     */
    public static boolean pathVerify(String filePath){
        if (filePath == null || filePath.length() == 0 ) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param filePath
     * @return
     */
    public static File getFile(String filePath){
        if(!pathVerify(filePath)){
            return null;
        }
        return new File(filePath);
    }

    public static void deleteFile(String filePath){
        File file = getFile(filePath);
        if(isExists(file)) {
            file.delete();
        }
    }

    /**
     * 关闭流
     * @param closeables
     */
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
