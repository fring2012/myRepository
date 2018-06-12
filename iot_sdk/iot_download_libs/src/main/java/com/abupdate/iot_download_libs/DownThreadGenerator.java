package com.abupdate.iot_download_libs;

import com.abupdate.iot_download_libs.segmentDownload.SegmentDownInfo;
import com.abupdate.trace.Trace;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by raise.yang on 17/08/01.
 */

public class DownThreadGenerator {

    private static String TAG = "DownThreadGenerator";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    /**
     * 给定一个下载实体，返回对应的DownThread
     *
     * @param downEntity
     * @return
     */
    public static List<DownThread> gen_threads(DownEntity downEntity) {

        List<DownThread> downThreadList = new ArrayList<>();
        File downFile = new File(downEntity.file_path);

        //判断下载路径
        File parentFile = downFile.getParentFile();
        boolean orExistsDir = DownUtils.createOrExistsDir(parentFile.getAbsolutePath());
        if (!orExistsDir) {
            DownThread downThread = new DownThread(downEntity, 0, downEntity.file_size);
            downThread.downEntity.download_status = DownError.ERROR_FILE_IO_EXCEPTION;
            downThreadList.add(downThread);
            return downThreadList;
        }

        //请求文件大小
        if (downEntity.file_size == 0) {
            //若当前没有设置file_size
            fetch_file_size(downEntity);
        }
        if (downEntity.file_size <= 0) {
            //文件大小获取失败，创建一个失败的线程
            DownThread downThread = new DownThread(downEntity, 0, downEntity.file_size);
            downThread.downEntity.download_status = DownError.ERROR_FETCH_FILE_SIZE;
            Trace.w(TAG, "gen_down_thread() url = %s read size error. Auto remove this task.", downEntity.url);
            downThreadList.add(downThread);
            return downThreadList;
        }

        if (downFile.exists()) {
            if (DownUtils.getMd5ByFile(new File(downEntity.file_path)).equalsIgnoreCase(downEntity.md5)) {
                // 文件存在并下载已完成
                DownThread downThread = new DownThread(downEntity, 0, downEntity.file_size);
                downThread.set_download_finished(true);
                downThreadList.add(downThread);
                return downThreadList;
            }
            if (downFile.length() != downEntity.file_size) {
                //文件大小不一致，已经不是之前的文件
                downFile.delete();
            }
        }

        if (!downFile.exists()) {
            // 文件不存在，创建文件
            long free_size = DownUtils.get_storage_free_size(downEntity.file_path);
            if (free_size != -1
                    && free_size < downEntity.file_size * 1.1) {
                //存储空间不够
                DownThread downThread = new DownThread(downEntity, 0, downEntity.file_size);
                downThread.downEntity.download_status = DownError.ERROR_FILE_IO_EXCEPTION;
                downThreadList.add(downThread);
                return downThreadList;
            }

            DLManager.getInstance().deleteTempFile(downEntity);

            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(downEntity.file_path, "rw");
                raf.setLength(downEntity.file_size);
            } catch (Exception e) {
                Trace.e(TAG, "gen_down_thread() e = " + e);
                // 创建文件失败
                DownThread downThread = new DownThread(downEntity, 0, downEntity.file_size);
                downThread.downEntity.download_status = DownError.ERROR_FILE_IO_EXCEPTION;
                downThreadList.add(downThread);
                return downThreadList;
            } finally {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (DownConfig.sSegmentDownload
                && null != downEntity.getSegmentDownInfos()
                && downEntity.getSegmentDownInfos().size() > 0) {
            for (SegmentDownInfo segmentDownInfo : downEntity.getSegmentDownInfos()) {
                DownThread downThread = new DownThread(downEntity, segmentDownInfo.getStartpos(), segmentDownInfo.getEndpos() - segmentDownInfo.getStartpos(), segmentDownInfo.getNum(), segmentDownInfo.getMd5());
                downThreadList.add(downThread);
            }
        } else {
            //单独处理文件大小小于最小分块大小情况
            if (downEntity.file_size < DownConfig.THREAD_BLOCK_CELL_MIN) {
                downThreadList.add(new DownThread(downEntity, 0, downEntity.file_size));
                return downThreadList;
            }

            //块大小
            long block_size = downEntity.file_size / CPU_COUNT;
            //块大小必须在min和max之间
            block_size = block_size < DownConfig.THREAD_BLOCK_CELL_MIN ?
                    DownConfig.THREAD_BLOCK_CELL_MIN : block_size > DownConfig.THREAD_BLOCK_CELL_MAX ? DownConfig.THREAD_BLOCK_CELL_MAX : block_size;
            long count = downEntity.file_size / block_size;
            //若最后一块大小大于块大小的1/10，则多分一块
            if (downEntity.file_size % block_size > block_size / 10) {
                count++;
            }
            List<DownThread> temp_thread_list = new ArrayList();
            for (int i = 0; i < count; i++) {
                long down_length = i == count - 1 ? downEntity.file_size - i * block_size : block_size;
                DownThread downThread = new DownThread(downEntity, i * block_size, down_length);
                temp_thread_list.add(downThread);
            }
            downThreadList.addAll(temp_thread_list);
        }

        return downThreadList;
    }

    private static void fetch_file_size(DownEntity downEntity) {
        downEntity.file_size = DownUtils.fetch_file_size(downEntity.url);
    }

}
