package saleson.api.terminal;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.model.Location;
import saleson.model.Terminal;
import saleson.model.data.CounterToolingCode;
import saleson.model.data.DashboardChartData;
import saleson.model.data.TerminalData;

import java.util.List;
import java.util.Optional;

public interface TerminalRepositoryCustom {
    Long countCounter(Long locationId);

    List<CounterToolingCode> getCounterToolingCodesById(Long id);

    List<CounterToolingCode> findLastStatisticsTerminalCounter(List<String> terminalCodes);
    List<CounterToolingCode> findAllLastStatisticsTerminalCounter();

    List<CounterToolingCode> findTiCiSameLocation(Predicate predicate, List<String> terminalCodes);

    Page<TerminalData> findTerminalDataSortByNumberCounter(Predicate predicate, Pageable pageable);

    List<Terminal> findByTerminalCodeInSorted(List<String> terminalCodes);

    List<DashboardChartData> findImplementationStatus(DashboardFilterPayload payload);

    List<Terminal> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<Location> findAllLocationByTerminalId(List<Long> terminalIdList);

    Optional<Location> findLocationByTerminalId(Long id);

    List<Long> findAllIdByPredicate(Predicate predicate);

    List<Terminal> findAllOrderByConnectionStatus(Predicate predicate, Pageable pageable);
}
