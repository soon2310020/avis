package saleson.model.data.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.DashboardChartType;
import saleson.service.util.NumberUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardTabData {

    private DashboardChartType type;
    private Double kpi;
    private Double trend;
    private String symbol;

    public DashboardTabData(DashboardChartType type) {
        this.type = type;
    }

    public DashboardTabData(DashboardChartType type, Double kpi, Double trend) {
        this.type = type;
        this.kpi = kpi != null ? NumberUtils.roundOffNumber(kpi) : 0;
        this.trend = trend != null ? NumberUtils.roundOffNumber(trend) : 0;
    }

    public DashboardTabData(DashboardChartType type, Double kpi) {
        this.type = type;
        this.kpi = kpi != null ? NumberUtils.roundOffNumber(kpi) : 0;
    }
}
