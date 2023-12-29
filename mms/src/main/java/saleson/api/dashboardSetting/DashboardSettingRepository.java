package saleson.api.dashboardSetting;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.model.DashboardSetting;

@Deprecated
public interface DashboardSettingRepository extends JpaRepository<DashboardSetting, Long>, QuerydslPredicateExecutor<DashboardSetting> {

	List<DashboardSetting> findAllByUserId(Long userId);

	List<DashboardSetting> findAllByDeletedIsFalse();

}
