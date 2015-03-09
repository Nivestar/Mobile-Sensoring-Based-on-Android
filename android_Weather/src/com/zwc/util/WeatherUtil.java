package com.zwc.util;

public class WeatherUtil {

	private enum Weather {
		SUNNY("晴", "00"), CLOUDY("多云", "01"), OVERCAST("阴", "02"), THUNDERSHOWER(
				"阵雨", "03");
		private String name;
		private String code;

		private Weather(String name, String code) {
			this.name = name;
			this.code = code;
		}

	}

	public static String getWeatherById(String id) {
		if (id.equals("00"))
			return "晴";
		else if (id.equals("01"))
			return "多云";
		else if (id.equals("02"))
			return "阴";
		else if (id.equals("03"))
			return "阵雨";
		else if (id.equals("04"))
			return "雷阵雨";
		else if (id.equals("05"))
			return "雷阵雨伴有冰雹";
		else if (id.equals("06"))
			return "雨夹雪";
		else if (id.equals("07"))
			return "小雨";
		else if (id.equals("08"))
			return "中雨";
		else if (id.equals("09"))
			return "大雨";
		else if (id.equals("10"))
			return "暴雨";
		else if (id.equals("11"))
			return "大暴雨";
		else if (id.equals("12"))
			return "特大暴雨";
		else if (id.equals("13"))
			return "阵雪";
		else if (id.equals("14"))
			return "小雪";
		else if (id.equals("15"))
			return "中雪";
		else if (id.equals("16"))
			return "大雪";
		else if (id.equals("17"))
			return "暴雪";
		else if (id.equals("18"))
			return "雾";
		else if (id.equals("19"))
			return "冻雨";
		else if (id.equals("20"))
			return "沙尘暴";
		else if (id.equals("21"))
			return "小到中雨";
		else if (id.equals("22"))
			return "中到大雨";
		else if (id.equals("23"))
			return "大到暴雨";
		else if (id.equals("24"))
			return "暴雨到大暴雨";
		else if (id.equals("25"))
			return "大暴雨到特大暴雨";
		else if (id.equals("26"))
			return "小到中雪";
		else if (id.equals("27"))
			return "中到大雪";
		else if (id.equals("28"))
			return "大到暴雪";
		else if (id.equals("29"))
			return "浮尘";
		else if (id.equals("30"))
			return "扬沙";
		else if (id.equals("31"))
			return "强沙尘暴";
		else if (id.equals("53"))
			return "霾";
		else if (id.equals("99"))
			return "无";
		else
			return "";

	}

	public static String getWindDirectionById(String windDirection) {

		if (windDirection.equals("0"))
			return "无持续风向";
		else if (windDirection.equals("1"))
			return "东北风";
		else if (windDirection.equals("2"))
			return "东风";
		else if (windDirection.equals("3"))
			return "东南风";
		else if (windDirection.equals("4"))
			return "南风";
		else if (windDirection.equals("5"))
			return "西南风";
		else if (windDirection.equals("6"))
			return "西风";
		else if (windDirection.equals("7"))
			return "西北风";
		else if (windDirection.equals("8"))
			return "北风";
		else if (windDirection.equals("9"))
			return "旋转风";
		else
			return "";
	}

	public static String getWindDegreeById(String windDegree) {

		if (windDegree.equals("0"))
			return "微风 <10m/h";
		else if (windDegree.equals("0"))
			return "3-4级 10`17m/h";
		else if (windDegree.equals("0"))
			return "4-5级 17`25m/h";
		else if (windDegree.equals("0"))
			return "5-6级 25`34m/h";
		else if (windDegree.equals("0"))
			return "6-7级 34`43m/h";
		else if (windDegree.equals("0"))
			return "7-8级 43`54m/h";
		else if (windDegree.equals("0"))
			return "8-9级 54`65m/h";
		else if (windDegree.equals("0"))
			return "9-10级 65`77m/h";
		else if (windDegree.equals("0"))
			return "10-11级 77`89m/h";
		else if (windDegree.equals("0"))
			return "11-12级 89`102m/h";
		else
			return "";
	}

}
