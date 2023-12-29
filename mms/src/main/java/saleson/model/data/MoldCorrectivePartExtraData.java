package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldCorrective;

@Data
public class MoldCorrectivePartExtraData
{
    private MoldCorrective moldCorrective;
    private String partCode;

    @QueryProjection
    public MoldCorrectivePartExtraData(MoldCorrective moldCorrective, String partCode){
        this.moldCorrective = moldCorrective;
        this.partCode = partCode;
    }
}
