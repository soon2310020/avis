package com.stg.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Date function
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
    public static final String DATE_YMD_HMS_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    public static final String DATE_DMY_HM = "dd/MM/yyyy HH:mm";
    public static final String DATE_YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String DATE_YYYYMMDD_HHMMSS = "yyyyMMddhhmmss";

    /**
     *
     */
    public static String localDateTimeToString(String pattern, LocalDateTime dateTime) {
        DateTimeFormatter simpleFormat = DateTimeFormatter.ofPattern(pattern);
        return simpleFormat.format(dateTime);
    }

    public static String localDateTimeToString(LocalDate dateTime) {
        return localDateTimeToString(dateTime, DATE_YYYY_MM_DD);
    }

    public static String localDateTimeToString(LocalDate dateTime, String pattern) {
        DateTimeFormatter simpleFormat = DateTimeFormatter.ofPattern(pattern);
        return simpleFormat.format(dateTime);
    }

    public static LocalDateTime localDateTimeToString(String pattern, String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
        return dateTime;
    }

    public static void main(String[] args) {
        String date = "2023-10-30T06:53:28.771";

        LocalDateTime dateTime = localDateTimeToString(DATE_YMD_HMS_ZONE, date);
        System.out.println(dateTime);
    }

    public static LocalDate stringToLocalDate(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_YYYY_MM_DD);
        LocalDate date = LocalDate.parse(dateTimeStr, formatter);
        return date;
    }


    public static LocalDate localDateFrom(String pattern, String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateTimeStr, formatter);
    }

    /**
     *
     */
    public static Date addSecondToCurrentDate(int second) {
        Calendar calendar = Calendar.getInstance(); // with default time zone and locale.
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }

    /***/
    public static String convertFormat(String data, String originalFormat, String targetFormat) {
        try {
            Date newDate = new SimpleDateFormat(originalFormat).parse(data);
            SimpleDateFormat formatter = new SimpleDateFormat(targetFormat);
            return formatter.format(newDate);
        } catch (Exception e) {
            log.error("Exception when change time format with input {}", data);
            return "";
        }
    }

    private static final Pattern OCA_DATE_PATTER = Pattern.compile("(?i)(ngày\\s+)?(\\d{1,2})\\s+tháng\\s+(\\d{1,2})\\s+năm\\s+(\\d{4})|(\\d{1,2}/\\d{1,2}/\\d{4})");
    public static String convertToCommonFormat(String inputDate) {
        // Regex pattern cho định dạng có thể có hoặc không có chữ "ngày" ở đầu (ignore case)
        try {
            Matcher matcher = OCA_DATE_PATTER.matcher(inputDate);
            if (matcher.matches()) {
                // Nếu định dạng khớp, chuyển đổi thành "yyyy/MM/dd"
                String day = matcher.group(2);
                String month = matcher.group(3);
                String year = matcher.group(4);

                if (day == null || month == null || year == null) {
                    // Trường hợp không có chữ "ngày" ở đầu, sử dụng định dạng dd/MM/yyyy
                    day = matcher.group(5).split("/")[0];
                    month = matcher.group(5).split("/")[1];
                    year = matcher.group(5).split("/")[2];
                }

                return year + "/" + month + "/" + day;
            } else {
                log.info("Convert date to date format with source date  {}", inputDate);
                // Nếu không khớp, xử lý theo các định dạng khác hoặc trả về null
                String[] formatsToTry = {DATE_DMY, "MM/yyyy", DATE_DMY_DASH};

                for (String format : formatsToTry) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat(format);
                        Date date = sdf.parse(inputDate);

                        SimpleDateFormat outputFormat = new SimpleDateFormat(DATE_YMD);

                        return outputFormat.format(date);
                    } catch (ParseException e) {
                        log.warn("Error when convert date, detail={}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception when convert date to date format with source date  {}", inputDate);
            return null;
        }
        return null;
    }

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
}
