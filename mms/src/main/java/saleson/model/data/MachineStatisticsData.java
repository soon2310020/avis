package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import saleson.api.machine.payload.DowntimeItemData;
import saleson.common.enumeration.DowntimeType;
import saleson.dto.common.TwoObject;
import saleson.model.DowntimeItem;
import saleson.model.Machine;
import saleson.model.MachineStatistics;
import saleson.service.util.DateTimeUtils;
import saleson.service.util.NumberUtils;

import java.time.Instant;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MachineStatisticsData {

    private Long id;
    private Machine machine;

    private Instant date;
    private String day;
    private Integer actualWorkingHour;
    private Integer actualWorkingHourMinute;
    private Double actualWorkingHourDecimal;

    private Integer dailyWorkingHour;
    private Integer dailyWorkingHourMinute;
    private Double dailyWorkingHourDecimal;
    private String dailyWorkingHourNote;
    private Instant dailyWorkingHourEditedAt;

    private Integer plannedDowntime;
    private Integer plannedDowntimeMinute;
    private Double plannedDowntimeDecimal;
    private String plannedDowntimeType;
    private String plannedDowntimeNote;

    private Integer unplannedDowntime;
    private Integer unplannedDowntimeMinute;
    private Double unplannedDowntimeDecimal;
    private String unplannedDowntimeType;
    private String unplannedDowntimeNote;

    private List<DowntimeItemData> downtimeItems;

    public MachineStatisticsData(Machine machine){
        this.machine = machine;
        this.dailyWorkingHour = 24;
        this.dailyWorkingHourMinute = 0;
        this.dailyWorkingHourDecimal = 24D;
        this.actualWorkingHour = 24;
        this.actualWorkingHourMinute = 0;
        this.actualWorkingHourDecimal = 24D;
        this.plannedDowntime = 0;
        this.plannedDowntimeDecimal = 0D;
        this.plannedDowntimeMinute = 0;
        this.unplannedDowntime = 0;
        this.unplannedDowntimeDecimal = 0D;
        this.unplannedDowntimeMinute = 0;
    }

    @QueryProjection
    public MachineStatisticsData(MachineStatistics machineStatistics) {
        this.id = machineStatistics.getId();
        this.machine = machineStatistics.getMachine();
        this.date = machineStatistics.getDate();
        this.day = machineStatistics.getDay();
        this.dailyWorkingHour = machineStatistics.getDailyWorkingHour() != null ? machineStatistics.getDailyWorkingHour() : 24;
        this.dailyWorkingHourMinute = machineStatistics.getDailyWorkingHourMinute() != null ? machineStatistics.getDailyWorkingHourMinute() : 0;
        this.dailyWorkingHourDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(dailyWorkingHour, dailyWorkingHourMinute));
        this.dailyWorkingHourNote = machineStatistics.getDailyWorkingHourNote();
        this.dailyWorkingHourEditedAt = machineStatistics.getDailyWorkingHourEditedAt();
        this.plannedDowntime = machineStatistics.getPlannedDowntime() != null ? machineStatistics.getPlannedDowntime() : 0;
        this.plannedDowntimeMinute = machineStatistics.getPlannedDowntimeMinute() != null ? machineStatistics.getPlannedDowntimeMinute() : 0;
        this.plannedDowntimeDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(plannedDowntime, plannedDowntimeMinute));
        this.plannedDowntimeType = machineStatistics.getPlannedDowntimeType();
        this.plannedDowntimeNote = machineStatistics.getPlannedDowntimeNote();
        this.unplannedDowntime = machineStatistics.getUnplannedDowntime() != null ? machineStatistics.getUnplannedDowntime() : 0;
        this.unplannedDowntimeMinute = machineStatistics.getUnplannedDowntimeMinute() != null ? machineStatistics.getUnplannedDowntimeMinute() : 0;
        this.unplannedDowntimeDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(unplannedDowntime, unplannedDowntimeMinute));
        this.unplannedDowntimeType = machineStatistics.getUnplannedDowntimeType();
        this.unplannedDowntimeNote = machineStatistics.getUnplannedDowntimeNote();

        TwoObject<Integer, Integer> totalDowntime = DateTimeUtils.sumHourAndMinute(plannedDowntime, plannedDowntimeMinute, unplannedDowntime, unplannedDowntimeMinute);
        TwoObject<Integer, Integer> actualHourAndMinute = DateTimeUtils.subtractHourAndMinute(
                totalDowntime.getLeft(), totalDowntime.getRight(),
                dailyWorkingHour != null ? dailyWorkingHour : 24,
                dailyWorkingHourMinute != null ? dailyWorkingHourMinute : 0);
        this.actualWorkingHour = actualHourAndMinute.getLeft();
        this.actualWorkingHourMinute = actualHourAndMinute.getRight();
        this.actualWorkingHourDecimal = NumberUtils.roundToOneDecimalDigit(DateTimeUtils.roundedHourFromHourAndMinute(actualWorkingHour, actualWorkingHourMinute));
    }
}
