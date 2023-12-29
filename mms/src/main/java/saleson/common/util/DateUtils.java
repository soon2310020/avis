package saleson.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.*;
import java.util.*;

import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;

import org.springframework.data.util.Pair;
import org.springframework.util.ObjectUtils;
import saleson.common.enumeration.DateViewType;
import saleson.dto.common.TwoObject;
import saleson.model.DayShift;

public class DateUtils {

	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	public static final String DEFAULT_DATE_FORMAT = "yyyyMMddHHmmss";
	public static final String YYYY_MM_DD_DATE_FORMAT = "yyyyMMdd";
	public static final String yyyyMMddHH = "yyyyMMddHH";
	public static final String yyyyMM = "yyyyMM";
	public static final String yyMMdd = "yyMMdd";
	public static final WeekFields DEFAULT_WEEK_FIELDS = WeekFields.of(DayOfWeek.SUNDAY, 4);
	public static final String MMM = "MMM";
	public static final String MMMM = "MMMM";
	public static final String YYYY_MM_dd = "yyyy-MM-dd";
	public static final String HH_mm = "HH:mm";
	public static final String HH = "HH";
	public static final String HH_mm_ss = "HH:mm:ss";

	public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

	/**
	 * TODO replace with DateUtils2.format(instant, DatePattern.yyyy_MM_dd, Zone.SYS, "")
	 * @param instant
	 * @return
	 * @deprecated		use DateUtils2.format instead
	 */
	@Deprecated
	public static String getDate(Instant instant) {
		if (instant == null) {
			return "";
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
		return formatter.format(instant);
	}

	/**
	 * TODO replace with DateUtils2.format(instant, pattern, Zone.SYS, "")
	 * @param instant
	 * @param pattern
	 * @return
	 * @deprecated		use DateUtils2.format instead
	 */
	@Deprecated
	public static String getDate(Instant instant, String pattern) {
		if (instant == null) {
			return "";
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault());
		return formatter.format(instant);
	}

	/**
	 * TODO replace with DateUtils2.format(instant, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS, "")
	 * @param instant
	 * @return
	 * @deprecated		use DateUtils2.format instead
	 */
	@Deprecated
	public static String getDateTime(Instant instant) {
		if (instant == null) {
			return "";
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault());
		return formatter.format(instant);
	}

	/**
	 * TODO replace with DateUtils2.format(instant, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT, "")
	 * @param instant
	 * @return
	 * @deprecated		use DateUtils2.format instead
	 */
	@Deprecated
	public static String getUTCDateTime(Instant instant) {
		if (instant == null) {
			return "";
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withLocale(DEFAULT_LOCALE).withZone(ZoneOffset.UTC);
		return formatter.format(instant);
	}

	/**
	 * TODO Nouse, Remove it
	 * @param date
	 * @param formatter
	 * @return
	 * @deprecated		remove it
	 */
	@Deprecated
	private static int getWeekOfYear(String date, DateTimeFormatter formatter) {
		try {
			LocalDate localDate = LocalDate.parse(date, formatter);
//			TemporalField woy = WeekFields.of(DEFAULT_LOCALE).weekOfWeekBasedYear();
			TemporalField woy = DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear();
			return localDate.get(woy);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * TODO replace with DateUtils2.getWeekOfYear(date, DatePattern.yyyyMMddHHmmss)
	 * @param date
	 * @return
	 * @deprecated	use DateUtils2.getWeekOfYear instead
	 */
	@Deprecated
	public static int getWeekOfYear(String date) {
		try {
			LocalDateTime localDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
			TemporalField woy = DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear();
			return localDate.get(woy);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * TODO Nouse, Remove it
	 * @param date
	 * @param format
	 * @return
	 * @deprecated		remove it
	 */
	@Deprecated
	private static int getWeekOfYear(String date, String format) {
		try {
			LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
			TemporalField woy = DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear();
			return localDate.get(woy);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * TODO replace with DateUtils2.toOtherPattern(date, DatePattern.yyyyMMddHHmmss, DatePattern.YYYYww)
	 * @param date
	 * @return
	 * @deprecated	use DateUtils2.toOtherPattern instead
	 */
	@Deprecated
	public static String getYearWeek(String date) {
		int weekNumber = getWeekOfYear(date);
		String week = weekNumber < 10 ? "0" + weekNumber : "" + weekNumber;

		String year = getYear(date);
		String month = getYearMonth(date).substring(4, 6);

		if (weekNumber < 10 && Integer.valueOf(month) == 12) {
			Integer yearNumb = Integer.valueOf(year) + 1;
			year = yearNumb.toString();
		}

		return year + week;
	}

	/**
	 * TODO replace with DateUtils2.toOtherPattern(date, format, DatePattern.YYYYww)
	 * @param date
	 * @param format
	 * @return
	 * @deprecated		use DateUtils2.toOtherPattern instead
	 */
	@Deprecated
	public static String getYearWeek(String date, String format) {
		int weekNumber = getWeekOfYear(date, format);
		String week = weekNumber < 10 ? "0" + weekNumber : "" + weekNumber;
		String year = getYear(date);
		String month = getYearMonth(date).substring(4, 6);

		if (weekNumber < 10 && Integer.valueOf(month) == 12) {
			Integer yearNumb = Integer.valueOf(year) + 1;
			year = yearNumb.toString();
		} else if (weekNumber >= 52 && Integer.valueOf(month) == 1) {
			Integer yearNumb = Integer.valueOf(year) - 1;
			year = yearNumb.toString();
		}

		return year + week;
	}

	/**
	 * TODO replace with DateUtils2.format(instant, pattern, Zone.SYS)
	 * @param instant
	 * @param pattern
	 * @return
	 * @deprecated		use DateUtils2.format instead
	 */
	@Deprecated
	public static String getYearWeek(Instant instant, String pattern) {
		String date = DateTimeFormatter.ofPattern(pattern).withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault()).format(instant);
		return getYearWeek(date, pattern);
	}

	/**
	 * TODO replace with DateUtils2.toOtherPattern(date, format, DatePattern.ww_YYYY)
	 * @param date
	 * @param format
	 * @return
	 * @deprecated		use DateUtils2.toOtherPattern instead
	 */
	@Deprecated
	public static String getWeekYear(String date, String format) {
		int weekNumber = getWeekOfYear(date, format);
		String week = weekNumber < 10 ? "0" + weekNumber : "" + weekNumber;
		if (format.equalsIgnoreCase("MMM dd, yyyy"))
			return week + " " + date.substring(8, format.length());
		return week + ", " + getYear(date);
	}

	@Deprecated
	/**
	 * TODO Nouse, Remove it
	 * @param date
	 * @return
	 * @deprecated	remove it
	 */
	private static String getWeekYear(String date) {
		int weekNumber = getWeekOfYear(date, DEFAULT_DATE_FORMAT);
		String week = weekNumber < 10 ? "0" + weekNumber : "" + weekNumber;
		return week + " " + getYear(date);
	}

	/**
	 * TODO replace with ValueUtils.abbreviate(date, 4)
	 * @param date
	 * @return
	 * @deprecated	use ValueUtils.abbreviate instead
	 */
	@Deprecated
	public static String getYear(String date) {
		if (date == null) {
			return "";
		}
		if (date.length() >= 4) {
			return date.substring(0, 4);
		}
		return "";
	}

	/**
	 * TODO replace with DateUtils2.format(instant, DatePattern.yyyy, Zone.SYS, "")
	 * @param date
	 * @return
	 * @deprecated	use DateUtils2.format instead
	 */
	@Deprecated
	public static String getYear(Instant date) {
		return getYear(getDate(date, DEFAULT_DATE_FORMAT));
	}

	/**
	 * TODO replace with ValueUtils.abbreviate(date, 6)
	 * @param date
	 * @return
	 * @deprecated	use ValueUtils.abbreviate instead
	 */
	@Deprecated
	public static String getYearMonth(String date) {
		if (date == null) {
			return "";
		}
		if (date.length() >= 6) {
			return date.substring(0, 6);
		}
		return "";
	}

	/**
	 * TODO replace with DateUtils2.format(instant, pattern, Zone.SYS, "")
	 * @param instant
	 * @param pattern
	 * @return
	 * @deprecated		use DateUtils2.format instead
	 */
	@Deprecated
	public static String getYearMonth(Instant instant, String pattern) {
		String date = DateTimeFormatter.ofPattern(pattern).withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault()).format(instant);
		return getYearMonth(date);
	}

	/**
	 * TODO replace with ValueUtils.abbreviate(date, 8)
	 * @param date
	 * @return
	 * @deprecated	use ValueUtils.abbreviate instead
	 */
	@Deprecated
	public static String getDay(String date) {
		if (date == null) {
			return "";
		}
		if (date.length() >= 8) {
			return date.substring(0, 8);
		}
		return "";
	}

	/**
	 * TODO replace with DateUtils2.format(instant, DatePattern.yyyyMMdd, Zone.SYS)
	 * @param date
	 * @return
	 * @deprecated	use DateUtils2.format instead
	 */
	@Deprecated
	public static String getDay(Instant date) {
		return getDay(getDate(date, DEFAULT_DATE_FORMAT));
	}

	public static int getDayOfWeek(Instant day) {
		Calendar c = Calendar.getInstance();
		c.setTime(Date.from(day));
		return c.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * TODO replace with ValueUtils.abbreviate(date, 10)
	 * @param date
	 * @return
	 * @deprecated	use DateUtils2.toOtherPattern instead
	 */
	@Deprecated
	public static String getHour(String date) {
		if (date == null) {
			return "";
		}
		if (date.length() >= 10) {
			return date.substring(0, 10);
		}
		return "";
	}

	/**
	 * TODO replace with DateUtils2.format(instant, DatePattern.yyyyMMddHH, Zone.SYS)
	 * @param date
	 * @return
	 * @deprecated	use DateUtils2.format instead
	 */
	@Deprecated
	public static String getHour(Instant date) {
		return getHour(getDate(date, DEFAULT_DATE_FORMAT));
	}

	/**
	 * TODO replace with DateUtils2.newString(pattern, Zone.SYS)
	 * @param pattern
	 * @return
	 * @deprecated use DateUtils2.newString instead
	 */
	@Deprecated
	public static String getToday(String pattern) {
		return DateTimeFormatter.ofPattern(pattern).withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault()).format(Instant.now()); //"yyyyMMddHHmmss"
	}

	/**
	 * TODO replace with DateUtils2.newString(DatePattern.yyyyMMddHHmmss, Zone.SYS)
	 * @return
	 * @deprecated use DateUtils2.newString instead
	 */
	@Deprecated
	public static String getToday() {
		return getToday(DEFAULT_DATE_FORMAT); //"yyyyMMddHHmmss"
	}

	/**
	 * TODO replace with DateUtils2.toInstant(date, pattern, Zone.SYS)
	 * @param date
	 * @param pattern
	 * @return
	 * @deprecated		use DateUtils2.toInstant instead
	 */
	@Deprecated
	public static Instant getInstant(String date, String pattern) {
		if (YYYY_MM_DD_DATE_FORMAT.equals(pattern)) {
			pattern = DEFAULT_DATE_FORMAT;
			date += "000000";
		}
		return getInstant(date, pattern, DEFAULT_LOCALE);
	}

	/**
	 * TODO Nouse, Remove it
	 * @param date
	 * @param pattern
	 * @param locale
	 * @return
	 * @deprecated		remove it
	 */
	@Deprecated
	private static Instant getInstant(String date, String pattern, Locale locale) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withLocale(locale).withZone(ZoneId.systemDefault());

		LocalDateTime lastShotTime = LocalDateTime.parse(date, formatter);
		Instant instant = lastShotTime.toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
		return instant;
	}

	public static LocalDate getLocalDate(String pattern, String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault());

		return LocalDate.parse(date, formatter);
	}

	public static LocalDate getLocalDate(String date) {
		return getLocalDate("yyyyMMdd", date);
	}

	public static String getYesterday(String pattern) {
		return DateTimeFormatter.ofPattern(pattern).withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault()).format(Instant.now().minus(Duration.ofDays(1)));
	}

	public static String getYesterday() {
		return getYesterday(DEFAULT_DATE_FORMAT);
	}

	public static String getNextDay(Instant currentDay, String pattern, Integer plusDay){
		return DateTimeFormatter.ofPattern(pattern)
				.withLocale(DEFAULT_LOCALE)
				.withZone(ZoneId.systemDefault())
				.format(currentDay.plus(Duration.ofDays(plusDay)));
	}

	public static String getPreviousDay(String pattern, Integer minusDay) {
		return DateTimeFormatter.ofPattern(pattern).withLocale(DEFAULT_LOCALE).withZone(ZoneId.systemDefault()).format(Instant.now().minus(Duration.ofDays(minusDay)));
	}

	public static String getPreviousDay(Integer minusDay) {
		return getPreviousDay(DEFAULT_DATE_FORMAT, minusDay);
	}

	public static int getLengthOfMonth(String date, String format) {
		try {
			LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
			return localDate.lengthOfMonth();
		} catch (Exception e) {
			return 0;
		}
	}

	public static String getReducedTime(String dateTime, String pattern, Long reducedTime, ChronoUnit chronoUnit) {
		Instant dateTimeStamp = getInstant(dateTime, pattern);
		Instant reducedTimeStamp = dateTimeStamp.minus(reducedTime, chronoUnit);
		return getDate(reducedTimeStamp, pattern);
	}

	public static String getAddedTime(String dateTime, String pattern, Long addedTime, ChronoUnit chronoUnit) {
		Instant dateTimeStamp = getInstant(dateTime, pattern);
		Instant addedTimeStamp = dateTimeStamp.plus(addedTime, chronoUnit);
		return getDate(addedTimeStamp, pattern);
	}

	public static String getReducedDate(String date, Long reducedDays) { // pattern "yyyyMMdd"
		String dateTime = date + "000000";
		String reducedDate = getReducedTime(dateTime, "yyyyMMddHHmmss", reducedDays, ChronoUnit.DAYS);
		return reducedDate.substring(0, 8);
	}

	public static String getPreviousMonth(String startDate) { // pattern "yyyyMMdd"
		String year = startDate.substring(0, 4);
		Integer yearIntValue = Integer.valueOf(year);
		String month = startDate.substring(4, 6);
		Integer monthIntValue = Integer.valueOf(month);
		if (monthIntValue == 1) {
			return (yearIntValue - 1) + "12";
		} else {
			Integer previousMonth = monthIntValue - 1;
			return year + (previousMonth < 10 ? "0" + previousMonth : previousMonth);
		}
	}

	public static String getPreviousHour(String hour){
		Instant instant = getInstant(hour, yyyyMMddHH);
		return getHour(instant.minus(60, ChronoUnit.MINUTES));
	}

	public static String getPreviousWeek(String startDate, String currentWeek) {
		String previousDay = getReducedDate(startDate, 1L);
		String year = currentWeek.substring(0, 4);
		Integer yearIntValue = Integer.valueOf(year);
		Integer currentWeekIntValue = Integer.valueOf(currentWeek.substring(4, 6));
		Integer weekOfYear = getWeekOfYear(previousDay + "000000");
		if (weekOfYear > currentWeekIntValue) {
			return String.valueOf(yearIntValue - 1) + (weekOfYear < 10 ? "0" + weekOfYear : weekOfYear);
		}
		return year + (weekOfYear < 10 ? "0" + weekOfYear : weekOfYear);
	}

	public static String getReducedMoment(String moment, DateViewType dateViewType, Long reducedValue) {
		if (dateViewType.equals(DateViewType.DAY)) {
			return getReducedDate(moment, reducedValue - 1); // include both start and end
		} else if (dateViewType.equals(DateViewType.WEEK) || dateViewType.equals(DateViewType.MONTH)) {
			if (moment.length() >= 6) {
				String yearString = moment.substring(0, 4);
				String weekOrMonthString = moment.substring(4, 6);
				Integer weekOrMonthNumb = Integer.valueOf(weekOrMonthString);
				Integer weekOrMonthReduced = weekOrMonthNumb - reducedValue.intValue() + 1; // start and end included
				if (weekOrMonthReduced > 0) {
					return yearString + (weekOrMonthReduced >= 10 ? weekOrMonthReduced.toString() : "0" + weekOrMonthReduced);
				} else {
					Integer previousYearNumb = Integer.valueOf(yearString) - 1;
					if (dateViewType.equals(DateViewType.MONTH)) {
						weekOrMonthReduced += 12; // max month - remaining month
					} else {
						int weekOfYear = getLastWeekOfYear(previousYearNumb);
						weekOrMonthReduced += weekOfYear;
					}
					String weekOrMonthReducedString = weekOrMonthReduced >= 10 ? weekOrMonthReduced.toString() : "0" + weekOrMonthReduced;
					return previousYearNumb.toString() + weekOrMonthReducedString;
				}
			}
		}
		return null;
	}

	public static int getLastWeekOfYear(int year) {
		LocalDate keyDayOfNextYear = LocalDate.of(year + 1, 1, 1);
		LocalDate lastDateWeek = keyDayOfNextYear.get(DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear()) == 1 ? keyDayOfNextYear.minusWeeks(1) : keyDayOfNextYear;
//		return lastDateWeek.get(WeekFields.of(DEFAULT_LOCALE).weekOfWeekBasedYear());
		return lastDateWeek.get(DEFAULT_WEEK_FIELDS.weekOfWeekBasedYear());
	}

	@Deprecated
	/**
	 * TODO Nouse, Remove it
	 * @param pattern
	 * @param dateStr
	 * @return
	 * @deprecated		remove it
	 */
	public static org.joda.time.DateTime getDateTimeFromString(String pattern, String dateStr) {
		org.joda.time.format.DateTimeFormatter formatter = DateTimeFormat.forPattern(pattern);
		return formatter.parseDateTime(dateStr);
	}

	public static List<String> getListStringDateBetween(String start, String end, String inputPattern, String outputPattern) {
		org.joda.time.format.DateTimeFormatter inputFormatter = DateTimeFormat.forPattern(inputPattern);
		org.joda.time.format.DateTimeFormatter outputFormatter = DateTimeFormat.forPattern(outputPattern);
		DateTime startDateTime = inputFormatter.parseDateTime(start);
		DateTime endDateTime = inputFormatter.parseDateTime(end.endsWith("000000") ? end : end.substring(0, 8) + "235959");

		List<String> datesBetween = new ArrayList<>();

		while (startDateTime.compareTo(endDateTime) < 0) {
			datesBetween.add(outputFormatter.print(startDateTime));
			DateTime dateBetween = startDateTime.plusDays(1);
			startDateTime = dateBetween;
		}
		return datesBetween;
	}

	public static Calendar getCalendar() {
		Calendar calendar = Calendar.getInstance();
		calendar.setMinimalDaysInFirstWeek(4);
		return calendar;
	}

	public static Calendar getFirstDayOfLastWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		return calendar;
	}

	public static Calendar getLastDayOfLastWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -7);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		return calendar;
	}

	public static Calendar getFirstDayOfLastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		return calendar;
	}

	public static Calendar getLastDayOfLastMonth() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
		return calendar;
	}

	public static Calendar getFirstDayOfNextMonth(Instant time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time.toEpochMilli());
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
		return calendar;
	}

	public static Calendar getFirstDayOfLastYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		calendar.set(Calendar.DAY_OF_YEAR, 1);
		return calendar;
	}

	public static Calendar getFirstDayOfNextYear(Instant time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time.toEpochMilli());
		calendar.add(Calendar.YEAR, 1);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMinimum(Calendar.DAY_OF_YEAR));
		return calendar;
	}

	public static Calendar getLastDayOfLastYear() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -1);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
		return calendar;
	}

	public static void main(String[] args) {
		Instant date = DateUtils.getInstant("20210101000000", DateUtils.DEFAULT_DATE_FORMAT);
		System.out.println(date);
		System.out.println(DateUtils.getDate(date, DateUtils.YYYY_MM_DD_DATE_FORMAT));
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(date.toEpochMilli());
		cal.set(Calendar.DAY_OF_MONTH, 0);
		System.out.println(DateUtils.getDate(cal.toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT));

	}

	public static Long getCeilRoundedYearBetween(Instant from, Instant to) {
		long days = ChronoUnit.DAYS.between(from, to);
		return (long) Math.ceil((double) days/365);
	}

	public static Long getCeilRoundedMonthBetween(Instant from, Instant to) {
		long days = ChronoUnit.DAYS.between(from, to);
		return (long) Math.ceil((double) days/30);
	}

	public static Instant plusYears(Instant time, int year) {
		if (year > 0){
			DateTime dateTime = new DateTime(Date.from(time));
			return Instant.ofEpochMilli(dateTime.plusYears(year).getMillis());
		}

		return time;
	}

	public static Instant plusMonths(Instant time, int month) {
		if (month > 0) {
			DateTime dateTime = new DateTime(Date.from(time));
			return Instant.ofEpochMilli(dateTime.plusMonths(month).getMillis());
		}

		return time;
	}

	public static Instant mapTimeToDate(String dateString, Integer hour, String patternDate) {
		Calendar calendar = Calendar.getInstance();
		Instant instant = getInstant(dateString, patternDate);
		calendar.setTime(Date.from(instant));
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		return calendar.toInstant();
	}

	public static boolean checkTimeInDay(Instant time, String day) {
		String timeString = getDate(time, "yyyyMMdd");
		return timeString.equals(day);
	}

	public static int getHourOfInstant(Instant time) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(time, ZoneOffset.systemDefault());
		return localDateTime.getHour();
	}

	public static Instant getEndOffHour(Instant time) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(time,ZoneOffset.systemDefault());
		localDateTime = localDateTime.withMinute(59).withSecond(59);
		return localDateTime.atZone(ZoneOffset.systemDefault()).toInstant();
	}

	public static String getPeriodDate(Instant start, Instant end) {
		ValueUtils.assertNotEmpty(start, "Start time");
		ValueUtils.assertNotEmpty(end, "End time");

//		LocalDate startDate = LocalDate.ofInstant(start, ZoneId.systemDefault());
		LocalDate startDate = start.atZone(ZoneId.systemDefault()).toLocalDate();
//		LocalDate endDate = LocalDate.ofInstant(end, ZoneId.systemDefault());
		LocalDate endDate = end.atZone(ZoneId.systemDefault()).toLocalDate();

		Period diff = Period.between(startDate, endDate);
		StringBuilder periodStr = new StringBuilder();
		int days = diff.getDays();
		int months = diff.getMonths();
		int years = diff.getYears();
		if (years > 0) {
			periodStr.append(years + " year" + (years > 1 ? "s" : ""));
		}
		if (months > 0) {
			if (periodStr.length() > 0) periodStr.append(", ");
			periodStr.append(months + " month" + (months > 1 ? "s" : ""));
		}
		if (days > 0) {
			if (periodStr.length() > 0) periodStr.append(", ");
			periodStr.append(days + " day" + (days > 1 ? "s" : ""));
		}
		return periodStr.toString();
	}

	public static String getDurationDate(Instant start, Instant end) {
		ValueUtils.assertNotEmpty(start, "Start time");
		ValueUtils.assertNotEmpty(end, "End time");

//		LocalDate startDate = LocalDate.ofInstant(start, ZoneId.systemDefault());
		LocalDate startDate = start.atZone(ZoneId.systemDefault()).toLocalDate();
//		LocalDate endDate = LocalDate.ofInstant(end, ZoneId.systemDefault());
		LocalDate endDate = end.atZone(ZoneId.systemDefault()).toLocalDate();

		Duration duration = Duration.between(start, end);
		Period period = Period.between(startDate, endDate);
		StringBuilder periodStr = new StringBuilder();
		int days = period.getDays();
		int months = period.getMonths();
		int years = period.getYears();
		long hours = duration.toHours() % 24;
		long minutes = duration.toMinutes() % 60;
		if (years > 0) {
			periodStr.append(years).append(" year").append(years > 1 ? "s" : "");
		}
		if (months > 0) {
			if (periodStr.length() > 0) periodStr.append(", ");
			periodStr.append(months).append(" month").append(months > 1 ? "s" : "");
		}
		if (days > 0) {
			if (periodStr.length() > 0) periodStr.append(", ");
			periodStr.append(days).append(" day").append(days > 1 ? "s" : "");
		}
		if (hours > 0) {
			if (periodStr.length() > 0) periodStr.append(", ");
			periodStr.append(hours).append(" hour").append(hours > 1 ? "s" : "");
		}
		if (minutes > 0) {
			if (periodStr.length() > 0) periodStr.append(", ");
			periodStr.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
		}
		return periodStr.toString();
	}

	public static String convertStringDateFormat(String dateString, String fromPattern, String toPattern) {
		try {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromPattern);
			Date date = simpleDateFormat.parse(dateString);
			return new SimpleDateFormat(toPattern).format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Instant plusDays(Instant time, int days) {
		DateTime dateTime = new DateTime(Date.from(time));
		return Instant.ofEpochMilli(dateTime.plusDays(days).getMillis());
	}

	public static Instant pushWeek(Instant time, int weeks) {
		LocalDate date = time.atZone(ZoneId.systemDefault()).toLocalDate();
		return date.plusWeeks(weeks).atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

	public static Instant withDayOfWeek(Instant time, saleson.common.enumeration.DayOfWeek dayOfWeek) {
		LocalDate date = time.atZone(ZoneId.systemDefault()).toLocalDate();
		return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.valueOf(dayOfWeek.name()))).atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

	public static Instant pushMonth(Instant time, int month) {
		LocalDate date = time.atZone(ZoneId.systemDefault()).toLocalDate();
		return date.plusMonths(month).atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

	public static Instant withDayOfMonth(Instant time, saleson.common.enumeration.DayOfWeek dayOfWeek, Integer schedOrdinalNum) {
		LocalDate date = time.atZone(ZoneId.systemDefault()).toLocalDate();
		return LocalDate.of(date.getYear(), date.getMonth(), 1)
				.with(TemporalAdjusters.dayOfWeekInMonth(schedOrdinalNum, dayOfWeek.getDayOfWeek())).atStartOfDay(ZoneId.systemDefault()).toInstant();
	}

/*
	public static Pair<Instant, Instant> getWeekDateRange(String examWeek) {
		// Parse the input string as a year-week value
		int year = Integer.parseInt(examWeek.substring(0, 4));
		int week = Integer.parseInt(examWeek.substring(4));

		// Create a WeekFields object for ISO week numbering
		WeekFields weekFields = DEFAULT_WEEK_FIELDS;

		// Calculate the date of the first day of the specified week
		LocalDate date = LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week).with(weekFields.dayOfWeek(), 1);
		Instant fromDate = date.atStartOfDay().toInstant(TimeZone.getTimeZone(DateUtils2.Zone.SYS));

		// Calculate the date of the last day of the specified week
		date = LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week).with(weekFields.dayOfWeek(), 7);
		Instant toDate = date.atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC); // Add one day to get the end of the day

		return Pair.of(fromDate, toDate);
	}
*/

	public static Pair<Instant, Instant> getWeekDateRange(String examWeek, String zoneId) {
		TimeZone timeZone =TimeZone.getTimeZone(zoneId);
		// Parse the input string as a year-week value
		int year = Integer.parseInt(examWeek.substring(0, 4));
		int week = Integer.parseInt(examWeek.substring(4));

		// Create a WeekFields object for ISO week numbering
		WeekFields weekFields = DEFAULT_WEEK_FIELDS;

		// Calculate the date of the first day of the specified week
		LocalDate date = LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week).with(weekFields.dayOfWeek(), 1);
		Instant fromDate = date.atStartOfDay(timeZone.toZoneId()).toInstant();

		// Calculate the date of the last day of the specified week
		date = LocalDate.ofYearDay(year, 1).with(weekFields.weekOfYear(), week).with(weekFields.dayOfWeek(), 7);
		Instant toDate = date.atStartOfDay(timeZone.toZoneId()).plusDays(1).toInstant(); // Add one day to get the end of the day

		return Pair.of(fromDate, toDate);
	}

	public static String getPrevTimeValue(String timeValue, TimeScale timeScale) {
		if (ObjectUtils.isEmpty(timeValue)) {
			return null;
		}

		String value = null;
		if (TimeScale.YEAR.equals(timeScale)) {
			Instant instant = DateUtils2.toInstant(timeValue, DateUtils2.DatePattern.yyyy, DateUtils2.Zone.SYS);
			instant = DateUtils2.plusMonths(instant, -12, DateUtils2.Zone.SYS);
			value = DateUtils2.format(instant, DateUtils2.DatePattern.yyyy, DateUtils2.Zone.SYS);
		} else if (TimeScale.MONTH.equals(timeScale)) {
			Instant instant = DateUtils2.toInstant(timeValue, DateUtils2.DatePattern.yyyyMM, DateUtils2.Zone.SYS);
			instant = DateUtils2.plusMonths(instant, -1, DateUtils2.Zone.SYS);
			value = (DateUtils2.format(instant, DateUtils2.DatePattern.yyyyMM, DateUtils2.Zone.SYS));
		} else if (TimeScale.WEEK.equals(timeScale)) {
			Instant instant = DateUtils2.toInstant(timeValue, DateUtils2.DatePattern.YYYYww, DateUtils2.Zone.SYS);
			instant = instant.minus(Duration.ofDays(7));
			value = (DateUtils2.format(instant, DateUtils2.DatePattern.YYYYww, DateUtils2.Zone.SYS));
		} else if (TimeScale.DATE.equals(timeScale)) {
			Instant instant = DateUtils2.toInstant(timeValue, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
			instant = instant.minus(Duration.ofDays(1));
			value = (DateUtils2.format(instant, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS));
		}
		return value;
	}

	public static Pair<String, String> getPrevTimeRange(String fromDate, String toDate) {
		Instant fromInst = DateUtils2.toInstant(fromDate, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
		Instant toInst = DateUtils2.toInstant(toDate, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS);
		long diff = ChronoUnit.DAYS.between(fromInst, toInst) + 1;
		Instant fromInstPrev = fromInst.minus(diff, ChronoUnit.DAYS);
		Instant toInstPrev = toInst.minus(diff, ChronoUnit.DAYS);
		String fromDatePrev = (DateUtils2.format(fromInstPrev, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS));
		String toDatePrev = (DateUtils2.format(toInstPrev, DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS));
		return Pair.of(fromDatePrev, toDatePrev);
	}
	public static Instant getEndOffDay(Instant time) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(time,ZoneOffset.systemDefault());
		localDateTime = localDateTime.withHour(23).withMinute(59).withSecond(59);
		return localDateTime.atZone(ZoneOffset.systemDefault()).toInstant();
	}

	public static boolean inRange(Instant time, Instant from,Instant to) {
		if (time.isAfter(from) && time.isBefore(to)) {
			return true;
		}
		return false;
	}

}
