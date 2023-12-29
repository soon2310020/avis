package saleson.api.dashboardSetting.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.DashboardSettingLevel;
import saleson.model.DashboardSetting;

@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSettingsLite {
	private DashboardSettingLevel level;
	private Integer monthNumber;

	public DashboardSettingsLite(DashboardSetting dashboardSetting) {
		this.level = dashboardSetting.getLevel();
		this.monthNumber = dashboardSetting.getMonthNumber();
	}
}
