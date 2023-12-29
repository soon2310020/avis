package saleson.api.counter;

import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Pageable;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.model.Counter;
import saleson.model.Location;
import saleson.model.data.CounterToolingCode;
import saleson.model.data.CounterToolingData;
import saleson.model.data.DashboardChartData;

import java.util.List;
import java.util.Optional;

public interface CounterRepositoryCustom {
    List<DashboardChartData> findImplementationStatus(DashboardFilterPayload payload);

    List<CounterToolingData> getListCounter(List<String> counterCodeList, Pageable pageable, String searchText, Boolean isUnmatched);

    List<Counter> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable);

    List<Location> findAllLocationByCounterId(List<Long> counterIds);
    Optional<Location> findLocationByCounterId(Long id);

    List<Counter> findAllOrderByStatus(Predicate predicate, Pageable pageable);
    List<Counter> findAllOrderBySpecialField(Predicate predicate, Pageable pageable);

    List<Long> findAllIdByPredicate(Predicate predicate);

}
