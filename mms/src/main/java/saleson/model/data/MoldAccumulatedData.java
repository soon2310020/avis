package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.Mold;

@Data
public class MoldAccumulatedData {
    private Mold mold;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldAccumulatedData(Mold mold, Integer accumulatedShots) {
        this.mold = mold;
        this.accumulatedShots = accumulatedShots;
    }
}
