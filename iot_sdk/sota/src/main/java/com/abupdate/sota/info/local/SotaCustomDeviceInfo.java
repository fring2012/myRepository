package com.abupdate.sota.info.local;


/**
 * @author fighter_lee
 * @date 2017/12/19
 */
public class SotaCustomDeviceInfo {

    public SotaCustomDeviceInfo setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public SotaCustomDeviceInfo setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public SotaCustomDeviceInfo setProduct_secret(String product_secret) {
        this.product_secret = product_secret;
        return this;
    }

    public SotaCustomDeviceInfo setVersion(String version) {
        this.version = version;
        return this;
    }

    public SotaCustomDeviceInfo setOem(String oem) {
        this.oem = oem;
        return this;
    }

    public SotaCustomDeviceInfo setModels(String models) {
        this.models = models;
        return this;
    }

    public SotaCustomDeviceInfo setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public SotaCustomDeviceInfo setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * 设备唯一标识码
     */
    public String mid;
    /**
     * 项目唯一标识码
     */
    public String productId;

    /**
     * 项目加密码
     */
    public String product_secret;

    /**
     * 设备版本号
     */
    public String version;
    /**
     * 厂商信息，广升提供
     */
    public String oem;
    /**
     * 设备型号，同一个厂商下面不允许出现相同型号的设备。oem+ models组成一个项目
     */
    public String models;
    /**
     * 芯片平台信息，如MTK6582、SPRD8830、MSM9x15，广升给出平台列表
     */
    public String platform;
    /**
     * 设备类型，如phone、box、pad、mifi等，广升给出类型列表
     */
    public String deviceType;

}
