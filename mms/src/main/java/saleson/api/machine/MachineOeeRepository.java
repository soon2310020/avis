package saleson.api.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.Frequent;
import saleson.model.Machine;
import saleson.model.MachineOee;
import saleson.model.Mold;

import java.util.List;
import java.util.Optional;

public interface MachineOeeRepository extends JpaRepository<MachineOee, Long>, QuerydslPredicateExecutor<MachineOee>, MachineOeeRepositoryCustom {
    Optional<MachineOee> findByMachineAndDay(Machine machine, String day);

    List<MachineOee> findByMachineAndHourIn(Machine machine, List<String> hours);

    List<MachineOee> findByMachineIdAndHourInAndPeriodType(Long machineId, List<String> hours, Frequent periodType);

    Optional<MachineOee> findFirstByMachineIdAndHourInAndPeriodTypeOrderByTenMinuteDesc(Long machineId, List<String> hours, Frequent periodType);

    Optional<MachineOee> findFirstByMachineIdAndHourInAndPeriodTypeOrderByHourDesc(Long machineId, List<String> hours, Frequent periodType);

    Optional<MachineOee> findFirstByMachineAndPeriodTypeOrderByHourDesc(Machine machine, Frequent periodType);

    Optional<List<MachineOee>> findByMachineAndHour(Machine machine, String hour);

    List<MachineOee> findAllByMachineIdAndDay(Long machineId, String day);

    List<MachineOee> findAllByMachineIdAndHourBetweenAndPeriodType(Long machineId, String hourStart, String hourEnd, Frequent periodType);

    Optional<List<MachineOee>> findByMachineAndHourAndPeriodType(Machine machine, String hour, Frequent periodType);

    Optional<List<MachineOee>> findByMoldAndHourAndPeriodType(Mold mold, String hour, Frequent periodType);

    List<MachineOee> findAllByMoldIdInAndDayAndPeriodType(List<Long> moldIdList, String day, Frequent periodType);
    List<MachineOee> findAllByMoldIdInAndHourAndPeriodType(List<Long> moldIdList, String day, Frequent periodType);

    List<MachineOee> findAllByMachineIdInAndHourBetweenAndPeriodType(List<Long> machineIdList, String hourStart, String hourEnd, Frequent periodType);
}
