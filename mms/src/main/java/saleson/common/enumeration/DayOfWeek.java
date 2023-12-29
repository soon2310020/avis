package saleson.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joda.time.DateTimeConstants;

@AllArgsConstructor
@Getter
public enum DayOfWeek {
    MONDAY("Monday", DateTimeConstants.MONDAY, java.time.DayOfWeek.MONDAY),
    TUESDAY("Tuesday", DateTimeConstants.TUESDAY, java.time.DayOfWeek.TUESDAY),
    WEDNESDAY("Wednesday", DateTimeConstants.WEDNESDAY, java.time.DayOfWeek.WEDNESDAY),
    THURSDAY("Thursday", DateTimeConstants.THURSDAY, java.time.DayOfWeek.THURSDAY),
    FRIDAY("Friday", DateTimeConstants.FRIDAY, java.time.DayOfWeek.FRIDAY),
    SATURDAY("Saturday", DateTimeConstants.SATURDAY, java.time.DayOfWeek.SATURDAY),
    SUNDAY("Sunday", DateTimeConstants.SUNDAY, java.time.DayOfWeek.SUNDAY);
    private final String name;
    private final int dateTimeNumber;
    private final java.time.DayOfWeek dayOfWeek;
}
