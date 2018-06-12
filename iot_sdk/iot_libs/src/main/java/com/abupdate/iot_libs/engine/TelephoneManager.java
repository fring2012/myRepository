package com.abupdate.iot_libs.engine;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

/**
 * Created by fighter_lee on 2017/6/29.
 */

public class TelephoneManager {

    private static TelephoneManager telephoneManager;
    private Context mCx;
    private TelephonyManager mTelephonyManager;
    private static final String TAG = "TelephoneManager";
    private String operator;
    private GsmCellLocation location;

    public static TelephoneManager build() {
        if (null == telephoneManager) {
            telephoneManager = new TelephoneManager();
        }
        return telephoneManager;
    }


    public TelephoneManager init(Context context) {
        this.mCx = context;
        try {
            mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            operator = mTelephonyManager.getNetworkOperator();
            location = (GsmCellLocation) mTelephonyManager.getCellLocation();
        }catch (Exception e){
            //获取不到
        }
        return this;
    }

    public String getLac() {
        String lac = "";
        try {
            lac = String.valueOf(location.getLac());
        }catch (Exception e){

        }
        return lac;
    }

    public String getCid() {
        String cid = "";
        try {
            cid = String.valueOf(location.getCid());
        }catch (Exception e){

        }
        return cid;
    }

    public String getMcc() {
        String mcc = "";
        try {
            mcc = String.valueOf(Integer.parseInt(operator.substring(0, 3)));
        }catch (Exception e){

        }
        return mcc;
    }

    public String getMnc() {
        String mnc = "";
        try {
            mnc = String.valueOf(Integer.parseInt(operator.substring(3)));
        }catch (Exception e){

        }
        return mnc;
    }

}
