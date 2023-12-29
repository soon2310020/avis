package saleson.api.mold;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.emoldino.framework.enumeration.ActiveStatus;
import com.querydsl.core.types.Predicate;

import saleson.api.chart.payload.ChartPayload;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.chart.payload.MapSearchPayload;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.machine.payload.MachineMoldData;
import saleson.api.mold.payload.ExportPayload;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.payload.PartPayload;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.DataRangeType;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.ReportType;
import saleson.common.enumeration.productivity.CompareType;
import saleson.dto.MapQueryType;
import saleson.model.Location;
import saleson.model.Machine;
import saleson.model.Mold;
import saleson.model.MoldCustomFieldValue;
import saleson.model.MoldEfficiency;
import saleson.model.data.CdataCounter;
import saleson.model.data.ChartData;
import saleson.model.data.CompanyChartData;
import saleson.model.data.CountLocationMold;
import saleson.model.data.DashboardChartData;
import saleson.model.data.LocationMoldData;
import saleson.model.data.MaintenanceTimeData;
import saleson.model.data.MapChartData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldAccumulatedData;
import saleson.model.data.MoldCycleTimeExtraData;
import saleson.model.data.MoldEfficiencyExtraData;
import saleson.model.data.MoldExtraData;
import saleson.model.data.MoldMachinePairData;
import saleson.model.data.MoldMaintenanceExtraData;
import saleson.model.data.MoldReportData;
import saleson.model.data.MoldReportDataPage;
import saleson.model.data.MoldShotData;
import saleson.model.data.PartProductionData;
import saleson.model.data.PartStatisticsPartIds;
import saleson.model.data.StatisticsData;
import saleson.model.data.StatisticsPartData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.data.cycleTime.CycleTimeOverviewDetailData;
import saleson.model.data.cycleTime.ToolingCycleTimeData;
import saleson.model.data.dashboard.cost.CostData;
import saleson.model.data.productivity.ProductivityOverviewData;
import saleson.model.data.productivity.ToolingProductivityData;
import saleson.model.data.wut.MoldCttTempData;


public interface MoldRepositoryCustom {

	List<ChartData> findChartData(ChartPayload payload);

	List<ChartData> findCycleTimeData(ChartPayload payload, Mold moldData);

	List<ChartData> findHourDetailsStatistics(ChartPayload payload, List<Long> moldIdList);

	List<DashboardChartData> findMoldOverview(DashboardFilterPayload payload);


	/**
	 * Company 상세 정보에 표시되는 차트 데이터 (Status)
	 * @param payload
	 * @return
	 */
	List<CompanyChartData> getCompanyDetailsChartData(CompanyPayload payload);


	/**
	 * 통계 데이터 기준 (STATISTICS, STATISTICS_PART)
	 * 제품(part) 생산량 (shotCount * cavity)
	 *
	 * @param partIds
	 * @return
	 */
	List<StatisticsPartData> getStatisticsPartData(List<Long> partIds, PartPayload payload);

	public List<StatisticsData> getMoldUptimeData(DashboardFilterPayload payload);


	/**
	 * 세계지도 맵 데이터
	 * @param payload
	 * @return
	 */
	List<MapChartData> getMapData(MoldPayload payload);

	List<LocationMoldData> getLocationMoldData(MoldPayload payload);


	/**
	 * 금형과 연결이 안된 채 동작하거나 등록이 안된채 동작하는 카운터 목록
	 * @param payload
	 * @param pageable
	 * @return
	 */
	Page<CdataCounter> findCdataCounters(CounterPayload payload, Pageable pageable);

	/**
	 * 금형과 연결이 안된 채 동작하거나 등록이 안된채 동작하는 카운터 수
	 * @param payload
	 * @return
	 */
	long findCdataCountersCount(CounterPayload payload);

	List<MapChartData> getMapDataByDashboardFilter(String countryCode, DashboardFilterPayload payload);

	List<MapChartData> getMapDataSearchByType(MapSearchPayload payload, MapQueryType mapQueryType);

	long getBaseMoldCount(DashboardFilterPayload payload);

	long getTotalMoldCount(DashboardFilterPayload payload);

	long getInstalledMoldCount(DashboardFilterPayload payload);

	Double getTotalCost(DashboardFilterPayload payload);

	Double getTotalCost(TabbedOverviewGeneralFilterPayload payload, boolean current);

	List<CostData> getTotalCostGroupByFrequent(TabbedOverviewGeneralFilterPayload payload);

	long getPartCount(DashboardFilterPayload payload);

	long getProducedPart(DashboardFilterPayload payload);

	List<PartStatisticsPartIds> findMoldIdsByDashboardPayloadAndPartIds(DashboardFilterPayload payload, List<Long> partIds, Frequent frequent);

	List<PartStatisticsPartIds> findPartStatisticsData(TabbedOverviewGeneralFilterPayload payload, List<Long> partIds, Frequent frequent);

	List<MaintenanceTimeData> findMaintenanceTimeData(DashboardFilterPayload payload);

	MoldReportDataPage findReportData(DashboardFilterPayload payload, Pageable pageable, ReportType type);

	Page<MoldReportData> findReportDataWithRateCapacity(DashboardFilterPayload payload, Pageable pageable);

	List<Long> getMoldIdsSubQuery();

	List<MoldCycleTimeExtraData> findMoldCycleTimeExtraData(Predicate predicate, Pageable pageable);

	List<MoldEfficiencyExtraData> findMoldEfficiencyExtraData(Predicate predicate, Pageable pageable);

	List<MoldMaintenanceExtraData> findMoldMaintenanceExtraData(Predicate predicate, Pageable pageable);
	public List<MoldMaintenanceExtraData> findMoldMaintenanceExtraDataForMoldTable(Predicate predicate, Pageable pageable);
	List<MoldExtraData> findMoldExtraData(Predicate predicate, Pageable pageable);

	List<MoldCustomFieldValue> findMoldCustomFieldValue(Predicate predicate, Pageable pageable);

	List<MiniComponentData> findExistsMoldCodes(List<String> moldCodes);

	List<MiniComponentData> findAllMoldIdMoldCodes(List<Long> moldIds);

	Page<MiniComponentData> findMoldLiteData(String code, Pageable pageable);

	List<MiniComponentData> findAllMoldIdMoldCodesByGeneralFilter(List<Long> moldIds);

    List<Long> findMoldIdsFromProductivitySearchPayload(ProductivitySearchPayload payload);

	ProductivityOverviewData findMaxProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson);

/*
	List<ToolingProductivityData> findToolingMaxProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson);
*/

	List<ToolingProductivityData> findToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson, Pageable pageable);
	Long countToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson);
	List<ToolingProductivityData> findProductivityMaxCapacity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson, Pageable pageable);
//	List<MoldCapacityReportData> findAvailableDowntime( List<Long> moldIds, ProductivitySearchPayload payload);
	List<ChartData> findDailyProducedQuantity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson);

	// Get avg cycle time: total uptime / total shot count
	List<ToolingCycleTimeData> getAvgCycleTimeInRange(List<Long> moldIds, ProductivitySearchPayload payload, CycleTimeStatus status);
	// get data cycle time by range
	List<CycleTimeOverviewDetailData> getReportCycleTimeInRange(List<Long> moldIds, ProductivitySearchPayload payload, DataRangeType dataRangeType);

	List<ToolingCycleTimeData> getComplianceShotCountInRange(List<Long> moldIds, ProductivitySearchPayload payload);



	List<MoldCttTempData> findMoldCttTempData(Long moldId, String startTime, String endTime);
	List<ToolingCycleTimeData> findToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable, boolean getAll);
	Long countToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload,boolean getAll);

	List<DashboardChartData> findImplementationStatus(DashboardFilterPayload payload);

	List<MiniComponentData> findMoldsForPMRegistering();
	List<MiniComponentData> findMoldsUnmatchedWithMachine();
	Page<MiniComponentData> findPageMoldsUnmatchedWithMachine(Predicate predicate, Pageable pageable);
	Page<MachineMoldData> findMoldToMatch(Predicate predicate, Pageable pageable);
	List<MoldMachinePairData> findMoldsMatchedWithMachine();
	Page<MoldMachinePairData> findPageMoldsMatchedWithMachine(Predicate predicate, Pageable pageable);

	List<DashboardChartData> findCycleTimeDashboard(Long moldId, Frequent frequent, Integer limit);

/*
	List<ChartData> findDataExportDynamic(ExportPayload payload, List<Mold> moldList);
*/

	List<ChartData> findDataExportDynamicNew(ChartDataType chartDataType,ExportPayload payload, List<Mold> moldList);

	Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg);

	CompletionRateData getCompanyCompletionRate(Long companyId);

	List<PartProductionData> findPartProduction(List<Long> moldIds, Long partId, String startDate, String endDate, CompareType compareType,List<Long> companyIds, Pageable pageable);

	List<Long> findUsedMoldIds(Long partId, String fromDate, String toDate);

	List<Mold> findMoldOrderByInactivePeriod(Predicate predicate, Pageable pageable);

	List<MoldAccumulatedData> findMoldOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds);

	List<MoldAccumulatedData> findMoldAccumulatedShotByStatistic(boolean isReSumAll, String year, List<Long> moldIds);

	List<MoldAccumulatedData> findMoldAccumulatedShot(String year, List<Long> moldIds);

	MoldAccumulatedData findMoldAccumulatedShotByLstLessThan(String lst, Long moldId);

	Long countByLastShotAt(Instant from, Instant to, List<Long> filteredIds);

	Long countByProduct(Long productId, Long partId, List<Long> supplierId);

	Long countByBrand(Long brandId, Long partId, List<Long> supplierId);

	Page<Mold> findByProject(Long projectId, Long partId, List<Long> supplierId, Pageable pageable);

	Page<Mold> findByBrand(Long brandId, Long partId, List<Long> supplierId, Pageable pageable);

	List<StatisticsPartData> getProjectTotalProduced(Long projectId);

	MoldShotData findProducedPartData(Long moldId, String time, Frequent frequent);

	List<CountLocationMold> countLocationMold(TabbedOverviewGeneralFilterPayload payload);

	List<MapChartData> getMapData(TabbedOverviewGeneralFilterPayload payload);

	Long countByPredicate(Predicate predicate);

	Long countByLastShotAt(Instant from, Instant to, TabbedOverviewGeneralFilterPayload payload);


	List<CountLocationMold> countLocationMold();

	List<Long> findMoldIdByMachineIdIn(List<Long> machineIds);

	List<Long> findMoldIdByMachineIn(List<Machine> machines);

	List<Long> findMoldIdByStartEndAndMachineIn(List<Machine> machines, String start);

	Long findMoldIdByMachineAndTime(Long machineId, Instant time);

	List<Mold> findMoldOrderByOperatingStatus(Predicate predicate, Pageable pageable);

	List<Mold> findMoldOrderBySlDepreciation(Predicate predicate, Pageable pageable);

	List<Mold> findMoldOrderByUpDepreciation(Predicate predicate, Pageable pageable);

	List<Mold> findMoldOrderByUpperTierCompanies(Predicate predicate, Pageable pageable);

	List<MoldEfficiency> findMoldEfficiencyOrderByOperatingStatus(Predicate predicate, Pageable pageable);
	List<MoldEfficiency> findMoldEfficiencyOrderByStatus(Predicate predicate, Pageable pageable);

	List<Location> findLocationByMoldIdIn(List<Long> moldIdList);

	Optional<Location> findLocationByMoldId(Long id);

	List<Long> findAllIdByPredicate(Predicate predicate);

	Long countByProductIdIn(List<Long> productIdList);

	List<Mold> findMoldOrderByStatus(Predicate predicate, Pageable pageable);

	Long countAllIncompleteData(List<String> deletedFieldMold);
	List<Mold> findAllMoldHaveOPMissMatch();

	List<Mold> getAllIncompleteData(List<String> deletedFieldMold);

	Long countByMasterFilter(Predicate predicate, ActiveStatus activeStatus);

	@Deprecated
	Long countByMasterFilter(Predicate predicate, Boolean deleted);

	Page<Mold> findAllByMasterFilter(Predicate predicate, ActiveStatus activeStatus, Pageable pageable);

	List<Long> findAllIdsByMasterFilter(Predicate predicate, ActiveStatus activeStatus);


	List<Mold> findMoldOrderByTco(Predicate predicate, Pageable pageable);
}
