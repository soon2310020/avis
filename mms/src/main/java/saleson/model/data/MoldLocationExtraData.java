package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.Mold;
import saleson.model.MoldLocation;

@Data
public class MoldLocationExtraData {
    private MoldLocation moldLocation;
    private String partCode;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldLocationExtraData(MoldLocation moldLocation, String partCode){
        this.moldLocation = moldLocation;
        this.partCode = partCode;
    }

    @QueryProjection
    public MoldLocationExtraData(MoldLocation moldLocation, Integer accumulatedShots){
        this.moldLocation = moldLocation;
        this.accumulatedShots = accumulatedShots;
    }
}
