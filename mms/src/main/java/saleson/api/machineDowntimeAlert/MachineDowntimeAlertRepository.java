package saleson.api.machineDowntimeAlert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.model.MachineDowntimeAlert;

public interface MachineDowntimeAlertRepository
        extends JpaRepository<MachineDowntimeAlert, Long>, QuerydslPredicateExecutor<MachineDowntimeAlert>, MachineDowntimeAlertRepositoryCustom {

    Optional<MachineDowntimeAlert> findByMachineId(Long machineId);

    Optional<MachineDowntimeAlert> findByMachineIdAndDowntimeStatusIn(Long machineId, List<MachineDowntimeAlertStatus> statusList);

	List<MachineDowntimeAlert> findByStartTimeBetween(Instant start, Instant end);

    List<MachineDowntimeAlert> findByMachineIdAndDowntimeStatusInAndLatestTrue(Long machineId, List<MachineDowntimeAlertStatus> statusList);

    Optional<MachineDowntimeAlert> findFirstByMachineIdAndStartTimeOrderByCreatedAtDesc(Long machineId, Instant startTime);

}
