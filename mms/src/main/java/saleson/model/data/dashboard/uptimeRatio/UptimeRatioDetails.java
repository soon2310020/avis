package saleson.model.data.dashboard.uptimeRatio;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class UptimeRatioDetails {
    private Long id;
    private String name;
    private String code;
    private Long moldCount;
    private Double actualUptimeHour;
    private Integer targetUptimeHour;
    private Double uptimeRatio;

    @QueryProjection
    public UptimeRatioDetails(Long id, String name, String code, Long moldCount,
                              Double actualUptimeHour, Integer targetUptimeHour, Double uptimeRatio){
        this.id = id;
        this.name = name;
        this.code = code;
        this.moldCount = moldCount;
        this.actualUptimeHour = actualUptimeHour;
        this.targetUptimeHour = targetUptimeHour;
        this.uptimeRatio = uptimeRatio;
    }
}
