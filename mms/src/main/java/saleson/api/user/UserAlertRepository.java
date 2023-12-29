package saleson.api.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.PeriodType;
import saleson.model.Alert;
import saleson.model.User;
import saleson.model.UserAlert;

import java.util.List;
import java.util.Optional;

public interface UserAlertRepository extends JpaRepository<UserAlert, Long>, QuerydslPredicateExecutor<UserAlert> {
	Optional<UserAlert> findByUserAndAlertType(User user, AlertType alertType);

	List<UserAlert> findByUser(User user);

	boolean existsByUserAndAlertType(User user, AlertType alertType);

	List<UserAlert> findByUserInAndAlertType(List<User> userList, AlertType alertType);

	List<UserAlert> findAllByAlertType(AlertType alertType);

	List<UserAlert> findAllByEmail(Boolean email);

	List<UserAlert> findByEmailAndPeriodType(Boolean email, PeriodType periodType);

	List<UserAlert> findByEmailAndAlertTypeAndPeriodType(Boolean email, AlertType alertType, PeriodType periodType);

	List<UserAlert> findByAlertTypeAndPeriodType(AlertType alertType, PeriodType periodType);

	List<UserAlert> findByAlertTypeIn(List<AlertType> alertTypes);

	List<UserAlert> findByUserAndAlertTypeIn(User user, List<AlertType> alertTypes);

	List<UserAlert> findByUserInAndAlertTypeInAndEmailIsTrue(List<User> users, List<AlertType> alertTypes);
	List<UserAlert> findByAlertTypeAndAlertOnIsTrue(AlertType alertType);
}

