package com.example.administrator.apk_up_receiver.engine;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.v4.content.FileProvider;

import android.util.Pair;


import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.IDownSimpleListener;
import com.abupdate.iot_libs.policy.PolicyManager;
import com.abupdate.trace.Trace;
import com.example.administrator.apk_up_receiver.utils.FileUtil;
import com.example.administrator.apk_up_receiver.utils.NetUtil;
import com.example.administrator.apk_up_receiver.utils.ShellUtils;
import com.example.administrator.apk_up_receiver.utils.ShellUtils.CommandResult;

import java.io.File;



public class ApkUpEngine implements Runnable {
    private static final String TAG = "ApkUpEngine";
    private static ApkUpEngine mInstance;
    private Context mCx;
    private boolean single = true;

    public static ApkUpEngine getInstance(){
        if(mInstance == null){
            synchronized (ApkUpEngine.class){
                if (mInstance == null){
                    mInstance = new ApkUpEngine();
                }
            }
        }
        return mInstance;
    }

    public void update(Context context){
        Trace.d(TAG,"update(Context context)");
        if (single) {
            single = false;
            mCx = context;
            new Thread(this).start();
        }
    }

    @Override
    public void run()  {
        Pair<Integer,VersionInfo> versionInfoPair = OtaAgentPolicy.checkVersionExecute();
        if(versionInfoPair.first != 1000){
            Trace.d(TAG,"versionInfoPair.first != 1000");
            single = true;
            return;
        }
        if (!PolicyManager.INSTANCE.isDownloadForce()){
            if(PolicyManager.INSTANCE.is_request_wifi() && !NetUtil.isWifiConnect(mCx)){
                single = true;
                return;
            }
        }
        if (!PolicyManager.INSTANCE.is_storage_space_enough(OtaAgentPolicy.getConfig().updatePath)) {
            //最小升级空间策略：空间不足 -> 不下载
            single = true;
            return;
        }
        download();
        single = true;
    }

    public void download(){
       OtaAgentPolicy.downloadExecute(new IDownSimpleListener() {
           @Override
           public void onCompleted(File file) {
               Trace.d(TAG,"onCompleted(File file)");

               if(!FileUtil.isExists(file)){
                   return;
               }

               if(!PolicyManager.INSTANCE.is_battery_enough(mCx)){
                   Trace.d(TAG,"配置了最低电量策略：电量不足 -> 不升级");
                   return;
               }

               String filePath = file.getAbsolutePath();
               String apkParentPath = file.getParentFile().getAbsolutePath() + File.separator + "apk" + File.separator;
               String apkPath = FileUtil.UnzipSingleFile(filePath,apkParentPath);

               if(VERSION.SDK_INT < VERSION_CODES.M){
                   //系统版本小于6.0，默认下载在包目录下，需要修改文件的读写权限才能进行安装apk
                   FileUtil.setFileRWX("775",apkParentPath);
                   FileUtil.setFileRWX("775",apkPath);
               }

               Trace.d(TAG,apkPath);

               if(PolicyManager.INSTANCE.is_force_install()){
                   Trace.d(TAG,"强制安装");
                   rebootUpdate(apkPath);
               }else {
                   Trace.d(TAG,"普通安装");
                   update(apkPath);
               }
           }
           @Override
           public void onFailed(int error) {
               super.onFailed(error);
           }
       });

    }



    /**
     * 普通升级
     * @param apkPath
     */
    private void update(String apkPath){
        Trace.d(TAG,"update(String apkPath)");
        File apkFile = FileUtil.getFile(apkPath);
        if(!FileUtil.isExists(apkFile)){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Trace.d(TAG,apkPath);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(
                    mCx
                    , "com.example.administrator.apk_up_receiver.fileprovider"
                    , apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mCx.startActivity(intent);
    }



    /**
     * /静默安装升级，需要root权限或者与系统相同的userid
     * @param apkPath
     */
    private void rebootUpdate(String apkPath){
        Trace.d(TAG,"rebootUpdate(String apkPath)");
        File apkFile = FileUtil.getFile(apkPath);
        if(!FileUtil.isExists(apkFile)){
            return;
        }
        //FileUtil.setFileRWX("775",apkPath);
        String packName = mCx.getPackageName();
        String command = null;
        if(VERSION.SDK_INT >= VERSION_CODES.N){
            update(apkPath);
//            command = "pm  install -i " + packName + " --user 0 -r " + apkPath;
        }else{
            command = "pm install -r "+ apkPath;
        }

        Trace.d(TAG,command);
        CommandResult cr = ShellUtils.execCmd(command, false);

        Trace.e("test-test", "successMsg:" + cr.successMsg + ", ErrorMsg:" + cr.errorMsg);
    }


}
