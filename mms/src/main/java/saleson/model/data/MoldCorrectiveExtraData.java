package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldCorrective;

@Data
public class MoldCorrectiveExtraData {
    private MoldCorrective moldCorrective;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldCorrectiveExtraData(MoldCorrective moldCorrective, Integer accumulatedShots) {
        this.moldCorrective = moldCorrective;
        this.accumulatedShots = accumulatedShots;
    }
}
