package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldDisconnect;

@Data
public class MoldDisconnectExtraData {
    private MoldDisconnect moldDisconnect;
    private Integer accumulatedShots;

    @QueryProjection
    public MoldDisconnectExtraData(MoldDisconnect moldDisconnect, Integer accumulatedShots) {
        this.moldDisconnect = moldDisconnect;
        this.accumulatedShots = accumulatedShots;
    }
}
