package saleson.api.machine;


import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.machine.payload.MachineMoldData;
import saleson.model.Location;
import saleson.model.Machine;
import saleson.model.data.MiniComponentData;
import saleson.model.customProperty.ObjectCustomFieldValue;
import saleson.model.data.MachineData;
import saleson.model.data.completionRate.CompletionRateData;

import java.util.List;
import java.util.Optional;

public interface MachineRepositoryCustom {
    List<MiniComponentData> findMachinesForMatchWithMold();

    Page<MiniComponentData> findMachineNotMatched(Predicate predicate, Pageable pageable);

    Page<MachineMoldData> findMachineToMatch(Predicate predicate, Pageable pageable);
    List<ObjectCustomFieldValue> findAndSortWithCustomFieldValue(Predicate predicate, Pageable pageable);

    Page<MachineData>  findAllMachineForStatistics(Predicate predicate, Pageable pageable, String day);

    Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);

    CompletionRateData getCompanyCompletionRate(Long companyId);

    List<Machine> findMachineMatchedWithTooling();

    List<Location> findAllLocationByMachineId(List<Long> machineIdList);

    Optional<Location> findLocationByMachineId(Long id);

    Page<Machine> findAllOrderBySpecial(String code, Predicate predicate, Pageable pageable);

    Long countAllIncompleteData();

    List<Machine> getAllIncompleteData();

    List<Long> findAllIdByPredicate(Predicate predicate);
}
