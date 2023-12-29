package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PartStatisticsPartIds {
    Long partId;
    List<Long> statisticsPartIds;

    @QueryProjection
    public PartStatisticsPartIds(Long partId, List<Long> statisticsPartIds){
        this.partId = partId;
        this.statisticsPartIds = statisticsPartIds;
    }
}
