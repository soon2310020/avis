package saleson.model.data.cycleTime;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.Mold;

@Data
@Builder
@NoArgsConstructor
public class ToolingCycleTimeDataLite {
    private Long moldId;
    private String moldCode;

    private Integer shotCountCompliance;
    private Double percentageCompliance;

    private Integer shotCountAboveL1;
    private Double percentageAboveL1;
    private Integer shotCountBelowL1;
    private Double percentageBelowL1;

    private Integer shotCountAboveL2;
    private Double percentageAboveL2;
    private Integer shotCountBelowL2;
    private Double percentageBelowL2;

    @QueryProjection
    public ToolingCycleTimeDataLite(Long moldId, String moldCode, Integer shotCountCompliance, Double percentageCompliance, Integer shotCountAboveL1, Double percentageAboveL1, Integer shotCountBelowL1, Double percentageBelowL1, Integer shotCountAboveL2, Double percentageAboveL2, Integer shotCountBelowL2, Double percentageBelowL2) {
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.shotCountCompliance = shotCountCompliance;
        this.percentageCompliance = percentageCompliance;
        this.shotCountAboveL1 = shotCountAboveL1;
        this.percentageAboveL1 = percentageAboveL1;
        this.shotCountBelowL1 = shotCountBelowL1;
        this.percentageBelowL1 = percentageBelowL1;
        this.shotCountAboveL2 = shotCountAboveL2;
        this.percentageAboveL2 = percentageAboveL2;
        this.shotCountBelowL2 = shotCountBelowL2;
        this.percentageBelowL2 = percentageBelowL2;
    }

    public ToolingCycleTimeDataLite(ToolingCycleTimeData data) {
        this.moldId = data.getMoldId();
        this.moldCode = data.getMoldCode();
        this.shotCountCompliance = data.getShotCountCompliance();
        this.percentageCompliance = data.getPercentageCompliance();
        this.shotCountAboveL1 = data.getShotCountAboveL1();
        this.percentageAboveL1 = data.getPercentageAboveL1();
        this.shotCountBelowL1 = data.getShotCountBelowL1();
        this.percentageBelowL1 = data.getPercentageBelowL1();
        this.shotCountAboveL2 = data.getShotCountAboveL2();
        this.percentageAboveL2 = data.getPercentageAboveL2();
        this.shotCountBelowL2 = data.getShotCountBelowL2();
        this.percentageBelowL2 = data.getPercentageBelowL2();
    }
}
