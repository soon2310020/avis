package saleson.api.rejectedPart.payload;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineCountEntryRecord {
    private Long machineId;
    private Long count;

    @QueryProjection
    public MachineCountEntryRecord(Long machineId, Long count) {
        this.machineId = machineId;
        this.count = count;
    }
}
