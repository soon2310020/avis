package saleson.service.util;

import saleson.common.util.DateUtils;
import saleson.dto.common.TwoObject;

import javax.persistence.criteria.CriteriaBuilder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static saleson.common.util.DateUtils.DEFAULT_DATE_FORMAT;

public class DateTimeUtils {
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Instant getStartOfYesterday(){
        LocalDate date = LocalDate.now().minusDays(1);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static Instant getStartOfToday(){
        LocalDate date = LocalDate.now();
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static Instant getStartOfTomorrow(){
        LocalDate date = LocalDate.now().plusDays(1);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static Instant getStartOf6DaysBefore(){
        LocalDate date = LocalDate.now().minusDays(6);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static Instant getStartOfTheWeek(){
        LocalDate date = LocalDate.now().minusDays(7);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static Instant getStartOfTheMonth(){
        LocalDate yesterday = LocalDate.now().minusDays(1);
        yesterday.withDayOfMonth(1);
        return yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static boolean todayIsMonday(){
        LocalDate date = LocalDate.now();
        if(date.getDayOfWeek().equals(DateUtils.DEFAULT_WEEK_FIELDS.getFirstDayOfWeek())) return true;
        return false;
    }

    public static boolean todayIsFirstDayOfMonth(){
        LocalDate date = LocalDate.now();
        if(date.getDayOfMonth() == 1) return true;
        return false;
    }

    public static int getRemainingDaysOfWeek(){
        LocalDate date = LocalDate.now();
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        DayOfWeek firstDayOfWeek = DateUtils.DEFAULT_WEEK_FIELDS.getFirstDayOfWeek();
//        if(firstDayOfWeek.equals(DayOfWeek.MONDAY)){
//            int passedDays = dayOfWeek.getValue() - firstDayOfWeek.getValue() + 1;
//            return 7 - passedDays;
//        }else{
            if(dayOfWeek.getValue() < firstDayOfWeek.getValue()){
                int remainingDays = firstDayOfWeek.getValue() - dayOfWeek.getValue() - 1;
                return remainingDays;
            }else {
                int passedDays = dayOfWeek.getValue() - firstDayOfWeek.getValue() + 1;
                return 7 - passedDays;
            }
//        }
    }

    public static String getFullMonthName(String shortName){
        if(shortName.equalsIgnoreCase("Jan")) return "January";
        else if(shortName.equalsIgnoreCase("Feb")) return "February";
        else if(shortName.equalsIgnoreCase("Mar")) return "March";
        else if(shortName.equalsIgnoreCase("Apr")) return "April";
        else if(shortName.equalsIgnoreCase("May")) return "May";
        else if(shortName.equalsIgnoreCase("Jun")) return "June";
        else if(shortName.equalsIgnoreCase("Jul")) return "July";
        else if(shortName.equalsIgnoreCase("Aug")) return "August";
        else if(shortName.equalsIgnoreCase("Sep")) return "September";
        else if(shortName.equalsIgnoreCase("Oct")) return "October";
        else if(shortName.equalsIgnoreCase("Nov")) return "November";
        else if(shortName.equalsIgnoreCase("Dec")) return "December";
        return "";
    }

    public static Instant getStartOfPreviousDay(Integer minusDays){
        LocalDate date = LocalDate.now().minusDays(minusDays);
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static String getPreviousTimePeriod(String timePeriod){
        if(timePeriod == null) return "";
        String previousPeriod = "";
        if(timePeriod.startsWith("Y")){
            Integer previousYear = Integer.valueOf(timePeriod.substring(1)) - 1;
            previousPeriod = "Y" + previousYear;
        }else if(timePeriod.startsWith("M")){
            Integer previousMonth = Integer.valueOf(timePeriod.substring(5)) - 1;
            if(previousMonth <= 0){
                Integer previousYear = Integer.valueOf(timePeriod.substring(1, 5)) - 1;
                previousPeriod = "M" + previousYear + 12;
            }else{
                if(previousMonth < 10) previousPeriod = "M" + timePeriod.substring(1,5) + "0" + previousMonth;
                else previousPeriod = "M" + timePeriod.substring(1,5) + previousMonth;
            }
        }else if(timePeriod.startsWith("W")){
            Integer previousWeek = Integer.valueOf(timePeriod.substring(5)) - 1;
            if(previousWeek <= 0){
                Integer previousYear = Integer.valueOf(timePeriod.substring(1, 5)) - 1;
                previousPeriod = "W" + previousYear + 52;
            }else{
                if(previousWeek < 10) previousPeriod = "W" + timePeriod.substring(1, 5) + "0" + previousWeek;
                else previousPeriod = "W" + timePeriod.substring(1, 5) + previousWeek;
            }
        }
        return previousPeriod;
    }
    public static String getTitleRangDate(Instant from, Instant to) {
        String title = (from != null ? DateUtils.getDate(from, "yyyy.MM.dd") : "") + " ~ " + (to != null ? DateUtils.getDate(to, "yyyy.MM.dd") : "");
        return title;
    }

    public static String formatDateTimeWithTimeZone(Instant date, Integer timezoneOffsetClient ){
        return formatDateTimeWithTimeZone(date,timezoneOffsetClient,DEFAULT_DATE_TIME_FORMAT);
    }

    public static String formatDateTimeWithTimeZone(Instant date, Integer timezoneOffsetClient, String pattern) {
        if (timezoneOffsetClient == null)
            timezoneOffsetClient = -Calendar.getInstance().getTimeZone().getRawOffset() / 60000;
        if(date==null) return null;
        return formatDateTimeWithRawOffset(new Date(date.toEpochMilli()), pattern, -timezoneOffsetClient * 60000);
    }

    public static String formatDateTimeWithRawOffset(Date date, String pattern, int rawOffset) {
        if (date == null) {
            return "";
        }
        DateFormat df = new SimpleDateFormat(pattern, Locale.US);
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        timeZone.setRawOffset(rawOffset);

        df.setTimeZone(timeZone);
        return df.format(date);
    }

    public static Integer diffBetweenTwoDates(String from, String to, boolean isIncluded){
        LocalDate fromDate = LocalDate.parse(from, DateTimeFormatter.BASIC_ISO_DATE);
        LocalDate toDate = LocalDate.parse(to, DateTimeFormatter.BASIC_ISO_DATE);
        long diff = ChronoUnit.DAYS.between(fromDate, toDate);
        if(isIncluded) diff++;
        return (int) diff;
    }

    public static Integer diffBetweenTwoDates(Instant from, Instant to, boolean isIncluded){
        long diff = ChronoUnit.DAYS.between(from, to);
        if(isIncluded) diff++;
        return (int) diff;
    }

    public static TwoObject<Integer, Integer> sumHourAndMinute(Integer originalHour, Integer originalMinute, Integer addedHour, Integer addedMinute) {
        TwoObject<Integer, Integer> result = new TwoObject<>();
        Integer totalMinute = (originalHour + addedHour) * 60 + originalMinute + addedMinute;
        result.setLeft(totalMinute / 60);
        result.setRight(totalMinute % 60);

        return result;
    }

    public static TwoObject<Integer, Integer> subtractHourAndMinute(Integer startHour, Integer startMinute, Integer endHour, Integer endMinute) {
        TwoObject<Integer, Integer> result = new TwoObject<>();
        Integer totalMinute = (endHour - startHour) * 60 - startMinute + endMinute;
        result.setLeft(totalMinute / 60);
        result.setRight(totalMinute % 60);

        return result;
    }

    public static Double roundedHourFromHourAndMinute(Integer hour, Integer minute) {
        return hour + minute/(double)60;
    }

    public static Instant getDayBefore(Instant date, Integer duration, boolean firstOfDay) {
        Calendar calendarDate =  Calendar.getInstance();
        calendarDate.setTime(Date.from(date));
        calendarDate.add(Calendar.DATE, -duration);
        if (firstOfDay) {
            return getStartOfDay(calendarDate.toInstant());
        } else {
            return getEndOfDay(calendarDate.toInstant());
        }
    }
    public static Instant getStartOfDay(Instant date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(date));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.toInstant();
    }

    public static Instant getEndOfDay(Instant date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Date.from(date));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        calendar.set(year, month, day, 23, 59, 59);
        return calendar.toInstant();
    }

    public static void main(String[] agrs){
        System.out.println("Diff: " + diffBetweenTwoDates("20210711", "20210811", true));
//        System.out.println(Calendar.getInstance().getTimeZone().getRawOffset());
//        System.out.println(formatDateTimeWithTimeZone(Instant.now(),-420,"yyyy-MM-dd HH:mm"));
//
//        String week="202101";
//        int year =Integer.valueOf(week.substring(0,4));
//        int weekValue =Integer.valueOf(week.substring(4,6));
//        Calendar calendar = Calendar.getInstance();
//        calendar.setWeekDate(year,weekValue,Calendar.MONDAY);
//        calendar.set(Calendar.HOUR_OF_DAY,0);
//        calendar.set(Calendar.MINUTE,0);
//        calendar.set(Calendar.SECOND,0);
//        calendar.add(Calendar.HOUR,-1);
//        String fromTime= DateUtils.getDate(calendar.toInstant(),DEFAULT_DATE_FORMAT);
//        calendar.add(Calendar.DATE,7);
//        calendar.add(Calendar.HOUR,1);
//        String toTime= DateUtils.getDate(calendar.toInstant(),DEFAULT_DATE_FORMAT);
//        System.out.println(fromTime);
//        System.out.println(toTime);
    }

    public static String getDayOrWeekOrMonthString(int a){
        return a >= 10 ? String.valueOf(a) : "0" + a;
    }
}
