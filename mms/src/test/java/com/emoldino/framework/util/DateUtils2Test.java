package com.emoldino.framework.util;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;

import saleson.common.util.DateUtils;

public class DateUtils2Test {

	@Test
	public void newInstant() {
		ThreadUtils.doScope("DateUtils2Test.newInstant", () -> {
			Instant now = DateUtils2.newInstant();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// Skip
			}
			Instant now2 = DateUtils2.newInstant();

			Assertions.assertNotEquals(now, now2, "2 values of newInstant should be different");

			Assertions.assertTrue(now.isBefore(now2), "newInstant, that called after 1 millis, should be bigger than previous newInstant");
		});
	}

	@Test
	public void getInstant() {
		ThreadUtils.doScope("DateUtils2Test.getInstant", () -> {
			Instant now = DateUtils2.newInstant();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// Skip
			}
			Instant now2 = DateUtils2.getInstant();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// Skip
			}
			Instant now3 = DateUtils2.getInstant();

			Assertions.assertEquals(now, now2, "getInstant should be equal to previous newInstant");
			Assertions.assertEquals(now2, now3, "2 values of getInstant should be equal");
		});
	}

	@Test
	public void toInstant() {
		String str = "20220718";
		Instant instant = DateUtils2.toInstant(str, DatePattern.yyyyMMdd, Zone.SYS);
		String str2 = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS);
		Assertions.assertEquals(str, str2);
	}

	@Test
	public void toInstant2() {
		String str = "2022-07-18";
		Instant instant = DateUtils2.toInstant(str, DatePattern.yyyy_MM_dd, Zone.SYS);
		String str2 = DateUtils2.format(instant, DatePattern.yyyy_MM_dd, Zone.SYS);
		Assertions.assertEquals(str, str2);
	}

	@Test
	public void toInstant3() {
		String str = "202207";
		Instant instant = DateUtils2.toInstant(str, DatePattern.yyyyMM, Zone.SYS);
		String str2 = DateUtils2.format(instant, DatePattern.yyyyMM, Zone.SYS);
		Assertions.assertEquals(str, str2);
	}

	@Test
	public void format() {
		String str = "20220723144332";
		Instant instant = DateUtils2.toInstant(str, DatePattern.yyyyMMddHHmmss, Zone.SYS);

		// yyyyMMddHHmmss
		{
			String str2 = DateUtils2.format(instant, DatePattern.yyyyMMddHHmmss, Zone.SYS);
			Assertions.assertEquals(str, str2);
		}
		// yyyy
		{
			String str2 = DateUtils2.format(instant, DatePattern.yyyy, Zone.SYS);
			Assertions.assertEquals(str.substring(0, 4), str2);
		}
		// MM
		{
			String str2 = DateUtils2.format(instant, DatePattern.MM, Zone.SYS);
			Assertions.assertEquals(str.substring(4, 6), str2);
		}
		// dd
		{
			String str2 = DateUtils2.format(instant, DatePattern.dd, Zone.SYS);
			Assertions.assertEquals(str.substring(6, 8), str2);
		}
		// HH
		{
			String str2 = DateUtils2.format(instant, DatePattern.HH, Zone.SYS);
			Assertions.assertEquals(str.substring(8, 10), str2);
		}
		// dd
		{
			String str2 = DateUtils2.format(instant, DatePattern.dd_MM, Zone.SYS);
			Assertions.assertEquals(DateUtils2.format(instant, DatePattern.dd, Zone.SYS) + "/" + DateUtils2.format(instant, DatePattern.MM, Zone.SYS), str2);
		}
	}

	@Test
	public void weekOfYear() {
		// YYYYww
		{
			Instant instant = DateUtils2.toInstant("20220101", DatePattern.yyyyMMdd, Zone.SYS);
			String str2 = DateUtils2.format(instant, DatePattern.YYYYww, Zone.SYS);
			Assertions.assertEquals("202152", str2);
		}
		{
			Instant instant = DateUtils2.toInstant("20220726", DatePattern.yyyyMMdd, Zone.SYS);
			String str2 = DateUtils2.format(instant, DatePattern.YYYYww, Zone.SYS);
			Assertions.assertEquals("202230", str2);
		}

		// ww YYYY
		{
			Instant instant = DateUtils2.toInstant("20220101", DatePattern.yyyyMMdd, Zone.SYS);
			String str2 = DateUtils2.format(instant, DatePattern.ww_YYYY, Zone.SYS);
			Assertions.assertEquals("52 2021", str2);
		}
		{
			Instant instant = DateUtils2.toInstant("20220726", DatePattern.yyyyMMdd, Zone.SYS);
			String str2 = DateUtils2.format(instant, DatePattern.ww_YYYY, Zone.SYS);
			Assertions.assertEquals("30 2022", str2);
		}

		Instant since = DateUtils2.toInstant("20220101", DatePattern.yyyyMMdd, Zone.SYS);
		Instant now = Instant.now();
		Instant instant = since;
		while (now.compareTo(instant) >= 0) {
			System.out.println(DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS)//
					+ " " + DateUtils2.format(instant, DatePattern.YYYYww, Zone.SYS)//
					+ " " + DateUtils.getYearWeek(instant, DatePattern.yyyyMMdd)//
					+ " " + DateUtils2.format(instant.plusSeconds(60 * 60 * 24 * 7), DatePattern.YYYYww, Zone.SYS));
			instant = instant.plus(Duration.ofDays(1));
		}
	}

	@Test
	public void toOtherPattern() {
		String str = "20220723144332";

		// yyyy-MM-dd HH:mm:ss
		{
			String str2 = DateUtils2.toOtherPattern(str, DatePattern.yyyyMMddHHmmss, DatePattern.yyyy_MM_dd_HH_mm_ss);
			Assertions.assertEquals("2022-07-23 14:43:32", str2);
		}
		// yyyy
		{
			String str2 = DateUtils2.toOtherPattern(str, DatePattern.yyyyMMddHHmmss, DatePattern.yyyy);
			Assertions.assertEquals(str.substring(0, 4), str2);
		}
		// MM
		{
			String str2 = DateUtils2.toOtherPattern(str, DatePattern.yyyyMMddHHmmss, DatePattern.MM);
			Assertions.assertEquals(str.substring(4, 6), str2);
		}
		// dd
		{
			String str2 = DateUtils2.toOtherPattern(str, DatePattern.yyyyMMddHHmmss, DatePattern.dd);
			Assertions.assertEquals(str.substring(6, 8), str2);
		}
		// HH
		{
			String str2 = DateUtils2.toOtherPattern(str, DatePattern.yyyyMMddHHmmss, DatePattern.HH);
			Assertions.assertEquals(str.substring(8, 10), str2);
		}
	}

}
