package saleson.api.machine.payload;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.util.Pair;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.model.HourShift;
import saleson.model.MachineDowntimeAlert;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeOeeDTO {
    private MachineAvailabilityType type;
    private String startTime;
    private String endTime;

    public DowntimeOeeDTO(MachineDowntimeAlert machineDowntimeAlert, Pair<Instant, Instant> shiftTimePair, HourShift hourShift) {
        String timeZone = LocationUtils.getZoneIdByLocationId(machineDowntimeAlert.getMachine().getLocationId());
        this.type = machineDowntimeAlert.getDowntimeType();
        this.startTime = machineDowntimeAlert.getStartTime().isBefore(shiftTimePair.getFirst())
                ? hourShift.getStart()
                : DateUtils2.format(machineDowntimeAlert.getStartTime(), "HHmm", timeZone);
        this.endTime = machineDowntimeAlert.getEndTime() == null || machineDowntimeAlert.getEndTime().isAfter(shiftTimePair.getSecond())
                ? hourShift.getEnd()
                : DateUtils2.format(machineDowntimeAlert.getEndTime(), "HHmm", timeZone);
    }
}
