package com.abupdate.sota.utils;

import android.text.TextUtils;

import com.abupdate.trace.Trace;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类，用于验证文件的 md5<p/>
 */
public class FileUtil {

    public static String getMd5ByFile(File file) {
        FileInputStream fis = null;
        StringBuffer buf = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 256];
            int length = -1;
            // Trace.d(logTag, "getFileMD5, GenMd5 start");
            long s = System.currentTimeMillis();
            if (fis == null || md == null) {
                return null;
            }
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            byte[] bytes = md.digest();
            if (bytes == null) {
                return null;
            }
            for (int i = 0; i < bytes.length; i++) {
                String md5s = Integer.toHexString(bytes[i] & 0xff);
                if (md5s == null || buf == null) {
                    return null;
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
            Trace.d("FileUtil", "getFileMD5, Exception " + ex.toString());
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


    public static String getMd5ByFile(String filePath) {
        File fd = new File(filePath);
        if (fd.exists()) {
            return getMd5ByFile(fd);
        }
        return "";
    }

    /**
     * @param filePath
     * @param md5sum   not null
     * @return 验证是否通过
     */
    public static boolean validateFile(String filePath, String md5sum) {
        if (TextUtils.isEmpty(filePath) || TextUtils.isEmpty(md5sum)) {
            return false;
        }
        String md5_file = getMd5ByFile(filePath);
        Trace.i("FileUtil", "validateFile() " + md5sum.equals(md5_file) + "md5_file = " + md5_file + " md5_net = " + md5sum);
        return md5sum.equals(md5_file);
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
     * 递归删除文件夹 要利用File类的delete()方法删除目录时， 必须保证该目录下没有文件或者子目录，否则删除失败，
     * 因此在实际应用中，我们要删除目录， 必须利用递归删除该目录下的所有子目录和文件， 然后再删除该目录。
     *
     * @param path
     */
    public static void delFileInDir(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] tmp = dir.listFiles();
            for (int i = 0; i < tmp.length; i++) {
                if (tmp[i].isDirectory()) {
                    delFileInDir(path + "/" + tmp[i].getName());
                } else {
                    tmp[i].delete();
                }
            }
        }
    }

    public static String convertFileSize(long size) {
        float result = size;
        String suffix = "B";
        if (result > 1024) {
            suffix = "KB";
            result = result / 1024;
        }
        if (result > 1024) {
            suffix = "MB";
            result = result / 1024;
        }
        if (result > 1024) {
            suffix = "GB";
            result = result / 1024;
        }
        if (result > 1024) {
            suffix = "TB";
            result = result / 1024;
        }

        final String roundFormat;
        if (result < 10) {
            roundFormat = "%.2f";
        } else if (result < 100) {
            roundFormat = "%.1f";
        } else {
            roundFormat = "%.0f";
        }
        return String.format(roundFormat + suffix, result);
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

    public static boolean fileRename(String srcFile, String desFile) {
        File src = new File(srcFile);
        File des = new File(desFile);
        return src.renameTo(des);
    }

    /**
     * 压缩文件
     *
     * @param resFilePath 待压缩文件路径
     * @param zipFilePath 压缩文件路径
     * @return {@code true}: 压缩成功<br>{@code false}: 压缩失败
     * @throws IOException IO 错误时抛出
     */
    public static boolean zipFile(File resFilePath, File zipFilePath)
            throws IOException {
        return zipFile(resFilePath, zipFilePath, null);
    }

    public static boolean zipFile(InputStream resFileStream,File zipFilePath,String rootPath) throws IOException {
        InputStream is = null;
        ZipOutputStream zos = null;
        try {
            is = new BufferedInputStream(resFileStream);
            zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
            ZipEntry entry = new ZipEntry(rootPath);
            zos.putNextEntry(entry);
            byte buffer[] = new byte[1024];
            int len;

            while ((len = is.read(buffer)) != -1) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        } finally {
            closeIO(is);
        }

        if (zipFilePath.exists()){
            return true;
        }
        return false;
    }

    /**
     * 压缩文件
     *
     * @param resFile 待压缩文件
     * @param zipFile 压缩文件
     * @param comment 压缩文件的注释
     * @return {@code true}: 压缩成功<br>{@code false}: 压缩失败
     * @throws IOException IO 错误时抛出
     */
    public static boolean zipFile(final File resFile,
                                  final File zipFile,
                                  final String comment)
            throws IOException {
        if (resFile == null || zipFile == null)
            return false;
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(new FileOutputStream(zipFile));
            return zipFile(resFile, "", zos, comment);
        } finally {
            if (zos != null) {
                closeIO(zos);
            }
        }
    }

    /**
     * 压缩文件
     *
     * @param resFile  待压缩文件
     * @param rootPath 相对于压缩文件的路径
     * @param zos      压缩文件输出流
     * @param comment  压缩文件的注释
     * @return {@code true}: 压缩成功<br>{@code false}: 压缩失败
     * @throws IOException IO 错误时抛出
     */
    private static boolean zipFile(final File resFile,
                                   String rootPath,
                                   final ZipOutputStream zos,
                                   final String comment)
            throws IOException {
        rootPath = rootPath + (isSpace(rootPath) ? "" : File.separator) + resFile.getName();
        if (resFile.isDirectory()) {
            File[] fileList = resFile.listFiles();
            // 如果是空文件夹那么创建它，我把'/'换为File.separator测试就不成功，eggPain
            if (fileList == null || fileList.length <= 0) {
                ZipEntry entry = new ZipEntry(rootPath + '/');
                entry.setComment(comment);
                zos.putNextEntry(entry);
                zos.closeEntry();
            } else {
                for (File file : fileList) {
                    // 如果递归返回 false 则返回 false
                    if (!zipFile(file, rootPath, zos, comment))
                        return false;
                }
            }
        } else {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(resFile));
                ZipEntry entry = new ZipEntry(rootPath);
                entry.setComment(comment);
                zos.putNextEntry(entry);
                byte buffer[] = new byte[1024];
                int len;

                while ((len = is.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            } finally {
                closeIO(is);
            }
        }
        return true;
    }
}
