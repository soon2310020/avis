package saleson.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class PartCustomFieldValue
{
    private Part part;
    private Long customFieldId;

    @QueryProjection
    public PartCustomFieldValue(Part part, Long customFieldId){
        this.part = part;
        this.customFieldId = customFieldId;
    }
}
