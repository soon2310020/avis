package saleson.model.data.chartData;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import saleson.model.data.ChartData;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataPreset {
    private Long moldId;
    private String moldCode;
    private String title;
    private Integer data = 0;
    private Integer lastShot;

    @QueryProjection
    public ChartDataPreset(Long moldId, String title, Integer data,Integer lastShot) {
        this.moldId = moldId;
        this.moldCode = moldId != null ? Long.toString(moldId) : "";
        this.title = title;
        this.data = data;
        this.lastShot = lastShot;
    }
    public ChartData toChartData(){
        return ChartData.builder()
                .moldId(moldId)
                .moldCode(moldCode)
                .title(title)
                .data(data)
                .lastShot(lastShot).build();
    }

}
