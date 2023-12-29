package com.stg.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;

/**
 * Các hàm xử lý với Date
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {
    public static final String DATE_DMY = "dd/MM/yyyy";
    public static final String DATE_DMY_DASH = "dd-MM-yyyy";
    public static final String DATE_YMD = "yyyy/MM/dd";
    public static final String DATE_YYYY_MM_DD = "yyyy-MM-dd";
    public static final String DATE_DMY_HMS = "dd/MM/yyyy HH:mm:ss";
    public static final String DATE_YMD_HMS_01 = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_YMD_HMS_DB = "yyyy-MM-dd HH:mm:ss.SSSSSS";
    public static final String DATE_DMY_HM = "dd/MM/yyyy HH:mm";
    public static final String DATE_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String DATE_YYYYMMDD_HHMMSS = "yyyyMMddhhmmss";
    public static final String CARD_DATE_EXPIRED = "yyMM";

    /**
     * format: %d phút, %02d giây
     * */
    public static String convertDurationToText(long ttlMillisecond) {
        long min = TimeUnit.MILLISECONDS.toMinutes(ttlMillisecond);
        long sec = TimeUnit.MILLISECONDS.toSeconds(ttlMillisecond) - TimeUnit.MINUTES.toSeconds(min);

        if (min > 0) {
            if (sec > 0) return String.format("%d phút, %02d giây", min, sec);
            else return String.format("%d phút", min);
        }

        return String.format("%d giây", sec);
    }

    // Timestamp format
    public static String formatDate(Timestamp date) {
        return formatDate(date, DATE_DMY_HMS);
    }

    public static String formatDate(Timestamp date, String format) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.format(date);
        } catch (Exception e) {
            log.error("formatDate error: " + e.getMessage());
            return "";
        }
    }

    // Date
    public static String formatDate(Date date) {
        return formatDate(date, DATE_DMY_HMS);
    }

    public static String formatDate(Date date, String format) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            return formatter.format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * convert String to Date
     *
     * @param date
     * @param format default dd/MM/yyyy HH:mm:ss
     * @return
     */
    public static Date stringToDate(String date, String format) {
        try {
            Date newDate = new SimpleDateFormat(format).parse(date);
            return newDate;
        } catch (Exception ex) {
            return null;
        }
    }

    public static Date stringToDate(String date) {
        return stringToDate(date, DATE_DMY_HMS);
    }


    public static String getCurrentDateByFormat(String strFormat) {
        DateFormat df = new SimpleDateFormat(strFormat);
        Date dateObj = new Date();
        return df.format(dateObj);
    }

    public static String localDateTimeToString(String pattern, LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter simpleFormat = DateTimeFormatter.ofPattern(pattern);
        return simpleFormat.format(dateTime);
    }

    public static LocalDateTime localDateTimeToString(String pattern, String dateTimeStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            log.error("localDateTimeToString[pattern={}, dateTimeStr={}], detail={}", pattern, dateTimeStr, e.getMessage());
            return null;
        }
    }

    public static boolean checkStatus(String pattern, String activeDate) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_DMY);
        return (sdf.parse(localDateTimeToString(pattern, LocalDateTime.now())).before(sdf.parse(activeDate)));
    }

    public static String convertFormat(String data, String originalFormat, String targetFormat) {
        try {
            Date newDate = new SimpleDateFormat(originalFormat).parse(data);
            SimpleDateFormat formatter = new SimpleDateFormat(targetFormat);
            return formatter.format(newDate);
        } catch (Exception e) {
            log.error("[MINI] Exception when change time format with input {}", data);
            return "";
        }
    }

    public static boolean checkCardExpired(String expiredTime, String period) {
        SimpleDateFormat sdf = new SimpleDateFormat(CARD_DATE_EXPIRED);
        try {
            return (sdf.parse(localDateTimeToString(CARD_DATE_EXPIRED, LocalDateTime.now().plusMonths(Long.parseLong(period)))).before(sdf.parse(expiredTime)));
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean compareTime(String expiredTime1, String expiredTime2) {
        SimpleDateFormat sdf = new SimpleDateFormat(CARD_DATE_EXPIRED);
        try {
            return (sdf.parse(expiredTime1).before(sdf.parse(expiredTime2)));
        } catch (ParseException e) {
            return false;
        }
    }

    public static LocalDate strToLocalDate(String pattern, String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateTimeStr, formatter);
    }

    public static String localDateToString(String pattern, LocalDate dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter simpleFormat = DateTimeFormatter.ofPattern(pattern);
        return simpleFormat.format(dateTime);
    }

    public static LocalDate strToLocalDateTryCatch(String pattern, String dateTimeStr) {
        if (dateTimeStr == null) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDate.parse(dateTimeStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
