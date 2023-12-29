package saleson.model.data.dashboard;

import lombok.Builder;
import lombok.Data;
import saleson.model.data.DashboardChartData;

import java.util.List;

@Data @Builder
public class ImplementationStatusData {
    private Long total;
    private List<DashboardChartData> companies;
}
