package saleson.api.chart;

import static saleson.dto.MapQueryType.LOCATION;
import static saleson.dto.MapQueryType.PART;
import static saleson.dto.MapQueryType.TOOLING;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.dsh.dto.DshPosition;
import com.emoldino.api.common.resource.composite.dsh.service.DshService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheUtils;
import com.emoldino.framework.util.Closure;
import com.emoldino.framework.util.ClosureWrapper;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.ThreadUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import saleson.api.category.CategoryRepository;
import saleson.api.category.payload.CategoryParam;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.chart.payload.MapSearchPayload;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartService;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.config.Const;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.DashboardChartType;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.FrequentUsage;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.ToolingCondition;
import saleson.common.enumeration.mapper.CodeMapperType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.DashboardMapSearchResultDTO;
import saleson.dto.MapToolingDTO;
import saleson.dto.productionPatternAnalysis.ProductionPatternDTO;
import saleson.model.Category;
import saleson.model.Mold;
import saleson.model.MoldPart;
import saleson.model.Part;
import saleson.model.QCategory;
import saleson.model.QMold;
import saleson.model.data.CategoryDetailsData;
import saleson.model.data.CategorySummary;
import saleson.model.data.ChartData;
import saleson.model.data.CompanyChartData;
import saleson.model.data.ContinentStatisticData;
import saleson.model.data.DashboardChartData;
import saleson.model.data.DashboardChartDataOte;
import saleson.model.data.GoogleMapData;
import saleson.model.data.MapChartData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldCapacityReportData;
import saleson.model.data.MoldPartYearWeekOrMonth;
import saleson.model.data.QuickStats;
import saleson.model.data.dashboard.ImplementationStatusData;
import saleson.model.data.dashboard.otd.OTDData;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioData;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioDetails;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioTooling;
import saleson.service.data.service.DataService;

@RestController
@RequestMapping("/api/chart")
@Slf4j
public class ChartController {

	@Autowired
	private MoldRepository moldRepository;

	@Lazy
	@Autowired
	private MoldService moldService;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private StatisticsRepository statisticsRepository;

	@Autowired
	private PartService partService;

	@Autowired
	private ChartService chartService;
	@Autowired
	private DataService dataService;
	@Autowired
	private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

	@GetMapping("/part")
	public List<ChartData> getPartChart(ChartPayload payload) {
		// 1. Adjust and Verification

		if (ObjectUtils.isEmpty(payload.getPartId())) {
			return Collections.emptyList();
		}

		adjust(payload);

		// 2. Retrieve Charts
		Map<ChartDataType, List<ChartData>> dataByChartType = new LinkedHashMap<>();

		List<ChartDataType> chartDataTypes = payload.getChartDataType();

		// 2.1 Retrieve Quantity Chart
		// TODO Currently, Quantity ChartDataType Chart is necessary.
		// Because we use it for finding all related moldIds
		// Let's improve it be possible without Quantity ChartDataType later
		payload.setChartDataType(Arrays.asList(ChartDataType.QUANTITY));
		List<ChartData> qtyCharts = moldRepository.findChartData(payload);

		dataByChartType.put(ChartDataType.QUANTITY, qtyCharts);

		// 2.2 Retrieve Maximum Capacity
		int maxCapacity = 0;
		if (chartDataTypes.contains(ChartDataType.MAXIMUM_CAPACITY)) {
			// 2.2.1 Get Mold IDs for manufacturing this part
			List<Long> moldIds;
			{
				String fromDate;
				String toDate;
				{
					String year = payload.getYear();
					String thisYear = DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyy, Zone.GMT);
					// If past year
					if (thisYear.compareTo(year) > 0) {
						fromDate = year + "0601";
						toDate = year + "1231";
					}
					// If this year
					else {
						String thisMonth = DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyyMM, Zone.GMT);
						if (thisMonth.compareTo(year + "06") > 0) {
							fromDate = thisYear + "01";
						} else {
							fromDate = (Integer.parseInt(thisYear) - 1) + "01";
						}
						toDate = DateUtils2.format(DateUtils2.getInstant(), DatePattern.yyyyMMdd, Zone.GMT);
					}
				}
				moldIds = moldService.getUsedMoldIds(payload.getPartId(), fromDate, toDate);
				// If empty
				if (moldIds.isEmpty()) {
					ProductivitySearchPayload psPayload = new ProductivitySearchPayload();
					psPayload.setPartId(payload.getPartId());
					moldIds = moldService.findMoldIdsFromProductivitySearchPayload(psPayload);
				}
			}

			// 2.2.2 Add all capacity of these molds
			{
				Frequent frequent = Frequent.DAILY;
				if (DateViewType.WEEK.equals(payload.getDateViewType())) {
					frequent = Frequent.WEEKLY;
				} else if (DateViewType.MONTH.equals(payload.getDateViewType())) {
					frequent = Frequent.MONTHLY;
				}

				boolean isDyson = "dyson".equals(ConfigUtils.getServerName());
				List<MoldCapacityReportData> list = statisticsRepository.findMaxCapacityOfTooling(new DashboardFilterPayload(), moldIds, frequent, isDyson);

				for (MoldCapacityReportData item : list) {
					maxCapacity += (item.getData() == null ? 0 : item.getData());
				}
			}
		}

		// 2.3 Retrieve CycleTime Chart
		if (chartDataTypes.contains(ChartDataType.CYCLE_TIME) || chartDataTypes.contains(ChartDataType.APPROVED_CYCLE_TIME)) {
			// 2.2.1 Get All MoldIds those are related to this quantity chart
			Set<Long> moldIds = new HashSet<>();
			for (ChartData item : qtyCharts) {
				if (ObjectUtils.isEmpty(item.getMoldShots())) {
					continue;
				}
				item.getMoldShots().forEach((moldShotData) -> {
					if (moldIds.contains(moldShotData.getMoldId())) {
						return;
					}
					moldIds.add(moldShotData.getMoldId());
				});
			}

			// 2.2.2 Retrieve CycleTime by each Mold and Summarize those by XAxis Title
			Map<String, CycleTimeSummary> summaryByXAxisTitle = new HashMap<>();
			for (Long moldId : moldIds) {
				// 2.2.2.1 Retrieve CycleTime by Mold
				payload.setChartDataType(Arrays.asList(ChartDataType.CYCLE_TIME));
				payload.setMoldId(moldId);
				Mold mold = moldRepository.findById(moldId).orElse(null);
				if (mold == null) {
					continue;
				}
				List<ChartData> dataList = moldService.findCycleTimeData(payload);
				for (ChartData data : dataList) {
					if (data.getTitle() == null || data.getTitle().length() == 0) {
						continue;
					}
					CycleTimeSummary sum = CacheUtils.get(summaryByXAxisTitle, data.getTitle(), () -> new CycleTimeSummary(data));
//					Map<Double, Double> ctQtys = sum.getCtQtys();
//					double qtys = 0;
//					if (ctQtys.containsKey(data.getCycleTime())) {
//						qtys = ctQtys.get(data.getCycleTime());
//					}
					double qty = new Double(data.getData()) * data.getAvgCavities();
					double contractedCt = data.getContractedCycleTime() != null && data.getContractedCycleTime() > 0 ? data.getContractedCycleTime() : data.getCycleTime();
					double totalCtLimit1 = mold.getCycleTimeLimit1() != null && mold.getCycleTimeLimit1() > 0 ? mold.getCycleTimeLimit1() : data.getCycleTime();
					double totalCtLimit2 = mold.getCycleTimeLimit2() != null && mold.getCycleTimeLimit2() > 0 ? mold.getCycleTimeLimit2() : data.getCycleTime();
					sum.totalQty += qty;
					sum.totalCt += data.getCycleTime() * qty;
					sum.totalContractedCt += contractedCt * qty;
					sum.totalCtLimit1 += totalCtLimit1 * qty;
					if (mold.getCycleTimeLimit1Unit() != null) {
						sum.limit1Unit = mold.getCycleTimeLimit1Unit();
					}
					sum.totalCtLimit2 += totalCtLimit2 * qty;
					if (mold.getCycleTimeLimit2Unit() != null) {
						sum.limit2Unit = mold.getCycleTimeLimit2Unit();
					}
//					qtys += qty;
//					ctQtys.put(data.getCycleTime(), qtys);
				}
			}

			// 2.2.3 Combine CycleTime
			if (!summaryByXAxisTitle.isEmpty()) {
				List<ChartData> list = new ArrayList<>();
				summaryByXAxisTitle.forEach((title, sum) -> {
					double totalQty = sum.totalQty;
					double ct = totalQty == 0 || sum.totalCt == 0 ? 0 : sum.totalCt / totalQty;
					double contractedCt = totalQty == 0 || sum.totalContractedCt == 0 ? 0 : sum.totalContractedCt / totalQty;
					Double limit1 = totalQty == 0 || sum.totalCtLimit1 == 0 ? 0 : sum.totalCtLimit1 / totalQty;
					Double limit2 = totalQty == 0 || sum.totalCtLimit2 == 0 ? 0 : sum.totalCtLimit2 / totalQty;

					ChartData data = sum.getData();
					data.setCycleTime(ct);
					data.setContractedCycleTime(contractedCt);
					ChartData.populate(list, contractedCt, limit1.intValue(), sum.limit1Unit, limit2.intValue(), sum.limit2Unit, null);
					list.add(data);
				});
				dataByChartType.put(ChartDataType.CYCLE_TIME, list);
			}

		}

		List<ChartData> list = toList(dataByChartType);

		// 3. Response
		if (maxCapacity > 0) {
			final int mc = maxCapacity;
			list.forEach(item -> item.setMaxCapacity(mc));
		}
		return list;
	}

	@Data
	private static class CycleTimeSummary {
		public CycleTimeSummary(ChartData data) {
			this.data = data;
		}

		double totalQty;
		double totalCt;
		double totalContractedCt;
		double totalCtLimit1;
		OutsideUnit limit1Unit;
		double totalCtLimit2;
		OutsideUnit limit2Unit;
//		Map<Double, Double> ctQtys = new HashMap<>();
		ChartData data;
	}

	@GetMapping("/molds")
	public List<ChartData> getMoldChart(ChartPayload payload) {
		// 1. Adjust and Verification
		if (ObjectUtils.isEmpty(payload.getMoldId())) {
			return Collections.emptyList();
		}

		adjust(payload);
//		List<Property> changed = adjust(payload);

		Closure<List<ChartData>> closure = () -> {
			// 2. Retrieve Charts
			Map<ChartDataType, List<ChartData>> dataByChartType = new LinkedHashMap<>();

			List<ChartDataType> chartDataTypes = payload.getChartDataType();

			List<ChartData> list = Collections.emptyList();

			// 2.1 Retrieve Quantity Chart
			if (chartDataTypes.contains(ChartDataType.QUANTITY)) {
				payload.setChartDataType(Arrays.asList(ChartDataType.QUANTITY));
				list = moldRepository.findChartData(payload);
				dataByChartType.put(ChartDataType.QUANTITY, list);
			}

			// 2.2 Retrieve Uptime Chart
			if (chartDataTypes.contains(ChartDataType.UPTIME)) {
				payload.setChartDataType(Arrays.asList(ChartDataType.UPTIME));
				list = moldRepository.findChartData(payload);
				dataByChartType.put(ChartDataType.UPTIME, list);
			}

			// 2.3 Retrieve CycleTime Related Chart
			// 2.3.1 Retrieve CycleTime Chart
			if (chartDataTypes.contains(ChartDataType.CYCLE_TIME) || chartDataTypes.contains(ChartDataType.APPROVED_CYCLE_TIME)) {
				payload.setChartDataType(Arrays.asList(ChartDataType.CYCLE_TIME));
				list = moldService.findCycleTimeData(payload);
//				//todo mock data resin code change
//				list.stream().filter(i -> i.getData() != null && i.getData() > 0).forEach(i -> i.setResinCodeChangeData(Arrays.asList(new ResinCodeChangeData("old material", "new material", 10D, 20D))));
				dataByChartType.put(ChartDataType.CYCLE_TIME, list);
//				list.stream().filter(d -> d.getMoldCode() != null).forEach(d -> System.out.println(d));
			}
			// 2.3.2 Retrieve CycleTime Analysis Chart
			else if (chartDataTypes.contains(ChartDataType.CYCLE_TIME_ANALYSIS)) {
				payload.setChartDataType(Arrays.asList(ChartDataType.CYCLE_TIME_ANALYSIS));
				list = moldService.findCycleTimeData(payload);
				dataByChartType.put(ChartDataType.CYCLE_TIME, list);
			}

			// 2.4 Retrieve Other Chart
			if (dataByChartType.isEmpty()) {
				list = moldRepository.findChartData(payload);
			} else {
				list = toList(dataByChartType);
			}

//			if (!payload.isSkipResinCodeChangeData()) {
//				moldService.fillResinCodeChangeData(list, payload);
//			}

			// 3. Response
			return list;
		};

		return closure.execute();
	}

	@NoArgsConstructor
	public static class ChartDataListOut extends ListOut<ChartData> {
		public ChartDataListOut(List<ChartData> content) {
			super(content);
		}
	}

	private static List<Property> adjust(ChartPayload payload) {
		List<Property> changed = new ArrayList<>();

		// Adjust ChartDataType
		if (ObjectUtils.isEmpty(payload.getChartDataType())) {
			payload.setChartDataType(Arrays.asList(ChartDataType.QUANTITY));
		}

//		if (DateViewType.YEAR.equals(payload.getDateViewType()) || DateViewType.MONTH.equals(payload.getDateViewType()) || DateViewType.WEEK.equals(payload.getDateViewType())
//				|| DateViewType.DAY.equals(payload.getDateViewType())) {
//			payload.setMonth(null);
//			payload.setDate(null);
//		}

		// Adjust Year
//		if (ObjectUtils.isEmpty(payload.getYear())) {
//			payload.setYear("" + LocalDate.now().getYear());
//		}

		// Adjust Month
		if (!ObjectUtils.isEmpty(payload.getYear()) && !ObjectUtils.isEmpty(payload.getMonth()) && payload.getMonth().length() <= 2) {
			StringBuilder buf = new StringBuilder();
			buf.append(payload.getYear());
			if (payload.getMonth().length() == 1) {
				buf.append("0");
			}
			buf.append(payload.getMonth());
			payload.setMonth(buf.toString());
		}

		// Adjust DateViewType
		if (payload.getDateViewType() == null) {
			payload.setDateViewType(DateViewType.WEEK);
		}

		return changed;
	}

	private static List<ChartData> toList(Map<ChartDataType, List<ChartData>> dataByChartType) {
		// If No Data
		if (ObjectUtils.isEmpty(dataByChartType)) {
			return Collections.emptyList();
		}
		// If Data by only 1 ChartDataType
		else if (dataByChartType.size() == 1) {
			return dataByChartType.values().iterator().next();
		}

		// If Data by multiple ChartDataTypes
		Map<String, ChartData> sortedMap = new TreeMap<>();
		dataByChartType.forEach((chartDataType, list) -> {
			list.forEach((data) -> {
				// No Title (May not happen)
				if (ObjectUtils.isEmpty(data.getTitle())) {
					return;
				}
				// No data with same title yet
				if (!sortedMap.containsKey(data.getTitle())) {
					sortedMap.put(data.getTitle(), data);
					return;
				}

				ChartData d = sortedMap.get(data.getTitle());
				// Set Quantity & Uptime
				if (ChartDataType.QUANTITY.equals(chartDataType)) {
					d.setData(data.getData());
					d.setDataPercent(data.getDataPercent());
					d.setTrend(data.getTrend());
					d.setMoldCount(data.getMoldCount());
					d.setMoldShots(data.getMoldShots());
					d.setLastShot(data.getLastShot());
					d.setMoldCode(data.getMoldCode());
				}
				// Set Uptime & Quantity
				else if (ChartDataType.UPTIME.equals(chartDataType)) {
					d.setUptime(data.getUptime());
					d.setApprovedUptime(data.getApprovedUptime());
				}
				// Set CycleTime
				else if (ChartDataType.CYCLE_TIME.equals(chartDataType)) {
					d.setCycleTime(data.getCycleTime());
					d.setMaxCycleTime(data.getMaxCycleTime());
					d.setMinCycleTime(data.getMinCycleTime());
					d.setCycleTimeMinusL1(data.getCycleTimeMinusL1());
					d.setCycleTimeMinusL2(data.getCycleTimeMinusL2());
					d.setCycleTimePlusL1(data.getCycleTimePlusL1());
					d.setCycleTimePlusL2(data.getCycleTimePlusL2());
					d.setCycleTimeWithin(data.getCycleTimeWithin());
					d.setCycleTimeL1(data.getCycleTimeL1());
					d.setCycleTimeL2(data.getCycleTimeL2());
					d.setApprovedCycleTime(data.getApprovedCycleTime());
					d.setContractedCycleTime(data.getContractedCycleTime());
					d.setUlct(data.getUlct());
					d.setMfct(data.getMfct());
					d.setLlct(data.getLlct());
					d.setAvgCavities(data.getAvgCavities());
//					if (d.getData() != null && d.getData() > 0) {
//						d.setResinCodeChangeData(Arrays.asList(new ResinCodeChangeData("old material", "new material", 10D, 20D)));
//					}
				}
			});
		});
		return sortedMap.isEmpty() ? Collections.emptyList() : sortedMap.values().stream().collect(Collectors.toList());
	}

	@GetMapping("/get-part-changes")
	public ApiResponse getPartChanges(ChartPayload payload) {
		return moldService.getPartsChangeHistory(payload.getMoldId(), payload.getStartDate(), payload.getEndDate());
	}

	@GetMapping("hour-details")
	public ResponseEntity<?> getHourStatisticsDetails(ChartPayload payload) {
		List<ChartData> dataList = moldService.findHourStatisticsDetails(payload);
		return ResponseEntity.ok(dataList);
	}

	@GetMapping("/dashboard-data-old")
	public ResponseEntity<Map<String, Object>> getDashboardDataOld(DashboardFilterPayload payload) throws ExecutionException, InterruptedException {
//		moldService.loadTreeCompanyForPayLoad(payload);
		Map<String, Object> data = new HashMap<>();
		data.put("quick-stats", getQuickStats(payload));
		data.put("graphs", getDashboard(payload));
		data.put("category-summary", getDashboardCategorySummary(payload));
		data.put("location-summary", getDashboardLocationSummary());
		data.put("list-search", getListSearch());
		data.put("graph-position", getGraphPosition());
		return ResponseEntity.ok(data);
	}

	@GetMapping("/dashboard-data")
	public ResponseEntity<Map<String, Object>> getDashboardData(DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		List<ClosureWrapper<Object>> tasks = new ArrayList<>(6);
		tasks.add(new ClosureWrapper<Object>("quick-stats", () -> getQuickStats(payload)));
		// tasks.add(new ClosureWrapper<>("graphs", () -> getDashboard(payload)));
		tasks.add(new ClosureWrapper<>("category-summary", () -> getDashboardCategorySummary(payload)));
		tasks.add(new ClosureWrapper<>("location-summary", () -> getDashboardLocationSummary()));
		tasks.add(new ClosureWrapper<>("list-search", () -> getListSearchFiltered()));
		tasks.add(new ClosureWrapper<>("graph-position", () -> getGraphPosition()));
		Map<String, Object> data = JobUtils.runConcurrently("ChartController.getDashboardData", new JobOptions().setAccessSummaryLogEnabled(true), tasks);
		return ResponseEntity.ok(data);
	}

	@GetMapping("/quick-stats")
	public QuickStats getQuickStats(DashboardFilterPayload payload) {
//		if(payload.getAccessCompanyIds().isEmpty())
//			moldService.loadTreeCompanyForPayLoad(payload);
		return moldService.getQuickStats(payload);
	}

	@GetMapping("/dashboard-old")
	public HashMap<String, List<DashboardChartData>> getDashboardOld(DashboardFilterPayload payload) {
		HashMap<String, List<DashboardChartData>> data = new HashMap<>();
		data.put("overview", moldRepository.findMoldOverview(payload));
		data.put("preventive", moldService.findPreventiveMaintenance(payload));
		data.put("cycleTimeStatus", moldService.findCycleTimeStatus(payload));
		data.put("efficiencyStatus", moldService.findEfficiencyStatus(payload));
		data.put("capacityUtilization", moldService.findCapacityUtilization(payload));
		data.put("utilizationRate", moldService.findUtilizationRate(payload));
		data.put("downtime", moldService.findMoldDowntime(payload));
		data.put("oee", moldService.findOee(payload));
//		data.put("production", moldService.findProductionPart(payload, null, Frequent.WEEKLY));
		return data;
	}

	@GetMapping("/dashboard")
	@Deprecated
	public Map<String, List<DashboardChartData>> getDashboard(DashboardFilterPayload payload) {
//		if(payload.getAccessCompanyIds().isEmpty())
//			moldService.loadTreeCompanyForPayLoad(payload);
		List<ClosureWrapper<List<DashboardChartData>>> tasks = new ArrayList<>(6);
		tasks.add(new ClosureWrapper<>("overview", () -> moldRepository.findMoldOverview(payload)));
		tasks.add(new ClosureWrapper<>("preventive", () -> moldService.findPreventiveMaintenance(payload)));
		tasks.add(new ClosureWrapper<>("cycleTimeStatus", () -> moldService.findCycleTimeStatus(payload)));
		tasks.add(new ClosureWrapper<>("efficiencyStatus", () -> moldService.findEfficiencyStatus(payload)));
		tasks.add(new ClosureWrapper<>("capacityUtilization", () -> moldService.findCapacityUtilization(payload)));
		tasks.add(new ClosureWrapper<>("utilizationRate", () -> moldService.findUtilizationRate(payload)));
		tasks.add(new ClosureWrapper<>("downtime", () -> moldService.findMoldDowntime(payload)));
		tasks.add(new ClosureWrapper<>("oee", () -> moldService.findOee(payload)));
//		tasks.add(new ClosureWrapper<>("production", () -> moldService.findProductionPart(payload, null, Frequent.WEEKLY)));
		Map<String, List<DashboardChartData>> data = JobUtils.runConcurrently("ChartController.getDashboard", new JobOptions().setAccessSummaryLogEnabled(true), tasks);
		return data;
	}

	@GetMapping("/tooling-overview")
	public List<DashboardChartData> findMoldOverview(DashboardFilterPayload payload) {
		return moldRepository.findMoldOverview(payload);
	}

	@GetMapping("/preventive")
	public List<DashboardChartData> findPreventiveMaintenance(DashboardFilterPayload payload) {
		return moldService.findPreventiveMaintenance(payload);
	}

	@GetMapping("/cycle-time-status")
	public List<DashboardChartData> findCycleTimeStatus(DashboardFilterPayload payload) {
		return moldService.findCycleTimeStatus(payload);
	}

	@GetMapping("/utilization-rate")
	public List<DashboardChartData> findUtilizationRate(DashboardFilterPayload payload) {
		return moldService.findUtilizationRate(payload);
	}

	@GetMapping("/efficiency-status")
	public List<DashboardChartData> findEfficiencyStatus(DashboardFilterPayload payload) {
		return moldService.findEfficiencyStatus(payload);
	}

	@GetMapping("/graph-capacity-utilization")
	public List<DashboardChartData> graphCapacityUtilization(DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		return moldService.findCapacityUtilization(payload);
	}

	@GetMapping("/graph-production")
	public Map<Long, List<MoldPartYearWeekOrMonth>> graphProduction(//
			DashboardFilterPayload payload, //
			@RequestParam(required = false) String type, //
			@RequestParam(required = false) List<Long> ids, //
			@RequestParam(required = false) Frequent frequent) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		return moldService.findProductionPart(payload, type, ids, frequent);
	}

	@GetMapping("/graph-downtime")
	public List<DashboardChartData> graphDowntime(DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		return moldService.findMoldDowntime(payload);
	}

	@GetMapping("/graph-oee")
	public List<DashboardChartData> graphOee(DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);

		return moldService.findOee(payload);
	}

	@GetMapping("/graph-ote")
	public List<DashboardChartDataOte> graphOte(DashboardFilterPayload payload, @RequestParam DateViewType dateViewType) {
		return moldService.findOte(payload, dateViewType);
	}

	@GetMapping("/graph-implementation-status")
	public ImplementationStatusData graphImplementationStatus(DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		return chartService.findImplementationStatus(payload);
	}

	@GetMapping("/graph-cycle-time")
	public List<ChartData> graphCycleTime(@RequestParam Long moldId, @RequestParam DateViewType dateViewType, @RequestParam(required = false) Long limit) {
		return moldService.findCycleTimeDashboard(moldId, dateViewType, limit);
	}

	@GetMapping("/graph-otd")
	public OTDData graphOtd(@RequestParam Long partId, @RequestParam String week, Pageable pageable) {
		return partService.findOtd(partId, week, pageable);
	}

	@GetMapping("/graph-otd-details")
	public List<ChartData> getToolingProducedPart(@RequestParam Long partId, @RequestParam String week, @RequestParam Long companyId) {
		return partService.findToolingProducedPart(partId, week, companyId);
	}

	@GetMapping("/graph-uptime-ratio")
	public UptimeRatioData getUptimeRatioData(//
			@RequestParam(required = false) String type, //
			@RequestParam(required = false) Long id, //
			@RequestParam(required = false) String from, //
			@RequestParam(required = false) String to, //
			Pageable pageable) {
		return moldService.findUptimeRatioData(type, id, from, to, pageable);
	}

	@GetMapping("/graph-uptime-ratio/details")
	public Page<UptimeRatioDetails> getUptimeRatioDetails(//
			@RequestParam(required = false) String type, //
			@RequestParam(required = false) Long id, //
			@RequestParam(required = false) String from, //
			@RequestParam(required = false) String to, //
			Pageable pageable) {
		return moldService.findUptimeRatioDetails(type, id, from, to, pageable);
	}

	@GetMapping("/graph-uptime-ratio/toolings")
	public Page<UptimeRatioTooling> getUptimeRatioTooling(//
			@RequestParam(required = false) FrequentUsage frequent, //
			@RequestParam(required = false) String type, //
			@RequestParam(required = false) Long id, //
			@RequestParam(required = false) String from, //
			@RequestParam(required = false) String to, //
			Pageable pageable) {
		return moldService.findUptimeRatioTooling(frequent, type, id, from, to, pageable);
	}

	@GetMapping("/dashboard-category-summary")
	public Map<String, Object> getDashboardCategorySummary(DashboardFilterPayload payload) {
//		if(payload.getAccessCompanyIds().isEmpty())
//			moldService.loadTreeCompanyForPayLoad(payload);

		CategoryParam param = new CategoryParam();
		param.setEnabled(true);
		param.setStatus("1");
		BooleanBuilder builder = new BooleanBuilder(param.getPredicate()).and(dashboardGeneralFilterUtils.getCategoryFilter(QCategory.category));
		List<Category> categories = categoryRepository.findAll(builder, PageRequest.of(0, 1000)).getContent();
//		List<Category> categories = categoryRepository.findAllByLevel(1);

		List<Long> partIdsFiltered = partService.findAllMiniDataByGeneralFilter()//
				.stream()//
				.map(MiniComponentData::getId)//
				.collect(Collectors.toList());

		Map<String, Object> chartData = new HashMap<>();

		List<CategorySummary> summaries = new ArrayList<>();
		// mold
		long totalMolds = 0;
		long installed = 0;
		long available = 0;
		long etc = 0;

		for (Category category : categories) {
			if (!category.isEnabled()) {
				continue;
			}

			CategorySummary categorySummary = new CategorySummary(category.getId(), category.getName(), 0L, 0L, 0L);

//			long childCount = 0;
			long partCount = 0;
			long moldCount = 0;

			if (category.getChildren() == null) {
				continue;
			}

			categorySummary.setProjectCount((long) category.getChildren().size());

			for (Category project : category.getChildren()) {
				if (!project.isEnabled()) {
					continue;
				}

				// List<Part> parts = partRepository.findAllByCategoryId(project.getId());

				MoldPayload moldPayload = new MoldPayload();
				moldPayload.setProjectId(project.getId());
				List<MoldPart> moldParts = moldService.findMoldPartAll(moldPayload, payload);

				if (moldParts == null || moldParts.isEmpty()) {
					continue;
				}

				Set<Mold> molds = new HashSet<>();
				Set<Part> parts = new HashSet<>();
				Set<Mold> installedMolds = new HashSet<>();
				Set<Mold> availableMolds = new HashSet<>();
				Set<Mold> etcMolds = new HashSet<>();

				for (MoldPart moldPart : moldParts) {
					Mold mold = ThreadUtils.getProp("ChartController.getDashboardCategorySummary.mold" + moldPart.getMoldId(), () -> moldPart.getMold());
					if (mold == null) {
						continue;
					}
					Part part = ThreadUtils.getProp("ChartController.getDashboardCategorySummary.part" + moldPart.getPartId(), () -> moldPart.getPart());
					if (part == null) {
						continue;
					}
					if (partIdsFiltered.contains(part.getId())) {
						parts.add(part);
					}

					molds.add(mold);

					if (mold.getEquipmentStatus() == EquipmentStatus.INSTALLED) {
						installedMolds.add(mold);
					} else if (mold.getEquipmentStatus() == EquipmentStatus.AVAILABLE) {
						availableMolds.add(mold);
					} else {
						etcMolds.add(mold);
					}

				}

				moldCount += molds.size();
				partCount += parts.size();

				installed = installed + installedMolds.size();
				available = available + availableMolds.size();
				etc = etc + etcMolds.size();
				totalMolds = totalMolds + molds.size();

				// M01
//				for (Part part : parts) {
//					if (part.getMolds() == null) {
//						continue;
//					}
//
//					partCount++;
//					moldCount += part.getMolds().size();
//
//					for (Mold mold : part.getMolds()) {
//						if (mold.getEquipmentStatus() == EquipmentStatus.INSTALLED) {
//							installed++;
//						} else if (mold.getEquipmentStatus() == EquipmentStatus.AVAILABLE) {
//							available++;
//						} else {
//							etc++;
//						}
//
//						totalMolds++;
//					}
//				}

			}

			categorySummary.setPartCount((long) partCount);
			categorySummary.setMoldCount((long) moldCount);
			summaries.add(categorySummary);
		}

		if (!SecurityUtils.isAdmin()) {
			summaries.removeIf(s -> s.getMoldCount() == 0);
		}

		chartData.put("totalMolds", totalMolds);
		chartData.put("installed", installed);
		chartData.put("available", available);
		chartData.put("etc", etc);
		chartData.put("categorySummaries", summaries);

		return chartData;

//		List<CategorySummary> categorySummaries = new ArrayList<>();
//		if (SecurityUtils.isAdmin()) {
//			categorySummaries = moldRepository.findCategorySummaryAll();
//		} else if (SecurityUtils.isSupplier()) {
//			categorySummaries = moldRepository.findCategorySummaryAllByCompanyId(SecurityUtils.getCompanyId());
//		} else { // Access Group 권한으로 체크 (ROLE)
//			categorySummaries = moldRepository.findCategorySummaryAll(); // 임시
//		}
//		return categorySummaries;
	}

	@GetMapping("/category-summary/details")
	public CategoryDetailsData categoryDetails(@RequestParam Long categoryId, DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		return chartService.getCategoryDetails(categoryId, payload);
	}

	@GetMapping("/dashboard-location-summary")
	public List<ContinentStatisticData> getDashboardLocationSummary() {
		return moldService.getContinentStatisticData();
	}

	@GetMapping("/list-search")
	public Map<String, List<MiniComponentData>> getListSearch() {
		return chartService.getListSearch(true);
	}

	@GetMapping("/list-search-filtered")
	public Map<String, List<MiniComponentData>> getListSearchFiltered() {
		return chartService.getListSearch(false);

	}

	/**
	 * 맵 차트 데이터 조회 .
	 * @param payload
	 * @return
	 */
	@GetMapping("/dashboard-map-chart-data")
	public List<GoogleMapData.MapData> dashboardMapChartData(MoldPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		List<MapChartData> mapData = moldRepository.getMapData(payload);
		return moldService.getMapDataList(mapData);
	}

	@GetMapping("/dashboard-map-continent")
	public List<GoogleMapData.MapData> dashboardMapContinent(//
			@RequestParam(required = false) String countryCode, //
			DashboardFilterPayload payload) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		List<MapChartData> mapData = moldRepository.getMapDataByDashboardFilter(countryCode, payload);
		return moldService.getMapDataList(mapData);
	}

	@GetMapping("/dashboard-map-search")
	public DashboardMapSearchResultDTO dashboardMapSearch(MapSearchPayload payload) {
		MapToolingDTO toolingSearchResult = StringUtils.isEmpty(payload.getQuery()) ? null : //
				moldRepository.getAllToolingMapData(payload.getQuery().toUpperCase());
		log.info("listMatchingTooling {}", toolingSearchResult);
		log.info("payload.getQuery() {}", payload.getQuery());

		if (toolingSearchResult != null) {
			List<MapChartData> mapChartDataList = moldRepository.getMapDataSearchByType(payload, TOOLING);
			return new DashboardMapSearchResultDTO(toolingSearchResult, moldService.getMapDataList(mapChartDataList), TOOLING);
		} else {
			List<MapChartData> mapChartDataList = moldRepository.getMapDataSearchByType(payload, LOCATION);
			if (CollectionUtils.isNotEmpty(mapChartDataList)) {
				return new DashboardMapSearchResultDTO(toolingSearchResult, moldService.getMapDataList(mapChartDataList), LOCATION);
			} else {
				mapChartDataList = moldRepository.getMapDataSearchByType(payload, PART);
				return new DashboardMapSearchResultDTO(toolingSearchResult, moldService.getMapDataList(mapChartDataList), PART);
			}
		}
	}

	@GetMapping("/company-chart/{companyId}")
	public HashMap<String, Object> companyChart(@PathVariable("companyId") Long companyId) {
		HashMap<String, Object> data = new HashMap<>();

		// 1. status
		CompanyPayload payload = CompanyPayload.builder().id(companyId).chartType("status").build();
		List<CompanyChartData> chartDataList = moldRepository.getCompanyDetailsChartData(payload);
		List<CompanyChartData> chartData = mergeEnumData(chartDataList, EquipmentStatus.class);

		data.put("status", chartData);

		// 2. condition
		payload = CompanyPayload.builder().id(companyId).chartType("condition").build();
		chartDataList = moldRepository.getCompanyDetailsChartData(payload);
		chartData = mergeEnumData(chartDataList, ToolingCondition.class);

		data.put("condition", chartData);

		// 3. Utilization
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(QMold.mold.companyId.eq(companyId)//
				.and(QMold.mold.deleted.isNull().or(QMold.mold.deleted.isFalse())));
		Iterable<Mold> molds = moldRepository.findAll(predicate);

		final Comparator<Mold> comp = (m1, m2) -> Double.compare(m1.getUtilizationRate(), m2.getUtilizationRate());

		Double maxUtilizationRate = StreamSupport//
				.stream(molds.spliterator(), false)//
				.max(comp).map(m -> m.getUtilizationRate())//
				.get();

		int maxRate = maxUtilizationRate == null ? 100 : maxUtilizationRate.intValue();

		if (maxRate < 100) {
			maxRate = 100;
		}

		List<CompanyChartData> utilization = new ArrayList<>();
		for (int i = 0; i <= maxRate / 10; i++) {
			int start = i * 10;
			int end = (i + 1) * 10;
			long moldCount = StreamSupport//
					.stream(molds.spliterator(), false)//
					.filter(m -> m.getUtilizationRate() > start && m.getUtilizationRate() < end)//
					.count();

			utilization.add(new CompanyChartData(start + "%", moldCount));
		}
		data.put("utilization", utilization);

		return data;
	}

	@PostMapping("/graph/update-index")
	public ResponseEntity updateIndex(//
			@RequestBody List<DshPosition> list, //
			@RequestParam(required = false) Boolean isDismiss) {
		BeanUtils.get(DshService.class).savePosition(list);
		return ResponseEntity.ok(list);
	}

	@GetMapping("/graph/position")
	public List<DshPosition> getGraphPosition() {
		return BeanUtils.get(DshService.class).getPosition();
	}

//	@PostMapping("/graph/update-status")
//    public ResponseEntity enableGraph(@RequestBody GraphIndexData graphIndexData){
//		if(graphIndexData.getType() == null) return ResponseEntity.ok("No graph type detected.");
//        return ResponseEntity.ok(userContentService.enableGraph(SecurityUtils.getUserId(), graphIndexData.getType()));
//    }

	/**
	 * DB 데이터가 없는 항목은 enum 값 기준을 0인 데이터를 목록으로 추가한다.
	 * 예) DB data가 toolingCondition 기준으로  Good(2) 만 있으면
	 *     -> Good(2), Fair(0), Poor(0)으로 데이터를 만듦(Enum 데이터 기준)
	 * @param statusDataList
	 * @param e
	 * @return
	 */
	private List<CompanyChartData> mergeEnumData(List<CompanyChartData> statusDataList, Class<? extends CodeMapperType> e) {
		return Stream//
				.of(e.getEnumConstants())//
				.filter(enumConst -> enumConst.isEnabled())//
				.map(enumConst -> statusDataList.stream()//
						.filter(s -> s.getTitle().equals(enumConst.getTitle()))//
						.findFirst()//
						.orElse(new CompanyChartData(enumConst.getTitle(), 0L))//
				)//
				.collect(Collectors.toList());
	}

	@GetMapping("/mold-capacity")
	public Map<Long, List<MoldCapacityReportData>> moldCapacity(//
			DashboardFilterPayload payload, //
//			@RequestParam(required = false) String type, //
			@RequestParam(required = false) List<Long> ids, //
			@RequestParam(required = false) Frequent frequent) {
//		moldService.loadTreeCompanyForPayLoad(payload);
		return moldService.findMoldMaxCapacity(payload, ids, frequent);
	}

	@GetMapping("/production-pattern-analysis")
	public List<ProductionPatternDTO> productionPatternAnalysis(//
			@RequestParam Long moldId, //
			@RequestParam String week) {
		List<ProductionPatternDTO> res = chartService.productionPatternAnalysis(moldId, week);
		return res;
	}

	@GetMapping("/mold-acceleration")
	public ResponseEntity getMoldAccelerationChartData(ChartPayload payload) {
		if (payload.getMoldId() == null) {
			return ResponseEntity.ok(ApiResponse.success(Const.SUCCESS, new ArrayList<>()));
		}
		if (StringUtils.isEmpty(payload.getDate())) {
			payload.setDate(DateUtils.getDate(Instant.now(), DateUtils.yyyyMMddHH));
		}
		return ResponseEntity.ok(ApiResponse.success(Const.SUCCESS, moldService.getAccelerationData(payload)));
	}

	@GetMapping("acceleration-time-range")
	public ResponseEntity<?> getAccelerationDataTimeRange(@RequestParam(required = false) String counterCode) {
		return new ResponseEntity<>(dataService.getDataTimeRange(counterCode), HttpStatus.OK);
	}

	@GetMapping("/tabbed-dashboard-data")
	public ApiResponse getTabbedDashboardData(TabbedOverviewGeneralFilterPayload payload) {
		try {
			List<ClosureWrapper<Object>> tasks = new ArrayList<>(10);
			tasks.add(new ClosureWrapper<>(DashboardChartType.TOTAL_PARTS.toString(), () -> partService.getTotalPartTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.PRODUCTION_QUANTITY.toString(), () -> moldService.getPartProducedTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.TOTAL_COST.toString(), () -> moldService.getTotalCostTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.MAINTENANCE.toString(), () -> moldService.getMaintenanceTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.PRODUCTION_CAPACITY.toString(), () -> moldService.getProductionCapacityTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.TOTAL_TOOLING.toString(), () -> moldService.getTotalToolingTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.INACTIVE_TOOLING.toString(), () -> moldService.getInactiveToolingTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.DIGITALIZATION_RATE.toString(), () -> moldService.getDigitalizationRateTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.CT_DEVIATION.toString(), () -> moldService.getCycleTimeTabData(payload)));
			tasks.add(new ClosureWrapper<>(DashboardChartType.END_OF_LIFE_CYCLE.toString(), () -> moldService.getEndOfLifeCycleTabData(payload)));
			Map<String, Object> data = JobUtils.runConcurrently("ChartController.getTabbedDashboardData", new JobOptions().setAccessSummaryLogEnabled(true), tasks);
			return ApiResponse.success(CommonMessage.OK, data);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	@GetMapping("/tabbed-total-part-chart-data")
	public ApiResponse getTabbedTotalPart(TabbedOverviewGeneralFilterPayload payload, Pageable pageable) {
		return partService.getTotalPartChartData(payload, pageable);
	}

	@GetMapping("/tabbed-total-part-tab-data")
	public ApiResponse getTabbedTotalPartTabData(TabbedOverviewGeneralFilterPayload payload) {
		return ApiResponse.success(CommonMessage.OK, partService.getTotalPartTabData(payload));
	}

	@GetMapping(value = "/tabbed-total-tooling-chart-data")
	public ApiResponse getTabbedTotalTooling(TabbedOverviewGeneralFilterPayload payload) {
		return ApiResponse.success(CommonMessage.OK, moldService.getTotalToolingChartData(payload));
	}

	@GetMapping(value = "/tabbed-inactive-tooling-chart-data")
	public ApiResponse getTabbedInactiveTooling(TabbedOverviewGeneralFilterPayload payload) {
		return ApiResponse.success(CommonMessage.OK, moldService.getInactiveToolingChartData(payload));
	}

	@GetMapping("/tabbed-part-produced-chart-data")
	public Map<Long, List<MoldPartYearWeekOrMonth>> getTabbedPartProduced(TabbedOverviewGeneralFilterPayload payload) {
		return moldService.getPartProduced(payload);
	}

	@GetMapping("/tabbed-total-cost-chart-data")
	public ApiResponse getTabbedTotalCost(TabbedOverviewGeneralFilterPayload payload) {
		return moldService.getTotalCost(payload);
	}

	@GetMapping("/tabbed-maintenance-chart-data")
	public ApiResponse getTabbedMaintenance(TabbedOverviewGeneralFilterPayload payload) {
		return moldService.getMaintenanceMapData(payload);
	}

	@GetMapping(value = "/tabbed-digitalization-rate-chart-data")
	public ApiResponse getDigitalizationRate(TabbedOverviewGeneralFilterPayload payload) {
		return ApiResponse.success(CommonMessage.OK, moldService.getDigitalizationRateChartData(payload));
	}

	@GetMapping(value = "/tabbed-cycle-time-chart-data")
	public ApiResponse getCycleTimeChartData(TabbedOverviewGeneralFilterPayload payload) {
		return ApiResponse.success(CommonMessage.OK, moldService.getCycleTimeChartData(payload));
	}

	@GetMapping(value = "/tabbed-end-of-life-cycle-chart-data")
	public ApiResponse getEndOfLifeCycleChartData(TabbedOverviewGeneralFilterPayload payload) {
		return ApiResponse.success(CommonMessage.OK, moldService.getEndOfLifeCycleChartData(payload));
	}

	@GetMapping("/tabbed-production-capacity-chart-data")
	public Map<Long, List<MoldCapacityReportData>> getProductionCapacity(TabbedOverviewGeneralFilterPayload payload) {
		return moldService.findProductionCapacity(payload);
	}

}
