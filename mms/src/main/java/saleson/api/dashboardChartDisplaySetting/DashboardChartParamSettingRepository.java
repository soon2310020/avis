package saleson.api.dashboardChartDisplaySetting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.DashboardChartParamSetting;

@Deprecated
public interface DashboardChartParamSettingRepository extends JpaRepository<DashboardChartParamSetting, Long>, QuerydslPredicateExecutor<DashboardChartParamSetting> {

	List<DashboardChartParamSetting> findAllByDashboardChartDisplaySettingId(Long dashboardChartDisplaySettingId);

	void deleteAllByDashboardChartDisplaySettingId(Long dashboardChartDisplaySettingId);

}
