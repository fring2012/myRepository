package com.abupdate.iot_libs.engine;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.StatFs;

import com.abupdate.iot_libs.utils.FileUtil;
import com.abupdate.trace.Trace;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author fighter_lee
 * @date 2018/2/12
 */
public class UnitProvide {

    private static UnitProvide mInstance;

    public static UnitProvide getInstance() {
        if (mInstance == null) {
            mInstance = new UnitProvide();
        }
        return mInstance;
    }

    public static void setInstance(UnitProvide instance) {
        mInstance = instance;
    }

    public Calendar getCalendar() {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
        Trace.d("xxxxx", "getCalendar() " + (calendar == null));
        return calendar;
    }

    /**
     * 下载对剩余空间的要求
     *
     * @param path
     * @return 需要的空间size
     */
    public long getStorageSpace(String path) {
        File file = new File(path);
        String verify_path = file.getParentFile().getAbsolutePath();
        boolean existsDir = FileUtil.createOrExistsDir(verify_path);
        if (!existsDir) {
            //文件路径不合法
            throw new IllegalArgumentException("Invalid path:" + path);
        }
        StatFs statfs = new StatFs(verify_path);
        long blockSize = 0;
        long blockCount = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statfs.getBlockSizeLong();
            blockCount = statfs.getAvailableBlocksLong();
        } else {
            blockSize = statfs.getBlockSize();
            blockCount = statfs.getBlockCount();
        }
        return blockSize * blockCount;
    }

    /**
     * 获取当前电量
     *
     * @param context
     * @return
     */
    public int getBatteryLevel(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = context.registerReceiver(null, filter);
        int batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        return batteryLevel;
    }

}
