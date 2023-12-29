package saleson.model.data.productivity;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductivityOverviewData {
    private Double avgProductivity;
    private Integer totalProductivity; // produced part quantity, max productivity of toolings
    private Integer availableProductivity;
    private Double trend;
    private Long moldCount;
    private List<ToolingProductivityData> top5Tooling;
    private Long totalElements;

    private Double totalProductivityPercent;
    private Double availableProductivityPercent;

    public ProductivityOverviewData(Double avgProductivity, Integer totalProductivity, Integer availableProductivity, Double trend, Long moldCount, List<ToolingProductivityData> top5Tooling){
        this.avgProductivity = avgProductivity;
        this.totalProductivity = totalProductivity;
        this.availableProductivity = availableProductivity;
        this.trend = trend;
        this.moldCount = moldCount;
        this.top5Tooling = top5Tooling;
    }

    @QueryProjection
    public ProductivityOverviewData(Double avgProductivity, Integer totalProductivity, Integer availableProductivity, Double trend){
        this.avgProductivity = avgProductivity;
        this.totalProductivity = totalProductivity;
        this.availableProductivity = availableProductivity;
        this.trend = trend;
    }

    @QueryProjection
    public ProductivityOverviewData(Integer totalProductivity){
        this.totalProductivity = totalProductivity;
    }
}
