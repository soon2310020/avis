package saleson.api.part;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.part.payload.PartPayload;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.model.DashboardGeneralFilter;
import saleson.model.Part;
import saleson.model.PartCustomFieldValue;
import saleson.model.data.ChartData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MiniGeneralData;
import saleson.model.data.PartWithStatisticsData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.data.cycleTime.CycleTimeOverviewData;
import saleson.model.data.dashboard.otd.OTDData;
import saleson.model.data.productivity.ProductivityOverviewData;

public interface PartRepositoryCustom {
    Page<PartWithStatisticsData> findWithSpecialSort(PartPayload payload, Pageable pageable);

    ProductivityOverviewData findProducedQuantity(List<Long> moldIds, ProductivitySearchPayload payload);

    CycleTimeOverviewData findCycleTimeOverviewData(ProductivitySearchPayload payload);

    List<String> findCountriesByPartId(Long partId);

    List<MiniComponentData> findExistsPartCodes(List<String> codes);

    List<MiniComponentData> findExistsPartNames(List<String> names);

    Integer findPartProducedByWeek(Long partId, String week);

    OTDData findListDetailsOtd(Long partId, String week, Pageable pageable);

    List<ChartData> findSupplierTotalCavityPart(Long partId);

    List<ChartData> findWeekRemainingCapacityToolings(Long partId, Integer remainingDays);

    List<ChartData> findToolingProducedPart(Long partId, Long companyId, String week);

    List<MiniComponentData> findListPart(List<String> partCodeList, String searchText, Long page, Long size);
    List<MiniComponentData> findListPartName();

    List<PartCustomFieldValue> findPartCustomFieldValue(Predicate predicate, Pageable pageable);

    List<MiniGeneralData> findPartByMoldIdIn(List<Long> moldIds);

    Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);

    CompletionRateData getCompanyCompletionRate(Long companyId);

    Long countByProduct(Long productId, List<Long> supplierId, Long moldId);

    Long countByBrand(Long brandId, List<Long> supplierId, Long moldId);

	Page<Part> findAllByProduct(Long productId, List<Long> supplierId, Long moldId, Pageable pageable);
	Page<Part> findAllByBrand(Long brandId, List<Long> supplierId, Long moldId, Pageable pageable);
	Page<Part> findAllByProduct(Long productId, List<Long> supplierId, Long moldId, String query, Pageable pageable);
	Page<Part> findAllByBrand(Long productId, List<Long> supplierId, Long moldId, String query, Pageable pageable);

    boolean checkIfSomePartsNotProducedByProjectId(Long projectId);

    List<Part> findAllByGeneralFilter(boolean isAll);
    List<Part> findAllByGeneralFilter(DashboardGeneralFilter filter);
    Long countByFilter(Predicate predicate);

    List<Long> findAllIdByPredicate(Predicate predicate);

    Page<Part> getPartOrderByMachineId(Predicate predicate, Pageable pageable);

    Page<Part> getPartOrderByCategoryName(Predicate predicate, Pageable pageable);

    Long countByProductIdIn(List<Long> productIdList);

    Long countAllIncompleteData();

    List<Part> getAllIncompleteData();

}
