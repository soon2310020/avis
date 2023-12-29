package saleson.api.machineDowntimeAlert.payload;

import com.google.common.collect.Lists;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.model.Machine;
import saleson.model.MachineDowntimeReason;
import saleson.model.Mold;
import saleson.model.User;

import java.time.Instant;
import java.util.List;

@Data
public class MachineDowntimeAlertData {
    private Long id;
    private String machineCode;
    private String locationCode;
    private String locationName;
    private Instant startTime;
    private Instant endTime;
    private Long duration;
    private Long filteredDuration;
    private MachineDowntimeAlertStatus downtimeStatus;
    private Instant updatedAt;
    private MachineAvailabilityType downtimeType;
    private User confirmedBy;
    private User reportedBy;
    private List<MachineDowntimeReason> machineDowntimeReasonList = Lists.newArrayList();
    private Long machineId;
    private Instant createdAt;

    private Mold mold;

    private String startTimeStr;

    private String endTimeStr;

    @QueryProjection
    public MachineDowntimeAlertData(Long id, String machineCode, String locationCode, String locationName,
                                    Instant startTime, Instant endTime, MachineDowntimeAlertStatus downtimeStatus,
                                    Instant updatedAt, MachineAvailabilityType downtimeType, User confirmedBy,
                                    User reportedBy, Long machineId, Instant createdAt, Mold mold) {
        this.id = id;
        this.machineCode = machineCode;
        this.locationCode = locationCode;
        this.locationName = locationName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.downtimeStatus = downtimeStatus;
        this.updatedAt = updatedAt;
        this.downtimeType = downtimeType;
        this.confirmedBy = confirmedBy;
        this.reportedBy = reportedBy;
        this.machineId = machineId;
        this.createdAt = createdAt;
        this.mold = mold;
    }

    public Machine getMachine() {
        if (mold != null) {
            return mold.getMachine();
        }
        return null;
    }
}
