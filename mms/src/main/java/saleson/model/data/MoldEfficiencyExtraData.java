package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.model.MoldEfficiency;

@Data
public class MoldEfficiencyExtraData {
    private MoldEfficiency moldEfficiency;
    private Double variance;

    @QueryProjection
    public MoldEfficiencyExtraData(MoldEfficiency moldEfficiency, Double variance){
        this.moldEfficiency = moldEfficiency;
        this.variance = variance;
    }
}
