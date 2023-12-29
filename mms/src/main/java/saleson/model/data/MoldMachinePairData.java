package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoldMachinePairData {
    private Long moldId;
    private String moldCode;
    private Long machineId;
    private String machineCode;

    @QueryProjection
    public MoldMachinePairData(Long moldId, String moldCode, Long machineId, String machineCode) {
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.machineId = machineId;
        this.machineCode = machineCode;
    }
}
