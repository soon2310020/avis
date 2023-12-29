package saleson.api.dashboardSetting.payload;

import lombok.Data;
import saleson.common.enumeration.DashboardSettingLevel;

@Deprecated
@Data
public class UpdateCheckedPayload {
	private DashboardSettingLevel level;
	private Integer monthNumber;
}
