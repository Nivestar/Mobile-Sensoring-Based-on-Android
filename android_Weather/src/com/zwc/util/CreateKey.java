package com.zwc.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class CreateKey {
	private static final String MAC_NAME = "HmacSHA1";
	private static final String ENCODING = "UTF-8";
	private String cityCode;
	private String type;

	/*
	 * @param cityCode 城市的code
	 * 
	 * @param type 天气类型：observe--实况，index--指数，forecast3d--常规预报
	 */
	public CreateKey(String cityCode, String type) {
		this.cityCode = cityCode;
		this.type = type;
	}

	public String createUrl() {
		final String appid = "96fc872075fa1ce2";
		final String privateKey = "bf8e09_SmartWeatherAPI_e252dfb";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		String date = simpleDateFormat.format(new Date());

		String publicKey = "http://open.weather.com.cn/data/?areaid="
				+ cityCode + "&type=" + type + "&date=" + date + "&appid="
				+ appid;
		String key = null;
		String url = null;
		try {
			key = URLEncoder.encode(Base64.encodeToString(
					HmacSHA1Encrypt(publicKey, privateKey), Base64.NO_WRAP),
					"utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		url = "http://open.weather.com.cn/data/?areaid=" + cityCode
				+ "&type=" + type + "&date=" + date + "&appid="
				+ appid.substring(0, 6) + "&key=" + key;

		return url;
	}

	private byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) {
		try {
			byte[] data = encryptKey.getBytes(ENCODING);
			// 根据指定的字节数组和加密算法，生成加密密钥
			SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
			// 生成一个指定的Mac算法的Mac对象
			Mac mac = Mac.getInstance(MAC_NAME);
			// 用给定密钥初始化Mac对象
			mac.init(secretKey);
			byte[] text = encryptText.getBytes(ENCODING);
			// 返回Mac操作
			return mac.doFinal(text);

		} catch (Exception e) {

			e.printStackTrace();
		}
		return null;

	}

}
