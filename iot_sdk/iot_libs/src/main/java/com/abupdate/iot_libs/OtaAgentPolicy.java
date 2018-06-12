package com.abupdate.iot_libs;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Pair;

import com.abupdate.http_libs.engine.HttpManager;
import com.abupdate.iot_download_libs.DLManager;
import com.abupdate.iot_libs.constant.OtaConstants;
import com.abupdate.iot_libs.constant.SDKConfig;
import com.abupdate.iot_libs.engine.CrashCatcher;
import com.abupdate.iot_libs.engine.FotaVerifyManager;
import com.abupdate.iot_libs.engine.IOTCallbackManager;
import com.abupdate.iot_libs.engine.OTAExecuteManager;
import com.abupdate.iot_libs.info.CustomDeviceInfo;
import com.abupdate.iot_libs.info.DeviceInfo;
import com.abupdate.iot_libs.info.ProductInfo;
import com.abupdate.iot_libs.info.RegisterInfo;
import com.abupdate.iot_libs.info.VersionInfo;
import com.abupdate.iot_libs.inter.ICheckVersionCallback;
import com.abupdate.iot_libs.inter.IDownSimpleListener;
import com.abupdate.iot_libs.inter.IDownloadListener;
import com.abupdate.iot_libs.inter.IRebootUpgradeCallBack;
import com.abupdate.iot_libs.inter.IRegisterListener;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.service.JobSchedulerService;
import com.abupdate.iot_libs.service.OtaService;
import com.abupdate.iot_libs.utils.BeanUtils;
import com.abupdate.iot_libs.utils.FileUtil;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.iot_libs.utils.Utils;
import com.abupdate.trace.Trace;

import java.io.File;

/**
 * Created by fighter_lee on 2017/5/16.
 */

public class OtaAgentPolicy {

    public static Context sCx;

    public static Builder config;

    /**
     * AndroidManifest是否验证通过
     */
    private static final String TAG = "OtaAgentPolicy";

    /**
     * 初始化Fota参数
     *
     * @param context
     */
    private static void initFota(Context context) throws FotaException {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "init Fota", OtaConstants.SINGLE_LINE);
        CrashCatcher.getInstance().init(context);
        SDKConfig.gen();
        verifyManifest(context);
        if (null == config.customDeviceInfo) {
            DeviceInfo.getInstance().init();
            ProductInfo.getInstance().init();
        } else {
            DeviceInfo.getInstance().initOtherInfo(
                    config.customDeviceInfo.version,
                    config.customDeviceInfo.oem,
                    config.customDeviceInfo.models,
                    config.customDeviceInfo.platform,
                    config.customDeviceInfo.deviceType,
                    config.customDeviceInfo.requestPush);
            if (!TextUtils.isEmpty(config.customDeviceInfo.productId) && !TextUtils.isEmpty(config.customDeviceInfo.product_secret)){
                ProductInfo.getInstance().productSecret = config.customDeviceInfo.product_secret;
                ProductInfo.getInstance().productId = config.customDeviceInfo.productId;
            }else{
                ProductInfo.getInstance().init();
            }
        }

        RegisterInfo.getInstance().init();
        MqttAgentPolicy.initMqtt();

        BeanUtils.setVersionInfoFromLocal();

        HttpManager.build(context)
                .setRedirectTimes(0)
                .setRetryTimes(3)
                .create();

        //5.0以上采用jobScheduler的方式进行上报以及和服务器周期通讯，5.0以下采用广播的方式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startJobService();
        }
    }

    /**
     * 开启JobScheduler进行信息上报以及周期check
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void startJobService() {
        Intent startServiceIntent = new Intent(sCx, JobSchedulerService.class);
        sCx.startService(startServiceIntent);

        ComponentName componentName = new ComponentName(sCx, JobSchedulerService.class);
        JobInfo info = new JobInfo.Builder(888, componentName)
                .setPeriodic(OtaConstants.STATIC_OTA_CYCLE_TASK)   //每一天执行一次必要的任务
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)   //网络可用才触发
                .setPersisted(true)    //开机启动
                .build();

        JobScheduler tm = (JobScheduler) sCx.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.schedule(info);
    }

    /**
     * 注册接口<br/>
     * <p>
     * 初始化Fota参数时默认会进行注册<br/>
     *
     * @param iRegisterListener
     */
    public static void registerAsync(IRegisterListener iRegisterListener) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "register", OtaConstants.SINGLE_LINE);
        IOTCallbackManager.setRegisterListener(iRegisterListener);
        OtaService.startByAction(OtaService.ACTION_REGISTER);
    }

    /**
     * 检测版本后调用，可以获得最新版本信息<br/>
     * 策略信息map,升级版本号,升级包大小,升级包ID,MD5,升级包下载地址,发布日期,日志内容<br/>
     *
     * @return VersionInfo
     */
    public static VersionInfo getVersionInfo() {
        return VersionInfo.getInstance();
    }


    /**
     * 检测版本（异步回调请求方法）<br/>
     * 获得版本信息，请使用：{@link OtaAgentPolicy#getVersionInfo()}<br/>
     *
     * @param iCheckVersionCallback Get result message.its methods will be called when the object do finish. callback will run in ui thread.<br/>
     * @see OtaAgentPolicy#checkVersionExecute
     */
    public static void checkVersionAsync(ICheckVersionCallback iCheckVersionCallback) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "check version", OtaConstants.SINGLE_LINE);
        IOTCallbackManager.setCheckVersionListener(iCheckVersionCallback);
        OtaService.startByAction(OtaService.ACTION_CHECK_VERSION);
    }

    /**
     * 检测版本（同步请求方法）<br/>
     * 获得版本信息，请使用：{@link OtaAgentPolicy#getVersionInfo()}<br/>
     *
     * @return {@link android.util.Pair}
     * @see OtaAgentPolicy#checkVersionAsync(ICheckVersionCallback)
     */
    public static Pair<Integer, VersionInfo> checkVersionExecute() {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "check version", OtaConstants.SINGLE_LINE);
        int checkVersionExecute = OTAExecuteManager.getInstance().checkVersionExecute();
        Pair<Integer, VersionInfo> pair = new Pair<>(checkVersionExecute, VersionInfo.getInstance());
        return pair;
    }

    /**
     * 启动下载服务（异步回调请求方法）<br/>
     * 获取下载路径：{@link OtaAgentPolicy#config}<br/>
     * 默认下载路径:context.getFilesDir() +"/update.zip"<br/>
     *
     * @param onDownloadListener Get result message.its methods will be called when the object do finish. callback will run in ui thread.<br/>
     * @see OtaAgentPolicy#downloadExecute(IDownSimpleListener)
     * @see OtaAgentPolicy#downloadExecute(IDownSimpleListener,boolean)
     */
    public static void downloadAsync(IDownloadListener onDownloadListener) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "downloadEnqueue", OtaConstants.SINGLE_LINE);
        IOTCallbackManager.setDownloadListener(onDownloadListener);
        OtaService.startByAction(OtaService.ACTION_DOWNLOAD);
    }

    /**
     * 同步下载升级包<br/>
     * 获取下载路径：{@link OtaAgentPolicy#config}<br/>
     * 默认下载路径:context.getFilesDir() +"/update.zip"<br/>
     *
     * @param listener 在哪个线程调用，回调到对应线程，如果需要在主线程接收回调消息，请看：{@link OtaAgentPolicy#downloadExecute(IDownSimpleListener,boolean)}.<br/>
     * @see OtaAgentPolicy#downloadAsync(IDownloadListener)
     * @see OtaAgentPolicy#downloadExecute(IDownSimpleListener,boolean)
     */
    public static boolean downloadExecute(IDownSimpleListener listener) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "downloadExecute", OtaConstants.SINGLE_LINE);
        return OTAExecuteManager.getInstance().downloadExecute(listener);
    }

    /**
     * 同步下载升级包，回调到主线程<br/>
     * 获取下载路径：{@link OtaAgentPolicy#config}<br/>
     * 默认下载路径:context.getFilesDir() +"/update.zip"<br/>
     *
     * @param listener 在哪个线程调用，回调到对应线程，如果需要在主线程接收回调消息，请看：{@link OtaAgentPolicy#downloadExecute(IDownSimpleListener,boolean)}.<br/>
     * @see OtaAgentPolicy#downloadAsync(IDownloadListener)
     * @see OtaAgentPolicy#downloadExecute(IDownSimpleListener)
     */
    public static boolean downloadExecute(IDownSimpleListener listener,boolean callbackToMain) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "downloadExecute", OtaConstants.SINGLE_LINE);
        return OTAExecuteManager.getInstance().downloadExecute(listener, callbackToMain);
    }


    /**
     * 暂停下载
     */
    public static void downloadCancel() {
        Trace.d(TAG, "downloadCancel().");
        OtaService.setDownloadCancel();
    }

    /**
     * 完整升级流程升级请调用此方法<br/>
     * 此方法会对升级包进行md5校验，保证升级包和服务器上的升级包统一
     * <p>
     * check -> download -> update<br/>
     *
     * @param iRebootUpgradeCallBack Get result message.its methods will be called when the object do finish. callback will run in ui thread.<br/>
     * @see OtaAgentPolicy#rebootLocalUpgrade(String, IRebootUpgradeCallBack)
     */
    public static void rebootUpgrade(IRebootUpgradeCallBack iRebootUpgradeCallBack) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "reboot update", OtaConstants.SINGLE_LINE);
        OtaService.startByAction(OtaService.ACTION_UPDATE);
        IOTCallbackManager.setUpdateCallBack(iRebootUpgradeCallBack);
    }

    /**
     * 若下载完成后对升级包进行了修改，请调用次升级接口<br/>
     * 此方法不会进行md5校验<br/>
     *
     * @param path
     * @param iRebootUpgradeCallBack Get result message.its methods will be called when the object do finish. callback will run in ui thread.<br/>
     * @see OtaAgentPolicy#rebootUpgrade(IRebootUpgradeCallBack)
     */
    public static void rebootLocalUpgrade(String path, IRebootUpgradeCallBack iRebootUpgradeCallBack) {
        Trace.d(TAG, "%s%s%s", OtaConstants.SINGLE_LINE, "reboot local update", OtaConstants.SINGLE_LINE);
        OtaService.startByAction(OtaService.ACTION_UPDATE,path);
        IOTCallbackManager.setUpdateCallBack(iRebootUpgradeCallBack);
    }

    /**
     * 校验Manifest是否添加必要权限,是否注册必要的组件
     */
    private static boolean verifyManifest(Context context) {
        boolean fota_verify = FotaVerifyManager.verify(context);
        if (!fota_verify) {
            throw new RuntimeException("AndroidManifest element and permissions is lack");
        }
        return true;
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
        sCx = context;
        SPFTool.initContext(sCx);
        OtaService.initContext(sCx);
        DLManager.getInstance().setContext(sCx);
        return config;
    }

    public static Builder getConfig() {
        return config;
    }

    public static class Builder {

        /**
         * 下载路径
         */
        public String updatePath;

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
        public CustomDeviceInfo customDeviceInfo;
        /**
         * 设备为唯一标识吗
         */
        public String mid;
        /**
         * 是否上报错误日志
         */
        public boolean reportLog = true;

        /**
         * 设置升级包下载路径
         *
         * @param path
         * @return {@link Builder}
         */
        public Builder setUpdatePath(String path) {
            Trace.d(TAG, "setUpdatePath() :" + path);
            boolean existsDir = FileUtil.createOrExistsDir(new File(path).getParentFile().getAbsolutePath());
            if (existsDir) {
                updatePath = path;
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
         * APP Crash或者升级失败时是否主动上报错误日志
         * @param report
         * @return
         */
        public Builder isReportLog(boolean report) {
            reportLog = report;
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
        public Builder setCustomDeviceInfo(CustomDeviceInfo deviceInfo) {
            customDeviceInfo = deviceInfo;
            return this;
        }

        public void commit() throws FotaException {
            boolean updatePathExistsDir = !TextUtils.isEmpty(updatePath) && FileUtil.createOrExistsDir(new File(updatePath).getParentFile().getAbsolutePath());
            if (!updatePathExistsDir) {
                updatePath = sCx.getFilesDir() + File.separator + "update.zip";
            }

            boolean traceExistsDir = !TextUtils.isEmpty(tracePath) && FileUtil.createOrExistsDir(new File(tracePath).getParentFile().getAbsolutePath());
            if (!traceExistsDir) {
                tracePath = Utils.setFotaLog(sCx);
            }

            Trace.setLevel(showTrace ? Trace.DEBUG : Trace.NONE);
            Trace.setLog_path(tracePath);
            Trace.setShowPosition(true);

            if (TextUtils.isEmpty(mid) && (null == customDeviceInfo || TextUtils.isEmpty(customDeviceInfo.mid))) {
                throw new RuntimeException("mid can not be null");
            }
            if (null != customDeviceInfo && !TextUtils.isEmpty(customDeviceInfo.mid)) {
                DeviceInfo.getInstance().initInfo(customDeviceInfo.mid);
            } else {
                DeviceInfo.getInstance().initInfo(mid);
            }
            initFota(sCx);
        }
    }
}
