package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.Mold;

@Data
public class MoldExtraData {
    private Mold mold;
    private String partCode;

    @QueryProjection
    public MoldExtraData(Mold mold, String partCode){
        this.mold = mold;
        this.partCode = partCode;
    }
}
