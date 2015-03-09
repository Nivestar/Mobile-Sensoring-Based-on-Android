package com.zwc.util;

public class WeatherUtil {

	private enum Weather {
		SUNNY("��", "00"), CLOUDY("����", "01"), OVERCAST("��", "02"), THUNDERSHOWER(
				"����", "03");
		private String name;
		private String code;

		private Weather(String name, String code) {
			this.name = name;
			this.code = code;
		}

	}

	public static String getWeatherById(String id) {
		if (id.equals("00"))
			return "��";
		else if (id.equals("01"))
			return "����";
		else if (id.equals("02"))
			return "��";
		else if (id.equals("03"))
			return "����";
		else if (id.equals("04"))
			return "������";
		else if (id.equals("05"))
			return "��������б���";
		else if (id.equals("06"))
			return "���ѩ";
		else if (id.equals("07"))
			return "С��";
		else if (id.equals("08"))
			return "����";
		else if (id.equals("09"))
			return "����";
		else if (id.equals("10"))
			return "����";
		else if (id.equals("11"))
			return "����";
		else if (id.equals("12"))
			return "�ش���";
		else if (id.equals("13"))
			return "��ѩ";
		else if (id.equals("14"))
			return "Сѩ";
		else if (id.equals("15"))
			return "��ѩ";
		else if (id.equals("16"))
			return "��ѩ";
		else if (id.equals("17"))
			return "��ѩ";
		else if (id.equals("18"))
			return "��";
		else if (id.equals("19"))
			return "����";
		else if (id.equals("20"))
			return "ɳ����";
		else if (id.equals("21"))
			return "С������";
		else if (id.equals("22"))
			return "�е�����";
		else if (id.equals("23"))
			return "�󵽱���";
		else if (id.equals("24"))
			return "���굽����";
		else if (id.equals("25"))
			return "���굽�ش���";
		else if (id.equals("26"))
			return "С����ѩ";
		else if (id.equals("27"))
			return "�е���ѩ";
		else if (id.equals("28"))
			return "�󵽱�ѩ";
		else if (id.equals("29"))
			return "����";
		else if (id.equals("30"))
			return "��ɳ";
		else if (id.equals("31"))
			return "ǿɳ����";
		else if (id.equals("53"))
			return "��";
		else if (id.equals("99"))
			return "��";
		else
			return "";

	}

	public static String getWindDirectionById(String windDirection) {

		if (windDirection.equals("0"))
			return "�޳�������";
		else if (windDirection.equals("1"))
			return "������";
		else if (windDirection.equals("2"))
			return "����";
		else if (windDirection.equals("3"))
			return "���Ϸ�";
		else if (windDirection.equals("4"))
			return "�Ϸ�";
		else if (windDirection.equals("5"))
			return "���Ϸ�";
		else if (windDirection.equals("6"))
			return "����";
		else if (windDirection.equals("7"))
			return "������";
		else if (windDirection.equals("8"))
			return "����";
		else if (windDirection.equals("9"))
			return "��ת��";
		else
			return "";
	}

	public static String getWindDegreeById(String windDegree) {

		if (windDegree.equals("0"))
			return "΢�� <10m/h";
		else if (windDegree.equals("0"))
			return "3-4�� 10`17m/h";
		else if (windDegree.equals("0"))
			return "4-5�� 17`25m/h";
		else if (windDegree.equals("0"))
			return "5-6�� 25`34m/h";
		else if (windDegree.equals("0"))
			return "6-7�� 34`43m/h";
		else if (windDegree.equals("0"))
			return "7-8�� 43`54m/h";
		else if (windDegree.equals("0"))
			return "8-9�� 54`65m/h";
		else if (windDegree.equals("0"))
			return "9-10�� 65`77m/h";
		else if (windDegree.equals("0"))
			return "10-11�� 77`89m/h";
		else if (windDegree.equals("0"))
			return "11-12�� 89`102m/h";
		else
			return "";
	}

}
