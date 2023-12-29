package saleson.model.data.dashboard.cost;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.service.util.NumberUtils;

@Data
@NoArgsConstructor
public class CostData {
    private String title;

    Integer year;
    Integer weekOrMonth;
    Integer day;
    private Double toolingCost;
    private Integer maintenanceCost;

    public CostData(String title) {
        this.title = title;
        this.year = Integer.valueOf(title.substring(0,4));
        this.weekOrMonth = Integer.valueOf(title.substring(4,6));
        if (title.length() == 8) this.day = Integer.valueOf(title.substring(6,8));
        this.toolingCost = 0D;
        this.maintenanceCost = 0;
    }

    @QueryProjection
    public CostData(String title, Double toolingCost, Integer maintenanceCost) {
        this.title = title;
        this.year = Integer.valueOf(title.substring(0,4));
        this.weekOrMonth = Integer.valueOf(title.substring(4,6));
        if (title.length() == 8) this.day = Integer.valueOf(title.substring(6,8));
        this.toolingCost = toolingCost != null ? NumberUtils.roundOffNumber(toolingCost) : 0;
        this.maintenanceCost = maintenanceCost != null ? maintenanceCost : 0;
    }
}
