package com.abupdate.iot_libs.engine;


import android.text.TextUtils;
import android.util.Log;

import com.abupdate.iot_download_libs.CallBackManager;
import com.abupdate.iot_download_libs.DLManager;
import com.abupdate.iot_download_libs.DownConfig;
import com.abupdate.iot_download_libs.DownEntity;
import com.abupdate.iot_download_libs.DownError;
import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.constant.Error;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.ProductInfo;
import com.abupdate.iot_libs.info.RegisterInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.IDownSimpleListener;
import com.abupdate.iot_libs.interact.HttpTools;
import com.abupdate.iot_libs.report.ReportManager;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.BeanUtils;
import com.abupdate.iot_libs.utils.FileUtil;
import com.abupdate.iot_libs.utils.JsonAnalyticsUtil;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.trace.Trace;

import java.io.File;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author fighter_lee
 * @date 2017/12/19
 * OTA 同步请求
 */
public class OTAExecuteManager {

    private static OTAExecuteManager mInstance;
    private static final String TAG = "OTAExecuteManager";

    public static OTAExecuteManager getInstance() {
        if (mInstance == null) {
            synchronized (OTAExecuteManager.class) {
                if (mInstance == null) {
                    mInstance = new OTAExecuteManager();
                }
            }
        }
        return mInstance;
    }

    public String checkVersion() throws FotaException {
        if (!DeviceInfo.getInstance().isValid()) {
            Trace.e(TAG, "check_version_task() device info is invalid!");
            throw new FotaException(Error.DEVICE_INFO_INIT_FAILED);
        }
        if (!RegisterInfo.getInstance().isValid() || !ProductInfo.getInstance().isProductValid()) {
            Trace.d(TAG, "check_version_task() start register!");
            //注册
            int registerCode = registerExecute();
            if (JsonAnalyticsUtil.SUCCESS != registerCode
                    || !RegisterInfo.getInstance().isValid()) {
                throw new FotaException(registerCode);
            }
        }
        FutureTask<String> checkVersionFutureTask = FutureTaskPool.getInstance().executeTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String version = HttpTools.getInstance().checkVersion(DeviceInfo.getInstance(), OtaAgentPolicy.sCx);
                return version;
            }
        });
        String version = "";
        try {
            version = checkVersionFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Trace.e(TAG, e);
        }
        return version;
    }

    public int checkVersionExecute() {
        String version = "";
        try {
            version = checkVersion();
        } catch (FotaException e) {
            e.printStackTrace();
            return e.getReasonCode();
        }
        if (TextUtils.isEmpty(version)) {
            //服务器没有返回数据
            Trace.e(TAG, "check_version_task() json is null!");
            return Error.SERVER_DATA_ERROR;
        }

        int i = JsonAnalyticsUtil.versionJson(version);

        if (JsonAnalyticsUtil.isSuccess(i)) {
            BeanUtils.setVersionInfo(version);
        }

        if (i == Error.REGISTER_SIGN_ERROR || i == Error.CHECK_DEVICE_IS_NOT_REGISTER) {
            RegisterInfo.getInstance().reset();
        }
        return i;
    }

    public boolean downloadExecute(IDownSimpleListener listener, boolean main) {
        //文件下载信息downEntity
        DownEntity downEntity = downloadPrepare();
        CallBackManager.getInstance().setListener(listener);
        DLManager.getInstance().setCallbackOnUIThread(main);

        //*****************************
        if(new File(OtaAgentPolicy.config.updatePath).exists()) {
            new File(OtaAgentPolicy.config.updatePath).delete();
            Trace.d(TAG,"文件位置：" + OtaAgentPolicy.config.updatePath);
        }

        //文件存在且下载正确
        if (new File(OtaAgentPolicy.config.updatePath).exists() && FileUtil.validateFile(OtaAgentPolicy.config.updatePath, VersionInfo.getInstance().md5sum)) {
            ReportManager.getInstance(OtaAgentPolicy.sCx).reportDownParamInfo(0, Utils.getSecondTime(), "");
            CallBackManager.getInstance().on_success(downEntity);
            return true;
        }
        DLManager.getInstance().add(downEntity);
        long downloadStartTime = Utils.getSecondTime();
        boolean execute = DLManager.getInstance().execute(listener);
        if (execute) {
            ReportManager.getInstance(OtaAgentPolicy.sCx).reportDownParamInfo(downEntity.download_status, downloadStartTime, "");
            return downEntity.download_status == DownError.NO_ERROR;
        }
        return false;
    }

    public boolean downloadExecute(IDownSimpleListener listener) {
        return downloadExecute(listener, false);
    }

    public int registerExecute() {
        if (!DeviceInfo.getInstance().isValid()){
            Trace.e(TAG, "register_task() failed. device info is null");
            return Error.DEVICE_INFO_INIT_FAILED;
        }
        if (!ProductInfo.getInstance().isProductValid()){
            if (!JsonAnalyticsUtil.isSuccess(OTAExecuteManager.getInstance().doObtainProduct()) ||
                    !ProductInfo.getInstance().isProductValid()){
                return Error.DEVICE_INFO_INIT_FAILED;
            }
        }

        FutureTask<String> registerFutureTask = FutureTaskPool.getInstance().executeTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String register = HttpTools.getInstance().doPostRegister(DeviceInfo.getInstance(), OtaAgentPolicy.sCx);
                return register;
            }
        });
        String register = "";
        try {
            register = registerFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            Trace.e(TAG, e);
        }
        if (TextUtils.isEmpty(register)) {
            //服务器返回值为空，可能是服务器或者网络异常
            return Error.SERVER_DATA_ERROR;
        }
        int i = JsonAnalyticsUtil.registerJson(register, OtaAgentPolicy.sCx);
        if (JsonAnalyticsUtil.isSuccess(i)) {
            BeanUtils.setRegisterInfo(register, OtaAgentPolicy.sCx);
            return JsonAnalyticsUtil.SUCCESS;
        } else {
            return i;
        }
    }

    public DownEntity downloadPrepare() {
        //上报下载
        OtaService.startByAction(OtaService.ACTION_REPORT);
        final String down_path = OtaAgentPolicy.config.updatePath + ".temp";//update.zip.temp
        DownEntity downEntity;
        if (DownConfig.sSegmentDownload
                && null != VersionInfo.getInstance().segmentSha
                && VersionInfo.getInstance().segmentSha.size() > 0) {
            downEntity = new DownEntity(VersionInfo.getInstance().deltaUrl, down_path, VersionInfo.getInstance().fileSize, VersionInfo.getInstance().md5sum)
                    .setSegmentDownInfo(VersionInfo.getInstance().segmentSha);

        } else {
            downEntity = new DownEntity(VersionInfo.getInstance().deltaUrl, down_path, VersionInfo.getInstance().fileSize, VersionInfo.getInstance().md5sum);
        }

        return downEntity;

    }

    public int doObtainProduct() {
        FutureTask<String> task = FutureTaskPool.getInstance().executeTask(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String obtainProduct = HttpTools.getInstance().doPostObtainProduct();
                return obtainProduct;
            }
        });
        String obtainProductResult = "";
        try {
            obtainProductResult = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(obtainProductResult)) {
            return Error.OBTAIN_PRODUCT_FAILED;
        }
        int i = obtainProductResult.indexOf("_");
        if (i > 0) {
            ProductInfo.getInstance().setAndStoreProductInfo(obtainProductResult.substring(0,i),obtainProductResult.substring(i+1));
            return 1000;
        }
        return Error.OBTAIN_PRODUCT_FAILED;
    }
}
