package saleson.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldCorrectiveCustomFieldValue
{
    private MoldCorrective moldCorrective;
    private Long customFieldId;

    @QueryProjection
    public MoldCorrectiveCustomFieldValue(MoldCorrective moldCorrective, Long customFieldId){
        this.moldCorrective = moldCorrective;
        this.customFieldId = customFieldId;
    }
}
