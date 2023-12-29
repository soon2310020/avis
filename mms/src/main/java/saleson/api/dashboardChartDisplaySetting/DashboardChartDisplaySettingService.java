package saleson.api.dashboardChartDisplaySetting;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.dashboardChartDisplaySetting.payload.DashboardChartDisplaySettingLite;
import saleson.api.location.LocationRepository;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.MachineService;
import saleson.api.machine.MachineStatisticsRepository;
import saleson.api.machine.payload.DetailOEE;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.part.PartRepository;
import saleson.api.part.PartService;
import saleson.api.statistics.StatisticsRepository;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.DashboardChartType;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.DashboardChartDisplaySetting;
import saleson.model.DashboardChartParamSetting;
import saleson.model.Machine;
import saleson.model.data.ChartData;
import saleson.model.data.ChartDataOte;
import saleson.model.data.LocationData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MoldCapacityReportData;
import saleson.model.data.PartStatisticsPartIds;

@Deprecated
@Service
public class DashboardChartDisplaySettingService {
	@Autowired
	private DashboardChartDisplaySettingRepository dashboardChartDisplaySettingRepository;

	@Autowired
	private DashboardChartParamSettingRepository dashboardChartParamSettingRepository;

	@Autowired
	private StatisticsRepository statisticsRepository;

	@Autowired
	private MoldRepository moldRepository;

	@Autowired
	private CompanyService companyService;

	@Autowired
	private MachineStatisticsRepository machineStatisticsRepository;

	@Autowired
	private MoldService moldService;

	@Autowired
	private PartService partService;

	@Autowired
	private PartRepository partRepository;

	@Autowired
	private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

	@Autowired
	private LocationRepository locationRepository;
	@Autowired
	private MachineService machineService;

	@Value("${customer.server.name}")
	private String serverName;

	@Transactional
	public ApiResponse save(DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite) {
		Long dashboardChartDisplaySettingId;
		DashboardChartDisplaySetting dashboardChartDisplaySetting = dashboardChartDisplaySettingRepository
				.findFirstByChartTypeAndUserId(dashboardChartDisplaySettingLite.getChartType(), SecurityUtils.getUserId());
		if (dashboardChartDisplaySetting != null) {
			dashboardChartDisplaySettingId = dashboardChartDisplaySetting.getId();
			dashboardChartParamSettingRepository.deleteAllByDashboardChartDisplaySettingId(dashboardChartDisplaySetting.getId());
		} else {
			dashboardChartDisplaySetting = new DashboardChartDisplaySetting();
			dashboardChartDisplaySetting.setChartType(dashboardChartDisplaySettingLite.getChartType());
			dashboardChartDisplaySetting.setUserId(SecurityUtils.getUserId());
			dashboardChartDisplaySettingRepository.save(dashboardChartDisplaySetting);
			dashboardChartDisplaySettingId = dashboardChartDisplaySetting.getId();
		}

		List<DashboardChartParamSetting> dashboardChartParamSettingList = dashboardChartDisplaySettingLite.getDashboardSettingData().entrySet().stream()
				.map(entry -> new DashboardChartParamSetting(entry, dashboardChartDisplaySettingId)).collect(Collectors.toList());

		dashboardChartParamSettingRepository.saveAll(dashboardChartParamSettingList);
		return ApiResponse.success(CommonMessage.OK);
	}

	public ApiResponse getDashboardChartDisplaySetting() {
		List<Future<DashboardChartDisplaySettingLite>> dashboardChartDisplaySettingLiteFutureList = Lists.newArrayList();
		List<DashboardChartDisplaySettingLite> dashboardChartDisplaySettingLiteList = Lists.newArrayList();
		List<DashboardChartDisplaySetting> dashboardChartDisplaySettingList = dashboardChartDisplaySettingRepository.findAllByUserId(SecurityUtils.getUserId());
		Map<DashboardChartType, DashboardChartDisplaySetting> dashboardChartTypeDashboardChartDisplaySettingMap = dashboardChartDisplaySettingList.stream()
				.collect(Collectors.toMap(DashboardChartDisplaySetting::getChartType, Function.identity(), (o, n) -> n));
		List<MiniComponentData> molds = moldService.findAllMiniDataByGeneralFilter();
		List<MiniComponentData> parts = partService.findAllMiniDataByGeneralFilter();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		executorService = new DelegatingSecurityContextExecutorService(executorService, SecurityContextHolder.getContext());
		for (DashboardChartType chartType : DashboardChartType.values()) {
			if (chartType.getDashboardChart()) {
				Future<DashboardChartDisplaySettingLite> dashboardChartDisplaySettingLiteFuture = executorService.submit(() -> {
					DashboardChartDisplaySetting dashboardChartDisplaySetting = dashboardChartTypeDashboardChartDisplaySettingMap.get(chartType);
					return getDashboardChartDisplaySettingByChartType(chartType, dashboardChartDisplaySetting, molds, parts);
				});

				dashboardChartDisplaySettingLiteFutureList.add(dashboardChartDisplaySettingLiteFuture);
			}
		}

		dashboardChartDisplaySettingLiteFutureList.forEach(dashboardChartDisplaySettingLiteFuture -> {
			try {
				dashboardChartDisplaySettingLiteList.add(dashboardChartDisplaySettingLiteFuture.get());
			} catch (InterruptedException interruptedException) {
				interruptedException.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		});
		executorService.shutdown();
		return ApiResponse.success(CommonMessage.OK, dashboardChartDisplaySettingLiteList);
	}

	private boolean checkSavedConfigMatchGeneralFilter(DashboardChartDisplaySetting dashboardChartDisplaySetting, List<MiniComponentData> molds, List<MiniComponentData> parts) {
		List<DashboardChartParamSetting> dashboardChartParamSettingList = dashboardChartParamSettingRepository
				.findAllByDashboardChartDisplaySettingId(dashboardChartDisplaySetting.getId());

		List<Long> partIdList = parts.stream().map(MiniComponentData::getId).collect(Collectors.toList());
		List<Long> moldIdList = molds.stream().map(MiniComponentData::getId).collect(Collectors.toList());
		List<Long> companyIdsFiltered = dashboardGeneralFilterUtils.getCompanyIds();
		switch (dashboardChartDisplaySetting.getChartType()) {
		case PRODUCTION_QUANTITY: {
			DashboardChartParamSetting type = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("type")).findAny().orElse(null);
			DashboardChartParamSetting ids = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("ids")).findAny().orElse(null);
			if (type == null || ids == null)
				return false;

			if (StringUtils.isBlank(ids.getParamValue()))
				return true;
			String[] stringIds = ids.getParamValue().split(",");
			if ("TOOLING".equals(type.getParamValue())) {
				for (String id : stringIds) {
					Long longId = Long.valueOf(id);
					if (moldIdList.contains(longId)) {
						return true;
					}
				}
				return false;
			} else {
				for (String id : stringIds) {
					Long longId = Long.valueOf(id);
					if (partIdList.contains(longId)) {
						return true;
					}
				}
				return false;
			}
		}

		case DATA_COMPLETION_RATE: {
			DashboardChartParamSetting companyId = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("companyId")).findAny().orElse(null);

			return companyId != null && (StringUtils.isBlank(companyId.getParamValue()) || companyIdsFiltered.contains(Long.valueOf(companyId.getParamValue())));
		}

		case OVERALL_EQUIPMENT_EFFECTIVENESS: {
			DashboardChartParamSetting companyId = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("companyId")).findAny().orElse(null);

			return companyId != null && companyIdsFiltered.contains(Long.valueOf(companyId.getParamValue()));
		}

		case ON_TIME_DELIVERY: {
			DashboardChartParamSetting partId = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("partId")).findAny().orElse(null);
			DashboardChartParamSetting week = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("week")).findAny().orElse(null);
			return week != null && StringUtils.isNotBlank(week.getParamValue()) && partId != null && partIdList.contains(Long.valueOf(partId.getParamValue()));
		}

		case CYCLE_TIME: {
			DashboardChartParamSetting moldId = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("moldId")).findAny().orElse(null);
			DashboardChartParamSetting dateViewType = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("dateViewType")).findAny().orElse(null);
			return dateViewType != null && StringUtils.isNotBlank(dateViewType.getParamValue()) && moldId != null && moldIdList.contains(Long.valueOf(moldId.getParamValue()));
		}

		case PRODUCTION_PATTERN_ANALYSIS: {
			DashboardChartParamSetting moldId = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("moldId")).findAny().orElse(null);
			DashboardChartParamSetting week = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("week")).findAny().orElse(null);
			return week != null && StringUtils.isNotBlank(week.getParamValue()) && moldId != null && moldIdList.contains(Long.valueOf(moldId.getParamValue()));
		}

		case ACTUAL_TARGET_UPTIME_RATIO: {
			List<Long> supplierIdsFiltered = companyService.findAllMiniDataFiltered(null, false).stream().map(MiniComponentData::getId).collect(Collectors.toList());
			DashboardChartParamSetting id = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("id")).findAny().orElse(null);
			DashboardChartParamSetting type = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("type")).findAny().orElse(null);
			if (type == null)
				return false;
			if ("PART".equals(type.getParamValue())) {
				return id == null || StringUtils.isBlank(id.getParamValue()) || partIdList.contains(Long.valueOf(id.getParamValue()));
			} else {
				return id == null || StringUtils.isBlank(id.getParamValue()) || supplierIdsFiltered.contains(Long.valueOf(id.getParamValue()));
			}
		}

		case MAXIMUM_CAPACITY: {
			DashboardChartParamSetting ids = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("ids")).findAny().orElse(null);
			return ids != null && (moldIdList.contains(Long.valueOf(ids.getParamValue())) || "All".equals(ids.getParamValue()));
		}

		case OEE_CENTER:
		case PART_PRODUCE: {
			DashboardChartParamSetting locationIdList = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("locationIdList")).findAny()
					.orElse(null);

			DashboardChartParamSetting machineIdList = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("machineIdList")).findAny().orElse(null);

			return locationIdList != null && machineIdList != null;
		}

		default:
			return true;
		}
	}

	private boolean checkSavedConfigMatchGeneralFilter(DashboardChartDisplaySetting dashboardChartDisplaySetting) {
		List<DashboardChartParamSetting> dashboardChartParamSettingList = dashboardChartParamSettingRepository
				.findAllByDashboardChartDisplaySettingId(dashboardChartDisplaySetting.getId());

		switch (dashboardChartDisplaySetting.getChartType()) {
		case PRODUCTION_QUANTITY:
			List<Long> moldIdsFiltered = moldService.findAllMiniDataByGeneralFilter().stream().map(MiniComponentData::getId).collect(Collectors.toList());
			List<Long> partIdsFiltered = partService.findAllMiniDataByGeneralFilter().stream().map(MiniComponentData::getId).collect(Collectors.toList());
			DashboardChartParamSetting type = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("type")).findAny().orElse(null);
			DashboardChartParamSetting ids = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("ids")).findAny().orElse(null);
			assert type != null;
			assert ids != null;
			if (StringUtils.isBlank(ids.getParamValue())) {
				return true;
			}
			String[] stringIds = ids.getParamValue().split(",");
			if ("TOOLING".equals(type.getParamValue())) {
				for (String id : stringIds) {
					Long longId = Long.valueOf(id);
					if (moldIdsFiltered.contains(longId)) {
						return true;
					}
				}
			} else {
				for (String id : stringIds) {
					Long longId = Long.valueOf(id);
					if (partIdsFiltered.contains(longId)) {
						return true;
					}
				}
			}
			return false;

		case OVERALL_EQUIPMENT_EFFECTIVENESS:
			List<Long> companyIdsFiltered = dashboardGeneralFilterUtils.getCompanyIds();
			DashboardChartParamSetting companyId = dashboardChartParamSettingList.stream().filter(param -> param.getParamName().equals("companyId")).findAny().orElse(null);
			assert companyId != null;
			Long id = Long.valueOf(companyId.getParamValue());
			return companyIdsFiltered.contains(id);
		default:
			return false;
		}
	}

	public ApiResponse getDashboardChartDisplaySettingByChartType(DashboardChartType chartType) {
		DashboardChartDisplaySetting dashboardChartDisplaySetting = dashboardChartDisplaySettingRepository.findFirstByChartTypeAndUserId(chartType, SecurityUtils.getUserId());
		List<MiniComponentData> molds = moldService.findAllMiniDataByGeneralFilter();
		List<MiniComponentData> parts = partService.findAllMiniDataByGeneralFilter();

		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = getDashboardChartDisplaySettingByChartType(chartType, dashboardChartDisplaySetting, molds, parts);

		return ApiResponse.success(CommonMessage.OK, dashboardChartDisplaySettingLite);
	}

	private DashboardChartDisplaySettingLite getDashboardChartDisplaySettingByChartType(DashboardChartType chartType, DashboardChartDisplaySetting dashboardChartDisplaySetting,
			List<MiniComponentData> molds, List<MiniComponentData> parts) {

		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		if (dashboardChartDisplaySetting != null && checkSavedConfigMatchGeneralFilter(dashboardChartDisplaySetting, molds, parts)) {
			List<DashboardChartParamSetting> dashboardChartParamSettingList = dashboardChartParamSettingRepository
					.findAllByDashboardChartDisplaySettingId(dashboardChartDisplaySetting.getId());
			dashboardChartDisplaySettingLite.setChartType(dashboardChartDisplaySetting.getChartType());
			Map<String, String> dashboardSettingDataMap = dashboardChartParamSettingList.stream().collect(Collectors.toMap(DashboardChartParamSetting::getParamName,
					dashboardChartParamSetting -> dashboardChartParamSetting.getParamValue() == null ? StringUtils.EMPTY : dashboardChartParamSetting.getParamValue()));
			dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		} else {
			switch (chartType) {
			case OVERALL_TOOLING_EFFECTIVENESS:
				dashboardChartDisplaySettingLite = getDefaultOverallToolingEffectiveness();
				break;
			case DOWNTIME:
				dashboardChartDisplaySettingLite = getDefaultDowntime();
				break;
			case PRODUCTION_QUANTITY:
				dashboardChartDisplaySettingLite = getDefaultProductionQuantity();
				break;
			case OVERALL_EQUIPMENT_EFFECTIVENESS:
				dashboardChartDisplaySettingLite = getDefaultOverallEquipmentEffectiveness();
				break;
			case PRODUCTION_PATTERN_ANALYSIS:
				dashboardChartDisplaySettingLite = getDefaultProductionPatternAnalysis(molds);
				break;
			case ACTUAL_TARGET_UPTIME_RATIO:
				dashboardChartDisplaySettingLite = getDefaultToolingUptimeRatio();
				break;
			case CYCLE_TIME:
				dashboardChartDisplaySettingLite = getDefaultCycleTime(molds);
				break;
			case MAXIMUM_CAPACITY:
				dashboardChartDisplaySettingLite = getDefaultProductionCapacity(molds);
				break;
			case DATA_COMPLETION_RATE:
				dashboardChartDisplaySettingLite = getDefaultDataCompletionRate();
				break;
			case ON_TIME_DELIVERY:
				dashboardChartDisplaySettingLite = getDefaultOnTimeDelivery(parts);
				break;
			case OEE_CENTER:
				dashboardChartDisplaySettingLite = getDefaultOEECenter(DashboardChartType.OEE_CENTER);
				break;
			case PART_PRODUCE:
				dashboardChartDisplaySettingLite = getDefaultOEECenter(DashboardChartType.PART_PRODUCE);
				break;
			}
		}
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultOnTimeDelivery(List<MiniComponentData> parts) {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.ON_TIME_DELIVERY);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR) - 1;
		int year = calendar.get(Calendar.YEAR);
		if (weekOfYear > 10) {
			dashboardSettingDataMap.put("week", String.format("%s%s", year, weekOfYear));
		} else {
			dashboardSettingDataMap.put("week", String.format("%s0%s", year, weekOfYear));
		}

		for (MiniComponentData part : parts) {
			List<ChartData> supplierCavityParts = partRepository.findSupplierTotalCavityPart(part.getId());
			if (CollectionUtils.isNotEmpty(supplierCavityParts)) {
				dashboardSettingDataMap.put("partId", part.getId().toString());
				break;
			}
		}
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultDataCompletionRate() {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.DATA_COMPLETION_RATE);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		dashboardSettingDataMap.put("companyId", "All");
		dashboardSettingDataMap.put("objectType", "All");
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultProductionCapacity(List<MiniComponentData> molds) {
		boolean isDyson = serverName.equalsIgnoreCase("dyson");
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.MAXIMUM_CAPACITY);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		List<Frequent> frequentList = Arrays.asList(Frequent.DAILY, Frequent.WEEKLY, Frequent.MONTHLY);
		moldLoop: for (MiniComponentData mold : molds) {
			for (Frequent frequent : frequentList) {
				List<MoldCapacityReportData> productionQuantityData = statisticsRepository.findMoldCapacityOfTooling(new DashboardFilterPayload(),
						Collections.singletonList(mold.getId()), frequent, isDyson);

				if (CollectionUtils.isNotEmpty(productionQuantityData) && productionQuantityData.stream()
						.anyMatch(data -> data.getAvailableOutput() > 0 || data.getOutputCapacity() > 0 || data.getAvailableDowntime() > 0 || data.getOverCapacity() > 0)) {
					dashboardSettingDataMap.put("ids", mold.getId().toString());
					dashboardSettingDataMap.put("frequent", frequent.toString());
					break moldLoop;
				}
			}
		}
		if (MapUtils.isEmpty(dashboardSettingDataMap) && CollectionUtils.isNotEmpty(molds)) {
			dashboardSettingDataMap.put("ids", molds.get(0).getId().toString());
			dashboardSettingDataMap.put("frequent", Frequent.DAILY.toString());
		}
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultCycleTime(List<MiniComponentData> molds) {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.CYCLE_TIME);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		List<DateViewType> dateViewTypeList = Arrays.asList(DateViewType.DAY, DateViewType.WEEK, DateViewType.MONTH);
		moldLoop: for (MiniComponentData mold : molds) {
			for (DateViewType dateViewType : dateViewTypeList) {
				List<ChartData> chartData = moldService.findCycleTimeDashboard(mold.getId(), dateViewType, null);
				if (CollectionUtils.isNotEmpty(chartData) && chartData.stream()
						.anyMatch(data -> data.getContractedCycleTime() > 0 || data.getCycleTimeWithin() > 0 || data.getCycleTimeL1() > 0 || data.getCycleTimeL2() > 0)) {
					dashboardSettingDataMap.put("moldId", mold.getId().toString());
					dashboardSettingDataMap.put("dateViewType", dateViewType.toString());
					break moldLoop;
				}
			}
		}
		if (MapUtils.isEmpty(dashboardSettingDataMap) && CollectionUtils.isNotEmpty(molds)) {
			dashboardSettingDataMap.put("moldId", molds.get(0).getId().toString());
			dashboardSettingDataMap.put("dateViewType", DateViewType.DAY.toString());
		}
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultToolingUptimeRatio() {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.ACTUAL_TARGET_UPTIME_RATIO);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		dashboardSettingDataMap.put("type", "PART");
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultProductionPatternAnalysis(List<MiniComponentData> molds) {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.PRODUCTION_PATTERN_ANALYSIS);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR) - 1;
		int year = calendar.get(Calendar.YEAR);
		if (weekOfYear > 10) {
			dashboardSettingDataMap.put("week", String.format("%s%s", year, weekOfYear));
		} else {
			dashboardSettingDataMap.put("week", String.format("%s0%s", year, weekOfYear));
		}
		if (CollectionUtils.isNotEmpty(molds)) {
			dashboardSettingDataMap.put("moldId", molds.get(0).getId().toString());
		}

		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultOEECenter(DashboardChartType chartType) {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(chartType);
		LocationPayload locationPayload = new LocationPayload();
		locationPayload.setStatus("active");
		Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		List<LocationData> locationDataList = locationRepository.findLocationData(locationPayload.getPredicate(), pageable, null);
		MachinePayload machinePayload = new MachinePayload();
		machinePayload.setStatus("enabled");
		Page<Machine> pageContent = machineService.findAll(machinePayload.getPredicate(), pageable);
		if (CollectionUtils.isNotEmpty(locationDataList)) {
			dashboardSettingDataMap.put("locationIdList", locationDataList.get(0).getLocation().getId().toString());
		}
		if (CollectionUtils.isNotEmpty(pageContent.getContent())) {
			dashboardSettingDataMap.put("machineIdList", pageContent.getContent().get(0).getId().toString());
		}
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultOverallEquipmentEffectiveness() {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.OVERALL_EQUIPMENT_EFFECTIVENESS);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		dashboardSettingDataMap.put("line", "all");

		CompanyPayload payload = new CompanyPayload();
		payload.setStatus("active");
		Pageable pageable = PageRequest.of(0, 999999, new Sort(Sort.Direction.DESC, "id"));
		Page<Company> pageContent = companyService.findAllOrderByName(payload, pageable);
		List<Long> companyIdList = pageContent.stream().map(Company::getId).collect(Collectors.toList());

		Map<String, List<String>> typeDateMap = new LinkedHashMap<>();
		typeDateMap.put("LAST_WEEK", Arrays.asList(DateUtils.getDate(DateUtils.getFirstDayOfLastWeek().toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT),
				DateUtils.getDate(DateUtils.getLastDayOfLastWeek().toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT)));

		typeDateMap.put("LAST_MONTH", Arrays.asList(DateUtils.getDate(DateUtils.getFirstDayOfLastMonth().toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT),
				DateUtils.getDate(DateUtils.getLastDayOfLastMonth().toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT)));

		typeDateMap.put("LAST_YEAR", Arrays.asList(DateUtils.getDate(DateUtils.getFirstDayOfLastYear().toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT),
				DateUtils.getDate(DateUtils.getLastDayOfLastYear().toInstant(), DateUtils.YYYY_MM_DD_DATE_FORMAT)));

		Pageable pageableMachineStatistic = PageRequest.of(0, 4, new Sort(Sort.Direction.ASC, "name"));

		String choiceCompanyId = null;
		String choicePeriod = null;

		companyLoop: for (Long companyId : companyIdList) {
			for (Map.Entry<String, List<String>> entry : typeDateMap.entrySet()) {
				List<String> dateList = entry.getValue();
				Page<DetailOEE> detailOEEPage = machineStatisticsRepository.findMachineStatisticsForOEE(dateList.get(0), dateList.get(1), "all", companyId,
						pageableMachineStatistic, false);
				if (CollectionUtils.isNotEmpty(detailOEEPage.getContent())) {
					choiceCompanyId = companyId.toString();
					choicePeriod = entry.getKey();
					break companyLoop;
				}
			}
		}

		if (CollectionUtils.isNotEmpty(companyIdList)) {
			dashboardSettingDataMap.put("companyId", choiceCompanyId != null ? choiceCompanyId : companyIdList.get(0).toString());
		}
		dashboardSettingDataMap.put("period", choicePeriod != null ? choicePeriod : "lastWeek");
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);

		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultProductionQuantity() {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.PRODUCTION_QUANTITY);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();

		DashboardFilterPayload payload = new DashboardFilterPayload();

		List<Frequent> frequentList = Arrays.asList(Frequent.DAILY, Frequent.WEEKLY, Frequent.MONTHLY, Frequent.YEARLY);
		Frequent frequentData = null;

		for (Frequent frequent : frequentList) {
			List<PartStatisticsPartIds> partStatisticsPartIds = moldRepository.findMoldIdsByDashboardPayloadAndPartIds(payload, null, frequent);
			if (CollectionUtils.isNotEmpty(partStatisticsPartIds) && CollectionUtils.isNotEmpty(partStatisticsPartIds.get(0).getStatisticsPartIds())) {
				if (statisticsRepository.checkHaveDataProductionQuantityData(partStatisticsPartIds.get(0).getStatisticsPartIds(), frequent)) {
					frequentData = frequent;
					break;
				}
			}
		}

		dashboardSettingDataMap.put("frequent", frequentData == null ? Frequent.DAILY.toString() : frequentData.toString());
		dashboardSettingDataMap.put("ids", StringUtils.EMPTY);
		dashboardSettingDataMap.put("type", "PART");
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);

		return dashboardChartDisplaySettingLite;
	}

	private DashboardChartDisplaySettingLite getDefaultDowntime() {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.DOWNTIME);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		dashboardSettingDataMap.put("timeDuration", "All time");
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;

	}

	private DashboardChartDisplaySettingLite getDefaultOverallToolingEffectiveness() {
		DashboardChartDisplaySettingLite dashboardChartDisplaySettingLite = new DashboardChartDisplaySettingLite();
		dashboardChartDisplaySettingLite.setChartType(DashboardChartType.OVERALL_TOOLING_EFFECTIVENESS);
		Map<String, String> dashboardSettingDataMap = new HashMap<>();
		dashboardSettingDataMap.put("ops", Arrays.stream(OperatingStatus.values()).map(Enum::toString).collect(Collectors.joining(",")));

		Long endTime = new Date().getTime();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DATE, -7);
		Long startTime = calendar.getTimeInMillis();
		DashboardFilterPayload payload = new DashboardFilterPayload();
		payload.setStartTime(startTime / 1000);
		payload.setEndTime(endTime / 1000);
		payload.setOps(Arrays.asList(OperatingStatus.values()));
		List<ChartDataOte> statisticsDataOteList = statisticsRepository.findChartDataOte(payload);
		if (CollectionUtils.isNotEmpty(statisticsDataOteList)) {
			dashboardSettingDataMap.put("dateViewType", DateViewType.LAST7DAYS.toString());
		} else {
			dashboardSettingDataMap.put("dateViewType", DateViewType.LAST30DAYS.toString());
		}
		dashboardChartDisplaySettingLite.setDashboardSettingData(dashboardSettingDataMap);
		return dashboardChartDisplaySettingLite;
	}
}
