package com.zwc.util;


public class DateUtil {
	public String getChineseWeek(int i) {
		switch (i) {
		case 1:
			return "��";
		case 2:
			return "һ";
		case 3:
			return "��";
		case 4:
			return "��";
		case 5:
			return "��";
		case 6:
			return "��";
		case 7:
			return "��";
		default:
			return null;
		}
	}

}
