package vn.com.twendie.avis.api.core.util;

import org.springframework.stereotype.Component;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

@Component
public class DateUtils {

    public static final String UTC_TIME_ZONE = "UTC";
    public static final String LOCAL_TIME_ZONE = "Asia/Ho_Chi_Minh";
    public static final String T_MEDIUM_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String MEDIUM_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String SHORT_PATTERN = "dd/MM/yyyy";
    public static final String R_SHORT_PATTERN = "yyyy-MM-dd";
    public static final int NUMBER_DAYS_OF_MONTH = 30;
    public static final String FULL_TIME_PATTERN = "HH:mm:ss";
    public static final String HOUR_SHORT_PATTERN = "HH:mm";
    public static final String MONTH_YEAR_PATTERN = "MMMM yyyy";

    public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
    public static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    public Timestamp startOfToday() {
        return startOfToday(LOCAL_TIME_ZONE);
    }

    public Timestamp startOfToday(String timezone) {
        return startOfDay(now(), timezone);
    }

    public Timestamp startOfDay(Timestamp timestamp) {
        return startOfDay(timestamp, LOCAL_TIME_ZONE);
    }

    public Timestamp startOfDay(Timestamp timestamp, String timezone) {

        return Objects.isNull(timestamp) ? null : startOfDay(timestamp.getTime(), timezone);

    }

    public Timestamp startOfDay(long timestampMilliSec) {

        return startOfDay(timestampMilliSec, LOCAL_TIME_ZONE);

    }

    public Timestamp startOfDay(long timestampMilliSec, String timezone) {

        Instant instant = Instant.ofEpochMilli(timestampMilliSec);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return new Timestamp(zonedDateTime.toInstant().toEpochMilli());

    }

    public Timestamp endOfToday() {
        return endOfToday(LOCAL_TIME_ZONE);
    }

    public Timestamp endOfToday(String timezone) {
        return endOfDay(now(), timezone);
    }

    public Timestamp endOfDay(Timestamp timestamp) {
        return endOfDay(timestamp, LOCAL_TIME_ZONE);
    }

    public Timestamp endOfDay(Timestamp timestamp, String timezone) {

        return Objects.isNull(timestamp) ? null : endOfDay(timestamp.getTime(), timezone);

    }

    public boolean isBetween(Timestamp input, Timestamp from, Timestamp to) {
        return from.getTime() <= input.getTime() && input.getTime() <= to.getTime();
    }

    public boolean isBetweenButNotEqualMax(Timestamp input, Timestamp from, Timestamp to) {
        return from.getTime() <= input.getTime() && input.getTime() < to.getTime();
    }

    public Timestamp endOfDay(long timestampMilliSec) {

        return endOfDay(timestampMilliSec, LOCAL_TIME_ZONE);

    }

    public Timestamp endOfDay(long timestampMilliSec, String timezone) {

        Instant instant = Instant.ofEpochMilli(timestampMilliSec);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999999999);
        return new Timestamp(zonedDateTime.toInstant().toEpochMilli());

    }

    public long getTimeBetween(Timestamp t1, Timestamp t2) {
        return t2.getTime() - t1.getTime();
    }

    public long getTimeBetween(Time t1, Time t2) {
        return t2.getTime() - t1.getTime();
    }

    public long getMinuteBetween(Time t1, Time t2) {
        return MINUTES.convert(getTimeBetween(t1, t2), MILLISECONDS);
    }

    public long getDaysBetween(Timestamp t1, Timestamp t2) {
        return DAYS.convert(getTimeBetween(startOfDay(t1), startOfDay(t2)), MILLISECONDS);
    }

    public int getMonthsBetween(Timestamp t1, Timestamp t2) {
        return (int) Math.ceil(getDaysBetween(t1, t2) / (double) NUMBER_DAYS_OF_MONTH);
    }

    public java.sql.Date getDate(Date date) {
        return getDate(date, LOCAL_TIME_ZONE);
    }

    public java.sql.Date getDate(Date date, String timezone) {
        return java.sql.Date.valueOf(format(date, R_SHORT_PATTERN, timezone));
    }

    public Time getTime(Date date) {
        return getTime(date, LOCAL_TIME_ZONE);
    }

    public Time getTime(Date date, String timezone) {
        return Objects.nonNull(date) ? Time.valueOf(format(date, FULL_TIME_PATTERN, timezone)) : null;
    }

    public Time roundTime(Time time, TimeUnit timeUnit) {
        long duration = timeUnit.convert(time.getTime(), MILLISECONDS);
        return new Time(MILLISECONDS.convert(duration, timeUnit));
    }

    public Date parse(String source, String pattern) throws ParseException {
        return new SimpleDateFormat(pattern).parse(source);
    }

    public Date parseWithTimeZone(String source, String pattern) throws ParseException {
        SimpleDateFormat simpleDateFormat= new SimpleDateFormat(pattern);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(LOCAL_TIME_ZONE));
        return simpleDateFormat.parse(source);
    }

    public String format(Date date, String pattern) {
        return format(date.getTime(), pattern, LOCAL_TIME_ZONE);
    }

    public String format(Date date, String pattern, String timezone) {
        return format(date.getTime(), pattern, timezone);
    }

    public String format(long timestampMilliSec, String pattern) {
        return format(timestampMilliSec, pattern, LOCAL_TIME_ZONE);
    }

    public String format(long timestampMilliSec, String pattern, String timezone) {
        Instant instant = Instant.ofEpochMilli(timestampMilliSec);
        ZoneId zoneId = ZoneId.of(timezone);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return zonedDateTime.format(formatter);
    }

    public Timestamp add(Timestamp t, long milliSecs) {
        return new Timestamp(t.getTime() + milliSecs);
    }

    public Timestamp add(Timestamp t, long number, TimeUnit timeUnit) {
        return add(t, timeUnit.toMillis(number));
    }

    public Timestamp subtract(Timestamp t, long milliSecs) {
        return new Timestamp(t.getTime() - milliSecs);
    }

    public Timestamp subtract(Timestamp t, long number, TimeUnit timeUnit) {
        return subtract(t, timeUnit.toMillis(number));
    }

    public List<Timestamp> getDatesBetween(Timestamp t1, Timestamp t2, String timezone) {
        List<Timestamp> dates = new ArrayList<>();
        for (Timestamp t = startOfDay(t1, timezone); !t.after(t2); t = add(t, 1L, DAYS)) {
            dates.add(t);
        }
        return dates;
    }

    public List<Timestamp> getDatesBetween(Timestamp t1, Timestamp t2) {
        return getDatesBetween(t1, t2, LOCAL_TIME_ZONE);
    }

    // TODO: fix this later
    public static String dateWithTimeZone(Date date, String pattern, String timeZone) {
        SimpleDateFormat isoFormat = new SimpleDateFormat(pattern);
        isoFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
        return isoFormat.format(date);
    }

    public String dateWithTimeZone(String date, String inputPattern, String outputPattern, String timeZone) {
        SimpleDateFormat isoFormat = getDateFormatter(outputPattern, timeZone);
        SimpleDateFormat utcFormat = getDateFormatter(inputPattern, UTC_TIME_ZONE);
        try {
            return isoFormat.format(utcFormat.parse(date));
        } catch (ParseException e) {
            return "";
        }
    }

    public Timestamp convertTimeZone(Date date, String from, String to) {
        ZonedDateTime zonedDateTime = date.toInstant()
                .atZone(ZoneId.of(UTC_TIME_ZONE))
                .withZoneSameLocal(ZoneId.of(from))
                .withZoneSameInstant(ZoneId.of(to));
        return new Timestamp(zonedDateTime.toInstant().toEpochMilli());
    }

    public SimpleDateFormat getDateFormatter(String pattern, String timeZoneId) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        formatter.setTimeZone(TimeZone.getTimeZone(timeZoneId));
        return formatter;
    }

    public DayOfWeek getDayOfWeek(Date date) {
        return date.toInstant().atZone(ZoneId.of(LOCAL_TIME_ZONE)).getDayOfWeek();
    }

    public int getMonthDays(Date date) {
        ZonedDateTime zonedDateTime = date.toInstant().atZone(ZoneId.of(LOCAL_TIME_ZONE));
        Month currentMonth = zonedDateTime.getMonth();
        int currentYear = zonedDateTime.getYear();
        YearMonth yearMonth = YearMonth.of(currentYear, currentMonth.getValue());
        return yearMonth.lengthOfMonth();
    }

    public Timestamp getLastDayOfMonth(Date date) {
        int lastDay = getMonthDays(date);
        return new Timestamp(startOfDay(date.getTime()).toInstant()
                .atZone(ZoneId.of(LOCAL_TIME_ZONE))
                .withDayOfMonth(lastDay)
                .toInstant().toEpochMilli()
        );
    }

    public Timestamp getFirstDayOfMonth(Date date) {
        return new Timestamp(startOfDay(date.getTime()).toInstant()
                .atZone(ZoneId.of(LOCAL_TIME_ZONE))
                .withDayOfMonth(1)
                .toInstant().toEpochMilli()
        );
    }

    public int compare(Date d1, Date d2) {
        long diff = d1.getTime() - d2.getTime();
        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    }

    public Timestamp min(Date... dates) {
        return Arrays.stream(dates)
                .filter(Objects::nonNull)
                .map(date -> new Timestamp(date.getTime()))
                .min(Comparator.comparingLong(Timestamp::getTime))
                .orElse(null);
    }

    public Timestamp max(Date... dates) {
        return Arrays.stream(dates)
                .filter(Objects::nonNull)
                .map(date -> new Timestamp(date.getTime()))
                .max(Comparator.comparingLong(Timestamp::getTime))
                .orElse(null);
    }

    public Timestamp getTomorrow(Date date) {
        return new Timestamp(startOfDay(date.getTime()).getTime() + 24 * 3600 * 1000);
    }

    public Long getOverTime(Timestamp fromDate, Timestamp toDate, Time fromTime, Time toTime,
                            Time contractFromTime, Time contractToTime) {
        if (Objects.isNull(fromDate) || Objects.isNull(toDate) ||
                Objects.isNull(fromTime) || Objects.isNull(toTime) ||
                Objects.isNull(contractFromTime) || Objects.isNull(contractToTime)) {
            return null;
        }
        toTime = new Time(getTimeBetween(startOfDay(fromDate), startOfDay(toDate)) + toTime.getTime());
        return getOverTime(fromTime, toTime, contractFromTime, contractToTime);
    }

    public Long getOverTime(Time fromTime, Time toTime, Time contractFromTime, Time contractToTime) {
        if (Objects.isNull(fromTime) || Objects.isNull(toTime) ||
                Objects.isNull(contractFromTime) || Objects.isNull(contractToTime)) {
            return null;
        }
        fromTime = roundTime(fromTime, MINUTES);
        toTime = roundTime(toTime, MINUTES);
        if (toTime.before(fromTime) || contractToTime.before(contractFromTime)) {
            return 0L;
        }
        if (fromTime.before(contractFromTime)) {
            if (toTime.before(contractFromTime)) {
                return getMinuteBetween(fromTime, toTime);
            } else if (toTime.before(contractToTime)) {
                return getMinuteBetween(fromTime, contractFromTime);
            } else {
                return getMinuteBetween(fromTime, contractFromTime) + getMinuteBetween(contractToTime, toTime);
            }
        } else if (fromTime.before(contractToTime)) {
            if (toTime.before(contractToTime)) {
                return 0L;
            } else {
                return getMinuteBetween(contractToTime, toTime);
            }
        } else {
            return getMinuteBetween(fromTime, toTime);
        }
    }

    public String convertMinuteToHour(Integer minute){
        if(minute == null) return "";
        int h = minute / 60;
        int m = minute % 60;
        return (h < 10 ? ("0" + h) : h) + ":" + (m < 10 ? ("0" + m) : m);
    }

    private String convert(Time time){
        String timeStr = "--:--";
        if(time != null) {
            String[] times = time.toString().split(":");
            if (times.length == 3) {
                timeStr = times[0] + ":" + times[1];
            }
        }
        return timeStr;
    }

    public String convertTime(Time from, Time to){
        String toStr = convert(to);
        String fromStr = convert(from);

        return fromStr + " - " + toStr;
    }

    public String convertMonthToString(int month){
        switch (month){
            case 1:
                return "JANUARY";
            case 2:
                return "FEBRUARY";
            case 3:
                return "MARCH";
            case 4:
                return "APRIL";
            case 5:
                return "MAY";
            case 6:
                return "JUNE";
            case 7:
                return "JULY";
            case 8:
                return "AUGUST";
            case 9:
                return "SEPTEMBER";
            case 10:
                return "OCTOBER";
            case 11:
                return "NOVEMBER";
            case 12:
                return "DECEMBER";
        }
        return null;
    }

    public static Date getDate(String date, String pattern) {
        if(SHORT_PATTERN.equals(pattern)){
            pattern=DEFAULT_DATE_FORMAT;
            date+=" 00:00:00";
        }
        return getDate(date, pattern, DEFAULT_LOCALE);
    }
    public static Date getDate(String date, String pattern, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern)
                .withLocale(locale)
                .withZone(ZoneId.systemDefault());

        LocalDateTime lastShotTime = LocalDateTime.parse(date, formatter);
        Date instant = Date.from(lastShotTime.toInstant(ZoneOffset.systemDefault().getRules().getOffset(Instant.now())));
        return instant;
    }
}
