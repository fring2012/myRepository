package com.abupdate.sota;

import android.content.Context;
import android.text.TextUtils;

import com.abupdate.http_libs.engine.HttpManager;
import com.abupdate.iot_download_libs.DLManager;
import com.abupdate.sota.info.local.SotaCustomDeviceInfo;
import com.abupdate.sota.info.local.SDKConfig;
import com.abupdate.sota.info.remote.SotaDeviceInfo;
import com.abupdate.sota.info.remote.SotaProductInfo;
import com.abupdate.sota.info.remote.SotaRegisterInfo;
import com.abupdate.sota.inter.multi.CheckAllAppTask;
import com.abupdate.sota.inter.multi.CheckNewVersionTask;
import com.abupdate.sota.inter.multi.DownloadTask;
import com.abupdate.sota.network.MyHostnameVerifier;
import com.abupdate.sota.security.FotaException;
import com.abupdate.sota.utils.FileUtil;
import com.abupdate.sota.utils.SPFTool;
import com.abupdate.sota.utils.Utils;
import com.abupdate.trace.Trace;

import java.io.File;

/**
 * @author fighter_lee
 * @date 2018/3/7
 */
public class SotaControler {
    private static final String TAG = "SotaControler";

    public static Context sContext;
    public static Builder config;

    /**
     * 获取后台配置的所有App包名
     * @return
     */
    public static CheckAllAppTask checkAllApp() {
        return new CheckAllAppTask();
    }

    /**
     * 检测是否有需要更新的APP
     * @return
     */
    public static CheckNewVersionTask checkNewVersion() {
        return new CheckNewVersionTask();
    }

    /**
     * 下载应用包
     * @return
     */
    public static DownloadTask download() {
        return DownloadTask.getInstance();
    }


    /**
     * FOTA初始化方法<br/>
     * <p>
     * <pre class="prettyprint">
     *     OtaAgentPolicy.init(context)
     *                    .setMid(mid)
     *                    .commit();
     *
     *  or
     *
     *  OtaAgentPolicy.init(context)
     *                  .setMid(mid)
     *                  .setCustomDeviceInfo(
     *                          new CustomDeviceInfo()
     *                          .setVersion(com.abupdate.iot_sdk.Constant.version)
     *                          .setOem(com.abupdate.iot_sdk.Constant.oem)
     *                          .setModels(com.abupdate.iot_sdk.Constant.model)
     *                          .setDeviceType(com.abupdate.iot_sdk.Constant.deviceType)
     *                          .setPlatform(com.abupdate.iot_sdk.Constant.platform)
     *                          .setProductId(com.abupdate.iot_sdk.Constant.productId)
     *                          .setProduct_secret(com.abupdate.iot_sdk.Constant.productSecret))
     *                  .commit();
     * </pre>
     *
     * @param context
     * @return
     */
    public static Builder init(Context context) {
        config = new Builder();
        sContext = context;
        return config;
    }

    public static Builder getConfig() {
        return config;
    }

    public static class Builder {

        /**
         * 下载路径
         */
        public String downloadDir;

        /**
         * 日志文件存放路径
         */
        public String tracePath;

        /**
         * 是否打印日志
         */
        public boolean showTrace = true;

        /**
         * 自定义设备信息
         */
        public SotaCustomDeviceInfo sotaCustomDeviceInfo;

        /**
         * 设备为唯一标识吗
         */
        public String mid;

        /**
         * 设置APP下载路径
         *
         * @param path
         * @return {@link Builder}
         */
        public Builder setDownloadDir(String path) {
            Trace.d(TAG, "setUpdatePath() :" + path);
            boolean existsDir = FileUtil.createOrExistsDir(path);
            if (existsDir) {
                downloadDir = path;
            } else {
                Trace.e(TAG, "setUpdatePath() path is invalid ! set path fail");
            }
            return this;
        }

        /**
         * 设置日志存储路径
         *
         * @param path
         * @return {@link Builder}
         */
        public Builder setTracePath(String path) {
            Trace.d(TAG, "setTracePath() path:" + path);
            boolean existsDir = FileUtil.createOrExistsDir(new File(path).getParentFile().getAbsolutePath());
            if (existsDir) {
                tracePath = path;
            } else {
                Trace.d(TAG, "setTracePath() path is invalid ! set path fail");
            }
            return this;
        }

        /**
         * 设置是否打印日志
         *
         * @param show
         * @return {@link Builder}
         */
        public Builder showTrace(boolean show) {
            showTrace = show;
            return this;
        }

        /**
         * 设置设备的唯一标识码
         *
         * @param mid
         * @return {@link Builder}
         */
        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        /**
         * 自定义DeviceInfo
         *
         * @param deviceInfo
         * @return {@link Builder}
         */
        public Builder setSotaCustomDeviceInfo(SotaCustomDeviceInfo deviceInfo) {
            sotaCustomDeviceInfo = deviceInfo;
            return this;
        }

        public void commit() throws FotaException {
            SPFTool.initContext(sContext);
            DLManager.getInstance().setContext(sContext);

            boolean updatePathExistsDir = !TextUtils.isEmpty(downloadDir) && FileUtil.createOrExistsDir(downloadDir);
            if (!updatePathExistsDir) {
                downloadDir = sContext.getFilesDir() + File.separator + "/downloadDir";
            }

            boolean traceExistsDir = !TextUtils.isEmpty(tracePath) && FileUtil.createOrExistsDir(new File(tracePath).getParentFile().getAbsolutePath());
            if (!traceExistsDir) {
                tracePath = Utils.setFotaLog(sContext);
            }

            Trace.setLevel(showTrace ? Trace.DEBUG : Trace.NONE);
            Trace.setLog_path(tracePath);
            Trace.setShowPosition(true);

            if (TextUtils.isEmpty(mid) && (null == sotaCustomDeviceInfo || TextUtils.isEmpty(sotaCustomDeviceInfo.mid))) {
                throw new RuntimeException("mid can not be null");
            }
            if (null != sotaCustomDeviceInfo && !TextUtils.isEmpty(sotaCustomDeviceInfo.mid)) {
                SotaDeviceInfo.getInstance().initInfo(sotaCustomDeviceInfo.mid);
            } else {
                SotaDeviceInfo.getInstance().initInfo(mid);
            }

            SDKConfig.isTest(false);
            if (null == sotaCustomDeviceInfo) {
                SotaDeviceInfo.getInstance().init();
                SotaProductInfo.getInstance().init();
            } else {
                SotaDeviceInfo.getInstance().initOtherInfo(
                        sotaCustomDeviceInfo.version,
                        sotaCustomDeviceInfo.oem,
                        sotaCustomDeviceInfo.models,
                        sotaCustomDeviceInfo.platform,
                        sotaCustomDeviceInfo.deviceType);
                if (!TextUtils.isEmpty(sotaCustomDeviceInfo.productId) && !TextUtils.isEmpty(sotaCustomDeviceInfo.product_secret)) {
                    SotaProductInfo.getInstance().productSecret = sotaCustomDeviceInfo.product_secret;
                    SotaProductInfo.getInstance().productId = sotaCustomDeviceInfo.productId;
                } else {
                    SotaProductInfo.getInstance().init();
                }
            }
            HttpManager.build(sContext)
                    .setRedirectTimes(0)
                    .setRetryTimes(3)
                    .setSSL(new String(SDKConfig.KEY), "/assets/adcom.bks", new MyHostnameVerifier())
                    .create();
            SotaRegisterInfo.getInstance().init();
        }
    }

}
