package vn.com.twendie.avis.api.mapping;

import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.core.util.DateUtils;

import java.sql.Timestamp;

@Component
public class DayOfWeekMapping implements ValueMapping<String> {

    private final DateUtils dateUtils;

    public DayOfWeekMapping(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    @Override
    public String map(Object value) {

        switch (dateUtils.getDayOfWeek((Timestamp) value)) {
            case MONDAY:
                return "Hai";
            case TUESDAY:
                return "Ba";
            case WEDNESDAY:
                return "Tư";
            case THURSDAY:
                return "Năm";
            case FRIDAY:
                return "Sáu";
            case SATURDAY:
                return "Bảy";
            case SUNDAY:
                return "Chủ nhật";
            default:
                return null;
        }
    }

}
