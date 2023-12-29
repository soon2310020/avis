package saleson.model.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.util.DateUtils;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class MoldEndLifeCycleChartResponse {
    private List<MoldEndLifeCycleChartData> moldEndLifeCycleChartDataList;
    private Long total;
    private String lastDate;
    private Integer designedShot;			// Forecasted Max shots
    private Integer accumulatingShot = 0;
    private Integer remainingShot = 0;
    private Instant endLifeAt;


    public String getEndLifeAtDate() {
        return DateUtils.getDate(getEndLifeAt());
    }
}
