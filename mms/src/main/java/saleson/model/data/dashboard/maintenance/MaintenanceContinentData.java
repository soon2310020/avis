package saleson.model.data.dashboard.maintenance;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ContinentName;

@Data
@NoArgsConstructor
public class MaintenanceContinentData {
    private ContinentName continent;
    private Long count;
    private Long upcoming;
    private Long overdue;

    @QueryProjection
    public MaintenanceContinentData(ContinentName continent, Long count) {
        this.continent = continent;
        this.count = count;
    }

    public MaintenanceContinentData(ContinentName continent) {
        this.continent = continent;
    }
}
