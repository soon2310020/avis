package saleson.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldLocationCustomFieldValue
{
    private MoldLocation moldLocation;
    private Long customFieldId;

    @QueryProjection
    public MoldLocationCustomFieldValue(MoldLocation moldLocation, Long customFieldId){
        this.moldLocation = moldLocation;
        this.customFieldId = customFieldId;
    }
}
