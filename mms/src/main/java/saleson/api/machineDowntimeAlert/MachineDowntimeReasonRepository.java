package saleson.api.machineDowntimeAlert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.MachineDowntimeReason;

import java.util.List;

public interface MachineDowntimeReasonRepository extends JpaRepository<MachineDowntimeReason, Long>, QuerydslPredicateExecutor<MachineDowntimeReason>, MachineDowntimeReasonRepositoryCustom{

    List<MachineDowntimeReason> findAllByMachineDowntimeAlertIdIn(List<Long> machineDowntimeAlertIdList);
    List<MachineDowntimeReason> findByMachineDowntimeAlertId(Long alertId);
    void deleteByMachineDowntimeAlertId(Long alertId);

}
