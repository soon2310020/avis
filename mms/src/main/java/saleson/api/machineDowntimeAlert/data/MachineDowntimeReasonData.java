package saleson.api.machineDowntimeAlert.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor
public class MachineDowntimeReasonData {
    private Long machineDowntimeAlertId;
    private MachineAvailabilityType downtimeType;
    private MachineDowntimeAlertStatus status;
    private List<MachineDowntimeReasonItem> items = new ArrayList<>();
}
