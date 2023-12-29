package saleson.api.machine;

import saleson.model.MachineMoldMatchingHistory;
import saleson.model.Mold;

import java.time.Instant;
import java.util.List;

public interface MachineMoldMatchingHistoryRepositoryCustom {
    List<MachineMoldMatchingHistory> findAllByMatchTimeBetweenUnMatchTimeBetween(Instant fromDate, Instant toDate);

    List<Mold> findAllMoldByMatchDayOrUnMatchDayAndMachineId(String matchDay, String unMatchDay, Long machineId);

    List<MachineMoldMatchingHistory> findAllMatchingByMatchingDayBetweenAndMachineId(String start, String end, Long machineId);
}
