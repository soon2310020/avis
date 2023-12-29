package saleson.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldCycleTimeCustomFieldValue
{
    private MoldCycleTime moldCycleTime;
    private Long customFieldId;

    @QueryProjection
    public MoldCycleTimeCustomFieldValue(MoldCycleTime moldCycleTime, Long customFieldId){
        this.moldCycleTime = moldCycleTime;
        this.customFieldId = customFieldId;
    }
}
