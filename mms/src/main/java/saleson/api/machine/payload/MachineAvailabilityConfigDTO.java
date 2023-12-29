package saleson.api.machine.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.MachineStatistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MachineAvailabilityConfigDTO {
    private Long machineId;
    private List<MachineStatisticsDetails> machineStatisticsDetails = new ArrayList<>();
}
