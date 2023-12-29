package saleson.api.dashboardChartDisplaySetting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.common.enumeration.DashboardChartType;
import saleson.model.DashboardChartDisplaySetting;

@Deprecated
public interface DashboardChartDisplaySettingRepository extends JpaRepository<DashboardChartDisplaySetting, Long>, QuerydslPredicateExecutor<DashboardChartDisplaySetting> {

	DashboardChartDisplaySetting findFirstByChartTypeAndUserId(DashboardChartType chartType, Long userId);

	List<DashboardChartDisplaySetting> findAllByUserId(Long userId);

}
