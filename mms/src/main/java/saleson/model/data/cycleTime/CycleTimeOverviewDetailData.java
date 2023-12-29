package saleson.model.data.cycleTime;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CycleTimeOverviewDetailData {
    private Long id;
    private String code;
    private String name;

    private Long moldId;
    private String moldCode;

    private Integer shotCount;
    private Double variance;
    private Double cycleTime;

    private Long numMolds;
    private Long totalMolds;

    @QueryProjection
    public CycleTimeOverviewDetailData(Long moldId, String moldCode, Integer shotCount, Double variance,Double cycleTime) {
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.shotCount = shotCount;
        this.variance = variance;
        this.cycleTime = cycleTime;
    }

    @QueryProjection
    public CycleTimeOverviewDetailData(Long id, String code, String name, Long moldId, String moldCode, Integer shotCount, Double variance,Double cycleTime) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.shotCount = shotCount;
        this.variance = variance;
        this.cycleTime = cycleTime;
    }
}
