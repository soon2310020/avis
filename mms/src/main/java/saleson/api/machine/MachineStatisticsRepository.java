package saleson.api.machine;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.Machine;
import saleson.model.MachineStatistics;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MachineStatisticsRepository extends JpaRepository<MachineStatistics, Long>, QuerydslPredicateExecutor<MachineStatistics>, MachineStatisticsRepositoryCustom {
    List<MachineStatistics> findByMachineIn(List<Machine> machines);

    Optional<List<MachineStatistics>> findByDateBetween(Instant start, Instant end);
    Optional<List<MachineStatistics>> findByMachineAndDateBetween(Machine machine, Instant start, Instant end);
    Optional<List<MachineStatistics>> findByMachineAndDateBetweenAndDailyWorkingHourNotNull(Machine machine, Instant start, Instant end);
    Optional<List<MachineStatistics>> findByMachineAndDateBetweenAndPlannedDowntimeNotNull(Machine machine, Instant start, Instant end);
    Optional<List<MachineStatistics>> findByMachineAndDateBetweenAndUnplannedDowntimeNotNull(Machine machine, Instant start, Instant end);

    Optional<MachineStatistics> findByMachineAndDay(Machine machine, String day);

    List<MachineStatistics> findByDayAndMachineIn(String day, List<Machine> machines);

    @Query("select m from MachineStatistics m where (m.date between :start and :end) and m.machine.companyId = :companyId")
    Optional<List<MachineStatistics>> findByDateBetweenAndCompany(Instant start, Instant end, Long companyId);

    @Query(nativeQuery = true, value = "select m.id from MACHINE_STATISTICS m where m.machine_id = :machineId and (m.day between :from and :to) and (m.planned_downtime is not null or m.unplanned_downtime is not null) limit 1")
    Long existsMachineStatisticsByMachineAndDateBetweenAndPlannedDowntimeIsNotNullOrUnplannedDowntimeIsNotNull(Long machineId, String from, String to);

    @Query("select m from MachineStatistics m where m.machine = :machine and (m.day between :from and :to) and (m.plannedDowntime is not null or m.unplannedDowntime is not null)")
    List<MachineStatistics> findByMachineAndDateBetweenAndPlannedDowntimeIsNotNullOrUnplannedDowntimeIsNotNull(Machine machine, String from, String to);

}
