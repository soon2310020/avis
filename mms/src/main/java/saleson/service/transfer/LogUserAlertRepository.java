package saleson.service.transfer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import saleson.common.enumeration.AlertType;
import saleson.model.LogUserAlert;

@Repository
public interface LogUserAlertRepository extends JpaRepository<LogUserAlert, Long>, QuerydslPredicateExecutor<LogUserAlert> {
	boolean existsByUserIdAndAlertTypeAndAlertId(Long userId, AlertType alertType, Long alertId);

	List<LogUserAlert> findByUserIdAndEmail(Long userId, Boolean email);

	List<LogUserAlert> findByUserIdInAndAlertTypeAndAlertIdIn(List<Long> userIds, AlertType alertType, List<Long> alertIds);

	List<LogUserAlert> findByUserIdAndAlertTypeAndAlertIdIn(Long userId, AlertType alertType, List<Long> alertIds);

	List<LogUserAlert> findByAlertTypeAndAlertId(AlertType alertType, Long alertId);

	List<LogUserAlert> findByAlertTypeAndAlertIdIn(AlertType alertType, List<Long> alertIds);

	List<LogUserAlert> findByUserIdAndAlertIdIn(Long userId, List<Long> alertIds);
}
