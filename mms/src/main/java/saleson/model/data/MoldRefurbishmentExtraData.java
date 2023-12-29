package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldRefurbishment;

@Data
public class MoldRefurbishmentExtraData {
    private MoldRefurbishment moldRefurbishment;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldRefurbishmentExtraData(MoldRefurbishment moldRefurbishment, Integer accumulatedShots) {
        this.moldRefurbishment = moldRefurbishment;
        this.accumulatedShots = accumulatedShots;
    }
}
