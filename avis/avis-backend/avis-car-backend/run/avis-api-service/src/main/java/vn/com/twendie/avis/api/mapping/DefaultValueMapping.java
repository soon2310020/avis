package vn.com.twendie.avis.api.mapping;

import org.apache.commons.lang3.StringUtils;

import java.sql.Time;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

public class DefaultValueMapping implements ValueMapping<Object> {

    @Override
    public Object map(Object value) {
        if (Objects.isNull(value)) {
            return StringUtils.EMPTY;
        } else if (value instanceof Boolean) {
            return BOOLEAN_VALUE_MAPPING.map(value);
        } else if (value instanceof Time) {
            return TIME_VALUE_MAPPING.map(value);
        } else if (value instanceof Date) {
            return DATE_VALUE_MAPPING.map(value);
        } else if (value instanceof Collection) {
            return STRING_COLLECTION_MAPPING.map(value);
        }
        return value;
    }

}
