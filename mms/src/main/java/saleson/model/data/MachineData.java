package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.Machine;

@Data
public class MachineData {
    private Long machineId;
    private Machine machine;

    @QueryProjection
    public MachineData(Long machineId, Machine machine) {
        this.machineId = machineId;
        this.machine = machine;
    }
}
