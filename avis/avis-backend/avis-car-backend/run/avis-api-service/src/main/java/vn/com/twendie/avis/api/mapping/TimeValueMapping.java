package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.api.core.util.DateUtils;

import java.sql.Time;

import static vn.com.twendie.avis.api.core.util.DateUtils.HOUR_SHORT_PATTERN;
import static vn.com.twendie.avis.api.core.util.DateUtils.UTC_TIME_ZONE;

public class TimeValueMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return new DateUtils().format((Time) value, HOUR_SHORT_PATTERN, UTC_TIME_ZONE);
    }

}
