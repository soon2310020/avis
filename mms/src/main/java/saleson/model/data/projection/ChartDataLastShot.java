package saleson.model.data.projection;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;
import saleson.common.util.DataUtils;
import saleson.model.data.ChartData;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChartDataLastShot {
    private String moldCode;
    private String title;
    private Integer data = 0;
    private Integer lastShot;

    @QueryProjection
    public ChartDataLastShot(Long moldId, String title, Integer data,Integer lastShot){
        this.moldCode =moldId!=null? Long.toString(moldId):"";
        this.title = title;
        this.data = data;
        this.lastShot = lastShot;
    }
    public ChartData convertToChartData(){
       return DataUtils.deepCopy(this,ChartData.class);
    }
}
