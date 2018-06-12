package com.abupdate.sota.security;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Codec2 {
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
	
	/** 
     * MAC算法可选以下多种算法 
     * <pre> 
     * HmacMD5  
     * HmacSHA1  
     * HmacSHA256  
     * HmacSHA384  
     * HmacSHA512 
     * </pre> 
     */  
    public static final String KEY_MAC = "HmacMD5";

	private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

//	/**
//	 * Write a byte array as hexadecimal String.
//	 */
//	public static String byteToHexString(byte[] bytes) {
//		return String.valueOf(Hex.encodeHex(bytes));
//	}

	/**
	 * byteArr转hexString
	 * <p>例如：</p>
	 * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
	 *
	 * @param bytes 字节数组
	 * @return 16进制大写字符串
	 */
	public static String byteToHexString(byte[] bytes) {
		if (bytes == null) {
            return null;
        }
		int len = bytes.length;
		if (len <= 0) {
            return null;
        }
		char[] ret = new char[len << 1];
		for (int i = 0, j = 0; i < len; i++) {
			ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
			ret[j++] = hexDigits[bytes[i] & 0x0f];
		}
		return new String(ret);
	}

	public static String hexHMAC(String secret, String data) {
		String HMAC_SHA1_ALGORITHM = "HmacSHA256";
		try {
			SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			byte[] rawHmac = mac.doFinal(data.getBytes());
			String result = new String(Codec2.byteToHexString(rawHmac));
			return result;
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
     * HMAC加密
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key.getBytes(), KEY_MAC);
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);
        return mac.doFinal(data);
    }

    /*签名认证*/
    public static String hexString(String key, String value) {
    	try {
			return byteToHexString(encryptHMAC(key.getBytes(),value)).toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

	public static String getHmacMd5Str(String s, String keyString)
	{
		if (null == s || null == keyString){
			return "";
		}
		String sEncodedString = null;
		try
		{
			SecretKeySpec key = new SecretKeySpec((keyString).getBytes("UTF-8"), "HmacMD5");
			Mac mac = Mac.getInstance("HmacMD5");
			mac.init(key);

			byte[] bytes = mac.doFinal(s.getBytes("ASCII"));

			StringBuffer hash = new StringBuffer();

			for (int i=0; i<bytes.length; i++) {
				String hex = Integer.toHexString(0xFF &  bytes[i]);
				if (hex.length() == 1) {
					hash.append('0');
				}
				hash.append(hex);
			}
			sEncodedString = hash.toString();
		}
		catch (UnsupportedEncodingException e) {}
		catch(InvalidKeyException e){}
		catch (NoSuchAlgorithmException e) {}
		return sEncodedString ;
	}
}