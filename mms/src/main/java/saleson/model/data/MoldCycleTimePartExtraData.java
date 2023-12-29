package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldCycleTime;

@Data
public class MoldCycleTimePartExtraData {
    private MoldCycleTime moldCycleTime;
    private String partCode;

    @QueryProjection
    public MoldCycleTimePartExtraData(MoldCycleTime moldCycleTime, String partCode){
        this.moldCycleTime = moldCycleTime;
        this.partCode = partCode;
    }
}
