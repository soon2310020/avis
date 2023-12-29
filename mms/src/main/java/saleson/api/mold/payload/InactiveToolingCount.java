package saleson.api.mold.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.DashboardSettingLevel;
import saleson.model.DashboardSetting;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InactiveToolingCount
{
    private DashboardSettingLevel level;
    private Long count;
    private Integer monthNumber;

    public InactiveToolingCount(Long count, DashboardSetting dashboardSetting)
    {
        this.level = dashboardSetting.getLevel();
        this.count = count;
        this.monthNumber = dashboardSetting.getMonthNumber();
    }
}
