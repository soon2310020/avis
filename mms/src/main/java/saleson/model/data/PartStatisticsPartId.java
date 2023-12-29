package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PartStatisticsPartId {
    Long partId;
    Long statisticsPartId;

    @QueryProjection
    public PartStatisticsPartId(Long partId, Long statisticsPartId){
        this.partId = partId;
        this.statisticsPartId = statisticsPartId;
    }
}
