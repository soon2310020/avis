package saleson.api.machine;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.model.DowntimeItem;
import saleson.model.MachineStatistics;

import java.util.List;

public interface DowntimeItemRepository extends JpaRepository<DowntimeItem, Long>, QuerydslPredicateExecutor<DowntimeItem>, DowntimeItemRepositoryCustom {
    List<DowntimeItem> findByMachineStatistics(MachineStatistics machineStatistics);

    List<DowntimeItem> findByMachineStatisticsAndType(MachineStatistics machineStatistics, MachineAvailabilityType type);

    void deleteByMachineStatistics(MachineStatistics machineStatistics);

    void deleteByMachineStatisticsAndType(MachineStatistics machineStatistics, MachineAvailabilityType type);
}
