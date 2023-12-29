package com.emoldino.framework.util;

import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.springframework.data.util.Pair;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.exception.LogicException;

public class DateUtils2 {
	public static class DatePattern {
		public static final String yyyy = "yyyy";
		public static final String yyyyMM = "yyyyMM";
		public static final String yyyyMMdd = "yyyyMMdd";
		public static final String yyyyMMddHH = "yyyyMMddHH";
		public static final String yyyyMMddHHmm = "yyyyMMddHHmm";
		public static final String yyyyMMddHHmmss = "yyyyMMddHHmmss";
		public static final String yyyyMMddHHmmssSSS = "yyyyMMddHHmmssSSS";
		public static final String MM = "MM";
		public static final String dd = "dd";
		public static final String HH = "HH";
		public static final String HH_mm_ss = "HH:mm:ss";
		public static final String yyyy_MM_dd = "yyyy-MM-dd";
		public static final String yyyy_MM_dd_HH_mm = "yyyy-MM-dd HH:mm";
		public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
		public static final String yyyy_MM_dd_HH_mm_ss_SSS = "yyyy-MM-dd HH:mm:ss.SSS";
		public static final String dd_MM = "dd/MM";
		public static final String YYYYww = "YYYYww";
		public static final String ww_YYYY = "ww YYYY";
		public static final String MMM_dd_YYYY = "MMM dd, YYYY";

		public static final String MMM_YYYY = "MMM, YYYY";
	}

	public static class Zone {
		public static final String GMT = "GMT";
		public static final String SYS = ZoneId.systemDefault().getId();
	}

	private static final WeekFields DEFAULT_WEEK_FIELDS = WeekFields.of(DayOfWeek.SUNDAY, 4);

	private static final String PROP_INSTANT = "DateUtils2.instant";

	/**
	 * Get Instant<br>
	 * If, this method is already called, the same value of previous one will be returned.<br>
	 * Otherwise, it will returns new Instant<br>
	 * @return
	 */
	public static Instant getInstant() {
		return ThreadUtils.getProp(PROP_INSTANT, () -> newInstant());
	}

	public static Instant adjust(Instant instant, Instant priority) {
		if (isReasonable(instant)) {
			return instant;
		}
		return priority;
	}

	public static Instant maxReasonable(Instant a, Instant b, Instant defaultValue) {
		boolean ar = isReasonable(a);
		boolean br = isReasonable(b);
		if (ar) {
			if (br) {
				return ValueUtils.max(a, b);
			} else {
				return a;
			}
		} else if (br) {
			return b;
		}
		return defaultValue;
	}

	public static boolean isReasonable(Instant instant) {
		return instant != null && instant.compareTo(get5YearsAgo()) > 0 && instant.compareTo(getTheDayAfterTomorrow()) < 0;
	}

	// TODO remove
	private static final String PROP_THE_DAY_AFTER_TOMORROW = "DateUtils2.theDayAfterTomorrow";

	private static Instant getTheDayAfterTomorrow() {
		return ThreadUtils.getProp(PROP_THE_DAY_AFTER_TOMORROW, () -> getInstant().plus(Duration.ofDays(2)));
	}

	// TODO remove
	private static final String PROP_5YEARSAGO = "DateUtils2.5yearsAgo";

	private static Instant get5YearsAgo() {
		return ThreadUtils.getProp(PROP_5YEARSAGO, () -> getInstant().minus(Duration.ofDays(365 * 5)));
	}

	/**
	 * New Instant<br>
	 * It returns Now Instant.
	 * @return
	 */
	public static Instant newInstant() {
		Instant now = Instant.now();
		ThreadUtils.setProp(PROP_INSTANT, now);
		return now;
	}

	/**
	 * Convert from Time String to Instant
	 * @param timeStr	Time String
	 * @param pattern	Time String Pattern
	 * @param zoneId	Time String TimeZone
	 * @return
	 */
	public static Instant toInstant(String timeStr, String pattern, String zoneId) {
		return toInstant(timeStr, pattern, zoneId, null);
	}

	/**
	 * Convert from Time String to Instant<br>
	 * If timeStr is empty, Default Value will be returned.<br>
	 * @param timeStr		Time String
	 * @param pattern		Time String Pattern
	 * @param zoneId		Time String TimeZone
	 * @param defaultValue	Default Value
	 * @return
	 */
	public static Instant toInstant(String timeStr, String pattern, String zoneId, Instant defaultValue) {
		if (ObjectUtils.isEmpty(timeStr)) {
			return defaultValue;
		}

		LogicUtils.assertNotEmpty(pattern, "pattern");
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		Instant instant;
		try {
			Date date = getDateFormatter(pattern, zoneId).parse(timeStr, Locale.US);
			instant = Instant.ofEpochMilli(date.getTime());
		} catch (ParseException e) {
			try {
				instant = getDateTimeFormatter(pattern, zoneId).parse(timeStr, Instant::from);
			} catch (DateTimeParseException e1) {
				if (!pattern.endsWith("ww") || !timeStr.endsWith("53")) {
					throw e1;
				}
				instant = toInstant(timeStr.subSequence(0, timeStr.length() - 1) + "2", pattern, zoneId, defaultValue).plus(Duration.ofDays(7));
			}
		}

		if (pattern.contains("ww")) {
			String str = format(instant, pattern, zoneId);
			if (str.compareTo(timeStr) < 0) {
				instant = instant.plus(Duration.ofDays(7));
			}
		}
		return instant;
	}

	/**
	 * It calculate plus Months at Instant
	 * @param instant	The Source Instant
	 * @param months	The months for calculating plus
	 * @param zoneId	Based TimeZone for recognize the Instant Value
	 * @return
	 */
	public static Instant plusMonths(Instant instant, int months, String zoneId) {
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		ZoneId zone = ZoneId.of(zoneId);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		Instant value = localDateTime.plusMonths(months).atZone(zone).toInstant();
		return value;
	}

	/**
	 * Get Time String<br>
	 * If, this method is already called, the same value of previous one will be returned.<br>
	 * Otherwise, it will returns new Time String<br>
	 * @param pattern	Time String Pattern
	 * @param zoneId	TimeZone
	 * @return
	 */
	public static String getString(String pattern, String zoneId) {
		LogicUtils.assertNotEmpty(pattern, "pattern");
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		Instant now = getInstant();
		return format(now, pattern, zoneId);
	}

	/**
	 * New Time String
	 * @param pattern	Time String Pattern
	 * @param zoneId	TimeZone
	 * @return
	 */
	public static String newString(String pattern, String zoneId) {
		LogicUtils.assertNotEmpty(pattern, "pattern");
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		Instant now = newInstant();
		return format(now, pattern, zoneId);
	}

	/**
	 * Format Instant -> Time String by Pattern and Zone
	 * @param instant	Instant to Format
	 * @param pattern	Time String Pattern
	 * @param zoneId	TimeZone
	 * @return
	 */
	public static String format(Instant instant, String pattern, String zoneId) {
		return format(instant, pattern, zoneId, null);
	}

	/**
	 * Format Instant -> Time String by Pattern and Zone<br>
	 * If Instant is null, Default Value will be returned.<br>
	 * @param instant	Instant to Format
	 * @param pattern	Time String Pattern
	 * @param zoneId	TimeZone
	 * @param defaultValue
	 * @return
	 */
	public static String format(Instant instant, String pattern, String zoneId, String defaultValue) {
		if (instant == null) {
			return defaultValue;
		}
		LogicUtils.assertNotEmpty(pattern, "pattern");
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		if (pattern.contains("w")) {
			String date = format(instant, DatePattern.yyyyMMdd, zoneId);
			LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(DatePattern.yyyyMMdd));
			TemporalField woy = DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear();
			int weekNumber = localDate.get(woy);
			String year = date.substring(0, 4);
			if (weekNumber == 1) {
				if (date.substring(4, 6).equals("12")) {
					year = (ValueUtils.toInteger(year, 0) + 1) + "";
				}
			} else if (weekNumber > 50 && date.substring(4, 6).equals("01")) {
				year = ValueUtils.toInteger(year, 0) - 1 + "";
			}
			if (DatePattern.YYYYww.equals(pattern)) {
				return year + (weekNumber < 10 ? "0" + weekNumber : weekNumber);
			} else if (DatePattern.ww_YYYY.equals(pattern)) {
				return (weekNumber < 10 ? "0" + weekNumber : weekNumber) + " " + year;
			}
		}

		String str = getDateTimeFormatter(pattern, zoneId).format(instant);
		return str;
	}

	@Deprecated
	public static String _format(Instant instant, String pattern, String zoneId, String defaultValue) {
		if (instant == null) {
			return defaultValue;
		}
		LogicUtils.assertNotEmpty(pattern, "pattern");
		LogicUtils.assertNotEmpty(zoneId, "zoneId");

		// How Dirty!!
		if (pattern.contains("w")) {

//			return DateUtils.getYearWeek(instant, DatePattern.yyyyMMddHHmmss);

			ZoneId zone = ZoneId.of(zoneId);
			LocalDateTime localDate = LocalDateTime.ofInstant(instant, zone);
			int weekOfYear = localDate.get(DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear());
			int weekOfYear2 = localDate.get(WeekFields.SUNDAY_START.weekOfWeekBasedYear());

			if (weekOfYear != weekOfYear2) {
				int year = localDate.get(DEFAULT_WEEK_FIELDS.weekBasedYear());
				int year2 = localDate.get(WeekFields.SUNDAY_START.weekBasedYear());
				localDate.get(DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear());
				Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(zone));
//				cal.setMinimalDaysInFirstWeek(4);
				cal.setTimeInMillis(instant.toEpochMilli());
				if (year != year2) {
					cal.set(Calendar.YEAR, year);
				}
				cal.set(Calendar.WEEK_OF_YEAR, weekOfYear);
				instant = cal.toInstant();
			}
		}

		String str = getDateTimeFormatter(pattern, zoneId).format(instant);
		return str;
	}

	private static DateFormatter getDateFormatter(String pattern, String zoneId) {
		DateFormatter formatter = new DateFormatter(pattern);
		zoneId = StringUtils.replace(zoneId, "UTC", "GMT");
		formatter.setTimeZone(TimeZone.getTimeZone(zoneId));
		return formatter;
	}

	private static DateTimeFormatter getDateTimeFormatter(String pattern, String zoneId) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.of(zoneId));
		return formatter;
	}

	/**
	 * Convert Time String From Pattern -> To Pattern
	 * @param timeStr		Time String
	 * @param fromPattern	From Pattern
	 * @param toPattern		To Pattern
	 * @return
	 */
	public static String toOtherPattern(String timeStr, String fromPattern, String toPattern) {
		if (ObjectUtils.isEmpty(timeStr)) {
			return timeStr;
		}
		LogicUtils.assertNotEmpty(fromPattern, "fromPattern");
		LogicUtils.assertNotEmpty(toPattern, "toPattern");
		Instant instant = toInstant(timeStr, fromPattern, Zone.GMT);
		String str = format(instant, toPattern, Zone.GMT);
		return str;
	}

	/**
	 * Convert Time String From TimeZone -> To TimeZone
	 * @param timeStr		Time String
	 * @param pattern		Time String Pattern
	 * @param fromZoneId	From Zone
	 * @param toZoneId		To Zone
	 * @return
	 */
	public static String toOtherZone(String timeStr, String pattern, String fromZoneId, String toZoneId) {
		if (ObjectUtils.isEmpty(timeStr)) {
			return timeStr;
		}

		LogicUtils.assertNotEmpty(pattern, "pattern");
		LogicUtils.assertNotEmpty(fromZoneId, "fromZoneId");
		LogicUtils.assertNotEmpty(toZoneId, "toZoneId");

		Instant instant = toInstant(timeStr, pattern, fromZoneId);
		String str = format(instant, pattern, toZoneId);
		return str;
	}

	public static String plus(String timeStr, String pattern, Duration duration) {
		String str = DateUtils2.format(DateUtils2.toInstant(timeStr, pattern, Zone.GMT).plus(duration), pattern, Zone.GMT);
		return str;
	}

//	/**
//	 * Get Week Of Year
//	 * @param timeStr	Time String
//	 * @param pattern	Time String Pattern
//	 * @return
//	 */
//	public static int getWeekOfYear(String timeStr, String pattern) {
//		if (ObjectUtils.isEmpty(timeStr)) {
//			return 0;
//		}
//		LogicUtils.assertNotEmpty(pattern, "pattern");
//
//		LocalDateTime localDate = LocalDateTime.parse(timeStr, getDateTimeFormatter(pattern, Zone.GMT));
//		int weekOfYear = localDate.get(DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear());
//		return weekOfYear;
//	}

	private static final List<TimeScale> ALL_TIMES_SCALES = Arrays.asList(TimeScale.values());

	public static Pair<String, String> toDateRange(TimeSetting timeSetting) {
		LogicUtils.assertNotNull(timeSetting, "timeSetting");
		ValueUtils.assertTimeSetting(timeSetting, ALL_TIMES_SCALES);

		String fromDate;
		String toDate;

		TimeScale timeScale = timeSetting.getTimeScale();
		String timeValue = timeSetting.getTimeValue();
		if (TimeScale.CUSTOM.equals(timeScale)) {
			fromDate = timeSetting.getFromDate();
			toDate = timeSetting.getToDate();
		} else if (TimeScale.YEAR.equals(timeScale)) {
			fromDate = timeValue + "0101";
			toDate = timeValue + "1231";
		} else if (TimeScale.HALF.equals(timeScale)) {
			String year = timeValue.substring(0, 4);
			int half = Integer.parseInt(timeValue.substring(4));
			if (half <= 1) {
				fromDate = year + "0101";
				toDate = year + "0630";
			} else {
				fromDate = year + "0701";
				toDate = year + "1231";
			}
		} else if (TimeScale.QUARTER.equals(timeScale)) {
			String year = timeValue.substring(0, 4);
			int quarter = Integer.parseInt(timeValue.substring(4));
			if (quarter <= 1) {
				fromDate = year + "0101";
				toDate = year + "0331";
			} else if (quarter == 2) {
				fromDate = year + "0401";
				toDate = year + "0630";
			} else if (quarter == 3) {
				fromDate = year + "0701";
				toDate = year + "0930";
			} else {
				fromDate = year + "1001";
				toDate = year + "1231";
			}
		} else if (TimeScale.MONTH.equals(timeScale)) {
			fromDate = timeValue + "01";
			Instant instant = DateUtils2.toInstant(fromDate, DatePattern.yyyyMMdd, Zone.GMT);
			instant = DateUtils2.plusMonths(instant, 1, Zone.GMT).minus(Duration.ofDays(1));
			toDate = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.GMT);
		} else if (TimeScale.WEEK.equals(timeScale)) {
			int year = Integer.parseInt(timeValue.substring(0, 4));
			int week = Integer.parseInt(timeValue.substring(4));
			// From Date
			{
				LocalDate localDate = LocalDate.ofYearDay(year, 1).with(DEFAULT_WEEK_FIELDS.weekOfYear(), week).with(DEFAULT_WEEK_FIELDS.dayOfWeek(), 1);
				Instant instant = localDate.atStartOfDay(ZoneId.of("GMT")).toInstant();
				fromDate = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.GMT);
			}
			// To Date
			{
				LocalDate localDate = LocalDate.ofYearDay(year, 1).with(DEFAULT_WEEK_FIELDS.weekOfYear(), week).with(DEFAULT_WEEK_FIELDS.dayOfWeek(), 7);
				Instant instant = localDate.atStartOfDay(ZoneId.of("GMT")).toInstant();
				toDate = DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.GMT);
			}
		} else if (TimeScale.DATE.equals(timeScale)) {
			fromDate = timeValue;
			toDate = timeValue;
		} else {
			throw new LogicException("DATE_RANGE_UNIMPLEMENTED", "Unimplemented Date Range: " + timeScale, new Property("timeScale", timeScale));
		}

		return Pair.of(fromDate, toDate);
	}

//	public static String toQuarter(TimeSetting timeSetting) {
//		if (TimeScale.MONTH.equals(timeSetting.getTimeScale())) {
//			return toQuarterByMonth(timeSetting.getTimeValue());
//		}
//		return null;
//	}

	public static String toQuarterByMonth(String month) {
		String year = month.substring(0, 4);
		int key = ValueUtils.toInteger(month.substring(4), 0);
		if (key <= 3) {
			return year + "01";
		} else if (key <= 6) {
			return year + "02";
		} else if (key <= 9) {
			return year + "03";
		} else {
			return year + "04";
		}
	}

	public static String toHalfByMonth(String month) {
		String year = month.substring(0, 4);
		int key = ValueUtils.toInteger(month.substring(4), 0);
		if (key <= 6) {
			return year + "01";
		} else {
			return year + "02";
		}
	}

	public static String toMonthByWeek(String week) {
		String value = ThreadUtils.getProp("DateUtils2.toMonthByWeek::" + week, () -> {
			return DateUtils2.toOtherPattern(week, DatePattern.YYYYww, DatePattern.yyyyMM);
//			Calendar cal = Calendar.getInstance();
//			String year = week.substring(0, 4);
//			cal.set(Calendar.YEAR, ValueUtils.toInteger(year, 0));
//			cal.set(Calendar.WEEK_OF_YEAR, ValueUtils.toInteger(week.substring(4), 0));
//			int month = cal.get(Calendar.MONTH) + 1;
//			return year + ValueUtils.pad(month, 2, "left", "0");
		});
		return value;
	}

	public static List<String> toMonths(TimeSetting timeSetting) {
		LogicUtils.assertNotNull(timeSetting, "timeSetting");
		ValueUtils.assertTimeSetting(timeSetting, ALL_TIMES_SCALES);

		TimeScale timeScale = timeSetting.getTimeScale();
		String timeValue = timeSetting.getTimeValue();

		List<String> values = ThreadUtils.getProp("DateUtils2.toMonths::" + timeScale.name() + "," + timeValue, () -> {
			List<String> months = new ArrayList<>();
			if (TimeScale.YEAR.equals(timeScale)) {
				String year = timeValue;
				months.add(year + "01");
				months.add(year + "02");
				months.add(year + "03");
				months.add(year + "04");
				months.add(year + "05");
				months.add(year + "06");
				months.add(year + "07");
				months.add(year + "08");
				months.add(year + "09");
				months.add(year + "10");
				months.add(year + "11");
				months.add(year + "12");
			} else if (TimeScale.HALF.equals(timeScale)) {
				String year = timeValue.substring(0, 4);
				int quarter = Integer.parseInt(timeValue.substring(4));
				if (quarter <= 1) {
					months.add(year + "01");
					months.add(year + "02");
					months.add(year + "03");
					months.add(year + "04");
					months.add(year + "05");
					months.add(year + "06");
				} else if (quarter == 2) {
					months.add(year + "07");
					months.add(year + "08");
					months.add(year + "09");
					months.add(year + "10");
					months.add(year + "11");
					months.add(year + "12");
				}
			} else if (TimeScale.QUARTER.equals(timeScale)) {
				String year = timeValue.substring(0, 4);
				int quarter = Integer.parseInt(timeValue.substring(4));
				if (quarter <= 1) {
					months.add(year + "01");
					months.add(year + "02");
					months.add(year + "03");
				} else if (quarter == 2) {
					months.add(year + "04");
					months.add(year + "05");
					months.add(year + "06");
				} else if (quarter == 3) {
					months.add(year + "07");
					months.add(year + "08");
					months.add(year + "09");
				} else {
					months.add(year + "10");
					months.add(year + "11");
					months.add(year + "12");
				}
			} else if (TimeScale.MONTH.equals(timeScale)) {
				months.add(timeValue);
			}
			return months;
		});
		return values;
	}

	public static List<String> toDays(TimeSetting timeSetting) {
		Pair<String, String> range = DateUtils2.toDateRange(timeSetting);
		String from = range.getFirst();
		String to = range.getSecond();
		if (ObjectUtils.isEmpty(from) || ObjectUtils.isEmpty(to)) {
			return new ArrayList<>();
		}

		List<String> days = new ArrayList<>();
		String value = from;
		do {
			days.add(value);
		} while ((value = DateUtils2.plus(value, DatePattern.yyyyMMdd, Duration.ofDays(1))).compareTo(to) <= 0);
		return days;
	}

	public static List<String> toWeeks(TimeSetting timeSetting) {
		LogicUtils.assertNotNull(timeSetting, "timeSetting");
		ValueUtils.assertTimeSetting(timeSetting, ALL_TIMES_SCALES);

		TimeScale timeScale = timeSetting.getTimeScale();
		String timeValue = timeSetting.getTimeValue();

		List<String> values = ThreadUtils.getProp("DateUtils2.toWeeks::" + timeScale.name() + "," + timeValue, () -> {
			List<String> weeks = new ArrayList<>();
			boolean started = false;
			if (TimeScale.YEAR.equals(timeScale)) {
				String year = timeValue;
				String date = year + "0101";
				String week = null;
				do {
					Instant instant = DateUtils2.toInstant(date, DatePattern.yyyyMMdd, Zone.GMT);
					week = DateUtils2.format(instant, DatePattern.YYYYww, Zone.GMT);
					if (week.startsWith(year)) {
						weeks.add(week);
						started = true;
					} else if (started) {
						break;
					}
					date = DateUtils2.format(instant.plus(Duration.ofDays(7)), DatePattern.yyyyMMdd, Zone.GMT);
				} while (date.startsWith(year) || week.startsWith(year));
			} else if (TimeScale.QUARTER.equals(timeScale)) {
				List<String> months = toMonths(timeSetting);
				String year = timeValue.substring(0, 4);
				String date = year + "0101";
				String week = null;
				do {
					Instant instant = DateUtils2.toInstant(date, DatePattern.yyyyMMdd, Zone.GMT);
					week = DateUtils2.format(instant, DatePattern.YYYYww, Zone.GMT);
					String month = DateUtils2.toMonthByWeek(week);
					if (months.contains(month)) {
						weeks.add(week);
						started = true;
					} else if (started) {
						break;
					}
					date = DateUtils2.format(instant.plus(Duration.ofDays(7)), DatePattern.yyyyMMdd, Zone.GMT);
				} while (date.startsWith(year) || week.startsWith(year));
			} else if (TimeScale.MONTH.equals(timeScale)) {
				String year = timeValue.substring(0, 4);
				String date = year + "0101";
				String week = null;
				do {
					Instant instant = DateUtils2.toInstant(date, DatePattern.yyyyMMdd, Zone.GMT);
					week = DateUtils2.format(instant, DatePattern.YYYYww, Zone.GMT);
					String month = DateUtils2.toMonthByWeek(week);
					if (timeValue.equals(month)) {
						weeks.add(week);
						started = true;
					} else if (started) {
						break;
					}
					date = DateUtils2.format(instant.plus(Duration.ofDays(7)), DatePattern.yyyyMMdd, Zone.GMT);
				} while (date.startsWith(year) || week.startsWith(year));
			} else if (TimeScale.WEEK.equals(timeScale)) {
				weeks.add(timeValue);
			}
			return weeks;
		});
		return values;
	}

	public static int getFinalWeekNo(String year) {
		String finalWeekStr = DateUtils2.toOtherPattern(year + "1231", DatePattern.yyyyMMdd, DatePattern.YYYYww);
		if (!finalWeekStr.startsWith(year)) {
			finalWeekStr = DateUtils2.toOtherPattern(year + "1224", DatePattern.yyyyMMdd, DatePattern.YYYYww);
		}
		int finalWeek = ValueUtils.toInteger(finalWeekStr.substring(4), 0);
		return finalWeek;
	}

}
