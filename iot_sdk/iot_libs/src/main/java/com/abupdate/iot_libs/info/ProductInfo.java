package com.abupdate.iot_libs.info;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.abupdate.iot_libs.OtaAgentPolicy;
import com.abupdate.iot_libs.security.FotaException;
import com.abupdate.iot_libs.utils.SPFTool;
import com.abupdate.trace.Trace;

/**
 * @author fighter_lee
 * @date 2018/2/7
 */
public class ProductInfo {

    private static final String TAG = "ProductInfo";
    private static ProductInfo mInstance;

    /**
     * 项目唯一标识码
     */
    public String productId;

    public String productSecret;

    public static ProductInfo getInstance() {
        if (mInstance == null) {
            synchronized (ProductInfo.class) {
                if (mInstance == null) {
                    mInstance = new ProductInfo();
                }
            }
        }
        return mInstance;
    }

    public void init() throws FotaException {
        try {
            ApplicationInfo applicationInfo = OtaAgentPolicy.sCx.getPackageManager().getApplicationInfo(OtaAgentPolicy.sCx.getPackageName(), PackageManager.GET_META_DATA);
            Object o_id = null;
            Object o_secret = null;
            if (null != applicationInfo && applicationInfo.metaData != null) {
                o_id = applicationInfo.metaData.get("fota_configuration_product_id");
                o_secret = applicationInfo.metaData.get("fota_configuration_product_secret");
            }
            if (null == o_id && null == o_secret) {
                initFromLocal();
            } else {
                String id = (String) o_id;
                String secret = (String) o_secret;
                if (TextUtils.isEmpty(id) || TextUtils.isEmpty(secret) || !id.startsWith("string/") || !secret.startsWith("string/")) {
                    throw new FotaException(FotaException.REASON_CODE_MANIFEST_META_DATA_ERROR);
                }
                productId = id.replace("string/", "").trim();
                productSecret = secret.replace("string/", "").trim();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new FotaException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new FotaException(FotaException.REASON_CODE_MANIFEST_META_DATA_ERROR, e);
        }
    }

    public boolean isProductValid() {
        boolean valid = true;
        if (TextUtils.isEmpty(productSecret)) {
            Trace.d(TAG, "isValid() product_secret = null");
            valid = false;
        }
        if (TextUtils.isEmpty(productId)) {
            Trace.d(TAG, "isValid() product id = null");
            valid = false;
        }
        return valid;
    }

    /**
     * 从本地记录加载进内存
     */
    private void initFromLocal() {
        this.productId = SPFTool.getString(SPFTool.KEY_PRODUCT_ID, "");
        this.productSecret = SPFTool.getString(SPFTool.KEY_PRODUCT_SECRET, "");
    }

    /**
     * 存储从服务器获取的项目信息
     * @param productId
     * @param productSecret
     */
    public void setAndStoreProductInfo(String productId, String productSecret) {
        SPFTool.putString(SPFTool.KEY_PRODUCT_ID, productId);
        SPFTool.putString(SPFTool.KEY_PRODUCT_SECRET, productSecret);
        ProductInfo.getInstance().productId = productId;
        ProductInfo.getInstance().productSecret = productSecret;
    }

    /**
     * 将项目信息重置
     */
    public void reset() {
        Trace.d(TAG, "product info reset");
        productId = "";
        productSecret = "";
        SPFTool.putString(SPFTool.KEY_PRODUCT_ID, "");
        SPFTool.putString(SPFTool.KEY_PRODUCT_SECRET, "");
    }

}
