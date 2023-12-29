package saleson.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldCustomFieldValue
{
    private Mold mold;
    private Long customFieldId;

    @QueryProjection
    public MoldCustomFieldValue(Mold mold, Long customFieldId){
        this.mold = mold;
        this.customFieldId = customFieldId;
    }
}
