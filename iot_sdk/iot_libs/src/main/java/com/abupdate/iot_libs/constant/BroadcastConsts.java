package com.abupdate.iot_libs.constant;

/**
 * Created by fighter_lee on 2017/5/25.
 */

public class BroadcastConsts {

    /**
     * 消息推送
     */
    public static final String ACTION_FOTA_NOTIFY = "action_fota_notify";

    /**
     * 升级结果
     */
    public static final String ACTION_FOTA_UPDATE_RESULT = "action_fota_update_result";

    /**
     * 获取消息内容：intent.getStringExtra(BroadcastConsts.KEY_FOTA_NOTIFY);
     */
    public static final String KEY_FOTA_NOTIFY = "key_fota_notify";

    /**
     * 获取升级结果：intent.getBooleanExtra(BroadcastConsts.KEY_FOTA_UPDATE_RESULT);
     */
    public static final String KEY_FOTA_UPDATE_RESULT = "key_fota_update_result";

    /**
     * 本应用接收SDK广播需要添加此权限，其他应用需要使用相同的签名
     */
    public static final String PERMISSION_FOTA_UPDATE = "permission.com.abupdate.fota.update";

}
