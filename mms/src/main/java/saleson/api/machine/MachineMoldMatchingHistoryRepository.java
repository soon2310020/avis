package saleson.api.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.model.Machine;
import saleson.model.MachineMoldMatchingHistory;
import saleson.model.MachineStatistics;
import saleson.model.Mold;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface MachineMoldMatchingHistoryRepository extends JpaRepository<MachineMoldMatchingHistory, Long>, QuerydslPredicateExecutor<MachineMoldMatchingHistory>, MachineMoldMatchingHistoryRepositoryCustom {

    Optional<List<MachineMoldMatchingHistory>> findByMachineAndMatchDay(Machine machine, String day);
    Optional<List<MachineMoldMatchingHistory>> findByMachineAndUnmatchDay(Machine machine, String day);

    Optional<List<Mold>> findMoldByMachineAndMatchDay(Machine machine, String day);
    Optional<List<Mold>> findMoldByMachineAndUnmatchDay(Machine machine, String day);

    @Query("select m from MachineMoldMatchingHistory m where m.machine = :machine and (m.matchDay = :day or m.unmatchDay = :day)")
    Optional<List<MachineMoldMatchingHistory>> findByMachineAndMatchDayOrUnmatchDay(Machine machine, String day);

    Optional<MachineMoldMatchingHistory> findFirstByMachineAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(Machine machine);
    Optional<MachineMoldMatchingHistory> findFirstByMoldAndMatchDayIsNotNullAndCompletedIsFalseOrderByMatchTimeDesc(Mold mold);

    @Query("select m from MachineMoldMatchingHistory m where m.machine = :machine and m.matchDay < :day and (m.unmatchDay > :day or m.unmatchDay is null)")
    Optional<MachineMoldMatchingHistory> findByMachineAndDay(Machine machine, String day);

    List<MachineMoldMatchingHistory> findAllByMatchDayOrUnmatchDayAndMachineId(String matchDay, String unMatchDay, Long machineId);

    MachineMoldMatchingHistory findFirstByMatchTimeBeforeOrderByMatchTimeDesc(Instant date);
    MachineMoldMatchingHistory findFirstByUnmatchTimeBeforeOrderByUnmatchTimeDesc(Instant date);
}
