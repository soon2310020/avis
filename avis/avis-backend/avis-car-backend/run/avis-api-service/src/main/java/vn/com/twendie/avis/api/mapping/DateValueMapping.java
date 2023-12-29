package vn.com.twendie.avis.api.mapping;

import vn.com.twendie.avis.api.core.util.DateUtils;

import java.util.Date;

import static vn.com.twendie.avis.api.core.util.DateUtils.SHORT_PATTERN;

public class DateValueMapping implements ValueMapping<String> {

    @Override
    public String map(Object value) {
        return new DateUtils().format((Date) value, SHORT_PATTERN);
    }

}
