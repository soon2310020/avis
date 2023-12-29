package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class PartProductionData {
    private Long groupId;
    private Long id;
    private String partCode;
    private String partName;
    private Long totalProduction;


    @QueryProjection
    public PartProductionData(Long groupId, Long id, String partCode, String partName, Long totalProduction) {
        this.groupId = groupId;
        this.id = id;
        this.partCode = partCode;
        this.partName = partName;
        this.totalProduction = totalProduction;
    }

    public PartProductionData(String partCode, String partName, Long totalProduction) {
        this.partCode = partCode;
        this.partName = partName;
        this.totalProduction = totalProduction;
    }
}
