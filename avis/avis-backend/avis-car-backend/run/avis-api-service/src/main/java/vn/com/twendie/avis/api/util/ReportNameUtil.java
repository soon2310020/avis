package vn.com.twendie.avis.api.util;

import vn.com.twendie.avis.api.core.util.DateUtils;

import java.sql.Timestamp;

import static vn.com.twendie.avis.api.core.util.DateUtils.SHORT_PATTERN;

public class ReportNameUtil {

    public static String reportNameWithTimeRange(Timestamp from, Timestamp to, DateUtils dateUtils) {
        return String.format("TỪ %s - %s", dateUtils.format(from, SHORT_PATTERN),
                dateUtils.format(to, SHORT_PATTERN));
    }
}
