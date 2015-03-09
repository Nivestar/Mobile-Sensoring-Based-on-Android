package com.zwc.util;


public class DateUtil {
	public String getChineseWeek(int i) {
		switch (i) {
		case 1:
			return "日";
		case 2:
			return "一";
		case 3:
			return "二";
		case 4:
			return "三";
		case 5:
			return "四";
		case 6:
			return "五";
		case 7:
			return "六";
		default:
			return null;
		}
	}

}
