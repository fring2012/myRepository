package com.abupdate.iot_libs.security;

import com.abupdate.iot_libs.info.DeviceInfo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author fighter_lee
 * @date 2018/2/7
 */
public class Encrypt {

    //MD5
    public static final byte[] encryptType = {77,68,53};
    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final String TAG = "Encrypt";
    public static String encryptObtainProduct() {
        try {
            StringBuilder builder = new StringBuilder();
            DeviceInfo deviceInfo = DeviceInfo.getInstance();
            byte[] b = builder.append(deviceInfo.models)
                    .append(deviceInfo.deviceType)
                    .append(deviceInfo.oem)
                    .append(deviceInfo.platform)
                    .toString().getBytes();
            String md5 = getMd5(getMd5(new String(b)));
            return md5;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String decodeObtainProduct(String key,String data) {
        byte[] data_byte = hexString2Bytes(data);
        byte[] key_byte = key.getBytes();
        byte[] bytes = desTemple(data_byte, key_byte, "AES");
        return new String(bytes);
    }


    /**
     * MD5 加密
     *
     * @param data 明文字节数组
     * @return 密文字节数组
     */
    public static byte[] encryptMD5(final byte[] data) {
        return hashTemplate(data, new String(encryptType));
    }

    private static byte[] hashTemplate(final byte[] data, final String algorithm) {
        if (data == null || data.length <= 0) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] desTemple(final byte[] data, final byte[] key, final String algorithm){
        if (data == null || data.length == 0 || key == null || key.length == 0) return null;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return cipher.doFinal(data);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String bytes2HexString(final byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

    private static byte[] hexString2Bytes(String hexString) {
        int len = hexString.length();
        if (len % 2 != 0) {
            hexString = "0" + hexString;
            len = len + 1;
        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] ret = new byte[len >> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }

    private static int hex2Dec(final char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public static String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance(new String(encryptType));
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
