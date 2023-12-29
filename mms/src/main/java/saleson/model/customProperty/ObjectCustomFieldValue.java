package saleson.model.customProperty;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Part;

@Data
@NoArgsConstructor
public class ObjectCustomFieldValue
{
    private Object t;
    private Long customFieldId;

    @QueryProjection
    public ObjectCustomFieldValue(Object t, Long customFieldId){
        this.t = t;
        this.customFieldId = customFieldId;
    }
}
