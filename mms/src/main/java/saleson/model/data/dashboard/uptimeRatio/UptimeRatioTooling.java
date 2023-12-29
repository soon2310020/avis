package saleson.model.data.dashboard.uptimeRatio;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.data.MiniGeneralData;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UptimeRatioTooling {
    private Long moldId;
    private String moldCode;
    private List<MiniGeneralData> info;
    private Integer madeYear;
    private Double actualUptimeHour;
    private Integer targetUptimeHour;
    private Double uptimeRatio;

    @QueryProjection
    public UptimeRatioTooling(Long moldId, String moldCode, Integer madeYear, Double actualUptimeHour, Integer targetUptimeHour, Double uptimeRatio){
        this.moldId = moldId;
        this.moldCode = moldCode;
        this.madeYear = madeYear;
        this.actualUptimeHour = actualUptimeHour;
        this.targetUptimeHour = targetUptimeHour;
        this.uptimeRatio = uptimeRatio;
    }
}
