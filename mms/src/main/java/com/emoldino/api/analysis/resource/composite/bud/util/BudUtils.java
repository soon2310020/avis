package com.emoldino.api.analysis.resource.composite.bud.util;

import java.time.LocalDateTime;

public class BudUtils {

	public static String getYear(int remainingDays) {
		LocalDateTime currTime = LocalDateTime.now();
		LocalDateTime calTime = currTime.plusDays(remainingDays);
		int year = calTime.getYear();
		return String.valueOf(year);
	}

	public static String getHalf(int remainingDays) {
		LocalDateTime currTime = LocalDateTime.now();
		LocalDateTime calTime = currTime.plusDays(remainingDays);

		int month = calTime.getMonthValue();

		int half = 1;
		if (month >= 1 && month <= 6) {
			half = 1;
		} else {
			half = 2;
		}

		return String.valueOf(half);
	}

	public static String getCurrentHalf(LocalDateTime currTime) {
		int month = currTime.getMonthValue();

		int half = 1;
		if (month >= 1 && month <= 6) {
			half = 1;
		} else {
			half = 2;
		}

		return String.valueOf(half);
	}

}
