package saleson.api.machine.payload;

import lombok.*;
import saleson.common.enumeration.*;
import saleson.dto.common.*;
import saleson.model.*;
import saleson.service.util.*;

@Data
public class DowntimeItemData {

    private Long id;
    private Long machineStatisticsId;
    private MachineStatistics machineStatistics;
    private MachineAvailabilityType type;

    private Integer hour;
    private Integer minute;
    private Double hourDecimal;

    private Integer hourFrom;
    private Integer minuteFrom;
    private Double hourFromDecimal;
    private Integer hourTo;
    private Integer minuteTo;
    private Double hourToDecimal;

    private String reason;
    private String note;

    public DowntimeItemData(DowntimeItem item) {
        this.id = item.getId();
        this.machineStatisticsId = item.getMachineStatisticsId();
        this.machineStatistics = item.getMachineStatistics();
        this.type = item.getType();
        TwoObject<Integer, Integer> hourAndMinute = DateTimeUtils.subtractHourAndMinute(item.getHourFrom(), item.getMinuteFrom(), item.getHourTo(), item.getMinuteTo());
        this.hour = hourAndMinute.getLeft();
        this.minute = hourAndMinute.getRight();
        this.hourDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(hour, minute));
        this.hourFrom = item.getHourFrom();
        this.minuteFrom = item.getMinuteFrom();
        this.hourFromDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(hourFrom, minuteFrom));
        this.hourTo = item.getHourTo();
        this.minuteTo = item.getMinuteTo();
        this.hourToDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(hourTo, minuteTo));
        this.reason = item.getReason();
        this.note = item.getNote();
    }
}
