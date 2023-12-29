package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldCycleTime;

@Data
public class MoldCycleTimeExtraData {
    private MoldCycleTime moldCycleTime;
    private Double variance;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldCycleTimeExtraData(MoldCycleTime moldCycleTime, Double variance){
        this.moldCycleTime = moldCycleTime;
        this.variance = variance;
    }

    @QueryProjection
    public MoldCycleTimeExtraData(MoldCycleTime moldCycleTime, Integer accumulatedShots){
        this.moldCycleTime = moldCycleTime;
        this.accumulatedShots = accumulatedShots;
    }
}
