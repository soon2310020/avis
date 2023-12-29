package saleson.api.machineDowntimeAlert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.MachineDowntimeAlertUser;

public interface MachineDowntimeAlertUserRepository  extends JpaRepository<MachineDowntimeAlertUser, Long>, QuerydslPredicateExecutor<MachineDowntimeAlertUser> {
}
