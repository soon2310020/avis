package saleson.api.location;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.model.DashboardGeneralFilter;
import saleson.model.Location;
import saleson.model.data.LocationData;
import saleson.model.data.completionRate.CompletionRateData;

public interface LocationRepositoryCustom {
	List<LocationData> findLocationData(Predicate predicate, Pageable pageable, List<String> locationCodeList);

	Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);

	CompletionRateData getCompanyCompletionRate(Long companyId);

	Long countCompletionRateData(DataCompletionRatePayload payload);

	List<Location> findAllByGeneralFilter(boolean isAll);

	List<Location> findAllByGeneralFilter(DashboardGeneralFilter filter);

	List<Long> findAllIdByPredicate(Predicate predicate);

	Long countAllIncompleteData();

    List<Location> getAllIncompleteData();
}
