package saleson.api.dashboardChartDisplaySetting.payload;

import java.util.Map;

import lombok.Data;
import saleson.common.enumeration.DashboardChartType;

@Deprecated
@Data
public class DashboardChartDisplaySettingLite {
    private DashboardChartType chartType;
    private Map<String, String> dashboardSettingData;
}
