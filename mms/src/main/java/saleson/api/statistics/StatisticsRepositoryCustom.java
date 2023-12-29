package saleson.api.statistics;

import java.time.Instant;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import saleson.api.chart.payload.*;
import saleson.api.machine.payload.*;
import saleson.api.statistics.payload.*;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.enumeration.*;
import saleson.dto.*;
import saleson.model.*;
import saleson.model.data.*;
import saleson.model.data.dashboard.uptimeRatio.*;

public interface StatisticsRepositoryCustom {

	@Modifying
	void resetStoredAllValidShotVals();

	@Modifying
	void updateValsAsOriginWhenIsNull();

	List<ChartData> findChartData(DashboardFilterPayload payload);

	List<Statistics> findStatisticsByDashboardPayload(DashboardFilterPayload payload);

	List<ProductionQuantityData> findProductionQuantityData(List<Long> statisticsPartIds, Frequent frequent);

	List<ProductionQuantityData> findProductionQuantityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent);

	List<ProductionQuantityData> findProductionQuantityOfTooling(TabbedOverviewGeneralFilterPayload payload, Frequent frequent);
	Long countProductionQuantityOfTooling(TabbedOverviewGeneralFilterPayload payload, boolean current);

	List<MoldCapacityReportData> findMoldCapacityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent, boolean isDyson);

	List<MoldCapacityReportData> findProductionCapacityOfTooling(TabbedOverviewGeneralFilterPayload payload, Frequent frequent, boolean isDyson);

	List<MoldCapacityReportData> findMaxCapacityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent, boolean isDyson);
	List<MoldCapacityReportData> findMaxCapacityOfTooling_New(TabbedOverviewGeneralFilterPayload payload, Frequent frequent, boolean isDyson);

	List<StatisticsAccumulatingShot> statisticsAccumulatingShotByDay(Mold mold, String day, Pageable pageable);

	Long countStatisticsAccumulatingShotByDay(String day);

	List<TransactionDTO> getTransactionByDay(String day);

	List<Statistics> findFirstStatistics();

	List<ChartDataOte> findChartDataOte(DashboardFilterPayload payload);

	List<ChartDataOte> findChartDataOtePart(DashboardFilterPayload payload);

	Page<UptimeRatioTooling> findUptimeRatioTooling(FrequentUsage frequent, String type, Long id, String from, String to, Pageable pageable, boolean getAll);

/*
	Page<UptimeRatioDetails> findUptimeRatioDetails(String type, Long id, String from, String to, Pageable pageable);
*/

	Page<UptimeRatioDetails> findUptimeRatioDetailsNew(String type, Long id, String from, String to, Pageable pageable);

	List<PerformanceRawData> getPerformanceRawData(long moldId, String day);

	List<PerformanceRawData> getPerformanceRawDataAfter(long moldId, String day, String time);

	List<PerformanceRawData> getPerformanceRawDataBefore(long moldId, String day, String time);

	List<StatisticsDaily> findAllGroupByYearAndMonthAndDayAndWeekAndMoldIdAndCi(String after, String before, String day, Long moldId, String ci, Pageable pageable);

	Integer getPartProjectProduced(Long partId, Long projectId);

	boolean checkHaveDataProductionQuantityData(List<Long> spIds, Frequent frequent);

	Double getPartWACTBefore(Long partId, String time);

	Double getPartWACTBetween(Long partId, String start, String end);
}
