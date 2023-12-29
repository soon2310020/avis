package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldMisconfigure;

@Data
public class MoldMisconfigureExtraData {
    private MoldMisconfigure moldMisconfigure;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldMisconfigureExtraData(MoldMisconfigure moldMisconfigure, Integer accumulatedShots) {
        this.moldMisconfigure = moldMisconfigure;
        this.accumulatedShots = accumulatedShots;
    }
}
