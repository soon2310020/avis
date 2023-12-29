package saleson.api.machineDowntimeAlert.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineDowntimeReasonItem {
    private Long id;
    private Long codeDataId; //reason id
    private Instant startTime;
    private Instant endTime;
    private String note;
}
