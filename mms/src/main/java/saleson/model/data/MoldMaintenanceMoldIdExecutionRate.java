package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldMaintenanceMoldIdExecutionRate {
    private Long moldId;
    private Double executionRate;

    @QueryProjection
    public MoldMaintenanceMoldIdExecutionRate(Long moldId, Double executionRate){
        this.moldId = moldId;
        this.executionRate = executionRate;
    }
}
