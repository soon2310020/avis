package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import saleson.model.Part;

@Data
public class PartWithStatisticsData {
    private Part part;
    private Long sortValueLong;
    private Integer sortValueInt;

    @QueryProjection
    public PartWithStatisticsData(Part part, Long sortValueLong){
        this.part = part;
        this.sortValueLong = sortValueLong;
    }

    @QueryProjection
    public PartWithStatisticsData(Part part, Integer sortValueInt){
        this.part = part;
        this.sortValueInt = sortValueInt;
    }

    @QueryProjection
    public PartWithStatisticsData(Part part) {
        this.part = part;
    }
}
