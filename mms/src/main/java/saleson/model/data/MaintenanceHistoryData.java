package saleson.model.data;

import lombok.Data;
import saleson.common.enumeration.MaintenanceTimeStatus;
import saleson.common.enumeration.MaintenanceType;

import java.time.Instant;
import java.util.List;

@Data
public class MaintenanceHistoryData{
    private Integer shotCount;
    private Boolean confirmed;
    private MaintenanceType maintenanceType;
    private Instant failureTime;
    private Instant startTime;
    private Instant endTime;
    private Integer cost;
    private String checkList;
    private MaintenanceTimeStatus onTimeStatus;
    private Boolean latest;

}
