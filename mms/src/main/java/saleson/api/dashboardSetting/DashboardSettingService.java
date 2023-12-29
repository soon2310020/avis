package saleson.api.dashboardSetting;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import saleson.api.dashboardSetting.payload.DashboardSettingsLite;
import saleson.common.config.Const;
import saleson.common.enumeration.DashboardSettingLevel;
import saleson.common.payload.ApiResponse;
import saleson.common.util.SecurityUtils;
import saleson.model.DashboardSetting;

@Deprecated
@Service
public class DashboardSettingService {
	@Autowired
	DashboardSettingRepository dashboardSettingRepository;

	public List<DashboardSettingsLite> getAllDashboardSetting() {
		List<DashboardSetting> dashboardSettingList = dashboardSettingRepository.findAllByUserId(SecurityUtils.getUserId());
		if (CollectionUtils.isNotEmpty(dashboardSettingList)) {
			return dashboardSettingList.stream().map(DashboardSettingsLite::new).collect(Collectors.toList());
		} else {
			return getDefaultSetting();
		}
	}

	private List<DashboardSettingsLite> getDefaultSetting() {
		DashboardSettingsLite dashboardSettingsLiteFirst = new DashboardSettingsLite(DashboardSettingLevel.FIRST_LEVEL, Const.DASHBOARD_SETTING_DEFAULT.FIRST_LEVEL);
		DashboardSettingsLite dashboardSettingsLiteSecond = new DashboardSettingsLite(DashboardSettingLevel.SECOND_LEVEL, Const.DASHBOARD_SETTING_DEFAULT.SECOND_LEVEL);
		DashboardSettingsLite dashboardSettingsLiteThird = new DashboardSettingsLite(DashboardSettingLevel.THIRD_LEVEL, Const.DASHBOARD_SETTING_DEFAULT.THIRD_LEVEL);
		return Arrays.asList(dashboardSettingsLiteFirst, dashboardSettingsLiteSecond, dashboardSettingsLiteThird);
	}

	public ApiResponse changeChecked(List<DashboardSettingsLite> updateCheckedPayloadList) {
		Long userId = SecurityUtils.getUserId();
		List<DashboardSetting> dashboardSettingList = dashboardSettingRepository.findAllByUserId(userId);
		Map<DashboardSettingLevel, DashboardSettingsLite> dashboardSettingLevelDashboardSettingsLiteMap = updateCheckedPayloadList.stream()
				.collect(Collectors.toMap(DashboardSettingsLite::getLevel, Function.identity()));

		if (CollectionUtils.isNotEmpty(dashboardSettingList)) {
			dashboardSettingList.forEach(dashboardSetting -> {
				DashboardSettingsLite dashboardSettingsLite = dashboardSettingLevelDashboardSettingsLiteMap.get(dashboardSetting.getLevel());
				if (dashboardSettingsLite != null) {
					dashboardSetting.setMonthNumber(dashboardSettingsLite.getMonthNumber());
				}
			});
		} else {
			dashboardSettingList = Arrays.stream(DashboardSettingLevel.values()).filter(level -> !level.equals(DashboardSettingLevel.ALL_LEVEL)).map(dashboardSettingLevel -> {
				DashboardSettingsLite dashboardSettingsLite = dashboardSettingLevelDashboardSettingsLiteMap.get(dashboardSettingLevel);
				return new DashboardSetting(dashboardSettingLevel, dashboardSettingsLite.getMonthNumber(), userId);
			}).collect(Collectors.toList());

		}
		dashboardSettingRepository.saveAll(dashboardSettingList);

		return ApiResponse.success();
	}
}
