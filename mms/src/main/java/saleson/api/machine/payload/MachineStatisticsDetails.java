package saleson.api.machine.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.DowntimeType;
import saleson.dto.common.TwoObject;
import saleson.model.Machine;
import saleson.model.MachineStatistics;
import saleson.service.util.DateTimeUtils;

import java.time.Instant;

@Data
@NoArgsConstructor
public class MachineStatisticsDetails {

    private Long id;
    private Machine machine;

    private Instant date;
    private String day;
    private Integer actualWorkingHour;
    private Integer actualWorkingHourMinute;

    private Integer dailyWorkingHour;
    private Integer dailyWorkingHourMinute;
    private String dailyWorkingHourNote;

    private Integer plannedDowntime;
    private Integer plannedDowntimeMinute;
    private String plannedDowntimeType;
    private String plannedDowntimeNote;

    private Integer unplannedDowntime;
    private Integer unplannedDowntimeMinute;
    private String unplannedDowntimeType;
    private String unplannedDowntimeNote;

    public MachineStatisticsDetails(Machine machine){
        this.machine = machine;
        this.dailyWorkingHour = 24;
    }

    @QueryProjection
    public MachineStatisticsDetails(MachineStatistics machineStatistics) {
        this.id = machineStatistics.getId();
        this.machine = machineStatistics.getMachine();
        this.date = machineStatistics.getDate();
        this.day = machineStatistics.getDay();
        this.dailyWorkingHour = machineStatistics.getDailyWorkingHour();
        this.dailyWorkingHourMinute = machineStatistics.getDailyWorkingHourMinute();
        this.dailyWorkingHourNote = machineStatistics.getDailyWorkingHourNote();
        this.plannedDowntime = machineStatistics.getPlannedDowntime();
        this.plannedDowntimeMinute = machineStatistics.getPlannedDowntimeMinute();
        this.plannedDowntimeType = machineStatistics.getPlannedDowntimeType();
        this.plannedDowntimeNote = machineStatistics.getPlannedDowntimeNote();
        this.unplannedDowntime = machineStatistics.getUnplannedDowntime();
        this.unplannedDowntimeMinute = machineStatistics.getUnplannedDowntimeMinute();
        this.unplannedDowntimeType = machineStatistics.getUnplannedDowntimeType();
        this.unplannedDowntimeNote = machineStatistics.getUnplannedDowntimeNote();

        TwoObject<Integer, Integer> totalDowntime = DateTimeUtils.sumHourAndMinute(plannedDowntime, plannedDowntimeMinute, unplannedDowntime, unplannedDowntimeMinute);
        TwoObject<Integer, Integer> actualHourAndMinute = DateTimeUtils.subtractHourAndMinute(
                totalDowntime.getLeft(), totalDowntime.getRight(),
                dailyWorkingHour != null ? dailyWorkingHour : 24,
                dailyWorkingHourMinute != null ? dailyWorkingHourMinute : 0);
        this.actualWorkingHour = actualHourAndMinute.getLeft();
        this.actualWorkingHourMinute = actualHourAndMinute.getRight();
    }
}
