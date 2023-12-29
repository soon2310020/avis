package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvgCavityStatisticsData {
    private String title;
    private Double avgCavity;
    private Integer totalCavity;

    @QueryProjection
    public AvgCavityStatisticsData(String title, Double avgCavity){
        this.title = title;
        this.avgCavity = avgCavity;
    }

    @QueryProjection
    public AvgCavityStatisticsData(String title, Integer totalCavity){
        this.title = title;
        this.totalCavity = totalCavity;
    }
}
