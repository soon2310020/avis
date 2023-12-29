package saleson.model;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MoldMaintenanceCustomFieldValue
{
    private MoldMaintenance moldMaintenance;
    private Long customFieldId;

    @QueryProjection
    public MoldMaintenanceCustomFieldValue(MoldMaintenance moldMaintenance, Long customFieldId){
        this.moldMaintenance = moldMaintenance;
        this.customFieldId = customFieldId;
    }
}
