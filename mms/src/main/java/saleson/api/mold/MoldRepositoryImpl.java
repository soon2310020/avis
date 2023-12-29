package saleson.api.mold;

import static saleson.api.mold.DynamicExportService.updateCycleTimeDataList;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.QCycleTimeDeviation;
import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.asset.resource.base.maintenance.repository.moldpmplan.QMoldPmPlan;
import com.emoldino.api.asset.resource.base.mold.dto.OptimalCycleTime;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.enumeration.ActiveStatus;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.CaseForEqBuilder;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.LiteralExpression;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
import saleson.common.config.Const;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ChartDataType;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.DataRangeType;
import saleson.common.enumeration.DateViewType;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.FrequentUsage;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.MoldStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.enumeration.RangeType;
import saleson.common.enumeration.ReportType;
import saleson.common.enumeration.StorageType;
import saleson.common.enumeration.productivity.CompareType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.StringUtils;
import saleson.dto.MapQueryType;
import saleson.model.Cdata;
import saleson.model.Location;
import saleson.model.Machine;
import saleson.model.Mold;
import saleson.model.MoldCustomFieldValue;
import saleson.model.MoldEfficiency;
import saleson.model.QAccessCompanyRelation;
import saleson.model.QCategory;
import saleson.model.QCdata;
import saleson.model.QCompany;
import saleson.model.QContinent;
import saleson.model.QCounter;
import saleson.model.QDataRequest;
import saleson.model.QDataRequestObject;
import saleson.model.QFileStorage;
import saleson.model.QMachine;
import saleson.model.QMachineMoldMatchingHistory;
import saleson.model.QMold;
import saleson.model.QMoldCorrective;
import saleson.model.QMoldCycleTime;
import saleson.model.QMoldDownTime;
import saleson.model.QMoldEfficiency;
import saleson.model.QMoldMaintenance;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.model.QPreset;
import saleson.model.QStatistics;
import saleson.model.QStatisticsPart;
import saleson.model.QStatisticsPreset;
import saleson.model.config.QCurrencyConfig;
import saleson.model.customField.QCustomField;
import saleson.model.customField.QCustomFieldValue;
import saleson.model.data.AvgCavityStatisticsData;
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
import saleson.model.data.MoldCapacityReportData;
import saleson.model.data.MoldCycleTimeExtraData;
import saleson.model.data.MoldEfficiencyExtraData;
import saleson.model.data.MoldExtraData;
import saleson.model.data.MoldMachinePairData;
import saleson.model.data.MoldMaintenanceExtraData;
import saleson.model.data.MoldMaintenanceMoldIdExecutionRate;
import saleson.model.data.MoldReportData;
import saleson.model.data.MoldReportDataPage;
import saleson.model.data.MoldShotData;
import saleson.model.data.PartProductionData;
import saleson.model.data.PartShotData;
import saleson.model.data.PartStatisticsPartId;
import saleson.model.data.PartStatisticsPartIds;
import saleson.model.data.QStatisticsSummary;
import saleson.model.data.StatisticsData;
import saleson.model.data.StatisticsPartData;
import saleson.model.data.chartData.ChartDataPreset;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.data.cycleTime.CycleTimeOverviewDetailData;
import saleson.model.data.cycleTime.ToolingCycleTimeData;
import saleson.model.data.dashboard.cost.CostData;
import saleson.model.data.productivity.ProductivityOverviewData;
import saleson.model.data.productivity.ToolingProductivityData;
import saleson.model.data.projection.ChartDataLastShot;
import saleson.model.data.wut.MoldCttTempData;
import saleson.service.util.NumberUtils;

public class MoldRepositoryImpl extends QuerydslRepositorySupport implements MoldRepositoryCustom {
	public MoldRepositoryImpl() {
		super(Cdata.class);
	}

	@Autowired
	private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

//	@Value("${mms.env}")
//	String env;

	private List<ChartData> getStatisticsData(ChartPayload payload) {
		Assert.notEmpty(payload.getChartDataType(), "ChartDataType is required!!");

		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCounter counter = QCounter.counter;

//		JPAExpressions.select(mold.counter.equipmentCode).from(mold).where(mold.part.id.eq(6335L)))

		JPQLQuery<?> tempquery = from(statistics);
		tempquery.leftJoin(counter).on(statistics.ci.eq(counter.equipmentCode));

		if (payload.getPartId() != null) {
			tempquery = from(statisticsPart);
			tempquery.innerJoin(statisticsPart.statistics, statistics);

			tempquery.where(statisticsPart.partId.eq(payload.getPartId()));
		}

		if (!StringUtils.isEmpty(payload.getYear())) {
			tempquery.where(statistics.year.eq(payload.getYear()));
		}

		if (!StringUtils.isEmpty(payload.getMonth())) {
			tempquery.where(statistics.month.eq(payload.getMonth()));
		}

		if (!StringUtils.isEmpty(payload.getDate())) {
			tempquery.where(statistics.day.eq(payload.getDate()));
		}

//		if (payload.getPartCode() != null) {
//			tempquery.where(statistics.partCode.eq(payload.getPartCode()));
//		}

//		if (payload.getMoldCode() != null) {
//			tempquery.where(statistics.moldCode.eq(payload.getMoldCode()));
//		}

		if (payload.getMoldId() != null) {
			tempquery.where(statistics.moldId.eq(payload.getMoldId()));
		}
		if (payload.getMoldIdList() != null) {
			tempquery.where(statistics.moldId.in(payload.getMoldIdList()));
		}
		// fix: weekly of CYCLE_TIME_DASHBOARD is week of year.
		if (payload.getStartDate() != null && payload.getEndDate() != null
				&& !payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_DASHBOARD)) {
			tempquery.where(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		}

		tempquery.where(statistics.moldId.isNotNull());

		NumberPath<Double> ct;
		NumberPath<Integer> shotCount;
		NumberPath<Double> llct;
		NumberPath<Double> ulct;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
			llct = statistics.llctVal;
			ulct = statistics.ulctVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
			llct = statistics.llct;
			ulct = statistics.ulct;
		}

		StringPath dataView = getDataView(statistics, payload.getDateViewType());
		tempquery.where(ct.ne(9999d));
		tempquery.where(shotCount.isNotNull().and(shotCount.gt(0)));
		tempquery.where(ct.gt(0).or(statistics.firstData.isTrue()));
		if (payload.getPartId() != null) {
			tempquery.where(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
		}
//		if (env == null || !(env.equals("local") || env.equals("dev"))) {
//			DateTemplate activatedAt = counter.activatedAt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y-%m-%d %h:%:i%s')", counter.activatedAt);
//			DateTemplate rt = statistics.rt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y%m%d%h%i%s')", statistics.rt);
//			tempquery.where(activatedAt.lt(rt));
//		}

		tempquery.groupBy(statistics.moldId, dataView);
		tempquery.orderBy(statistics.moldId.asc(), dataView.asc());
		if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.QUANTITY)) {
			JPQLQuery<ChartDataLastShot> query;

			// [2021-02-26] Ignore all ct == 0 when get chart data
			tempquery.where(shotCount.gt(0).or(statistics.firstData.isTrue()));

			if (payload.getPartId() != null) {
				query = tempquery.select(
						Projections.constructor(ChartDataLastShot.class, statistics.moldId, dataView, (shotCount.multiply(statisticsPart.cavity)).sum(), statistics.sc.max()));
			} else {
				query = tempquery.select(Projections.constructor(ChartDataLastShot.class, statistics.moldId, dataView, getShotCountSum(statistics, dataView), statistics.sc.max()));
		}
			List<ChartDataLastShot> chartDataLastShots = query.fetch();
			return chartDataLastShots.stream().map(c -> c.convertToChartData()).collect(Collectors.toList());
		}

		JPQLQuery<ChartData> query;
		if (payload.getChartDataType().contains(ChartDataType.UPTIME)) {
			tempquery.where(ct.gt(0));

			query = tempquery.select(Projections.constructor(ChartData.class, statistics.moldId, dataView, getShotCountSum(statistics, dataView), statistics.uptimeSeconds.sum()));
		} else if (payload.getChartDataType().contains(ChartDataType.CYCLE_TIME)) {
			// JPQLQuery query2 = from(statistics); // 최빈값 구하기
			tempquery.where(ct.gt(0).and(shotCount.gt(0)));

			query = tempquery.select(Projections.constructor(ChartData.class, statistics.moldId, dataView, ct.multiply(shotCount).sum(), getShotCountSum(statistics, dataView), ct.max(), ct.min()));
		} else if (payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_ANALYSIS)) {
			tempquery.where(ct.gt(0).and(shotCount.gt(0)));

			query = tempquery.select(Projections.constructor(ChartData.class, statistics.moldId, dataView, getShotCountSum(statistics, dataView), ulct.max(), ct.max(), llct.max(),
					statistics.uptimeSeconds.sum()));
		} else if (payload.getChartDataType().contains(ChartDataType.TEMPERATURE_ANALYSIS)) {
			tempquery.where(ct.gt(0));

			query = tempquery.select(
					Projections.constructor(ChartData.class, statistics.moldId, dataView, getShotCountSum(statistics, dataView), statistics.thi.max(), statistics.tav.avg().intValue(), statistics.tlo.min()));
		} else if (payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_DASHBOARD)) {
			if (payload.getStartDate() != null && payload.getEndDate() != null)
				tempquery.where(dataView.between(payload.getStartDate(), payload.getEndDate()));
			tempquery.where(ct.gt(0));

			query = tempquery.select(Projections.constructor(ChartData.class, statistics.moldId, dataView, ct.multiply(shotCount).sum(), getShotCountSum(statistics, dataView), ct.max(), ct.min()));
		} else {
			throw new LogicException("UNKNOWN_CHART_DATA_TYPE", "Unknown ChartDataType: " + payload.getChartDataType().get(0));
		}

		return query.fetch();
	}

	private List<ChartData> getChartPresetList(ChartPayload payload) {

		if (payload.getChartDataType() != null && !payload.getChartDataType().contains(ChartDataType.QUANTITY)) {
			return new ArrayList<>();
		}
		QStatisticsPreset statisticsPreset = QStatisticsPreset.statisticsPreset;
		QPreset preset = QPreset.preset1;

		JPQLQuery query = from(statisticsPreset).join(preset).on(statisticsPreset.presetId.eq(preset.id));

		NumberExpression<Integer> resetToShot = new CaseBuilder()//
				.when(preset.preset.isNull())//
				.then(0)//
				.otherwise(preset.preset.castToNum(Integer.class));

		if (payload.getYear() != null) {
			query.where(statisticsPreset.year.eq(payload.getYear()));
		}

		if (payload.getMonth() != null && !payload.getMonth().isEmpty()) {
			query.where(statisticsPreset.month.eq(payload.getMonth()));
		}

		if (payload.getDate() != null) {
			query.where(statisticsPreset.day.eq(payload.getDate()));
		}

		if (payload.getMoldId() != null) {
			query.where(statisticsPreset.moldId.eq(payload.getMoldId()));
		}
		if (payload.getMoldIdList() != null) {
			query.where(statisticsPreset.moldId.in(payload.getMoldIdList()));
		}
		if (payload.getStartDate() != null && payload.getEndDate() != null)
			query.where(statisticsPreset.day.between(payload.getStartDate(), payload.getEndDate()));
		query.where(statisticsPreset.moldId.isNotNull());

		StringPath dataView = getDataViewStatisticsPreset(statisticsPreset, payload);

//		query.groupBy(statisticsPreset.moldId, dataView);
		query.orderBy(statisticsPreset.moldId.asc(), statisticsPreset.hour.asc(), statisticsPreset.id.asc(), dataView.asc());

//			if (payload.getPartId() != null) {
//
//				query.select(Projections.constructor(ChartData.class, statistics.moldId, dataView, (statistics.shotCount.multiply(statisticsPart.cavity)).sum()));
//			} else {
		query.select(Projections.constructor(ChartDataPreset.class, statisticsPreset.moldId, dataView, statisticsPreset.shotMissing, resetToShot));
//			}
		List<ChartDataPreset> chartDataPresets = query.fetch();
		return chartDataPresets.stream().map(c -> c.toChartData()).collect(Collectors.toList());
	}

	@Override
	public List<StatisticsData> getMoldUptimeData(DashboardFilterPayload payload){
		QCounter counter = QCounter.counter;
		QMold mold = QMold.mold;

		JPQLQuery<StatisticsData> query;
		if (payload != null && payload.getStartTime() != null && payload.getEndTime() != null) {
			QStatistics table = QStatistics.statistics;
			query = from(table).select(
					Projections.constructor(StatisticsData.class, mold.equipmentCode, mold.createdAt.coalesce(Instant.now().minus(7, ChronoUnit.DAYS)), table.uptimeSeconds.sum()));
			query.innerJoin(mold).on(table.moldId.eq(mold.id));
			query.leftJoin(counter).on(table.ci.eq(counter.equipmentCode));
			if (payload != null && payload.getStartTime() != null && payload.getEndTime() != null) {
				query.where(table.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
			}

			NumberPath<Integer> shotCountVal = table.shotCount;
			query.where(shotCountVal.isNotNull().and(shotCountVal.gt(0)));
//			if (env == null || !(env.equals("local") || env.equals("dev"))) {
//				DateTemplate activatedAt = counter.activatedAt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y-%m-%d %h:%:i%s')", counter.activatedAt);
//				DateTemplate rt = statistics.rt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y%m%d%h%i%s')", statistics.rt);
//				query.where(activatedAt.lt(rt));
//			}
		} else {
			QStatisticsSummary table = QStatisticsSummary.statisticsSummary;
			query = from(table).select(
					Projections.constructor(StatisticsData.class, mold.equipmentCode, mold.createdAt.coalesce(Instant.now().minus(7, ChronoUnit.DAYS)), table.uptimeSeconds.sum()));
			query.innerJoin(mold).on(table.moldId.eq(mold.id));
			query.leftJoin(counter).on(table.ci.eq(counter.equipmentCode));
			if (payload != null && payload.getStartTime() != null && payload.getEndTime() != null) {
				query.where(table.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
			}

			NumberPath<Long> shotCountVal = table.shotCount;
			query.where(shotCountVal.isNotNull().and(shotCountVal.gt(0)));
//			if (env == null || !(env.equals("local") || env.equals("dev"))) {
//				DateTemplate activatedAt = counter.activatedAt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y-%m-%d %h:%:i%s')", counter.activatedAt);
//				DateTemplate rt = statistics.rt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y%m%d%h%i%s')", statistics.rt);
//				query.where(activatedAt.lt(rt));
//			}
		}

		query.where(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload != null){
//			if(payload.getId() != null)
//				query.where(mold.supplier.id.eq(payload.getId()));
//			if(payload.getToolMakerId() != null)
//				query.where(mold.toolMaker.id.eq(payload.getToolMakerId()));
//			if(payload.getLocationId() != null)
//				query.where(mold.location.id.eq(payload.getLocationId()));
//			if(payload.getStartTime() != null && payload.getEndTime() != null)
//				query.where(statistics.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		query.groupBy(mold.equipmentCode, mold.createdAt);
		List<StatisticsData> list = query.fetch();
		if(payload != null && payload.getStartTime() != null && payload.getEndTime() != null){
			list.forEach(item -> {
				if(item.getMoldCreatedAt() < payload.getStartTime()){
					item.setAliveTime(payload.getEndTime() - payload.getStartTime());
				}else{
					item.setAliveTime(payload.getEndTime() - item.getMoldCreatedAt());
				}
			});
		}
		return list;
	}

	@Override
	public List<ChartData> findChartData(ChartPayload payload) {
		return findChartData(payload, false);
	}

	public List<ChartData> findChartData(ChartPayload payload, boolean isExportDynamic) {
		List<ChartData> chartDataList = getStatisticsData(payload);

		// get reset value
		Map<String, List<ChartData>> chartPresetMap = new HashMap<>();
		if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.QUANTITY)) {
			List<ChartData> chartPresetList = getChartPresetList(payload);
			chartPresetList.stream().forEach(chartPreset -> {
				String key = chartPreset.getTitle() + "_" + chartPreset.getMoldCode();
				List<ChartData> list = chartPresetMap.get(key);
				if (list == null) {
					list = new ArrayList<>();
				}
				list.add(chartPreset);
				chartPresetMap.put(key, list);
			});
		}

		// Prepare list of title
		List<String> titles = chartDataList.stream()//
				.filter(x -> x.getTitle() != null)//
				.map(x -> x.getTitle())//
				.collect(Collectors.toList());
//		chartDataList.forEach(data -> {
//			titles.add(data.getTitle());
//		});
		Map<String, AvgCavityStatisticsData> cavities = new LinkedHashMap<>();
		List<MoldShotData> moldShotData = new ArrayList<>();
		List<PartShotData> partShotData = new ArrayList<>();
		if (payload.getMoldId() != null) {
			cavities = getCavitiesByTitles(payload, titles);
			partShotData = getPartShotList(payload, titles);
		}
		if (payload.getPartId() != null) {
			moldShotData = getMoldShotList(payload, titles);
			List<Long> moldIds = moldShotData.stream()//
					.map(x -> x.getMoldId())//
					.distinct()//
					.collect(Collectors.toList());
			List<MiniComponentData> moldIdMoldCode = findAllMoldIdMoldCodes(moldIds);
			moldShotData.forEach(moldSD -> {
				String moldCode = moldIdMoldCode.stream()//
						.filter(x -> x.getId().equals(moldSD.getMoldId()))//
						.map(x -> x.getName())//
						.findAny().orElse(null);
				moldSD.setMoldCode(moldCode);
			});
		}

		// 데이터 가공
		List<ChartData> results = new ArrayList<>();
		for (ChartData chartData : chartDataList) {
			boolean matched = false;
			for (ChartData data : results) {

				if (data.getTitle().equals(chartData.getTitle()) && Objects.equals(data.getMoldCode(), chartData.getMoldCode())) {
					long uptime1 = data.getUptime() == null ? 0 : data.getUptime();
					long uptime2 = chartData.getUptime() == null ? 0 : chartData.getUptime();

					data.setData(data.getData() + chartData.getData());
					data.setTotalPartProduced(data.getTotalPartProduced() + chartData.getTotalPartProduced());
					data.setUptime(uptime1 + uptime2);
					data.setMoldCount(data.getMoldCount() + 1);

					partShotData.forEach(pd -> {
						if (pd.getTitle().equals(chartData.getTitle())) {
							data.setTotalPartProduced(data.getTotalPartProduced() + pd.getPartProduced());
							data.getPartData().add(pd);
						}
					});

					matched = true;
					break;
				}
			}

			if (!matched) {
				ChartData newChartData = new ChartData(chartData.getTitle(), chartData.getData(), 1);
				newChartData.setMoldCode(chartData.getMoldCode());
				newChartData.setLastShot(chartData.getLastShot());
				if (payload.getMoldId() != null) {
					Integer totalCavity = cavities.containsKey(chartData.getTitle()) ? cavities.get(chartData.getTitle()).getTotalCavity() : 0;
					if (chartData.getData() == 0) {
						newChartData.setAvgCavities(NumberUtils.roundOffNumber(0.0));
					} else {
						Double avgCavities = (double) totalCavity / (double) chartData.getData();
						newChartData.setAvgCavities(NumberUtils.roundOffNumber(avgCavities));
					}
				}
				if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.QUANTITY)) {
					if (payload.getPartId() != null) {
						List<MoldShotData> moldShots = moldShotData.stream()//
								.filter(x -> x.getTitle().equals(chartData.getTitle()))//
								.collect(Collectors.toList());
						int[] qty = { 0 };
						moldShots.forEach(moldShot -> {
							Double avgCavity = 0.0;
							if (moldShot.getShotCount() > 0) {
								qty[0] += ValueUtils.toInteger(moldShot.getQuantity(), 0);
								avgCavity = NumberUtils.roundOffNumber((double) moldShot.getQuantity() / (double) moldShot.getShotCount());
							}
							moldShot.setAvgCavity(avgCavity);
						});
						newChartData.setData(qty[0]);
						newChartData.setMoldShots(moldShots);
					}
					newChartData.setTotalPartProduced(chartData.getTotalPartProduced());
					partShotData.forEach(pd -> {
						if (pd.getTitle().equals(chartData.getTitle())) {
							newChartData.setTotalPartProduced(newChartData.getTotalPartProduced() + pd.getPartProduced());
							newChartData.getPartData().add(pd);
						}
					});
					results.add(newChartData);

				} else if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.UPTIME)) {

					if (DateViewType.DAY == payload.getDateViewType() && chartData.getUptime() > 86400) {
						chartData.setUptime(86400L);
					} else if (DateViewType.WEEK == payload.getDateViewType() && chartData.getUptime() > 604800) {
						chartData.setUptime(604800L);
					}
//					results.add(new ChartData(chartData.getTitle(), chartData.getUptime(), newChartData.getAvgCavities(), 1));
					newChartData.setUptime(chartData.getUptime());
					results.add(newChartData);

				} else {
					chartData.setMoldCount(1);
					results.add(chartData);
				}
			}
		}

		System.out.println("=========================");
		if (DateViewType.DAY == payload.getDateViewType()) {

		}

		// 차트데이터에 빈 날짜(주, 월) 채우기
		List<ChartData> data = results.stream().sorted().collect(Collectors.toList());

		if (isExportDynamic) {
			return data;
		}
		//maker start end data for rage date
		makeStartEndDefaultForRange(data, payload);

		// 비어있는 날짜 데이터를 추가하여 리턴함.
		List<ChartData> chartDataList1 = getChartDataAfterAddBlankDay(data, payload.getDateViewType());

		if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.QUANTITY) && payload.getPartId() == null) {
			for (ChartData chartData : chartDataList1) {
				String moldCode = chartData.getMoldCode();
				if (StringUtils.isEmpty(moldCode) && payload.getMoldId() != null)
					moldCode = payload.getMoldId().toString();
				String key = chartData.getTitle() + "_" + moldCode;
				List<ChartData> list = chartPresetMap.get(key);
				Integer resetValue = null;
				if (list != null) {
					resetValue = 0;
					Integer lastShot = 0;
					for (ChartData o : list) {
						resetValue += o.getData();
						if (o.getLastShot() != null && o.getLastShot() > 0) {
							lastShot = o.getLastShot();
						}
					}
					if (list.size() > 0 && list.get(list.size() - 1).getLastShot() != null && list.get(list.size() - 1).getLastShot() > 0) {
						chartData.setLastShot(list.get(list.size() - 1).getLastShot());
					} else if (lastShot > 0) {
						chartData.setLastShot(lastShot);
					}

				}
				chartData.setResetValue(resetValue);
			}
		}

		return chartDataList1;
	}

	@Override
	public List<ChartData> findCycleTimeData(ChartPayload payload, Mold mold) {
		List<ChartData> chartDataList = getStatisticsData(payload);

		// Prepare list of title
		List<String> titles = chartDataList.stream()//
				.filter(x -> x.getTitle() != null)//
				.map(x -> x.getTitle())//
				.collect(Collectors.toList());
		Map<String, AvgCavityStatisticsData> cavities = getCavitiesByTitles(payload, titles);

		//update correct shot data
		ChartPayload payloadForShot = DataUtils.deepCopy(payload, ChartPayload.class);
		payloadForShot.setChartDataType(Arrays.asList(ChartDataType.QUANTITY));
		List<ChartData> chartDataListForShotData = getStatisticsData(payloadForShot);
		for (ChartData chartData : chartDataList) {
			Integer data = chartDataListForShotData.stream()//
					.filter(x -> x.getTitle().equals(chartData.getTitle()))//
					.map(m -> m.getData())//
					.findAny().orElse(null);
			if (data != null) {
				chartData.setData(data);
			}
		}

		// 3. Cycle Time 체크 (L1, L2)
		String month = null;
		if (!ObjectUtils.isEmpty(payload.getMonth())) {
			month = payload.getMonth().toString();
		} else if (!ObjectUtils.isEmpty(payload.getYear())) {
			String thisYear = LocalDate.now().getYear() + "";
			int compare = payload.getYear().compareTo(thisYear);
			if (compare >= 0) {
				month = null;
			} else {
				month = payload.getYear() + "12";
			}
		}
		OptimalCycleTime oct = MoldUtils.getOptimalCycleTime(mold.getId(), mold.getContractedCycleTimeToCalculation(), month);
		double optimalCt = oct.getValue(); // 기준 사이클 타임 (contracted? ) --> 초로 계산
		double baseCycleTime = optimalCt / 10.0;
		ChartData defaultCycleTimeValue = updateCycleTimeDataList(chartDataList, mold, baseCycleTime, cavities).getSecond();

		// Resolve if cycle time chart dashboard without start and end of chain of data
		if (payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_DASHBOARD) && payload.getStartDate() != null
				&& payload.getEndDate() != null) {
			ChartData firstData = chartDataList.stream()//
					.filter(x -> x.getTitle().equals(payload.getStartDate()))//
					.findFirst().orElse(null);
			if (firstData == null) {
				chartDataList.add(new ChartData(payload.getMoldId().toString(), payload.getStartDate(), 0.0));
			}
			ChartData lastData = chartDataList.stream()//
					.filter(x -> x.getTitle().equals(payload.getEndDate()))//
					.findFirst().orElse(null);
			if (lastData == null)
				chartDataList.add(new ChartData(payload.getMoldId().toString(), payload.getEndDate(), 0.0));
		}

		// 차트데이터에 빈 날짜(주, 월) 채우기
		List<ChartData> data = chartDataList.stream().sorted().collect(Collectors.toList());
		if (payload.getChartDataType() == null || !payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_DASHBOARD)) {
			makeStartEndDefaultForRange(data, payload);
		}

		if (data.size() >= 1) {
			data = getChartDataAfterAddBlankDay(data, payload.getDateViewType());
		} else if (data.isEmpty()) {
			ChartData chartData = new ChartData();
			chartData.setTitle("00");
			data.add(chartData);
		}

		// 비어있는 날짜 데이터를 추가하여 리턴함.
//		List<ChartData> chartDataList1=getChartDataAfterAddBlankDay(data, payload.getDateViewType());
		List<ChartData> chartDataList1 = data;
		chartDataList1.stream().forEach(c -> {
//			if (c.getContractedCycleTime() == 0.0 && mold.getContractedCycleTime() != null) {
			c.setContractedCycleTime(baseCycleTime);

			// L1, L2
			c.setCycleTimeMinusL1(defaultCycleTimeValue.getCycleTimeMinusL1());
			c.setCycleTimeMinusL2(defaultCycleTimeValue.getCycleTimeMinusL2());
			c.setCycleTimePlusL1(defaultCycleTimeValue.getCycleTimePlusL1());
			c.setCycleTimePlusL2(defaultCycleTimeValue.getCycleTimePlusL2());
//			}
		});
		return chartDataList1;
	}

	private void makeStartEndDefaultForRange(final List<ChartData> data, ChartPayload payload){
//		if(payload.getDateViewType()==DateViewType.HOUR) return;
		String fromDate=payload.getStartDate();
		String toDate=payload.getEndDate();
			ExportPayload exportPayload = new ExportPayload();
		if(StringUtils.isEmpty(payload.getStartDate()) || StringUtils.isEmpty(payload.getEndDate())){
			if(payload.getDateViewType()==DateViewType.DAY){
				exportPayload.setRangeType(RangeType.MONTHLY);
				exportPayload.setTime(payload.getMonth());//is yearMonth
			}else
			if(payload.getDateViewType()==DateViewType.WEEK || payload.getDateViewType()==DateViewType.MONTH){
				exportPayload.setRangeType(RangeType.YEARLY);
				exportPayload.setTime(payload.getYear());
			}
			if(payload.getDateViewType()==DateViewType.HOUR){
				fromDate=payload.getDate();
				toDate=payload.getDate();
			}else{
			DynamicExportService.generateRageTimeFromTime(exportPayload);
			fromDate=exportPayload.getFromDate();
			toDate=exportPayload.getToDate();
			}
		}
		DateViewType dateViewType = payload.getDateViewType();
		List<ChartData> chartDataListDefault = DynamicExportService.makeChartDataListDefault(fromDate, toDate, dateViewType);
		//add ignore default other year on view week in dashboard
		if (DateViewType.WEEK.equals(dateViewType) && !ObjectUtils.isEmpty(payload.getYear())) {
			if (!chartDataListDefault.get(0).getTitle().startsWith(payload.getYear())) {
				chartDataListDefault.get(0).setTitle(payload.getYear() + "01");
			}
			if (!chartDataListDefault.get(chartDataListDefault.size() - 1).getTitle().startsWith(payload.getYear())) {
				chartDataListDefault.get(chartDataListDefault.size() - 1)
						.setTitle(payload.getYear() + DateUtils.getLastWeekOfYear(Integer.valueOf(payload.getYear())));
			}
		}
		if(!data.stream().anyMatch(d->d.getTitle()!=null && d.getTitle().equals(chartDataListDefault.get(0).getTitle()))){
			data.add(0,chartDataListDefault.get(0));
		}
		if(!data.stream().anyMatch(d->d.getTitle()!=null && d.getTitle().equals(chartDataListDefault.get(chartDataListDefault.size()-1).getTitle()))){
			data.add(chartDataListDefault.get(chartDataListDefault.size()-1));
		}

	}
	/**
	 * 차트 데이터에 비어있는 날짜(일, 주, 월) 데이터를 추가하여 리턴함.
	 * @param data
	 * @param dateViewType
	 * @return
	 */
	private List<ChartData> getChartDataAfterAddBlankDay(List<ChartData> data, DateViewType dateViewType) {
		return getChartDataAfterAddBlankDay(data,dateViewType,false);
	}
	public static List<ChartData> getChartDataAfterAddBlankDay(List<ChartData> data, DateViewType dateViewType,boolean isExport) {
		if(data == null || data.size() == 0) return new ArrayList<>();

		// 일별 보기인 경우
		List<ChartData> allDates = new ArrayList<>();
		if (DateViewType.DAY == dateViewType) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
					.withLocale(Locale.getDefault())
					.withZone(ZoneId.systemDefault());

			LocalDate start = DateUtils.getLocalDate(data.get(0).getTitle());
			LocalDate end = DateUtils.getLocalDate(data.get(data.size() - 1).getTitle()).plusDays(1);
			allDates = Stream.iterate(start, date -> date.plusDays(1))
					.limit(ChronoUnit.DAYS.between(start, end))
					.map(l ->
							new ChartData(l.format(formatter), 0, 0)
					)
					.collect(Collectors.toList());

		}else if(dateViewType.equals(DateViewType.HOUR)){
			if(isExport ||
					!org.apache.commons.lang3.StringUtils.equals(data.get(0).getTitle().substring(0, 8),data.get(data.size()-1).getTitle().substring(0, 8))){
				List<ChartData> fullDays = getChartDataAfterAddBlankDay(data.stream().map(d-> new ChartData(d.getTitle().substring(0, 8),d.getData(),d.getMoldCount())).collect(Collectors.toList())
						, DateViewType.DAY, true);
				List<ChartData> fullHourlyInRange = new ArrayList<>();
				fullDays.stream().forEach(d->{
					String date = d.getTitle().substring(0, 8);
					for(int i = 0; i < 24; i++){
						String title = date + (i < 10 ? "0" + i : i);
						fullHourlyInRange.add(new ChartData(title, 0, 0));
					}
				});
				allDates.addAll(fullHourlyInRange);
			}else {
			String date = data.get(0).getTitle().substring(0, 8);
			for(int i = 0; i < 24; i++){
				String title = date + (i < 10 ? "0" + i : i);
				allDates.add(new ChartData(title, 0, 0));
			}
			}
		} else {		// WEEK, MONTH
			String year = data.get(0).getTitle().substring(0, 4);
			String startData = data.get(0).getTitle().substring(4, 6);
			String endYear = data.get(data.size() - 1).getTitle().substring(0, 4);
			String endData = data.get(data.size() - 1).getTitle().substring(4, 6);

			int start = Integer.parseInt(startData);
			int end = Integer.parseInt(endData);

			if(year.equals(endYear)) {
				for (int i = start; i <= end; i++) {
					String title = year + "" + (i < 10 ? "0" + i : i);

					allDates.add(new ChartData(title, 0, 0));
				}
			}else{
				int lastWeekOrMonth = 12;
				if(dateViewType.equals(DateViewType.WEEK)){
					lastWeekOrMonth = DateUtils.getLastWeekOfYear(Integer.valueOf(year));
				}
				for (int i = start; i <= lastWeekOrMonth; i++){
					String title = year + "" + (i < 10 ? "0" + i : i);

					allDates.add(new ChartData(title, 0, 0));
				}
				for (int i = 1; i <= end; i++){
					String title = endYear + "" + (i < 10 ? "0" + i : i);

					allDates.add(new ChartData(title, 0, 0));
				}
			}
		}

		List<ChartData> newChartData = new ArrayList<>();
		for (ChartData chartData: allDates) {
			chartData.setCycleTimeWithin(0.0);
			chartData.setCycleTimeL1(0.0);
			chartData.setCycleTimeL2(0.0);

			boolean isMatched = false;
			for (ChartData realData: data) {
				if (realData.getTitle().equalsIgnoreCase(chartData.getTitle())) {
					newChartData.add(realData);
					isMatched = true;
					break;
				}
			}

			if (!isMatched) {
				newChartData.add(chartData);
			}
		}
		return newChartData;

	}

	public List<ChartData> findHourDetailsStatistics(ChartPayload payload, List<Long> moldIdList){
		QCdata cdata = QCdata.cdata;
		QStatistics statistics = QStatistics.statistics;

		StringPath ctt;
		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ctt = cdata.cttVal;
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ctt= cdata.ctt;
			ct= statistics.ct;
			shotCount= statistics.shotCount;
		}

		JPQLQuery query = from(cdata)
				.leftJoin(statistics).on(cdata.id.eq(statistics.cdataId));

		if(moldIdList!=null&& !moldIdList.isEmpty())
		query.where(statistics.moldId.in(moldIdList));
		else if(payload.getMoldId()!=null)
		query.where(statistics.moldId.eq(payload.getMoldId()));
		else
			query.where(statistics.moldId.isNotNull());

		if(payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.TEMPERATURE_ANALYSIS)){
			if(payload.getDate()!=null){
				query.where(statistics.day.eq(payload.getDate()));
			}else if(payload.getStartDate() != null && payload.getEndDate() != null){
				query.where(ct.gt(0).and(shotCount.gt(0)));
				query.where(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
			}
			query.orderBy(statistics.rt.asc());
			query.select(Projections.constructor(ChartData.class, statistics.rt, cdata.temp));
		}else if(payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.CYCLE_TIME_ANALYSIS)){
			if(payload.getDate()!=null){
			query.where(statistics.hour.eq(payload.getDate()));
			}else if(payload.getStartDate() != null && payload.getEndDate() != null){
				query.where(ct.gt(0).and(shotCount.gt(0)));
				query.where(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
			}

			query.orderBy(statistics.hour.asc());
			query.select(Projections.constructor(ChartData.class,statistics.moldId, statistics.hour, ctt));
		}
		return query.fetch();
	}

	@Override
	public List<DashboardChartData> findMoldOverview(DashboardFilterPayload payload) {
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		builder.and(mold.operatingStatus.isNotNull());
		QueryUtils.isMold(builder);
		builder.and(mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload != null){
//			if(payload.getSupplierIds() != null && payload.getSupplierIds().size() > 0){
//				builder.and(mold.supplier.id.in(payload.getSupplierIds()));
//			}
//			if(payload.getToolMakerIds() != null && payload.getSupplierIds().size() > 0){
//				builder.and(mold.toolMaker.id.in(payload.getToolMakerIds()));
//			}
//			if(payload.getLocationIds() != null && payload.getLocationIds().size() > 0){
//				builder.and(mold.location.id.in(payload.getLocationIds()));
//			}
			if(payload.getStartTime() != null && payload.getEndTime() != null){
				builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
			}
		}

		JPQLQuery query = from(mold)
							.where(builder
									.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
							.groupBy(mold.operatingStatus)
							.select(Projections.constructor(DashboardChartData.class, mold.operatingStatus, mold.id.count()));

		return query.fetch();
	}


	/**
	 * Company 상세 정보에 표시되는 차트 데이터 (Status)
	 * @param payload
	 * @return
	 */
	@Override
	public List<CompanyChartData> getCompanyDetailsChartData(CompanyPayload payload) {
		QMold mold = QMold.mold;

		JPQLQuery query = from(mold);

		query.where(mold.deleted.isNull().or(mold.deleted.isFalse()));
		if (payload.getId() != null) {
			query.where(mold.companyId.eq(payload.getId()));
		}

		if ("status".equals(payload.getChartType())) {
			query.groupBy(mold.equipmentStatus);
			query.select(Projections.constructor(CompanyChartData.class, mold.equipmentStatus, mold.count()));

		} else {		// condition
			query.groupBy(mold.toolingCondition);
			query.select(Projections.constructor(CompanyChartData.class, mold.toolingCondition, mold.count()));
		}

		return query.fetch();
	}


	@Override
	public List<StatisticsPartData> getStatisticsPartData(List<Long> partIds, PartPayload payload) {

		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCounter counter = QCounter.counter;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		JPQLQuery query = from(statistics);
		query.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId));
		query.leftJoin(counter).on(statistics.ci.eq(counter.equipmentCode));

		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(statistics.moldId.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

//		if(payload != null && payload.getStartTime() != null && payload.getEndTime() != null){
//			query.where(statistics.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//		}
		if(payload != null){
			if(payload.getTimePeriod() != null) {
				String timePeriod = payload.getTimePeriod();
				if (timePeriod.startsWith("Y") || timePeriod.startsWith("y")) {
					query.where(statistics.year.eq(timePeriod.substring(1)));
				} else if (timePeriod.startsWith("M") || timePeriod.startsWith("m")) {
					query.where(statistics.month.eq(timePeriod.substring(1)));
				} else if (timePeriod.startsWith("W") || timePeriod.startsWith("w")) {
					query.where(statistics.week.eq(timePeriod.substring(1)));
				}
			}else if(payload.getStartDate() != null && payload.getEndDate() != null){
				query.where(statistics.day.goe(payload.getStartDate())
						.and(statistics.day.loe(payload.getEndDate())));
			}
		}

		if (partIds != null && partIds.size() > 0) {
			query.where(statisticsPart.partId.in(partIds));
		}

		query.where(shotCount.isNotNull().and(shotCount.gt(0))) ;
		query.where(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0))) ;
		// [2021-02-26] Ignore shot with ct == 0
		query.where(ct.gt(0).or(statistics.firstData.isTrue()));
//		if (env == null || !(env.equals("local") || env.equals("dev"))) {
//			DateTemplate activatedAt = counter.activatedAt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y-%m-%d %h:%:i%s')", counter.activatedAt);
//			DateTemplate rt = statistics.rt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y%m%d%h%i%s')", statistics.rt);
//			query.where(activatedAt.lt(rt));
//		}



		query.groupBy(statisticsPart.partId);
		query.select(Projections.constructor(StatisticsPartData.class, statisticsPart.partId, (shotCount.multiply(statisticsPart.cavity)).sum()));

		return query.fetch();

	}


	@Override
	public List<MapChartData> getMapData(MoldPayload payload) {
		QMold mold = QMold.mold;
		QCompany company = QCompany.company;

		JPQLQuery query = from(mold).innerJoin(company).on(mold.supplier.id.eq(company.id));

		query.where(dashboardGeneralFilterUtils.getMoldFilter(mold));
		query.where(mold.operatingStatus.isNotNull().and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		if (!StringUtils.isEmpty(payload.getQuery())) {
			query.where(
					mold.equipmentCode.contains(payload.getQuery())
							.or(mold.location.name.contains(payload.getQuery()))
							.or(mold.location.locationCode.contains(payload.getQuery()))
							//.or(mold.location.company.name.contains(payload.getQuery()))
			);
		}
		if (payload.getOperatingStatus() != null) {
			query.where(mold.operatingStatus.eq(payload.getOperatingStatus()));
		}
		query.groupBy(mold.location.id, mold.operatingStatus);
		query.select(Projections.constructor(MapChartData.class, mold.location.name, mold.location.address, mold.operatingStatus, mold.count()));

		return query.fetch();

	}

	@Override
	public List<MapChartData> getMapDataByDashboardFilter(String countryCode, DashboardFilterPayload payload){
		QMold mold = QMold.mold;

		JPQLQuery query = from(mold);

		query.where(mold.operatingStatus.isNotNull());
		query.where(mold.equipmentStatus.in(EquipmentStatus.INSTALLED, EquipmentStatus.DETACHED)
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));
		query.where(dashboardGeneralFilterUtils.getMoldFilter(mold));

		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
//		if(payload != null) {
			if (countryCode != null && !countryCode.equalsIgnoreCase("")) {
				query.where(mold.location.countryCode.eq(countryCode)
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())));
			} else if(payload != null){
				if (payload.getStartTime() != null && payload.getEndTime() != null) {
					query.where(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
				}
			}
//		}
		query.groupBy(mold.location.id, mold.operatingStatus, mold.counter.equipmentCode, mold.counter.operatingStatus);
		query.select(Projections.constructor(MapChartData.class, mold.location.name, mold.location.address, mold.operatingStatus, mold.counter.equipmentStatus, mold.counter.operatingStatus, mold.count()));

		return query.fetch();
	}

	@Override
	public List<MapChartData> getMapDataSearchByType(MapSearchPayload payload, MapQueryType mapQueryType) {
		if (payload != null) {
			QMold mold = QMold.mold;
			QMoldPart moldPart = QMoldPart.moldPart;
			QPart part = QPart.part;
			QCategory category = QCategory.category;
			JPQLQuery query = from(mold)
					.innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
					.innerJoin(part).on(moldPart.partId.eq(part.id))
					.innerJoin(category).on(part.categoryId.eq(category.id));
			query.groupBy(mold.location.id, mold.operatingStatus, part.id);
			query.select(Projections.constructor(MapChartData.class, mold.location.name, mold.location.address, mold.operatingStatus, mold.count(), part.name));

			BooleanBuilder builder = new BooleanBuilder();
			if(payload.getCategoryIds() != null && payload.getCategoryIds().size() > 0) {
				builder.and(part.categoryId.in(payload.getCategoryIds())
						.or(category.parentId.in(payload.getCategoryIds())));
			}
			builder.and(mold.operatingStatus.isNotNull());
			builder.and(mold.equipmentStatus.in(EquipmentStatus.INSTALLED, EquipmentStatus.DETACHED)
					.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
					.and(part.deleted.isNull().or(part.deleted.isFalse()));

			if (!StringUtils.isEmpty(payload.getQuery())) {
				switch (mapQueryType) {
					case TOOLING :
						builder.and(mold.equipmentCode.eq(payload.getQuery().trim()));
						break;
					case LOCATION :
						BooleanBuilder orBuilder = new BooleanBuilder();
						orBuilder.or(mold.location.name.like('%'+payload.getQuery().trim()+'%'))
								.or(mold.location.address.like('%'+payload.getQuery().trim()+'%'));
						builder.and(orBuilder);
						break;
					case PART :
						builder.and(part.partCode.like('%'+payload.getQuery().trim()+'%'));
						query.groupBy(mold.location.id, mold.operatingStatus, part.id);
						query.select(Projections.constructor(MapChartData.class, mold.location.name, mold.location.address, mold.operatingStatus, mold.count(), part.partCode));
						break;
				}
			}

			query.where(builder);
			return query.fetch();
		}
		return new ArrayList<>();
	}


	@Override
	public List<LocationMoldData> getLocationMoldData(MoldPayload payload){
		QMold mold = QMold.mold;
		QContinent continent = QContinent.continent;
		JPQLQuery query = from(mold);
		query.innerJoin(continent)
				.on(mold.location.countryCode.eq(continent.countryCode));
		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		query.where(mold.operatingStatus.isNotNull());
		query.where(mold.equipmentStatus.in(EquipmentStatus.INSTALLED, EquipmentStatus.DETACHED)
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));
		query.where(dashboardGeneralFilterUtils.getMoldFilter(mold));
//		query.where(mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
		query.groupBy(mold.location.countryCode, continent.continentName);
		query.select(Projections.constructor(LocationMoldData.class, mold.location.countryCode, continent.continentName, mold.count()));
		return query.fetch();
	}


	@Override
	public Page<CdataCounter> findCdataCounters(CounterPayload payload, Pageable pageable) {
		JPQLQuery query = getCdataCountersJPQLQuery(payload);


		long totalCount = query.fetchCount();

		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		List<CdataCounter> cdataCounters;
		if (SpecialSortProperty.cdataCounterProperties.contains(properties[0])) {
			QCounter counter = QCounter.counter;
			Expression sortValue = counter.equipmentCode;
			if (properties[0].equalsIgnoreCase("cdata.createdAt")) {
				QCdata cdata = QCdata.cdata;
				sortValue = cdata.createdAt.max();
			}

			OrderSpecifier orderSpecifier = ((LiteralExpression) sortValue).desc();
			if (directions[0].equals(Sort.Direction.ASC)) {
				orderSpecifier = ((LiteralExpression) sortValue).asc();
			}
			query.orderBy(orderSpecifier);
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
			cdataCounters = query.fetch();
		} else {

			cdataCounters = getQuerydsl().applyPagination(pageable, query).fetch();
		}

		return new PageImpl<>(cdataCounters, pageable, totalCount);
	}

	@Override
	public long findCdataCountersCount(CounterPayload payload) {
		JPQLQuery query = getCdataCountersJPQLQuery(payload);
		return query.fetchCount();
	}

	private JPQLQuery getCdataCountersJPQLQuery(CounterPayload payload) {
		Instant now = Instant.now();
		Instant nowMinus1Month = now.minus(30, ChronoUnit.DAYS);


		QCdata cdata = QCdata.cdata;
		QCounter counter = QCounter.counter;

		JPQLQuery query = from(cdata)
				.leftJoin(counter)
					.on(cdata.ci.eq(counter.equipmentCode));
		query.where(cdata.createdAt.goe(nowMinus1Month));
		if (payload.getQuery() != null && !payload.getQuery().isEmpty()) {
			query.where(cdata.ci.like('%' + payload.getQuery() + '%')
					.or(counter.equipmentCode.like('%' + payload.getQuery() + '%')));
		}
		query.groupBy(cdata.ci);


		query.select(Projections.constructor(CdataCounter.class, cdata.ci, counter.equipmentCode, cdata.createdAt.max()));
		return query;
	}

	private StringPath getDataView(QStatistics statistics, DateViewType dateViewType) {
		StringPath dataView = statistics.week;
		if (DateViewType.DAY.equals(dateViewType)) {
			dataView = statistics.day;
		} else if (DateViewType.MONTH.equals(dateViewType)) {
			dataView = statistics.month;
		} else if (DateViewType.YEAR.equals(dateViewType)) {
			dataView = statistics.year;
		} else if (DateViewType.HOUR.equals(dateViewType)) {
			dataView = statistics.hour;
		}
		return dataView;
	}

	private StringPath getDataViewStatisticsPreset(QStatisticsPreset statisticsPreset, ChartPayload payload){
		StringPath dataView = statisticsPreset.week;
		if (DateViewType.DAY == payload.getDateViewType()) {
			dataView = statisticsPreset.day;
		}

		if (DateViewType.MONTH == payload.getDateViewType()) {
			dataView = statisticsPreset.month;
		}

		if (DateViewType.YEAR == payload.getDateViewType()) {
			dataView = statisticsPreset.year;
		}

		if(payload.getDateViewType().equals(DateViewType.HOUR)){
			dataView = statisticsPreset.hour;
		}
		return dataView;
	}

	public Map<String, AvgCavityStatisticsData> getCavitiesByTitles(ChartPayload payload, List<String> titles) {
		if (ObjectUtils.isEmpty(titles)) {
			return Collections.emptyMap();
		}

		String _minTitle = null;
		String _maxTitle = null;
		for (String title : titles) {
			if (_minTitle == null) {
				_minTitle = title;
				_maxTitle = title;
				continue;
			}
			if (_minTitle.compareTo(title) > 0) {
				_minTitle = title;
			}
			if (_maxTitle.compareTo(title) < 0) {
				_maxTitle = title;
			}
		}
		String minTitle = _minTitle;
		String maxTitle = _maxTitle;

		StringBuilder buf = new StringBuilder();
		buf.append(ValueUtils.toString(payload.getDateViewType(), "null"));
		buf.append("::").append(ValueUtils.toString(payload.getPartId() == null ? payload.getMoldId() : payload.getPartId(), "null"));
		buf.append("::").append(_minTitle);
		buf.append("::").append(_maxTitle);

		Map<String, AvgCavityStatisticsData> cavities = ThreadUtils.getProp(buf.toString(), () -> {
			QStatistics statistics = QStatistics.statistics;
			QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

			NumberPath<Double> ct;
			NumberPath<Integer> shotCount;
			if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
				ct = statistics.ctVal;
				shotCount = statistics.shotCountVal;
			} else {
				ct = statistics.ct;
				shotCount = statistics.shotCount;
			}

			StringPath titleField = getDataView(statistics, payload.getDateViewType());

			JPQLQuery<AvgCavityStatisticsData> query = from(statisticsPart)//
					.innerJoin(statistics)//
					.on(statistics.id.eq(statisticsPart.statisticsId))//
					.select(Projections.constructor(AvgCavityStatisticsData.class, titleField, statisticsPart.cavity.avg(), shotCount.multiply(statisticsPart.cavity).sum()));
			BooleanBuilder builder = new BooleanBuilder();
			builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

			builder.and(shotCount.isNotNull().and(shotCount.gt(0)));
			builder.and(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));

			NumberExpression<Long> credential = statistics.moldId;
			Long value = payload.getMoldId();
			if (payload.getPartId() != null) {
				credential = statisticsPart.partId;
				value = payload.getPartId();
			}
			builder.and(credential.eq(value));
//			builder.and(titleField.in(titles));
			builder.and(titleField.goe(minTitle));
			builder.and(titleField.loe(maxTitle));

			query.where(builder);
			query.groupBy(titleField, credential);

			List<AvgCavityStatisticsData> list = query.fetch();

			Map<String, AvgCavityStatisticsData> map = new LinkedHashMap<>();
			for (AvgCavityStatisticsData item : list) {
				if (map.containsKey(item.getTitle())) {
					continue;
				}
				map.put(item.getTitle(), item);
			}
			return map;
		});
		return cavities;
	}

	private List<MoldShotData> getMoldShotList(ChartPayload payload, List<String> titles) {
		return (List<MoldShotData>) getShotList(ObjectType.TOOLING, payload, titles);
	}

	public List<PartShotData> getPartShotList(ChartPayload payload, List<String> titles) {
		return (List<PartShotData>) getShotList(ObjectType.PART, payload, titles);
	}

	private List<?> getShotList(ObjectType objectType, ChartPayload payload, List<String> titles) {
		if (ObjectUtils.isEmpty(titles)) {
			return Collections.emptyList();
		}

		String _minTitle = null;
		String _maxTitle = null;
		for (String title : titles) {
			if (_minTitle == null) {
				_minTitle = title;
				_maxTitle = title;
				continue;
			}
			if (_minTitle.compareTo(title) > 0) {
				_minTitle = title;
			}
			if (_maxTitle.compareTo(title) < 0) {
				_maxTitle = title;
			}
		}
		String minTitle = _minTitle;
		String maxTitle = _maxTitle;

		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCounter counter = QCounter.counter;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

		JPQLQuery query = from(statisticsPart)//
				.innerJoin(statistics)//
				.on(statistics.id.eq(statisticsPart.statisticsId))//
				.leftJoin(counter)//
				.on(statistics.ci.eq(counter.equipmentCode))//
				.where(builder);

		StringPath titleField = getDataView(statistics, payload.getDateViewType());

		query.where(shotCount.isNotNull().and(shotCount.gt(0)));
		query.where(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
//		if (env == null || !(env.equals("local") || env.equals("dev"))) {
//			DateTemplate activatedAt = counter.activatedAt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y-%m-%d %h:%:i%s')", counter.activatedAt);
//			DateTemplate rt = statistics.rt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y%m%d%h%i%s')", statistics.rt);
//			query.where(activatedAt.lt(rt));
//		}
		if (ObjectType.PART.equals(objectType)) {
			query.where(titleField.goe(minTitle), titleField.loe(maxTitle), statistics.moldId.eq(payload.getMoldId()));
			query.select(Projections.constructor(PartShotData.class, //
					titleField, statisticsPart.partId, statisticsPart.partCode.max(), shotCount.sum(), shotCount.multiply(statisticsPart.cavity).sum()));
			query.groupBy(titleField, statistics.moldId, statisticsPart.partId);
		} else {
			query.where(titleField.goe(minTitle), titleField.loe(maxTitle), statisticsPart.partId.eq(payload.getPartId()));
			query.select(Projections.constructor(MoldShotData.class, titleField, statistics.moldId, shotCount.sum(), shotCount.multiply(statisticsPart.cavity).sum()));
			query.groupBy(titleField, statistics.moldId, statisticsPart.partId);
		}
//		query.having(dataView.in(titles), statisticsPart.partId.eq(payload.getPartId()));

		return query.fetch();
	}

	@Override
	public long getBaseMoldCount(DashboardFilterPayload payload){
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold);

//		query.where(mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
		if (AccessControlUtils.isAccessFilterRequired()) {
			BooleanBuilder builder = new BooleanBuilder();
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
			query.where(builder);
		}
		query.where(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload != null){
//			if(payload.getId() != null)
//				query.where(mold.supplier.id.eq(payload.getId()));
//			if(payload.getToolMakerId() != null)
//				query.where(mold.toolMaker.id.eq(payload.getToolMakerId()));
//			if(payload.getLocationId() != null)
//				query.where(mold.location.id.eq(payload.getLocationId()));
			if(payload.getStartTime() != null && payload.getEndTime() != null)
				query.where(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		return query.fetchCount();
	}

	@Override
	public long getTotalMoldCount(DashboardFilterPayload payload){
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold))
				.and(mold.equipmentStatus.ne(EquipmentStatus.DISCARDED))
				.and(mold.deleted.isFalse().or(mold.deleted.isNull()));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		JPQLQuery moldQuery = from(mold)
				.where(builder);
		return moldQuery.fetchCount();
	}

	@Override
	public long getInstalledMoldCount(DashboardFilterPayload payload){
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		builder.and(mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
		JPQLQuery moldQuery = from(mold)
				.where(builder);
		return moldQuery.fetchCount();
	}

	@Override
	public Double getTotalCost(DashboardFilterPayload payload){
		QMold mold = QMold.mold;
		QCurrencyConfig currencyConfig = QCurrencyConfig.currencyConfig;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		NumberExpression<Float> moldCost=new CaseBuilder().when(mold.cost.isNull()).then(0f).otherwise(mold.cost.castToNum(Float.class))
				.divide(currencyConfig.rate.coalesce(payload.getMainRate()));
		NumberExpression<Float> totalCmCosts=new CaseBuilder().when(mold.totalCmCosts.isNull()).then(0f).otherwise(mold.totalCmCosts.castToNum(Float.class))
				.divide(currencyConfig.rate.coalesce(payload.getMainRate()));
		NumberExpression<Float> cost=moldCost.add(totalCmCosts);

		JPQLQuery moldQuery = from(mold)
				.leftJoin(currencyConfig).on(mold.costCurrencyType.eq(currencyConfig.currencyType))
				.where(builder)
//				.select((mold.cost.divide(currencyConfig.rate.coalesce(1d))).sum().coalesce(0).add((mold.totalCmCosts.divide(currencyConfig.rate.coalesce(1d))).sum().coalesce(0)));
//				.select((moldCost).sum().coalesce(0f).add(totalCmCosts.sum().coalesce(0f)));
				.select(cost.sum());
		Double costMold = (Double) moldQuery.fetchOne();
		costMold = costMold!=null? costMold * payload.getMainRate():0d;
		if(costMold == null) return 0d;

//		QMoldCorrective moldCorrective = QMoldCorrective.moldCorrective;
//		BooleanBuilder mcBuilder = new BooleanBuilder();
//		mcBuilder.and(JPQLQueryUtils.getMoldFilterBuilder(moldCorrective.mold, payload));
//		if(payload.getStartTime() != null && payload.getEndTime() != null){
//			mcBuilder.and(moldCorrective.mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//		}
//		JPQLQuery mcQuery = from(moldCorrective)
//				.where(mcBuilder)
//				.select(moldCorrective.cost.sum());
//		Integer costMC = (Integer) mcQuery.fetchOne();

		return costMold;
	}

	@Override
	public Double getTotalCost(TabbedOverviewGeneralFilterPayload payload, boolean current) {
		QMold mold = QMold.mold;
		QCurrencyConfig currencyConfig = QCurrencyConfig.currencyConfig;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(payload.getMoldFilterWithPeriod(current));
		NumberExpression<Float> moldCost=new CaseBuilder().when(mold.cost.isNull()).then(0f).otherwise(mold.cost.castToNum(Float.class))
				.divide(currencyConfig.rate.coalesce(payload.getMainRate()));
		NumberExpression<Float> totalCmCosts=new CaseBuilder().when(mold.accumulatedMaintenanceCost.isNull()).then(0f).otherwise(mold.accumulatedMaintenanceCost.castToNum(Float.class))
				.divide(currencyConfig.rate.coalesce(payload.getMainRate()));
		NumberExpression<Float> cost=moldCost.add(totalCmCosts);

		JPQLQuery moldQuery = from(mold)
				.leftJoin(currencyConfig).on(mold.costCurrencyType.eq(currencyConfig.currencyType))
				.where(builder)
				.select(cost.sum());
		Double costMold = (Double) moldQuery.fetchOne();
		costMold = costMold!=null? costMold * payload.getMainRate():0d;
		if(costMold == null) return 0d;

		return costMold;
	}

	@Override
	public List<CostData> getTotalCostGroupByFrequent(TabbedOverviewGeneralFilterPayload payload) {
		QMold mold = QMold.mold;
		QCurrencyConfig currencyConfig = QCurrencyConfig.currencyConfig;
		JPQLQuery query = from(mold)
				.leftJoin(currencyConfig).on(mold.costCurrencyType.eq(currencyConfig.currencyType))
				.where(payload.getMoldFilterWithPeriod(true));
		StringPath dataView = getDataView(payload.getFrequentFromDuration());
		NumberExpression<Float> moldCost=new CaseBuilder().when(mold.cost.isNull()).then(0f).otherwise(mold.cost.castToNum(Float.class))
				.divide(currencyConfig.rate.coalesce(payload.getMainRate()));
		NumberExpression<Float> totalCmCosts=new CaseBuilder().when(mold.accumulatedMaintenanceCost.isNull()).then(0f).otherwise(mold.accumulatedMaintenanceCost.castToNum(Float.class))
				.divide(currencyConfig.rate.coalesce(payload.getMainRate()));
		query.groupBy(dataView);
		query.select(Projections.constructor(CostData.class, dataView, moldCost.sum(), totalCmCosts.sum().intValue()));
		return query.fetch();
	}

	private StringPath getDataView(Frequent frequent) {
		if (Frequent.DAILY.equals(frequent)) return QMold.mold.day;
		else if (Frequent.WEEKLY.equals(frequent)) return QMold.mold.week;
		else return QMold.mold.month;
	}

	@Override
	public long getPartCount(DashboardFilterPayload payload){
		QPart part = QPart.part;
		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder builder = new BooleanBuilder();

//		if (AccessControlUtils.isAccessFilterRequired()) {
//			builder.and(moldPart.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
//		}

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}

//		builder.and(JPQLQueryUtils.getMoldFilterBuilder(moldPart.mold, payload));
//		if(payload.getStartTime() != null && payload.getEndTime() != null){
//			builder.and(moldPart.mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//		}

//		builder.and(moldPart.mold.operatingStatus.isNotNull());
		builder.and(part.enabled.isTrue()).and(part.deleted.isNull().or(part.deleted.isFalse()));
		builder.and(dashboardGeneralFilterUtils.getPartFilter(part));

		JPQLQuery query = from(part)
//				.leftJoin(moldPart).on(moldPart.partId.eq(part.id))
				.where(builder);

//		builder.and(dashboardGeneralFilterUtils.getMoldFilter(moldPart.mold));
		if(payload != null && (payload.getSupplierIds() != null || payload.getToolMakerIds() != null || payload.getOps() != null
			|| payload.getLocationIds() != null || payload.getStartTime() != null)){
			if(payload.getStartTime() != null && payload.getEndTime() != null){
				builder.and(moldPart.mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
			}
			query = from(part)
					.innerJoin(moldPart).on(moldPart.partId.eq(part.id)).distinct()
					.where(builder);
		}
		return query.fetchCount();
	}

	@Override
	public long getProducedPart(DashboardFilterPayload payload){
		QMold mold = QMold.mold;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statisticsPart.statistics.shotCountVal;
		} else {
			shotCount = statisticsPart.statistics.shotCount;
		}
		builder.and(shotCount.isNotNull().and(shotCount.gt(0)));

		JPQLQuery query = from(statisticsPart)
				.innerJoin(mold).on(statisticsPart.statistics.moldId.eq(mold.id))
				.where(builder)
				.select(shotCount.multiply(statisticsPart.cavity).sum());

		return Long.valueOf((Integer) query.fetch().get(0));
	}

	@Override
	public List<PartStatisticsPartIds> findMoldIdsByDashboardPayloadAndPartIds(DashboardFilterPayload payload, List<Long> partIds, Frequent frequent){
		List<PartStatisticsPartIds> partStatisticsPartIdsList = new ArrayList<>();
		QMoldPart moldPart = QMoldPart.moldPart;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		BooleanBuilder builder = new BooleanBuilder();

		BooleanBuilder subBuilder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			subBuilder.and(moldPart.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		subBuilder.and(dashboardGeneralFilterUtils.getMoldFilter(moldPart.mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			subBuilder.and(moldPart.mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		builder.and(statisticsPart.partId.in(JPAExpressions.selectDistinct(moldPart.partId)
							.from(moldPart)
							.where(subBuilder)));

		if(!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(statisticsPart.statistics.day.gt(DateUtils.getDate(Instant.now().minus(12, ChronoUnit.DAYS), "yyyyMMdd"))
					.and(statisticsPart.statistics.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(statisticsPart.statistics.week.gt(DateUtils.getYearWeek(Instant.now().minus(12 * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
					.and(statisticsPart.statistics.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(statisticsPart.statistics.month.gt(DateUtils.getYearMonth(Instant.now().minus(12 * 30, ChronoUnit.DAYS), "yyyyMMdd"))
					.and(statisticsPart.statistics.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		builder.and(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
		builder.and(statisticsPart.statistics.shotCount.isNotNull().and(statisticsPart.statistics.shotCount.gt(0)));
		builder.and(statisticsPart.partId.in(dashboardGeneralFilterUtils.getPartIds_Old()));

		if(partIds == null || partIds.size() == 0){
			JPQLQuery query = from(statisticsPart)
					.innerJoin(moldPart).on(moldPart.partId.eq(statisticsPart.partId))
					.where(builder)
					.select(statisticsPart.id).distinct();
			List<Long> statisticsPartIds = query.fetch();
			partStatisticsPartIdsList.add(new PartStatisticsPartIds(0L, statisticsPartIds));
		}else{
			builder.and(moldPart.partId.in(partIds));
			JPQLQuery query = from(statisticsPart)
					.innerJoin(moldPart).on(moldPart.partId.eq(statisticsPart.partId))
					.where(builder)
					.select(Projections.constructor(PartStatisticsPartId.class, moldPart.partId, statisticsPart.id))
					.distinct();
			List<PartStatisticsPartId> partStatisticsPartIdList = query.fetch();
			if(partStatisticsPartIdList != null && partStatisticsPartIdList.size() > 0){
				partStatisticsPartIdList.forEach(partStatisticsPartId -> {
					PartStatisticsPartIds exist = partStatisticsPartIdsList.stream().filter(x -> x.getPartId().equals(partStatisticsPartId.getPartId())).findAny().orElse(null);
					if(exist == null){
						exist = new PartStatisticsPartIds(partStatisticsPartId.getPartId(), new ArrayList<>());
						exist.getStatisticsPartIds().add(partStatisticsPartId.getStatisticsPartId());
						partStatisticsPartIdsList.add(exist);
					}else {
						exist.getStatisticsPartIds().add(partStatisticsPartId.getStatisticsPartId());
					}
				});
			}
		}

		return partStatisticsPartIdsList;
	}

	@Override
	public List<PartStatisticsPartIds> findPartStatisticsData(TabbedOverviewGeneralFilterPayload payload, List<Long> partIds, Frequent frequent) {
		List<PartStatisticsPartIds> partStatisticsPartIdsList = new ArrayList<>();
		QMoldPart moldPart = QMoldPart.moldPart;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		BooleanBuilder builder = new BooleanBuilder();

		BooleanBuilder subBuilder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			subBuilder.and(moldPart.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		subBuilder.and(payload.getMoldFilter(moldPart.mold));
		builder.and(statisticsPart.partId.in(JPAExpressions.selectDistinct(moldPart.partId)
				.from(moldPart)
				.where(subBuilder)));

		if(!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(statisticsPart.statistics.day.gt(DateUtils.getDate(Instant.now().minus(12, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statisticsPart.statistics.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(statisticsPart.statistics.week.gt(DateUtils.getYearWeek(Instant.now().minus(12 * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
						.and(statisticsPart.statistics.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(statisticsPart.statistics.month.gt(DateUtils.getYearMonth(Instant.now().minus(12 * 30, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statisticsPart.statistics.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		builder.and(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
		builder.and(statisticsPart.statistics.shotCount.isNotNull().and(statisticsPart.statistics.shotCount.gt(0)));

		if(partIds == null || partIds.size() == 0){
			JPQLQuery query = from(statisticsPart)
					.innerJoin(moldPart).on(moldPart.partId.eq(statisticsPart.partId))
					.where(builder)
					.select(statisticsPart.id).distinct();
			List<Long> statisticsPartIds = query.fetch();
			partStatisticsPartIdsList.add(new PartStatisticsPartIds(0L, statisticsPartIds));
		}else{
			builder.and(moldPart.partId.in(partIds));
			JPQLQuery query = from(statisticsPart)
					.innerJoin(moldPart).on(moldPart.partId.eq(statisticsPart.partId))
					.where(builder)
					.select(Projections.constructor(PartStatisticsPartId.class, moldPart.partId, statisticsPart.id))
					.distinct();
			List<PartStatisticsPartId> partStatisticsPartIdList = query.fetch();
			if(partStatisticsPartIdList != null && partStatisticsPartIdList.size() > 0){
				partStatisticsPartIdList.forEach(partStatisticsPartId -> {
					PartStatisticsPartIds exist = partStatisticsPartIdsList.stream().filter(x -> x.getPartId().equals(partStatisticsPartId.getPartId())).findAny().orElse(null);
					if(exist == null){
						exist = new PartStatisticsPartIds(partStatisticsPartId.getPartId(), new ArrayList<>());
						exist.getStatisticsPartIds().add(partStatisticsPartId.getStatisticsPartId());
						partStatisticsPartIdsList.add(exist);
					}else {
						exist.getStatisticsPartIds().add(partStatisticsPartId.getStatisticsPartId());
					}
				});
			}
		}

		return partStatisticsPartIdsList;
	}

	@Override
	public List<MaintenanceTimeData> findMaintenanceTimeData(DashboardFilterPayload payload){
		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldMaintenance.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(moldMaintenance.mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(moldMaintenance.startTime.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		builder.and(moldMaintenance.startTime.isNotNull()).and(moldMaintenance.endTime.isNotNull());
		JPQLQuery query = from(moldMaintenance)
				.where(builder)
				.select(Projections.constructor(MaintenanceTimeData.class, moldMaintenance.startTime, moldMaintenance.endTime));
		List<MaintenanceTimeData> result = query.fetch();

		QMoldCorrective moldCorrective = QMoldCorrective.moldCorrective;
		builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(moldCorrective.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())
//					.or(moldCorrective.mold.createdBy.eq(SecurityUtils.getUserId()))
			);
		}
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(moldCorrective.mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(moldCorrective.startTime.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}
		builder.and(moldCorrective.startTime.isNotNull()).and(moldCorrective.endTime.isNotNull());
		query = from(moldCorrective)
				.where(builder)
				.select(Projections.constructor(MaintenanceTimeData.class, moldCorrective.startTime, moldCorrective.endTime));

		result.addAll(query.fetch());
		return result;
	}

	@Override
	public MoldReportDataPage findReportData(DashboardFilterPayload payload, Pageable pageable, ReportType type){
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCounter counter = QCounter.counter;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload.getStartTime() != null && payload.getEndTime() != null){
			builder.and(statistics.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
		}

		// [2021-02-26] Ignore shot with CT == 0
		builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

		builder.and(shotCount.isNotNull().and(shotCount.gt(0)));
		builder.and(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
//		if (env == null || !(env.equals("local") || env.equals("dev"))) {
//			DateTemplate activatedAt = counter.activatedAt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y-%m-%d %h:%:i%s')", counter.activatedAt);
//			DateTemplate rt = statistics.rt == null ? null : Expressions.dateTemplate(LocalDate.class, "STR_TO_DATE({0}, '%Y%m%d%h%i%s')", statistics.rt);
//			builder.and(activatedAt.lt(rt));
//		}

		NumberPath<Long> sortValue = Expressions.numberPath(Long.class, "value");

		NumberExpression<Long> value = statistics.uptimeSeconds.sum();

		if(type.equals(ReportType.OEE)){
			value = statistics.uptimeSeconds.sum().multiply((ct.divide(10).multiply(shotCount)).sum()); // convert ct 100ms -> s
		}

//        JPQLQuery subQuery = from(moldPart)
//                .select(Projections.constructor(MoldReportData.class, moldPart.moldId, moldPart.cavity.sum())) // sum as producedQuantity
//                .groupBy(moldPart.moldId);
//		List<MoldReportData> sumCavities = subQuery.fetch();

		JPQLQuery query = from(mold)
				.innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
				.leftJoin(statistics).on(statistics.moldId.eq(mold.id))
				.leftJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
				.leftJoin(counter).on(statistics.ci.eq(counter.equipmentCode))
				.where(builder)
				.groupBy(mold.id)
				.orderBy(sortValue.desc())
				.select(Projections.constructor(MoldReportData.class, mold.id, mold.equipmentCode, (shotCount.multiply(statisticsPart.cavity)).sum().coalesce(0),
						mold.createdAt.coalesce(Instant.now().minus(7, ChronoUnit.DAYS)), value.as(sortValue)))
				.limit(pageable.getPageSize())
				.offset(pageable.getOffset());
		List<MoldReportData> data = query.fetch();
//        data.forEach(record -> {
//            MoldReportData matchData = sumCavities.stream().filter(x -> x.getMoldId().equals(record.getMoldId())).findAny().orElse(null);
//            Integer cavity = matchData == null ? 0 : matchData.getQuantityProduced();
//            record.setQuantityProduced(record.getQuantityProduced() * cavity);
//        });

		if(payload != null && payload.getStartTime() != null && payload.getEndTime() != null){
			data.forEach(moldReportData -> {
				if(moldReportData.getMoldCreatedAt() < payload.getStartTime()){
					moldReportData.setAliveTime(payload.getEndTime() - payload.getStartTime());
				}else{
					moldReportData.setAliveTime(payload.getEndTime() - moldReportData.getMoldCreatedAt());
				}
			});
		}

		JPQLQuery getTotalQuery = from(mold)
				.innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
				.innerJoin(statistics).on(statistics.moldId.eq(mold.id))
				.where(builder)
				.groupBy(mold.id);
		long total = getTotalQuery.fetchCount();
		return new MoldReportDataPage(total, data);
	}

	public Query buildQueryReportDataWithRateCapacity(DashboardFilterPayload payload, Pageable pageable,boolean isCount){
		Map<String,Object> paramMap = new HashMap<>();
		StringBuilder queryBuilder = new StringBuilder();

		String startDateStr = null;
		String endDateStr = null;
		Long duaTime = null;


		if (payload.getStartTime() != null && payload.getEndTime() != null) {
			startDateStr = DateUtils.getDateTime(Instant.ofEpochSecond(payload.getStartTime()));
			endDateStr = DateUtils.getDateTime(Instant.ofEpochSecond(payload.getEndTime()));
			duaTime = payload.getEndTime() - payload.getStartTime();
		}
		paramMap.put("startDate", startDateStr);
		paramMap.put("endDate", endDateStr);
		paramMap.put("duaTime", duaTime);
		if(isCount){
			queryBuilder.append("SELECT count(*) FROM (");
		}
		queryBuilder.append("SELECT \n" +
				"    mold0_.ID AS moldId,\n" +
				"    mold0_.EQUIPMENT_CODE AS moldCode,\n" +
				"    COALESCE(SUM(statistics2_.SHOT_COUNT * statistics3_.CAVITY),\n" +
				"            0) AS quantityProduced,\n" +
				"    COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)) AS moldCreatedAt,\n" +
				"    SUM(statistics2_.UPTIME_SECONDS) AS value,\n" +

				" IF((:startDate != NULL AND :endDate != NULL),\n" +
				"        IF(mold0_.CREATED_AT < TIMESTAMP(:startDate),\n" +
				"            :duaTime,\n" +
				"            TIMESTAMPDIFF(SECOND,\n" +
				"                TIMESTAMP(:endDate),\n" +
				"                mold0_.CREATED_AT)),\n" +
				"        TIMESTAMPDIFF(SECOND,\n" +
				"            COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)),\n" +
				"            NOW())) AS aliveTime," +

				"    SUM(statistics2_.UPTIME_SECONDS) * 100 / IF((:startDate != NULL AND :endDate != NULL),\n" +
				"        IF(mold0_.CREATED_AT < TIMESTAMP(:startDate),\n" +
				"            :duaTime,\n" +
				"            TIMESTAMPDIFF(SECOND,\n" +
				"                TIMESTAMP(:endDate),\n" +
				"                mold0_.CREATED_AT)),\n" +
				"        TIMESTAMPDIFF(SECOND,\n" +
				"            COALESCE(mold0_.CREATED_AT, ADDDATE(NOW(), - 7)),\n" +
				"            NOW())) AS percentage\n" +
				"FROM\n" +
				"    MOLD mold0_\n" +
				"        INNER JOIN\n" +
				"    MOLD_PART moldpart1_ ON (moldpart1_.MOLD_ID = mold0_.ID)\n" +
				"        LEFT OUTER JOIN\n" +
				"    STATISTICS statistics2_ ON (statistics2_.MOLD_ID = mold0_.ID)\n" +
				"        LEFT OUTER JOIN\n" +
				"    STATISTICS_PART statistics3_ ON (statistics2_.ID = statistics3_.STATISTICS_ID)\n" +
				"WHERE\n" +
				"    1 = 1\n");

		if (payload.getOps() != null && !payload.getOps().isEmpty()) {
			queryBuilder.append("        AND ( mold0_.OPERATING_STATUS IN :ops)\n");
			paramMap.put("ops", payload.getOps().stream().map(p -> p.getCode()).collect(Collectors.toList()));
		}
		if (payload.getStartTime() != null && payload.getEndTime() != null) {
			queryBuilder.append("        AND ( statistics2_.CREATED_AT BETWEEN TIMESTAMP(:startDate) AND TIMESTAMP(:endDate))\n");
		}
		queryBuilder.append(" GROUP BY mold0_.ID\n");
		if (!StringUtils.isEmpty(payload.getRateCapacity()) && !payload.getRateCapacity().equals(FrequentUsage.ALL.getCode())) {
			if (payload.getRateCapacity().equals(FrequentUsage.FREQUENTLY.getCode())) {
//				rateFrom = 50d;
				queryBuilder.append(" HAVING (percentage >= 50) ");
			} else if (payload.getRateCapacity().equals(FrequentUsage.OCCASIONALLY.getCode())) {
//				rateFrom = 20d;
//				rateTo = 50d;
				queryBuilder.append(" HAVING  ( percentage < 50 and percentage >= 20) ");
			} else if (payload.getRateCapacity().equals(FrequentUsage.RARELY.getCode())) {
//				rarelyFrom = 0.5d;
				queryBuilder.append(" HAVING  ( percentage < 20 and percentage > 0) ");

			} else if (payload.getRateCapacity().equals(FrequentUsage.NEVER.getCode())) {
//				isNever = true;
				queryBuilder.append(" HAVING (percentage = 0 or percentage is null) ");
			}
		}

		if (!isCount) {
			Integer pageFrom = pageable.getPageNumber() * pageable.getPageSize();
			Integer pageSize = pageable.getPageSize();
			queryBuilder.append("  order by value DESC   LIMIT :pageFrom, :pageSize ");
			paramMap.put("pageFrom", pageFrom);
			paramMap.put("pageSize", pageSize);
		}
		if(isCount){
			queryBuilder.append(" ) AS report ");
		}
		Query query = getEntityManager().createNativeQuery(queryBuilder.toString());
		for(String key:paramMap.keySet()){
			query.setParameter(key,paramMap.get(key));
		}
		return query;
	}
	@Override
	public Page<MoldReportData> findReportDataWithRateCapacity(DashboardFilterPayload payload, Pageable pageable){
		long total = 0;
//		List<MoldReportData> data = new ArrayList<>();

		Query query = buildQueryReportDataWithRateCapacity(payload, pageable, false);
		List<Object[]> objects= query.getResultList();
		List<MoldReportData> moldReportDataList = objects.stream().map(m -> new MoldReportData(m)).collect(Collectors.toList());

		Query queryCount = buildQueryReportDataWithRateCapacity(payload, pageable, true);
		total = NumberUtils.convertToLong(queryCount.getSingleResult());

		Page<MoldReportData> result = new PageImpl<>(moldReportDataList, pageable, total);

		return result;

	}
	public List<Long> getMoldIdsSubQuery(){
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()))
				.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
		JPQLQuery query = from(mold)
				.select(mold.id)
				.where(builder);
		return query.fetch();
	}

	@Override
	public List<MoldCycleTimeExtraData> findMoldCycleTimeExtraData(Predicate predicate, Pageable pageable){
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			directions[0] = order.getDirection();
		});

		QMoldCycleTime moldCycleTime = QMoldCycleTime.moldCycleTime;
		JPQLQuery query = from(moldCycleTime)
				.where(predicate);
		NumberExpression variance = (moldCycleTime.cycleTime.add(moldCycleTime.contractedCycleTime.negate()).multiply(100)).divide(moldCycleTime.contractedCycleTime);
		OrderSpecifier order = variance.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = variance.asc();
		}

		query.select(Projections.constructor(MoldCycleTimeExtraData.class, moldCycleTime, variance));
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldEfficiencyExtraData> findMoldEfficiencyExtraData(Predicate predicate, Pageable pageable){
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			directions[0] = order.getDirection();
		});

		QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;
		JPQLQuery query = from(moldEfficiency)
				.where(predicate);
		NumberExpression efficiency= new CaseBuilder()
				.when(moldEfficiency.efficiency.gt(100)).then(100d)
				.when(moldEfficiency.efficiency.lt(-100)).then(-100d)
				.otherwise(moldEfficiency.efficiency);
		NumberExpression baseEfficiency= new CaseBuilder()
				.when(moldEfficiency.baseEfficiency.isNull().or(moldEfficiency.baseEfficiency.eq(0d))).then(1d)
				.otherwise(moldEfficiency.baseEfficiency);

//		NumberExpression variance = (moldEfficiency.efficiency.add(moldEfficiency.baseEfficiency.negate()).multiply(100)).divide(moldEfficiency.baseEfficiency);
		NumberExpression variance = (efficiency.add(baseEfficiency.negate()).multiply(100)).divide(baseEfficiency);
		OrderSpecifier order = variance.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = variance.asc();
		}

		query.select(Projections.constructor(MoldEfficiencyExtraData.class, moldEfficiency, variance));
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldMaintenanceExtraData> findMoldMaintenanceExtraData(Predicate predicate, Pageable pageable){
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
		JPQLQuery query = from(moldMaintenance);

		NumberExpression pmCheckPointValue=new CaseBuilder()
				.when(moldMaintenance.periodStart.isNotNull().and(moldMaintenance.mold.preventUpcoming.isNotNull()))
				.then(moldMaintenance.periodStart.add(moldMaintenance.mold.preventUpcoming))
				.when(moldMaintenance.periodEnd.isNotNull().and(moldMaintenance.mold.preventOverdue.isNotNull()))
				.then(moldMaintenance.periodEnd.subtract(moldMaintenance.mold.preventOverdue))
				.when(moldMaintenance.preventCycle.isNotNull())
				.then(moldMaintenance.preventCycle)
				.otherwise(1);
		NumberExpression sortValue;
		if(properties[0].equalsIgnoreCase("executionRate")){
			QMoldMaintenance moldMaintenanceAll = new QMoldMaintenance("moldMaintenanceAll");
			QMoldMaintenance moldMaintenanceDone = new QMoldMaintenance("moldMaintenanceDone");

			NumberExpression numMaintenaced=moldMaintenanceAll.id.countDistinct();
			NumberExpression numDone=moldMaintenanceDone.id.countDistinct();
			NumberExpression executionRate= (NumberExpression) new CaseBuilder()
					.when(numMaintenaced.gt(0))
					.then(numDone.floatValue().divide(numMaintenaced))
					.otherwise(0);

			sortValue = executionRate;
//			sortValue = moldMaintenance.mold.maintenanceCount.floatValue().divide(moldMaintenance.moldId.count());
			OrderSpecifier countOrder = sortValue.desc();
			if(directions[0].equals(Sort.Direction.ASC)){
				countOrder = sortValue.asc();
			}

			JPQLQuery countQuery = from(moldMaintenance)
					.leftJoin(moldMaintenanceAll).on(moldMaintenanceAll.maintenanceStatus.in(MaintenanceStatus.DONE, MaintenanceStatus.OVERDUE)
							.and(moldMaintenance.mold.eq(moldMaintenanceAll.mold)))
					.leftJoin(moldMaintenanceDone).on(moldMaintenanceDone.maintenanceStatus.in(MaintenanceStatus.DONE)
							.and(moldMaintenance.mold.eq(moldMaintenanceDone.mold)))
					.select(Projections.constructor(MoldMaintenanceMoldIdExecutionRate.class,moldMaintenance.moldId, sortValue))
					.where(moldMaintenance.moldId.in(
							from(moldMaintenance)
									.select(moldMaintenance.moldId)
									.where(predicate)
					))
					.groupBy(moldMaintenance.moldId)
					.orderBy(countOrder)
					.limit(pageable.getPageSize())
					.offset(pageable.getOffset());
/*

			JPQLQuery countQuery = from(moldMaintenance)
					.select(Projections.constructor(MoldMaintenanceMoldIdExecutionRate.class,moldMaintenance.moldId, sortValue))
					.where(moldMaintenance.moldId.in(
							from(moldMaintenance)
									.select(moldMaintenance.moldId)
									.where(predicate)
					))
					.groupBy(moldMaintenance.moldId)
					.orderBy(countOrder)
					.limit(pageable.getPageSize())
					.offset(pageable.getOffset());
*/

			List<MoldMaintenanceMoldIdExecutionRate> moldMaintenanceMoldIdExecutionRates = countQuery.fetch();
			List<Long> moldIds = moldMaintenanceMoldIdExecutionRates.stream().map(x -> x.getMoldId()).collect(Collectors.toList());

			// CASE WHEN THEN ELSE
			CaseForEqBuilder<Long>.Cases<Long, NumberExpression<Long>> cases = moldMaintenance.moldId.when(0L).then(-1L);
			for(int i = 0; i < moldIds.size(); i++){
				cases.when(moldIds.get(i)).then(Long.valueOf(i));
			}
			NumberExpression<Long> guidExpression = cases.otherwise(-1L);

			BooleanBuilder builder = new BooleanBuilder(predicate);
			builder.and(moldMaintenance.moldId.in(moldIds));

			query.select(Projections.constructor(MoldMaintenanceExtraData.class, moldMaintenance));
			query.where(builder);
			query.orderBy(guidExpression.asc());
			return query.fetch();
		}else if(properties[0].equalsIgnoreCase("lastShotCheckpoint")){
			sortValue = moldMaintenance.mold.lastShot.divide(pmCheckPointValue);
		}else if(properties[0].equalsIgnoreCase("utilNextPm")){
			sortValue = moldMaintenance.lastShotMade.divide(moldMaintenance.mold.preventCycle.coalesce(1));
		}else if(properties[0].equalsIgnoreCase("shotUntilNextPM")){
			sortValue = moldMaintenance.preventCycle.subtract(moldMaintenance.lastShotMade);
		}else{
			sortValue = pmCheckPointValue;
//			sortValue = ((moldMaintenance.mold.lastShot.add(moldMaintenance.preventUpcoming).add(moldMaintenance.preventCycle.negate())).divide(moldMaintenance.preventCycle).floor().add(1)).multiply(moldMaintenance.preventCycle);
		}
		OrderSpecifier order = sortValue.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = sortValue.asc();
		}
		query.where(predicate);
		query.select(Projections.constructor(MoldMaintenanceExtraData.class, moldMaintenance, sortValue));
		if (properties[0].equalsIgnoreCase("dueDate")) {
			if (directions[0].equals(Sort.Direction.ASC)) {

				query.orderBy(moldMaintenance.maintenanceStatus.asc(), moldMaintenance.dueDate.asc());
			} else
				query.orderBy(moldMaintenance.maintenanceStatus.desc(), moldMaintenance.dueDate.desc());
		} else
			query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldMaintenanceExtraData> findMoldMaintenanceExtraDataForMoldTable(Predicate predicate, Pageable pageable){
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		QMold mold = QMold.mold;
		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
		JPQLQuery query = from(mold).leftJoin(moldMaintenance).on(moldMaintenance.moldId.eq(mold.id)
			.and(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.UPCOMING)
				.or(moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.OVERDUE))).and(moldMaintenance.latest.isTrue()
			));

		NumberExpression pmCheckPointValue=new CaseBuilder()
				.when(moldMaintenance.periodStart.isNotNull().and(moldMaintenance.mold.preventUpcoming.isNotNull()))
				.then(moldMaintenance.periodStart.add(moldMaintenance.mold.preventUpcoming))
				.when(moldMaintenance.periodEnd.isNotNull().and(moldMaintenance.mold.preventOverdue.isNotNull()))
				.then(moldMaintenance.periodEnd.subtract(moldMaintenance.mold.preventOverdue))
				.when(moldMaintenance.preventCycle.isNotNull())
				.then(moldMaintenance.preventCycle)
				.otherwise(1);
		NumberExpression sortValue;

		if(properties[0].equalsIgnoreCase("lastShotCheckpoint")){
			sortValue = moldMaintenance.mold.lastShot.divide(pmCheckPointValue);
		}else if(properties[0].equalsIgnoreCase("utilNextPm")){
			sortValue = moldMaintenance.lastShotMade.divide(moldMaintenance.mold.preventCycle.coalesce(1));
/*
		}else if(properties[0].equalsIgnoreCase("shotUntilNextPM")){
			sortValue = moldMaintenance.preventCycle.subtract(moldMaintenance.lastShotMade);
*/
		}else{
			sortValue = pmCheckPointValue;
		}
		OrderSpecifier order = sortValue.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = sortValue.asc();
		}
		query.where(predicate);
		query.select(Projections.constructor(MoldMaintenanceExtraData.class, moldMaintenance, mold));

		if (properties[0].equalsIgnoreCase("dueDate")) {
			if (directions[0].equals(Sort.Direction.ASC)) {

				query.orderBy(moldMaintenance.maintenanceStatus.asc(), moldMaintenance.dueDate.asc());
			} else
				query.orderBy(moldMaintenance.maintenanceStatus.desc(), moldMaintenance.dueDate.desc());
		} else
			query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldExtraData> findMoldExtraData(Predicate predicate, Pageable pageable){
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;

		JPQLQuery query = from(mold)
				.leftJoin(moldPart).on(mold.id.eq(moldPart.moldId));
		BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
		NumberExpression numCavity= (new CaseBuilder().when(moldPart.cavity.goe(0)).then(moldPart.cavity).otherwise(0)).sum();
		booleanBuilder.and((moldPart.id.in(JPAExpressions
							.select(moldPart.id.min())
							.from(moldPart).having(numCavity.eq(0) )
							.groupBy(moldPart.moldId))
				.or(moldPart.id.in(JPAExpressions
								.select(moldPart.id.min())
								.from(moldPart)
								.where(moldPart.cavity.gt(0))
								.groupBy(moldPart.moldId))))
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		query.where(booleanBuilder);
		StringExpression expression = null;
		if("part".equals(properties[0])) {
			expression = moldPart.part.partCode;
		} else if("productAndCategory".equals(properties[0])) {
			expression = moldPart.part.category.name;
		}

		if (expression != null) {
			OrderSpecifier order = expression.desc();
			if(directions[0].equals(Sort.Direction.ASC)){
				order = expression.asc();
			}

			query.orderBy(order);
		}

		query.select(Projections.constructor(MoldExtraData.class, mold, moldPart.part.partCode));
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldCustomFieldValue> findMoldCustomFieldValue(Predicate predicate, Pageable pageable)
	{
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		Long customFieldId = Long.valueOf((properties[0].split("-"))[1]);

		QMold mold = QMold.mold;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;

		JPQLQuery query = from(mold)
				.leftJoin(customFieldValue).on(mold.id.eq(customFieldValue.objectId));
		BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
		booleanBuilder.and(customFieldValue.id
				.in(JPAExpressions
					.select(customFieldValue.id)
					.from(customFieldValue)
                	.where(customFieldValue.customField.id.eq(customFieldId)))
				.or(customFieldValue.isNull()));

		query.where(booleanBuilder);

		StringExpression expression = customFieldValue.value;
		OrderSpecifier order = expression.desc();
		StringExpression expression2 = mold.equipmentCode;
		OrderSpecifier order2 = expression2.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}

		query.orderBy(order, order2);
		query.select(Projections.constructor(MoldCustomFieldValue.class, mold, customFieldValue.id));
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MiniComponentData> findExistsMoldCodes(List<String> moldCodes){
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold)
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode))
				.where(mold.equipmentCode.in(moldCodes)
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())));
		return query.fetch();
	}

	@Override
	public List<MiniComponentData> findAllMoldIdMoldCodes(List<Long> moldIds){
		QMold mold = QMold.mold;

		if(moldIds == null || moldIds.size() == 0) {
			JPQLQuery query = from(mold)
					.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));
			BooleanBuilder builder = new BooleanBuilder();
			builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
			if (AccessControlUtils.isAccessFilterRequired()) {
				builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
			}
			query.where(builder);
			return query.fetch();
		}
		JPQLQuery query = from(mold)
				.where(mold.id.in(moldIds))
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));
		return query.fetch();
	}

	@Override
	public Page<MiniComponentData> findMoldLiteData(String code, Pageable pageable) {
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
		if (!StringUtils.isEmpty(code)) {
			builder.and(mold.equipmentCode.containsIgnoreCase(code));
		}
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		JPQLQuery query = from(mold)
				.where(builder)
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));
		long total = query.fetchCount();

		if (pageable != null) {
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}
		query.orderBy(mold.equipmentCode.asc());

		List<MiniComponentData> list = query.fetch();
		return new PageImpl<>(list, pageable, total);
	}

	@Override
	public List<MiniComponentData> findAllMoldIdMoldCodesByGeneralFilter(List<Long> moldIds){
		QMold mold = QMold.mold;

		if(moldIds == null || moldIds.size() == 0) {
			JPQLQuery query = from(mold)
					.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));
			BooleanBuilder builder = new BooleanBuilder();
			builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
			builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
			if (AccessControlUtils.isAccessFilterRequired()) {
				builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
			}
			query.where(builder);
			return query.fetch();
		}
		JPQLQuery query = from(mold)
				.where(mold.id.in(moldIds))
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));
		return query.fetch();
	}

/*
	private BooleanBuilder filterByDashboardGeneralFilter() {
		BooleanBuilder builder = new BooleanBuilder();
		QMold mold = QMold.mold;
		if (dashboardGeneralFilterUtils.checkExistedGeneralFilter()) {
			//get dashboard general filter
			List<Long> supplierIds = dashboardGeneralFilterUtils.getSupplierIds();
			List<Long> toolMakerIds = dashboardGeneralFilterUtils.getToolMakerIds();
			List<Long> locationIds = dashboardGeneralFilterUtils.getLocationIds();

			if (CollectionUtils.isNotEmpty(locationIds)
					&& CollectionUtils.isNotEmpty(supplierIds)
					&& CollectionUtils.isNotEmpty(toolMakerIds)) {// location + supplier + toolmaker
				builder.and(mold.location.id.in(locationIds)
						.or(mold.supplier.id.in(supplierIds))
						.or(mold.toolMaker.id.in(toolMakerIds)));
			} else if (CollectionUtils.isNotEmpty(locationIds)
					&& CollectionUtils.isEmpty(supplierIds)
					&& CollectionUtils.isNotEmpty(toolMakerIds)) {//location + toolmaker
				builder.and(mold.location.id.in(locationIds)
						.or(mold.toolMaker.id.in(toolMakerIds)));
			} else if (CollectionUtils.isNotEmpty(locationIds)
					&& CollectionUtils.isNotEmpty(supplierIds)
					&& CollectionUtils.isEmpty(toolMakerIds)) {//location + supplier
				builder.and(mold.location.id.in(locationIds)
						.or(mold.supplier.id.in(supplierIds)));
			} else if (CollectionUtils.isNotEmpty(locationIds)
					&& CollectionUtils.isEmpty(supplierIds)
					&& CollectionUtils.isEmpty(toolMakerIds)) {//location
				builder.and(mold.location.id.in(locationIds));
			} else if (CollectionUtils.isEmpty(locationIds)
					&& CollectionUtils.isNotEmpty(supplierIds)
					&& CollectionUtils.isNotEmpty(toolMakerIds)) {//supplier + toolmaker
				builder.and(mold.supplier.id.in(supplierIds)
						.or(mold.toolMaker.id.in(toolMakerIds)));
			} else if (CollectionUtils.isEmpty(locationIds)
					&& CollectionUtils.isEmpty(supplierIds)
					&& CollectionUtils.isNotEmpty(toolMakerIds)) {//toolmaker
				builder.and(mold.toolMaker.id.in(toolMakerIds));
			} else if (CollectionUtils.isEmpty(locationIds)
					&& CollectionUtils.isNotEmpty(supplierIds)
					&& CollectionUtils.isEmpty(toolMakerIds)) {//supplier
				builder.and(mold.supplier.id.in(supplierIds));
			} else if (CollectionUtils.isEmpty(locationIds)
					&& CollectionUtils.isEmpty(supplierIds)
					&& CollectionUtils.isEmpty(toolMakerIds)) {//none
				builder.and(mold.id.isNull());//trick for none
			}
		}

		return builder;
	}
*/

	@Override
	public List<Long> findMoldIdsFromProductivitySearchPayload(ProductivitySearchPayload payload){
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
//		QContinent continent = QContinent.continent;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(mold.operatingStatus.isNotNull());
		builder.and(mold.location.company.isEmoldino.isFalse());
		if(payload.getPartId() != null){
			builder.and(moldPart.partId.eq(payload.getPartId()));
		}
		if(payload.getCountryCode() != null && payload.getCountryCode().size() > 0){
			builder.and(mold.location.countryCode.in(payload.getCountryCode()));
		}
		if(payload.getSupplierIds() != null && payload.getSupplierIds().size() > 0){
			builder.and(mold.location.company.id.in(payload.getSupplierIds()));
		}
		if(payload.getOperatingStatus() != null){
			builder.and(mold.operatingStatus.in(payload.getOperatingStatus()));
		}
		if(payload.getMoldStatusList() != null) {
			BooleanBuilder statusBuilder =  new BooleanBuilder();
			for (MoldStatus moldStatus : payload.getMoldStatusList()) {
				switch (moldStatus) {
					case IN_PRODUCTION:{
						statusBuilder.or(new BooleanBuilder().and(mold.operatingStatus.eq(OperatingStatus.WORKING)
										.and(mold.counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case IDLE:{
						statusBuilder.or(new BooleanBuilder().and(mold.operatingStatus.eq(OperatingStatus.IDLE)
								.and(mold.counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case NOT_WORKING: {
						statusBuilder.or(new BooleanBuilder().and(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING)
								.and(mold.counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case SENSOR_OFFLINE:{
						statusBuilder.or(new BooleanBuilder().and(mold.counter.operatingStatus.eq(OperatingStatus.DISCONNECTED)
								.and(mold.counter.equipmentStatus.ne(EquipmentStatus.DETACHED))));
						break;
					}
					case SENSOR_DETACHED:{
						statusBuilder.or(mold.counter.equipmentStatus.eq(EquipmentStatus.DETACHED));
						break;
					}
				}
			}
			builder.and(statusBuilder);
		}
		if(payload.isCheckForNewCounter()){
			builder.and(mold.counter.equipmentCode.startsWithIgnoreCase("NCM").or(mold.counter.equipmentCode.startsWithIgnoreCase("EMA")));
		}
		if(payload.getDuration()!=null && !StringUtils.isEmpty(payload.getStartDate())){
			String startDate=payload.getStartDate();
			if (!startDate.endsWith("000000") && startDate.length() < 14) startDate += "000000";
			builder.and(mold.operatedStartAt.loe(DateUtils.getInstant(startDate,DateUtils.DEFAULT_DATE_FORMAT)));
		}
		if(CompareType.TOOLMAKER.equals(payload.getCompareBy())){
			builder.and(mold.toolMaker.isNotNull()).and(mold.toolMaker.isEmoldino.isFalse());
		}

//		if(payload.getContinent() != null){
//			builder.and(continent.continentName.eq(payload.getContinent()));
//		}

//		builder.and(mold.operatingStatus.isNotNull());
		JPQLQuery query = from(mold)
				.innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
//				.leftJoin(continent).on(mold.location.countryCode.eq(continent.countryCode))
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
				.select(mold.id).distinct();

		return query.fetch();
	}

//	@Override
//	public JPQLQuery<Long> buildMoldIdsSubqueryBySearchPayload(FltSearchPayload payload){
//		QMold mold = QMold.mold;
//		QMoldPart moldPart = QMoldPart.moldPart;
//
//		BooleanBuilder builder = new BooleanBuilder();
//		if (AccessControlUtils.isAccessFilterRequired()) {
//			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
//		}
//		builder.and(mold.operatingStatus.isNotNull());
//		builder.and(mold.location.company.isEmoldino.isFalse());
//		if(payload.getPartId() != null){
//			builder.and(moldPart.partId.eq(payload.getPartId()));
//		}
//		if(payload.getSupplierIds() != null && payload.getSupplierIds().size() > 0){
//			builder.and(mold.location.company.id.in(payload.getSupplierIds()));
//		}
//		JPQLQuery<Long> query =  JPAExpressions.select(mold.id).from(mold)
//						.innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
//						.where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())));
//
//		return query;
//	}

	@Override
	public ProductivityOverviewData findMaxProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
//		QContinent continent = QContinent.continent;
		QStatistics statistics=QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder();
		JPQLQuery query = from(mold);

		NumberPath ct;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
		} else {
			ct = statistics.ct;
		}

		if(moldIds != null) builder.and(mold.id.in(moldIds));

		// [2021-08-25] Ignore tooling with CT == 0 in time
		BooleanBuilder builderStatistics = new BooleanBuilder();
		builderStatistics.and(ct.gt(0).or(statistics.firstData.isTrue()));
		if (payload.getStartDate() != null && payload.getEndDate() != null) {
			builderStatistics.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		} else if (payload.getEndDate() != null) {
			builderStatistics.and(statistics.day.loe(payload.getEndDate()));
		}
		if (payload.getEndDate() != null)
			builder.and(mold.id.in(
					(JPAExpressions
							.select(statistics.moldId)
							.from(statistics)
							.where(builderStatistics))
			));

		query.where(builder
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		NumberExpression<Integer> value;
		if(payload.getDuration() != null){

			value = mold.dailyMaxCapacity.multiply(payload.getDuration()).sum();

		}else{
			value = mold.passedDays
					.multiply(mold.dailyMaxCapacity)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7))
					.divide(7)
					.sum();
		}
		query.select(Projections.constructor(ProductivityOverviewData.class, value));
		ProductivityOverviewData result = (ProductivityOverviewData) query.fetchOne();
		if(result == null)
			result = ProductivityOverviewData.builder()
					.totalProductivity(0)
					.build();

		JPQLQuery countMoldQuery = from(mold)
				.innerJoin(moldPart).on(mold.id.eq(moldPart.moldId)).distinct()
//				.leftJoin(continent).on(mold.location.countryCode.eq(continent.countryCode))
				.where(builder)
				.groupBy(moldPart.moldId)
				.select(moldPart.moldId);
		Long moldCount = countMoldQuery.fetchCount();
		result.setMoldCount(moldCount);
		return result;
	}

	/**
	 *
	 * @param moldIds
	 * @param payload
	 * @param isDyson
	 * @param pageable
	 * @return
	 *  List<ToolingProductivityData>:
	 *  Long id: companyId, String code, String name,
	 *  Integer producedQuantity,
	 *  Double percentageProductivity,
	 *  Integer dailyMaxCapacity,
	 *  Integer maxCapacity
	 *  Integer availableDowntime
	 *  OR
	 *  Long moldId, String moldCode, Mold mold, String companyCode,
	 *  Integer producedQuantity,
	 *  Double percentageProductivity,
	 *  Integer dailyMaxCapacity,
	 *  Integer maxCapacity
	 *  Integer availableDowntime
	 */
	@Override
	public List<ToolingProductivityData> findToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson, Pageable pageable) {
		return (List<ToolingProductivityData>) findToolingProductivity(moldIds,payload,isDyson,pageable,false);
	}
	@Override
	public Long countToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson) {
		Long total= (Long) findToolingProductivity(moldIds,payload,isDyson,null,true);
		return total!=null? total:0L;
	}
//	@Override
	private Object findToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson, Pageable pageable, boolean isCount) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCompany company = QCompany.company;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();

		JPQLQuery query = from(mold);

		if (payload.getPartId() != null) {
			builder.and(statisticsPart.partId.eq(payload.getPartId()));
		}

		if (payload.getStartDate() != null && payload.getEndDate() != null) {
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		} else if (payload.getEndDate() != null) {
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		if(moldIds != null
				//&& moldIds.size() > 0
		){
			builder.and(mold.id.in(moldIds));
		}

		// [2021-02-16] Ignore shot with CT == 0
		builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

		query.where(builder
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		Expression<Integer> producedQuantity = (shotCount.multiply(statisticsPart.cavity)).sum().coalesce(0);

		Expression<Integer> maxCapacity;
		if(payload.getDuration() != null){
			maxCapacity = mold.dailyMaxCapacity.multiply(payload.getDuration());
		}else{
			maxCapacity = mold.passedDays
					.multiply(mold.dailyMaxCapacity)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7))
					.divide(7);
		}

		NumberExpression<Float> percentageProductivity = ((Coalesce<Integer>) producedQuantity).asNumber().floatValue().multiply(100).divide(maxCapacity);

		//

		query.innerJoin(statistics).on(statistics.moldId.eq(mold.id));
		query.innerJoin(statisticsPart).on(statisticsPart.statisticsId.eq(statistics.id));
		if(CompareType.SUPPLIER.equals(payload.getCompareBy())){
			query.where(builder.and(mold.location.company.isEmoldino.isFalse()));
			if(isCount)
				query.select(mold.location.company.id.countDistinct());
			else{
			query.groupBy(mold.location.company.id,mold.location.company.companyCode, mold.location.company.name);
			query.select(Projections.constructor(ToolingProductivityData.class, mold.location.company.id,mold.location.company.companyCode,  mold.location.company.name,
					((Coalesce<Integer>) producedQuantity).asNumber(), percentageProductivity, mold.dailyMaxCapacity.sum(), ((NumberExpression<Integer>) maxCapacity).sum()));
			}

		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.where(builder.and(mold.toolMaker.isEmoldino.isFalse()).and(mold.location.company.isEmoldino.isFalse()));
			if(isCount)
				query.select(mold.toolMaker.id.countDistinct());
			else{
			query.groupBy(mold.toolMaker.id,mold.toolMaker.companyCode, mold.toolMaker.name);
			query.select(Projections.constructor(ToolingProductivityData.class, mold.toolMaker.id,mold.toolMaker.companyCode, mold.toolMaker.name,
					((Coalesce<Integer>) producedQuantity).asNumber(), percentageProductivity, mold.dailyMaxCapacity.sum()
					, ((NumberExpression<Integer>) maxCapacity).sum()));
			}
		}
		else {
			query.innerJoin(company).on(mold.companyId.eq(company.id));

			if(isCount)
				query.select(mold.id.countDistinct());
			else{

				query.groupBy(mold.id);
				query.select(Projections.constructor(ToolingProductivityData.class, mold.id, mold.equipmentCode, mold,
						company.companyCode, producedQuantity, percentageProductivity, mold.dailyMaxCapacity, maxCapacity
				,statisticsPart.partId.countDistinct().intValue()));
			}
		}
		if(isCount){
			return (Long)query.fetchOne();
		}
		if(pageable != null) {
			Sort.Direction[] directions = {Sort.Direction.DESC};
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
			});
			if (directions[0].equals(Sort.Direction.ASC))
				query.orderBy(((Coalesce<Integer>) producedQuantity).asc());
			else
				query.orderBy(((Coalesce<Integer>) producedQuantity).desc());
			query.orderBy(mold.id.asc());
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}
		List<ToolingProductivityData> result=query.fetch();
		//get DailyMax capacity and Max capacity for group by supplier/tool maker
		//Re Calculation max capacity for supplier/toolmaker because join with statistics table is incorrect
		if (Arrays.asList(CompareType.SUPPLIER, CompareType.TOOLMAKER).contains(payload.getCompareBy())) {
			List<ToolingProductivityData> resultMax = findProductivityMaxCapacity(moldIds, payload, isDyson, null);
			result.forEach(r -> {
				resultMax.stream().forEach(rm -> {
					if (r.getId() != null && r.getId().equals(rm.getId())) {
						r.setDailyMaxCapacity(rm.getDailyMaxCapacity());
						r.setMaxCapacity(rm.getMaxCapacity());
					}
				});
			});
		}
		//add AvailableDowntime
		List<Long> idRes=result.stream().map(r->r.getId()).collect(Collectors.toList());
		List<MoldCapacityReportData> availableDowntimeDataList=findAvailableDowntime( moldIds,payload, idRes);
		result.forEach(r->{
			availableDowntimeDataList.forEach(av->{
				if (r.getId() != null && r.getId().equals(av.getId())) {
					r.setAvailableDowntime(av.getData());
				}
			});
		});
		return result;
	}


	@Override
	public List<PartProductionData> findPartProduction(List<Long> moldIds, Long partId, String startDate, String endDate, CompareType compareType, List<Long> companyIds,
			Pageable pageable) {
		ProductivitySearchPayload payload = new ProductivitySearchPayload();
		payload.setPartId(partId);
		payload.setStartDate(startDate);
		payload.setEndDate(endDate);
		payload.setCompareBy(compareType);
		return (List<PartProductionData>) findPartProduction(moldIds, payload, companyIds, pageable, false);
	}

	@Override
	public List<Mold> findMoldOrderByInactivePeriod(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder(predicate);
		NumberExpression<Integer> inactiveDays = new CaseBuilder()
				.when(mold.lastShotAt.isNotNull())
				.then(Expressions.numberTemplate(Integer.class, "datediff({0}, {1})", mold.lastShotAt, Instant.now()))
				.otherwise(Expressions.numberTemplate(Integer.class, "datediff({0}, {1})", mold.createdAt, Instant.now()));

		JPQLQuery query = from(mold)
				.where(builder);
		NumberExpression numberExpression = inactiveDays;
		OrderSpecifier numberOrder = numberExpression.asc();
		if (directions[0].equals(Sort.Direction.ASC)) {
			numberOrder = numberExpression.desc();
		}
		query.orderBy(numberOrder);
		query.offset(pageable.getOffset());
		query.limit(pageable.getPageSize());

		return query.fetch();
	}

	@Override
	public List<MoldAccumulatedData> findMoldOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder(predicate);

		if (CollectionUtils.isNotEmpty(moldIds)) {
			builder.and(mold.id.in(moldIds));
		}
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statistics.shotCountVal;
		} else {
			shotCount = statistics.shotCount;
		}

		JPQLQuery query;
		NumberExpression sumShotCount;
		if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			String[] sortValues = properties[0].split("\\.");
			if (!"all".equals(sortValues[1])) {
				sumShotCount = shotCount.sum();
				query = from(mold)
						.leftJoin(statistics).on(statistics.moldId.eq(mold.id)
								.and(statistics.year.eq(sortValues[1]))
								.and(statistics.ct.gt(0).or(statistics.firstData.isTrue())))
						.where(builder);
			} else {
				sumShotCount = mold.lastShot;
				query = from(mold)
						.where(builder);
			}
		} else {
			sumShotCount = mold.lastShot;
			query = from(mold)
					.where(builder);
		}
		query.groupBy(mold.id);
		OrderSpecifier numberOrder = sumShotCount.asc();
		if (directions[0].equals(Sort.Direction.DESC)) {
			numberOrder = sumShotCount.desc();
		}
		query.orderBy(numberOrder);

		if (CollectionUtils.isEmpty(moldIds)) {
			query.offset(pageable.getOffset());
			query.limit(pageable.getPageSize());
		}
		query.select(Projections.constructor(MoldAccumulatedData.class, mold, sumShotCount));

		return query.fetch();
	}


	@Override
	public List<MoldAccumulatedData> findMoldAccumulatedShotByStatistic(boolean isReSum, String year, List<Long> moldIds) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder();

		if (CollectionUtils.isNotEmpty(moldIds)) {
			builder.and(mold.id.in(moldIds));
		}

		JPQLQuery query;
		if (!StringUtils.isEmpty(year)) {
			builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));
			query = from(mold)
					.leftJoin(statistics).on(statistics.moldId.eq(mold.id).and(statistics.year.eq(year)))
					.where(builder);
		} else if(isReSum){
			builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));
			query = from(mold)
					.leftJoin(statistics).on(statistics.moldId.eq(mold.id))
					.where(builder);
		} else {
			query = from(mold)
//					.leftJoin(statistics).on(statistics.moldId.eq(mold.id))
					.where(builder);
		}
		query.groupBy(mold.id);

		NumberExpression sumShotCount = (new CaseBuilder().when(statistics.isNull()).then(0).otherwise(statistics.shotCount)).sum();

		if (!StringUtils.isEmpty(year) || isReSum) {
			query.select(Projections.constructor(MoldAccumulatedData.class, mold, sumShotCount));
		} else {
			query.select(Projections.constructor(MoldAccumulatedData.class, mold, mold.lastShot));
		}

		return query.fetch();
	}

	@Override
	public List<MoldAccumulatedData> findMoldAccumulatedShot(String year, List<Long> moldIds) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder();

		if (CollectionUtils.isNotEmpty(moldIds)) {
			builder.and(mold.id.in(moldIds));
		}

		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statistics.shotCountVal;
		} else {
			shotCount = statistics.shotCount;
		}

		JPQLQuery query;
		if (!StringUtils.isEmpty(year)) {
			builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));
			query = from(mold)
					.leftJoin(statistics).on(statistics.moldId.eq(mold.id).and(statistics.year.eq(year)))
					.where(builder);
		} else {
			query = from(mold)
//					.leftJoin(statistics).on(statistics.moldId.eq(mold.id))
					.where(builder);
		}
		query.groupBy(mold.id);

		NumberExpression sumShotCount = shotCount.sum();
		if (!StringUtils.isEmpty(year)) {
			query.select(Projections.constructor(MoldAccumulatedData.class, mold, sumShotCount));
		} else {
			query.select(Projections.constructor(MoldAccumulatedData.class, mold, mold.lastShot));
		}

		return query.fetch();
	}

	@Override
	public MoldAccumulatedData findMoldAccumulatedShotByLstLessThan(String lst, Long moldId) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(mold.id.eq(moldId));
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statistics.shotCountVal;
		} else {
			shotCount = statistics.shotCount;
		}

		JPQLQuery query;
		if (!StringUtils.isEmpty(lst)) {
			builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));
			builder.and(shotCount.isNotNull().and(shotCount.gt(0)));
			query = from(mold)
					.leftJoin(statistics).on(statistics.moldId.eq(mold.id).and(statistics.lst.lt(lst)))
					.where(builder);
		} else {
			query = from(mold)
					.where(builder);
		}

		NumberExpression sumShotCount = statistics.sc.max();
		if (!StringUtils.isEmpty(lst)) {
			query.select(Projections.constructor(MoldAccumulatedData.class, mold, sumShotCount));
		} else {
			query.select(Projections.constructor(MoldAccumulatedData.class, mold, mold.lastShot));
		}

		return (MoldAccumulatedData) query.fetchOne();
	}

	private Object findPartProduction(List<Long> moldIds, ProductivitySearchPayload payload,List<Long> companyIds, Pageable pageable, boolean isCount) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCompany company = QCompany.company;
		QPart part = QPart.part;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();

		JPQLQuery query = from(mold);

		if (payload.getPartId() != null) {
			builder.and(statisticsPart.partId.eq(payload.getPartId()));
		}

		if (payload.getStartDate() != null && payload.getEndDate() != null) {
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		} else if (payload.getEndDate() != null) {
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		if(moldIds != null
			//&& moldIds.size() > 0
		){
			builder.and(mold.id.in(moldIds));
		}

		// [2021-02-16] Ignore shot with CT == 0
		builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

		query.where(builder
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		Expression<Integer> producedQuantity = (shotCount.multiply(statisticsPart.cavity)).sum().coalesce(0);

/*
		Expression<Integer> maxCapacity;
		if(payload.getDuration() != null){
			maxCapacity = mold.dailyMaxCapacity.multiply(payload.getDuration());
		}else{
			maxCapacity = mold.passedDays
					.multiply(mold.dailyMaxCapacity)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7))
					.divide(7);
		}

		NumberExpression<Float> percentageProductivity = ((Coalesce<Integer>) producedQuantity).asNumber().floatValue().multiply(100).divide(maxCapacity);
*/

		//

		query.innerJoin(statistics).on(statistics.moldId.eq(mold.id));
		query.innerJoin(statisticsPart).on(statisticsPart.statisticsId.eq(statistics.id));
		query.innerJoin(part).on(statisticsPart.partId.eq(part.id));
		if(CompareType.SUPPLIER.equals(payload.getCompareBy())){
			if(companyIds!=null){
				builder.and(mold.location.company.id.in(companyIds));
			}
			query.where(builder.and(mold.location.company.isEmoldino.isFalse()));
			if(isCount)
				query.select(mold.location.company.id.countDistinct());
			else{
				query.groupBy(statisticsPart.partId,mold.location.company.id);
				query.select(Projections.constructor(PartProductionData.class, mold.location.company.id,part.id,part.partCode
						,part.name, ((Coalesce<Integer>) producedQuantity).asNumber().longValue()));
			}

		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			if(companyIds!=null){
				builder.and(mold.toolMaker.id.in(companyIds));
			}
			query.where(builder.and(mold.toolMaker.isEmoldino.isFalse()).and(mold.location.company.isEmoldino.isFalse()));
			if(isCount)
				query.select(mold.toolMaker.id.countDistinct());
			else{
				query.groupBy(mold.toolMaker.id,part.id);
				query.select(Projections.constructor(PartProductionData.class, mold.toolMaker.id,part.id,part.partCode
						,part.name, ((Coalesce<Integer>) producedQuantity).asNumber().longValue()));
			}
		}
		else {
			if(isCount)
				query.select(part.id.countDistinct());
			else{

				query.groupBy(mold.id,part.id);
				query.select(Projections.constructor(PartProductionData.class, mold.id,part.id,part.partCode
						,part.name, ((Coalesce<Integer>) producedQuantity).asNumber().longValue()));
			}
		}
		if(isCount){
			return (Long)query.fetchOne();
		}
		if(pageable != null) {
			Sort.Direction[] directions = {Sort.Direction.DESC};
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
			});
			if (directions[0].equals(Sort.Direction.ASC))
				query.orderBy(((Coalesce<Integer>)producedQuantity).asc());
			else
				query.orderBy(((Coalesce<Integer>)producedQuantity).desc());
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}
		List<PartProductionData> result=query.fetch();

		return result;
	}

	/**
	 * Calculation max capacity for supplier/toolmaker because not join with statistics table
	 * @param moldIds
	 * @param payload
	 * @param isDyson
	 * @param pageable
	 * @return
	 */
	public List<ToolingProductivityData> findProductivityMaxCapacity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson, Pageable pageable) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCompany company = QCompany.company;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();

		JPQLQuery query = from(mold);


		if(Arrays.asList(CompareType.SUPPLIER,CompareType.TOOLMAKER).contains(payload.getCompareBy())){
			if (payload.getPartId() != null) {
//				builder.and(statisticsPart.partId.eq(payload.getPartId()));
				builder.and(mold.id.in(JPAExpressions.select(statisticsPart.statistics.moldId).from(statisticsPart)
						.where(statisticsPart.partId.eq(payload.getPartId()))));
			}

			if (payload.getStartDate() != null && payload.getEndDate() != null) {
				builder.and(mold.id.in(JPAExpressions.select(statistics.moldId).from(statistics).where(statistics.day.between(payload.getStartDate(), payload.getEndDate())
								.and(ct.gt(0).or(statistics.firstData.isTrue())))));
			} else if (payload.getEndDate() != null) {
				builder.and(mold.id.in(JPAExpressions.select(statistics.moldId).from(statistics).where(statistics.day.loe(payload.getEndDate())
						.and(ct.gt(0).or(statistics.firstData.isTrue())))));
			}
		}else {
		if (payload.getPartId() != null) {
			builder.and(statisticsPart.partId.eq(payload.getPartId()));
		}

		if (payload.getStartDate() != null && payload.getEndDate() != null) {
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		} else if (payload.getEndDate() != null) {
			builder.and(statistics.day.loe(payload.getEndDate()));
		}
		}

		if(moldIds != null
			//	&& moldIds.size() > 0
		){
			builder.and(mold.id.in(moldIds));
		}

		// [2021-02-26] Ignore shot with CT == 0
//		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));

		query.where(builder
				.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		Expression<Integer> producedQuantity = (shotCount.multiply(statisticsPart.cavity)).sum().coalesce(0);

		Expression<Integer> maxCapacity;
		if(payload.getDuration() != null){
			maxCapacity = mold.dailyMaxCapacity.multiply(payload.getDuration());
		}else{
			maxCapacity = mold.passedDays
					.multiply(mold.dailyMaxCapacity)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7))
					.divide(7);
		}

		NumberExpression<Float> percentageProductivity = ((Coalesce<Integer>) producedQuantity).asNumber().floatValue().multiply(100).divide(maxCapacity);

		if(CompareType.SUPPLIER.equals(payload.getCompareBy())){
			query.groupBy(mold.location.company.id,mold.location.company.companyCode, mold.location.company.name);
			query.select(Projections.constructor(ToolingProductivityData.class, mold.location.company.id,mold.location.company.companyCode,  mold.location.company.name,
					Expressions.numberTemplate(Integer.class,"0"),Expressions.numberTemplate(Double.class,"0"), mold.dailyMaxCapacity.sum(), ((NumberExpression<Integer>) maxCapacity).sum()));
		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.groupBy(mold.toolMaker.id,mold.toolMaker.companyCode, mold.toolMaker.name);
			query.select(Projections.constructor(ToolingProductivityData.class, mold.toolMaker.id,mold.toolMaker.companyCode, mold.toolMaker.name,
					Expressions.numberTemplate(Integer.class,"0"), Expressions.numberTemplate(Double.class,"0"), mold.dailyMaxCapacity.sum(), ((NumberExpression<Integer>) maxCapacity).sum()));
		}
		else {
			query.innerJoin(statistics).on(statistics.moldId.eq(mold.id));
			query.innerJoin(statisticsPart).on(statisticsPart.statisticsId.eq(statistics.id));
			query.innerJoin(company).on(mold.companyId.eq(company.id));

			query.groupBy(mold.id);
			query.select(Projections.constructor(ToolingProductivityData.class, mold.id, mold.equipmentCode, mold,
					company.companyCode, producedQuantity, percentageProductivity, mold.dailyMaxCapacity, maxCapacity
					,statisticsPart.partId.countDistinct().intValue()));
		}

		if(pageable != null) {
			Sort.Direction[] directions = {Sort.Direction.DESC};
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
			});
			if (directions[0].equals(Sort.Direction.ASC))
				query.orderBy(percentageProductivity.asc());
			else
				query.orderBy(percentageProductivity.desc());
			query.limit(pageable.getPageSize());
		}
		return query.fetch();
	}

/*
	@Override
	public List<ToolingProductivityData> findToolingMaxProductivity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson) {
		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder builder = new BooleanBuilder();

		if (payload.getPartId() != null) {
			builder.and(moldPart.partId.eq(payload.getPartId()));
		}

		if (moldIds != null
				//&& moldIds.size() > 0
		) {
			builder.and(moldPart.moldId.in(moldIds));
		}

		Expression uptimeTarget = isDyson ?
				moldPart.mold.uptimeTarget.coalesce(90).asNumber().divide(100) : Expressions.asNumber(1);
		Expression<Integer> dailyMaxCapacity = ((moldPart.mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600))
				.multiply(uptimeTarget)
				.divide(moldPart.mold.contractedCycleTime.coalesce(1).asNumber().divide(10))).sum().castToNum(Integer.class);

		Expression<Integer> maxCapacity;
		if (payload.getDuration() != null) {
			maxCapacity = ((NumberExpression<Integer>) dailyMaxCapacity).multiply(payload.getDuration());
		} else {
			maxCapacity = (moldPart.mold.designedShot.multiply(moldPart.cavity.sum())).coalesce(0);
		}

		JPQLQuery query = from(moldPart)
				.where(builder);

		if (CompareType.SUPPLIER.equals(payload.getCompareBy())) {
			query.groupBy(moldPart.mold.location.company.id);
			query.select(Projections.constructor(ToolingProductivityData.class, moldPart.mold.location.company.id,
					maxCapacity, dailyMaxCapacity));
		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.groupBy(moldPart.mold.toolMaker.id);
			query.select(Projections.constructor(ToolingProductivityData.class, moldPart.mold.toolMaker.id,
					maxCapacity, dailyMaxCapacity));
		} else {
			query.groupBy(moldPart.moldId);
			query.select(Projections.constructor(ToolingProductivityData.class, moldPart.moldId, maxCapacity, dailyMaxCapacity));
		}
		return query.fetch();
	}
*/

	@Override
	public List<ChartData> findDailyProducedQuantity(List<Long> moldIds, ProductivitySearchPayload payload, boolean isDyson){
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

		NumberPath shotCount = statistics.shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statistics.shotCountVal;
		} else {
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();

		if(moldIds != null) builder.and(mold.id.in(moldIds));
        if(payload.getPartId() != null){
            builder.and(statisticsPart.partId.eq(payload.getPartId()));
        }
		if(payload.getStartDate() != null && payload.getEndDate() != null){
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		}else if(payload.getEndDate() != null){
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		JPQLQuery query = from(mold)
				.innerJoin(statistics).on(mold.id.eq(statistics.moldId))
				.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		if (CompareType.SUPPLIER.equals(payload.getCompareBy())) {
			query.groupBy(mold.id, mold.location.company.id, statistics.day);
			query.select(Projections.constructor(ChartData.class, mold.id, mold.location.company.id, statistics.day, (shotCount.multiply(statisticsPart.cavity.coalesce(1))).sum().coalesce(0)));
		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.groupBy(mold.id, mold.toolMaker.id, statistics.day);
			query.select(Projections.constructor(ChartData.class, mold.id, mold.toolMaker.id, statistics.day, (shotCount.multiply(statisticsPart.cavity.coalesce(1))).sum().coalesce(0)));
		} else {
			query.groupBy(mold.id, statistics.day);
			query.select(Projections.constructor(ChartData.class, mold.id, statistics.day, (shotCount.multiply(statisticsPart.cavity.coalesce(1))).sum().coalesce(0)));
		}

		return query.fetch();
	}

	public List<MoldCapacityReportData> findAvailableDowntime(List<Long> moldIds, ProductivitySearchPayload payload, List<Long> idRes) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QMoldDownTime moldDownTime = QMoldDownTime.moldDownTime;
		int rangTime = 7;
		JPQLQuery query = from(moldDownTime)
				.innerJoin(mold).on(mold.id.eq(moldDownTime.moldId))
				.innerJoin(moldPart).on(mold.id.eq(moldPart.moldId));
		BooleanBuilder builder = new BooleanBuilder();
/*
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
*/

		if (payload.getPartId() != null) {
			builder.and(moldPart.partId.eq(payload.getPartId()));
		}

		if (payload.getStartDate() != null && payload.getEndDate() != null) {
			builder.and(moldDownTime.day.between(payload.getStartDate(), payload.getEndDate()));
		} else if (payload.getEndDate() != null) {
			builder.and(moldDownTime.day.loe(payload.getEndDate()));
		}

		if (moldIds != null
			//	&& moldIds.size() > 0
		) {
			builder.and(mold.id.in(moldIds));
		}
		// [2021-02-26] Ignore shot with CT == 0
//		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));

//		DashboardFilterPayload payload1=new DashboardFilterPayload();
//		payload1.setSupplierIds(payload.getSupplierIds());
//		builder.and(JPQLQueryUtils.getMoldFilterBuilder(mold, payload1));


//		StringExpression dataView = getDataViewMoldDownTime(moldDownTime, frequent);

		// TODO OCT
		Expression<Long> dataExp = ((moldDownTime.downTime.coalesce(0L).asNumber().divide(1000).multiply(moldPart.cavity.coalesce(1)))).sum().castToNum(Long.class);
//		Expression<Integer> dataExp = ((moldDownTime.downTime.coalesce(0L).asNumber().divide(1000).multiply(moldPart.cavity.coalesce(1)))
//				.divide(mold.contractedCycleTime.coalesce(1)).divide(10)
//		).sum().castToNum(Integer.class);
		NumberPath colKey;
		if (CompareType.SUPPLIER.equals(payload.getCompareBy())) {
			colKey = mold.location.company.id;
			query.groupBy(mold.location.company.id, mold.location.company.companyCode, mold.location.company.name);
			query.select(Projections.constructor(MoldCapacityReportData.class, mold.location.company.id, mold.location.company.companyCode,
					dataExp));
		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			colKey = mold.toolMaker.id;
			query.groupBy(mold.toolMaker.id, mold.toolMaker.companyCode, mold.toolMaker.name);
			query.select(Projections.constructor(MoldCapacityReportData.class, mold.toolMaker.id, mold.toolMaker.companyCode
					, dataExp));
		} else {
			colKey = mold.id;
			query.groupBy(mold.id);
			query.select(Projections.constructor(MoldCapacityReportData.class, mold.id, mold.equipmentCode,
					dataExp));
		}
		if (idRes != null)
			builder.and(colKey.in(idRes));
		query.where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		List<MoldCapacityReportData>list = query.fetch();
		// TODO OCT
		divideCycleTime(list);
		return list;
	}

	public static void divideCycleTime(List<MoldCapacityReportData> list) {
		list.forEach(item -> {
			double act = MoldUtils.getOptimalCycleTime(item.getId(), item.getContractedCycleTime(), null).getValue();
			if (item.getDataLong() != null) {
				long data = item.getDataLong();
				data /= (act / 10);
				item.setDataLong(data);
			}
			item.setContractedCycleTime(ValueUtils.toInteger(act, null));
		});
	}

	@Override
	public List<ToolingCycleTimeData> getAvgCycleTimeInRange(List<Long> moldIds, ProductivitySearchPayload payload, CycleTimeStatus status){
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;

		NumberPath ct;
		NumberPath<Integer> shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(shotCount.ne(0));
		if(moldIds != null) builder.and(mold.id.in(moldIds));
		if(payload.getStartDate() != null && payload.getEndDate() != null){
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		}else if(payload.getEndDate() != null){
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		NumberExpression<Integer> uptime = shotCount.multiply(ct).sum();
		NumberExpression<Integer> totalShots = new CaseBuilder()
				.when(shotCount.sum().gt(0))
				.then(shotCount.sum())
				.otherwise(1);

		NumberExpression<Integer> startL1 = new CaseBuilder()
				.when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.subtract(mold.cycleTimeLimit1.multiply(10))) // convert second to 100ms
				.otherwise(mold.contractedCycleTime.subtract(mold.contractedCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
		NumberExpression<Integer> endL1 = new CaseBuilder()
				.when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.add(mold.cycleTimeLimit1.multiply(10)))
				.otherwise(mold.contractedCycleTime.add(mold.contractedCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
		NumberExpression<Integer> startL2 = new CaseBuilder()
				.when(mold.cycleTimeLimit2Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.subtract(mold.cycleTimeLimit2.multiply(10)))
				.otherwise(mold.contractedCycleTime.subtract(mold.contractedCycleTime.multiply(mold.cycleTimeLimit2).divide(100)));
		NumberExpression<Integer> endL2 = new CaseBuilder()
				.when(mold.cycleTimeLimit2Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.add(mold.cycleTimeLimit2.multiply(10)))
				.otherwise(mold.contractedCycleTime.add(mold.contractedCycleTime.multiply(mold.cycleTimeLimit2).divide(100)));

		NumberExpression<Float> avgCycleTime = uptime.floatValue().divide(totalShots);
		JPQLQuery query = from(mold)
				.innerJoin(statistics).on(mold.id.eq(statistics.moldId))
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		if (CompareType.SUPPLIER.equals(payload.getCompareBy())) {
			query.select(Projections.constructor(ToolingCycleTimeData.class,  mold.location.company.id, mold.location.company.companyCode, mold.location.company.name,mold.id, mold.equipmentCode, avgCycleTime.divide(10)));
		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.select(Projections.constructor(ToolingCycleTimeData.class, mold.toolMaker.id,mold.toolMaker.companyCode,mold.toolMaker.name, mold.id, mold.equipmentCode, avgCycleTime.divide(10)));

		} else {
			query.select(Projections.constructor(ToolingCycleTimeData.class, mold.id, mold.equipmentCode, avgCycleTime.divide(10))); // 100ms to seconds
		}
		query.groupBy(mold.id, mold.contractedCycleTime, mold.cycleTimeLimit1, mold.cycleTimeLimit2, mold.cycleTimeLimit1Unit, mold.cycleTimeLimit2Unit);
		if(status == null){
			return query.fetch();
		}

		BooleanBuilder subBuilder = new BooleanBuilder();
		if(status.equals(CycleTimeStatus.WITHIN_TOLERANCE))
			subBuilder.and(avgCycleTime.gt(startL1).and(avgCycleTime.lt(endL1)));
		else if(status.equals(CycleTimeStatus.OUTSIDE_L1))
			subBuilder.and(avgCycleTime.between(startL2, startL1).or(avgCycleTime.between(endL1, endL2)));
		else
			subBuilder.and(avgCycleTime.lt(startL2).or(avgCycleTime.gt(endL2)));


//		query.groupBy(mold.id, mold.contractedCycleTime, mold.cycleTimeLimit1, mold.cycleTimeLimit2, mold.cycleTimeLimit1Unit, mold.cycleTimeLimit2Unit);
		query.having(subBuilder);

		return query.fetch();
	}

	@Override
	public List<CycleTimeOverviewDetailData> getReportCycleTimeInRange(List<Long> moldIds, ProductivitySearchPayload payload, DataRangeType dataRangeType){
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;

		NumberPath ct;
		NumberPath<Integer> shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(shotCount.ne(0));
		if(moldIds != null) builder.and(mold.id.in(moldIds));
		if(payload.getStartDate() != null && payload.getEndDate() != null){
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		}else if(payload.getEndDate() != null){
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		NumberExpression<Integer> uptime = shotCount.multiply(ct).sum();
		NumberExpression<Integer> totalShots = new CaseBuilder()
				.when(shotCount.sum().gt(0))
				.then(shotCount.sum())
				.otherwise(1);
		NumberExpression variance = (((ct.add(mold.contractedCycleTime.negate())).multiply(100)).divide(mold.contractedCycleTime)).avg();


		NumberExpression<Integer> startL1 = new CaseBuilder()
				.when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.subtract(mold.cycleTimeLimit1.multiply(10))) // convert second to 100ms
				.otherwise(mold.contractedCycleTime.subtract(mold.contractedCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
		NumberExpression<Integer> endL1 = new CaseBuilder()
				.when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.add(mold.cycleTimeLimit1.multiply(10)))
				.otherwise(mold.contractedCycleTime.add(mold.contractedCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
/*
		NumberExpression<Integer> startL2 = new CaseBuilder()
				.when(mold.cycleTimeLimit2Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.subtract(mold.cycleTimeLimit2.multiply(10)))
				.otherwise(mold.contractedCycleTime.subtract(mold.contractedCycleTime.multiply(mold.cycleTimeLimit2).divide(100)));
		NumberExpression<Integer> endL2 = new CaseBuilder()
				.when(mold.cycleTimeLimit2Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.add(mold.cycleTimeLimit2.multiply(10)))
				.otherwise(mold.contractedCycleTime.add(mold.contractedCycleTime.multiply(mold.cycleTimeLimit2).divide(100)));
*/

		NumberExpression<Float> avgCycleTime = uptime.floatValue().divide(totalShots);
		JPQLQuery query = from(mold)
				.innerJoin(statistics).on(mold.id.eq(statistics.moldId))
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())));

		if (CompareType.SUPPLIER.equals(payload.getCompareBy())) {
			query.select(Projections.constructor(CycleTimeOverviewDetailData.class,  mold.location.company.id, mold.location.company.companyCode, mold.location.company.name,mold.id, mold.equipmentCode, totalShots,variance, avgCycleTime.divide(10)));
		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.select(Projections.constructor(CycleTimeOverviewDetailData.class, mold.toolMaker.id,mold.toolMaker.companyCode,mold.toolMaker.name, mold.id, mold.equipmentCode, totalShots,variance, avgCycleTime.divide(10)));

		} else {
			query.select(Projections.constructor(CycleTimeOverviewDetailData.class, mold.id, mold.equipmentCode, totalShots,variance, avgCycleTime.divide(10))); // 100ms to seconds
		}
		query.groupBy(mold.id, mold.contractedCycleTime, mold.cycleTimeLimit1, mold.cycleTimeLimit2, mold.cycleTimeLimit1Unit, mold.cycleTimeLimit2Unit);
		if(dataRangeType == null){
			return query.fetch();
		}

		BooleanBuilder subBuilder = new BooleanBuilder();
		if(dataRangeType.equals(DataRangeType.WITHIN))
			subBuilder.and(avgCycleTime.gt(startL1).and(avgCycleTime.lt(endL1)));
		else if(dataRangeType.equals(DataRangeType.BELOW))
			subBuilder.and(avgCycleTime.lt(startL1));
		else
			subBuilder.and(avgCycleTime.gt(endL1));


//		query.groupBy(mold.id, mold.contractedCycleTime, mold.cycleTimeLimit1, mold.cycleTimeLimit2, mold.cycleTimeLimit1Unit, mold.cycleTimeLimit2Unit);
		query.having(subBuilder);

		return query.fetch();
	}

	@Override
	public List<ToolingCycleTimeData> getComplianceShotCountInRange(List<Long> moldIds, ProductivitySearchPayload payload){
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;

		NumberPath ct;
		NumberPath<Integer> shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		BooleanBuilder builder = new BooleanBuilder();
		if(moldIds != null) builder.and(mold.id.in(moldIds));
		if(payload.getStartDate() != null && payload.getEndDate() != null){
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		}else if(payload.getEndDate() != null){
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		NumberExpression<Integer> startL1 = new CaseBuilder()
				.when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.subtract(mold.cycleTimeLimit1.multiply(10))) // convert second to 100ms
				.otherwise(mold.contractedCycleTime.subtract(mold.contractedCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
		NumberExpression<Integer> endL1 = new CaseBuilder()
				.when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(mold.contractedCycleTime.add(mold.cycleTimeLimit1.multiply(10)))
				.otherwise(mold.contractedCycleTime.add(mold.contractedCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));

		builder.and(ct.gt(startL1).and(ct.lt(endL1)));

		JPQLQuery query = from(mold)
				.innerJoin(statistics).on(mold.id.eq(statistics.moldId))
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
				.groupBy(mold.id)
				.select(Projections.constructor(ToolingCycleTimeData.class, mold.id, mold.equipmentCode, shotCount.sum()));
		return query.fetch();
	}

	@Override
	public List<ToolingCycleTimeData> findToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload
			, Pageable pageable, boolean getAll){
		return (List<ToolingCycleTimeData>) findToolingCycleTimeData(moldIds,payload,pageable,getAll,false);
	}
	@Override
	public Long countToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload
			, boolean getAll){
		Long total= (Long) findToolingCycleTimeData(moldIds,payload,null,getAll,true);
		return total!=null?total:0L;
	}
//	@Override
	public Object findToolingCycleTimeData(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable, boolean getAll, boolean isCount) {
		QMold mold = QMold.mold;
		QStatistics statistics = QStatistics.statistics;

		NumberPath<Double> ct;
		NumberPath<Integer> shotCount;
/*
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
*/
			ct = statistics.ct;
			shotCount = statistics.shotCount;
/*
		}
*/

		BooleanBuilder builder = new BooleanBuilder();
		if (moldIds != null) {
			builder.and(mold.id.in(moldIds));
		}

		if (payload.getStartDate() != null && payload.getEndDate() != null) {
			builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
		} else if (payload.getEndDate() != null) {
			builder.and(statistics.day.loe(payload.getEndDate()));
		}

		JPQLQuery query = from(mold).innerJoin(statistics).on(mold.id.eq(statistics.moldId))
				.where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())).and(shotCount.gt(0)))
//				.groupBy(mold.id)
		;

		NumberExpression<Integer> ctCycleTime= new CaseBuilder()
				.when(mold.contractedCycleTime.isNull()).then(mold.toolmakerContractedCycleTime).otherwise(mold.contractedCycleTime);
		NumberExpression<Integer> totalShotCount = new CaseBuilder().when(shotCount.sum().gt(0)).then(shotCount.sum()).otherwise(1);
		NumberExpression variance = (((ct.add(ctCycleTime.negate())).multiply(100)).divide(ctCycleTime)).avg();

		NumberExpression<Integer> startL1 = new CaseBuilder().when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(ctCycleTime.subtract(mold.cycleTimeLimit1.multiply(10)))
				.otherwise(ctCycleTime.subtract(ctCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
		NumberExpression<Integer> endL1 = new CaseBuilder().when(mold.cycleTimeLimit1Unit.eq(OutsideUnit.SECOND))
				.then(ctCycleTime.add(mold.cycleTimeLimit1.multiply(10)))
				.otherwise(ctCycleTime.add(ctCycleTime.multiply(mold.cycleTimeLimit1).divide(100)));
		NumberExpression<Integer> startL2 = new CaseBuilder().when(mold.cycleTimeLimit2Unit.eq(OutsideUnit.SECOND))
				.then(ctCycleTime.subtract(mold.cycleTimeLimit2.multiply(10)))
				.otherwise(ctCycleTime.subtract(ctCycleTime.multiply(mold.cycleTimeLimit2).divide(100)));
		NumberExpression<Integer> endL2 = new CaseBuilder().when(mold.cycleTimeLimit2Unit.eq(OutsideUnit.SECOND))
				.then(ctCycleTime.add(mold.cycleTimeLimit2.multiply(10)))
				.otherwise(ctCycleTime.add(ctCycleTime.multiply(mold.cycleTimeLimit2).divide(100)));

		NumberExpression<Integer> complianceShots = new CaseBuilder().when(ct.gt(startL1).and(ct.lt(endL1))).then(shotCount).otherwise(0);
		NumberExpression<Integer> compliance = complianceShots.sum();
		NumberExpression<Float> percentageCompliance = compliance.floatValue().multiply(100).divide(totalShotCount);

		NumberExpression<Integer> limit1Shots = new CaseBuilder().when(ct.between(startL2, startL1).or(ct.between(endL1, endL2))).then(shotCount).otherwise(0);
		NumberExpression<Integer> limit1 = limit1Shots.sum();
		NumberExpression<Float> percentageLimit1 = limit1.floatValue().multiply(100).divide(totalShotCount);

		NumberExpression<Integer> limit2Shots = new CaseBuilder().when(ct.lt(startL2).or(ct.gt(endL2))).then(shotCount).otherwise(0);
		NumberExpression<Integer> limit2 = limit2Shots.sum();
		NumberExpression<Float> percentageLimit2 = limit2.floatValue().multiply(100).divide(totalShotCount);

		NumberExpression<Integer> shotCountAboveL1 = new CaseBuilder().when(ct.between(endL1, endL2)).then(shotCount).otherwise(0);
		NumberExpression<Integer> shotCountAboveL1Sum = shotCountAboveL1.sum();
		NumberExpression<Float> percentageAboveL1 = shotCountAboveL1Sum.floatValue().multiply(100).divide(totalShotCount);

		NumberExpression<Integer> shotCountBelowL1 = new CaseBuilder().when(ct.between(startL2, startL1)).then(shotCount).otherwise(0);
		NumberExpression<Integer> shotCountBelowL1Sum = shotCountBelowL1.sum();
		NumberExpression<Float> percentageBelowL1 = shotCountBelowL1Sum.floatValue().multiply(100).divide(totalShotCount);

		NumberExpression<Integer> shotCountAboveL2 = new CaseBuilder().when(ct.gt(endL2)).then(shotCount).otherwise(0);
		NumberExpression<Integer> shotCountAboveL2Sum = shotCountAboveL2.sum();
		NumberExpression<Float> percentageAboveL2 = shotCountAboveL2Sum.floatValue().multiply(100).divide(totalShotCount);

		NumberExpression<Integer> shotCountBelowL2 = new CaseBuilder().when(ct.lt(startL2)).then(shotCount).otherwise(0);
		NumberExpression<Integer> shotCountBelowL2Sum = shotCountBelowL2.sum();
		NumberExpression<Float> percentageBelowL2 = shotCountBelowL2Sum.floatValue().multiply(100).divide(totalShotCount);

		/*
		if(!getAll){
			query.select(Projections.constructor(ToolingCycleTimeData.class, mold.id, percentageCompliance));
			return query.fetch();
		}
		*/

		/*
		query.select(Projections.constructor(ToolingCycleTimeData.class, mold.id, mold.equipmentCode, ctCycleTime.floatValue().divide(10), percentageCompliance, percentageLimit1, percentageLimit2));
		*/

		if (CompareType.SUPPLIER.equals(payload.getCompareBy())) {
			query.where(builder.and(mold.location.company.isEmoldino.isFalse()));
			if (isCount)
				query.select(mold.location.company.id.countDistinct());
			else {
				query.groupBy(mold.location.company.id);

				if (!getAll) {
					query.select(Projections.constructor(ToolingCycleTimeData.class, mold.location.company.id, percentageCompliance));
				} else
					query.select(Projections.constructor(ToolingCycleTimeData.class, mold.location.company.id, mold.location.company.name,
							Expressions.numberTemplate(Double.class, "0"), percentageCompliance, percentageLimit1, percentageLimit2, variance));
			}

		} else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
			query.where(builder.and(mold.location.company.isEmoldino.isFalse()).and(mold.toolMaker.isEmoldino.isFalse()));
			if (isCount)
				query.select(mold.toolMaker.id.countDistinct());
			else {
				query.groupBy(mold.toolMaker.id);

				if (!getAll) {
					query.select(Projections.constructor(ToolingCycleTimeData.class, mold.toolMaker.id, percentageCompliance));
				} else
					query.select(Projections.constructor(ToolingCycleTimeData.class, mold.toolMaker.id, mold.toolMaker.name, Expressions.numberTemplate(Double.class, "0"),
							percentageCompliance, percentageLimit1, percentageLimit2, variance));
			}

		} else {
			if (isCount)
				query.select(mold.id.countDistinct());
			else {

				query.groupBy(mold.id);

				if (!getAll) {
					query.select(Projections.constructor(ToolingCycleTimeData.class, mold.id, percentageCompliance));
				} else if (payload.isForTooltip()) {
					if (payload.isCompareBySupplier()) {
						query.where(mold.location.company.id.eq(payload.getCompanyId()));
					} else if (payload.isCompareByToolMaker()) {
						query.where(mold.toolMaker.id.eq(payload.getCompanyId()));
					}
				}
				query.select(Projections.constructor(ToolingCycleTimeData.class, mold.id, mold.equipmentCode, ctCycleTime.floatValue().divide(10),
						percentageCompliance, percentageLimit1, percentageLimit2, shotCountAboveL1Sum, percentageAboveL1, shotCountBelowL1Sum, percentageBelowL1,
						shotCountAboveL2Sum, percentageAboveL2, shotCountBelowL2Sum, percentageBelowL2, compliance, variance));
			}
		}
		if (isCount) {
			return query.fetchOne();
		}
		if (pageable != null) {
			Sort.Direction[] directions = { Sort.Direction.DESC };
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
			});
			if (directions[0].equals(Sort.Direction.ASC))
				query.orderBy(percentageCompliance.asc());
			else
				query.orderBy(percentageCompliance.desc());
			query.orderBy(mold.id.asc());
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}

		return query.fetch();
	}

	@Override
	public List<MoldCttTempData> findMoldCttTempData(Long moldId, String startTime, String endTime){
		QCdata cdata = QCdata.cdata;
		QStatistics statistics = QStatistics.statistics;

		NumberPath ct = cdata.ct;
		StringPath ctt = cdata.ctt;
		NumberPath<Integer> shotCount = statistics.shotCount;
		//Wut 데이타 계산은 신규 데이타이던 Batch(add-valid-invalid-data-filter) 작업이던 기존 값으로 처리
//		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
//			ct = cdata.ctVal;
//			ctt = cdata.cttVal;
//			shotCount = statistics.shotCountVal;
//		}

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(cdata.moldId.eq(moldId));

		if(startTime != null && endTime != null){
			builder.and(cdata.lst.between(startTime, endTime));
		}

		JPQLQuery query = from(cdata)
				.innerJoin(statistics).on(statistics.cdataId.eq(cdata.id))
				.where(builder)
				.orderBy(cdata.id.asc())
				.select(Projections.constructor(
							MoldCttTempData.class, cdata.id, cdata.createdAt, ct, cdata.lst,
								cdata.moldCode, shotCount, ctt, cdata.temp, statistics.uptimeSeconds,statistics.moldId));
		return query.fetch();
	}

	@Override
	public List<DashboardChartData> findImplementationStatus(DashboardFilterPayload payload){
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		if(payload != null && payload.getCompanyId() != null){
			builder.and(mold.location.companyId.eq(payload.getCompanyId()));
			JPQLQuery query = from(mold)
					.where(builder)
					.groupBy(mold.operatingStatus)
					.select(Projections.constructor(DashboardChartData.class, mold.operatingStatus, mold.id.count()));
			return query.fetch();
		}
		JPQLQuery query = from(mold)
				.where(builder)
				.groupBy(mold.location.company)
				.select(Projections.constructor(DashboardChartData.class, mold.location.company, mold.id.count()));

		return query.fetch();
	}

	@Override
	public List<MiniComponentData> findMoldsForPMRegistering(){
		QMold mold = QMold.mold;
		QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;

		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(mold.id.notIn(JPAExpressions.select(moldMaintenance.moldId)
						.from(moldMaintenance)
						.where(moldMaintenance.maintenanceStatus.in(MaintenanceStatus.UPCOMING, MaintenanceStatus.OVERDUE)
								.and(moldMaintenance.latest.isTrue())))
		);

		JPQLQuery query = from(mold)
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));

		return query.fetch();
	}

	@Override
	public List<MiniComponentData> findMoldsUnmatchedWithMachine() {
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		JPQLQuery query = from(mold)
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse()))
						.and(mold.machineId.isNull()))
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));

		return query.fetch();
	}

	@Override
	public Page<MiniComponentData> findPageMoldsUnmatchedWithMachine(Predicate predicate, Pageable pageable) {
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder(predicate);

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		JPQLQuery query = from(mold)
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse()))
						.and(mold.machineId.isNull()))
				.select(Projections.constructor(MiniComponentData.class, mold.id, mold.equipmentCode));

		long total = query.fetchCount();

		StringExpression expression = mold.equipmentCode;
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);

		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		List<MiniComponentData> list = query.fetch();

		return new PageImpl<>(list, pageable, total);
	}

	@Override
	public Page<MachineMoldData> findMoldToMatch(Predicate predicate, Pageable pageable) {
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		QMachine machine = QMachine.machine;

		BooleanBuilder builder = new BooleanBuilder(predicate);

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		JPQLQuery query = from(mold).leftJoin(machine).on(machine.id.eq(mold.machineId))
				.where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
				.select(Projections.constructor(MachineMoldData.class, mold.id, mold.equipmentCode, machine.id, machine.machineCode));

		long total = query.fetchCount();

		StringExpression expression = mold.equipmentCode;
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);

		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		List<MachineMoldData> list = query.fetch();

		return new PageImpl<>(list, pageable, total);
	}

	@Override
	public List<MoldMachinePairData> findMoldsMatchedWithMachine() {
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		JPQLQuery query = from(mold)
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse()))
						.and(mold.machineId.isNotNull()))
				.select(Projections.constructor(MoldMachinePairData.class, mold.id, mold.equipmentCode, mold.machineId, mold.machine.machineCode));

		return query.fetch();
	}

	@Override
	public Page<MoldMachinePairData> findPageMoldsMatchedWithMachine(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder(predicate);

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		JPQLQuery query = from(mold)
				.where(builder
						.and(mold.deleted.isNull().or(mold.deleted.isFalse()))
						.and(mold.machineId.isNotNull()))
				.select(Projections.constructor(MoldMachinePairData.class, mold.id, mold.equipmentCode, mold.machineId, mold.machine.machineCode));
		long total = query.fetchCount();

		StringExpression expression = mold.equipmentCode;
		if (properties[0].equalsIgnoreCase("machine")){
			expression = mold.machine.machineCode;
		}
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		List<MoldMachinePairData> list = query.fetch();

		return new PageImpl<>(list, pageable, total);
	}

	@Override
	public List<DashboardChartData> findCycleTimeDashboard(Long moldId, Frequent frequent, Integer limit){
		if(moldId == null) return null;
		QStatistics statistics = QStatistics.statistics;

		NumberPath ct;
		NumberPath<Integer> shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		StringPath dataView = statistics.day;

		if(frequent.equals(Frequent.WEEKLY)){
			dataView = statistics.week;
		}else if(frequent.equals(Frequent.MONTHLY)){
			dataView = statistics.month;
		}

		JPQLQuery query = from(statistics)
				.where(statistics.moldId.eq(moldId)
					.and(ct.gt(0)))
				.groupBy(dataView)
				.orderBy(dataView.desc())
				.limit(limit != null ? limit : 10)
				.select(Projections.constructor(DashboardChartData.class, dataView, ct.multiply(shotCount).sum().divide(shotCount.sum())));

		return query.fetch();
	}
/*

	@Override
	public List<ChartData> findDataExportDynamic(ExportPayload exportPayload, List<Mold> moldList) {
//		Map<Long,List<ChartData>> mapDataListResult= new HashMap<>();

		ChartPayload payload= new ChartPayload();
//		if (exportPayload.getFrequency().equals(DateViewType.HOUR))
//			payload.setChartDataType(ChartDataType.CYCLE_TIME_ANALYSIS);
//		else
		if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.CYCLE_TIME)) {
			payload.addChartDataType(ChartDataType.CYCLE_TIME);
		} else if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.SHORT_COUNT)) {
			payload.addChartDataType(ChartDataType.QUANTITY);
		} else if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.UPTIME)) {
			payload.addChartDataType(ChartDataType.UPTIME);
		} else if (exportPayload.getDataTypes().contains(Const.DYNAMIC_DATA.TOOLING.TEMPERATURE)) {
			payload.addChartDataType(ChartDataType.TEMPERATURE_ANALYSIS);
		} else {
			payload.addChartDataType(ChartDataType.CYCLE_TIME);
		}
		payload.setDateViewType(exportPayload.getFrequency());
		payload.setStartDate(exportPayload.getFromDate());
		payload.setEndDate(exportPayload.getToDate());
		payload.setMoldIdList(exportPayload.getIds());
		List<ChartData> chartDataListTotal = new ArrayList<>();
		if(payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.CYCLE_TIME)){
			chartDataListTotal=getStatisticsData(payload);
		}else {
			chartDataListTotal=findChartData(payload,true);
		}

		return chartDataListTotal;
	}
*/
	@Override
	public List<ChartData> findDataExportDynamicNew(ChartDataType chartDataType,ExportPayload exportPayload, List<Mold> moldList) {
//		Map<Long,List<ChartData>> mapDataListResult= new HashMap<>();

		ChartPayload payload= new ChartPayload();
		payload.addChartDataType(chartDataType);
		payload.setDateViewType(exportPayload.getFrequency());
		payload.setStartDate(exportPayload.getFromDate());
		payload.setEndDate(exportPayload.getToDate());
		payload.setMoldIdList(exportPayload.getIds());
		List<ChartData> chartDataListTotal = new ArrayList<>();
		if(payload.getChartDataType() != null && payload.getChartDataType().contains(ChartDataType.CYCLE_TIME)){
			chartDataListTotal=getStatisticsData(payload);
		}else{
			chartDataListTotal=findChartData(payload,true);
		}

		return chartDataListTotal;
	}

	@Override
	public Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		QMachine machine= QMachine.machine;
		QFileStorage fileStorage = QFileStorage.fileStorage;
		QCustomField customField = QCustomField.customField;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;
		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		if (!StringUtils.isEmpty(payload.getQuery())) {
			builder.and(mold.equipmentCode.containsIgnoreCase(payload.getQuery()));
		}

//        if (payload.getIsDataRequest() != null && payload.getIsDataRequest()) {
//			builder.and(mold.id.in(
//                    JPAExpressions.select(dataRequestObject.objectId)
//                            .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
//                            .where(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION)
//                                    .and(dataRequestObject.objectType.eq(ObjectType.TOOLING)))
//            ));
//        }

		if (payload.getDataRequestId() != null) {
			builder.and(mold.id.in(
					JPAExpressions.select(dataRequestObject.objectId)
							.from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
							.where(
									dataRequestObject.objectType.eq(ObjectType.TOOLING)
											.and(dataRequest.id.eq(payload.getDataRequestId()))
							)
			));
		}

		if (payload.getObjectId() != null) {
			builder.and(mold.id.eq(payload.getObjectId()));
		}

		if (CollectionUtils.isNotEmpty(payload.getCompanyId()) && (payload.getIsDashboard() == null || !payload.getIsDashboard())){
			builder.and(mold.location.company.id.in(payload.getCompanyId()));
		}
		if (payload.getCompanyType() != null) {
			builder.and(mold.location.company.companyType.eq(payload.getCompanyType()));
		}

		if (payload.getIsDashboard() != null && payload.getIsDashboard()) {
			builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
			List<Long> supplierIds = dashboardGeneralFilterUtils.getSupplierIds_Old();
			List<Long> toolmakerIds = dashboardGeneralFilterUtils.getToolMakerIds_Old();
			if(CollectionUtils.isNotEmpty(payload.getCompanyId())){
				if (CollectionUtils.isNotEmpty(supplierIds) && supplierIds.contains(payload.getCompanyId().get(0))) {
					builder.and(mold.supplierCompanyId.in(payload.getCompanyId()));
				}
				if (CollectionUtils.isNotEmpty(toolmakerIds) && toolmakerIds.contains(payload.getCompanyId().get(0))) {
					builder.and(mold.toolMakerCompanyId.in(payload.getCompanyId()));
				}
			}
		}
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()))
				.and(mold.location.company.isEmoldino.isFalse())
		;

		JPQLQuery query = from(mold).leftJoin(machine).on(mold.machineId.eq(machine.id))
				.where(builder);
		NumberExpression<Integer> zeroValue = Expressions.asNumber(0);
		NumberExpression<Integer> numEntered =
				(new CaseBuilder().when(mold.equipmentCode.isEmpty().or(mold.equipmentCode.isNull())).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.designedShot.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.location.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.toolMaker.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.supplier.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.totalCavities.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.contractedCycleTime.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.cycleTimeLimit1.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.cycleTimeLimit2.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.uptimeLimitL1.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.uptimeLimitL2.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.preventCycle.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(mold.preventOverdue.isNull()).then(0).otherwise(1))
				.add(new CaseBuilder().when(
								JPAExpressions
										.select(fileStorage.id.min())
										.from(fileStorage)
										.where(
												fileStorage.refId.eq(mold.id)
														.and(fileStorage.storageType.eq(StorageType.MOLD_MAINTENANCE_DOCUMENT)))
										.isNull())
						.then(0)
						.otherwise(1))
				.add(new CaseBuilder().when(
								JPAExpressions
										.select(fileStorage.id.min())
										.from(fileStorage)
										.where(
												fileStorage.refId.eq(mold.id)
														.and(fileStorage.storageType.eq(StorageType.MOLD_INSTRUCTION_VIDEO)))
										.isNull())
						.then(0)
						.otherwise(1))
//						.add(new CaseBuilder().when(mold.moldParts.isEmpty()).then(0).otherwise(1))
				.add(1)
//						.add(new CaseBuilder().when(mold.moldParts.any().cavity.isNull()).then(0).otherwise(1))
				.add(JPAExpressions
						.select(customFieldValue.id.count())
						.from(customFieldValue)
						.where(customFieldValue.objectId.eq(mold.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
				)
						//BASIC
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.BASIC.toolingLetter) ? (new CaseBuilder().when(mold.toolingLetter.isEmpty().or(mold.toolingLetter.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.BASIC.toolingType) ? (new CaseBuilder().when(mold.toolingType.isEmpty().or(mold.toolingType.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.BASIC.toolingComplexity) ? (new CaseBuilder().when(mold.toolingComplexity.isEmpty().or(mold.toolingComplexity.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.BASIC.madeYear) ? (new CaseBuilder().when(mold.madeYear.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.BASIC.engineers) ? (new CaseBuilder().when(mold.engineersInCharge.isEmpty()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.BASIC.toolDescription) ? (new CaseBuilder().when(mold.toolDescription.isEmpty().or(mold.toolDescription.isNull())).then(0).otherwise(1)) : zeroValue)
						//PHYSICAL
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.PHYSICAL.size) ? (new CaseBuilder().when(mold.size.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.PHYSICAL.weight) ? (new CaseBuilder().when(mold.weight.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.PHYSICAL.injectionMachineId) ? (new CaseBuilder().when(mold.injectionMachineId.isEmpty().or(mold.injectionMachineId.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.PHYSICAL.quotedMachineTonnage) ? (new CaseBuilder().when(mold.quotedMachineTonnage.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(new CaseBuilder().when(
										JPAExpressions
												.select(fileStorage.id.min())
												.from(fileStorage)
												.where(
														fileStorage.refId.eq(mold.id)
																.and(fileStorage.storageType.eq(StorageType.MOLD_CONDITION)))
												.isNull())
								.then(0)
								.otherwise(1))
						//RUNNER SYSTEM
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.runnerType) ? (new CaseBuilder().when(mold.runnerType.isEmpty().or(mold.runnerType.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.runnerMaker) ? (new CaseBuilder().when(mold.runnerMaker.isEmpty().or(mold.runnerMaker.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.weightRunner) ? (new CaseBuilder().when(mold.weightRunner.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.hotRunnerDrop) ? (new CaseBuilder().when(mold.hotRunnerDrop.isEmpty().or(mold.hotRunnerDrop.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.hotRunnerZone) ? (new CaseBuilder().when(mold.hotRunnerZone.isEmpty().or(mold.hotRunnerZone.isNull())).then(0).otherwise(1)) : zeroValue)
						//COST
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.COST.cost) ? (new CaseBuilder().when(mold.cost.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.COST.memo) ? (new CaseBuilder().when(mold.memo.isEmpty().or(mold.memo.isNull())).then(0).otherwise(1)) : zeroValue)
						//SUPPLIER INFORMATION
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.SUPPLIER.labour) ? (new CaseBuilder().when(mold.labour.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.SUPPLIER.shiftsPerDay) ? (new CaseBuilder().when(mold.shiftsPerDay.isEmpty().or(mold.shiftsPerDay.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!payload.getDeletedFields().contains(Const.ColumnCode.tooling.SUPPLIER.productionDays) ? (new CaseBuilder().when(mold.productionDays.isEmpty().or(mold.productionDays.isNull())).then(0).otherwise(1)) : zeroValue)
				;

		JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.TOOLING));
		long totalCustomField = queryCountCustomField.fetchCount();

		NumberExpression rateValue;

		if (forAvg){
			rateValue = (numEntered.floatValue().divide(Const.numberLine.TOOLING - payload.getDeletedFields().size() + totalCustomField)).avg();
		} else {
			rateValue = numEntered.floatValue().divide(Const.numberLine.TOOLING - payload.getDeletedFields().size() + totalCustomField);
		}

		if (payload.isUncompletedData()){
			builder.and(rateValue.doubleValue().lt(1));
		}
		if (CollectionUtils.isNotEmpty(payload.getIds())){
			builder.and(mold.id.in(payload.getIds()));
		}

		if ("rate".equalsIgnoreCase(properties[0])) {
			NumberExpression numberExpression = rateValue;
			OrderSpecifier numberOrder = numberExpression.desc();
			if (directions[0].equals(Sort.Direction.ASC)) {
				numberOrder = numberExpression.asc();
			}
			query.orderBy(numberOrder);
		} else {
			StringExpression expression = mold.equipmentCode;
			OrderSpecifier order = expression.desc();
			if (directions[0].equals(Sort.Direction.ASC)) {
				order = expression.asc();
			}
			query.orderBy(order);
		}

		Long total = query.fetchCount();

		if (total > 0){
			query.select(Projections.constructor(CompletionRateData.class,mold.id,mold.equipmentCode,mold.equipmentCode,numEntered,rateValue, mold.updatedAt, mold));

			if (!forAvg) {
				query.offset(pageable.getOffset());
				query.limit(pageable.getPageSize());
			}
			List<CompletionRateData> list= query.fetch();
			return new PageImpl<>(list, pageable, total);
		} else {
			return new PageImpl<>(new ArrayList<>(), pageable, 0);
		}
	}

	@Override
	public CompletionRateData getCompanyCompletionRate(Long companyId) {
		QMold mold = QMold.mold;
		QMachine machine= QMachine.machine;
		QFileStorage fileStorage = QFileStorage.fileStorage;
        QCustomField customField = QCustomField.customField;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(mold.location.company.id.eq(companyId));
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())).and(mold.location.company.isEmoldino.isFalse());

		JPQLQuery query = from(mold).leftJoin(machine).on(mold.machineId.eq(machine.id))
				.where(builder);
		NumberExpression<Integer> numEntered =
				(new CaseBuilder().when(mold.equipmentCode.isEmpty().or(mold.equipmentCode.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.toolingLetter.isEmpty().or(mold.toolingLetter.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.toolingType.isEmpty().or(mold.toolingType.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.toolingComplexity.isEmpty().or(mold.toolingComplexity.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.madeYear.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.designedShot.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.location.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.engineer.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.toolDescription.isEmpty().or(mold.toolDescription.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.size.isEmpty().or(mold.size.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.weight.isEmpty().or(mold.weight.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.toolMaker.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.injectionMachineId.isEmpty().or(mold.injectionMachineId.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(machine.isNull().or(machine.machineTonnage.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(
								JPAExpressions
										.select(fileStorage.id.min())
										.from(fileStorage)
										.where(
												fileStorage.refId.eq(mold.id)
														.and(fileStorage.storageType.eq(StorageType.MOLD_CONDITION)))
										.isNull())
								.then(0)
								.otherwise(1))
						.add(new CaseBuilder().when(mold.runnerType.isNull().or(mold.runnerType.isEmpty())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.runnerMaker.isEmpty().or(mold.runnerMaker.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.weightRunner.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.hotRunnerDrop.isEmpty().or(mold.hotRunnerDrop.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.hotRunnerZone.isEmpty().or(mold.hotRunnerZone.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.runnerMaker.isEmpty().or(mold.runnerMaker.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.cost.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.memo.isEmpty().or(mold.memo.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.supplier.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.labour.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.shiftsPerDay.isEmpty().or(mold.shiftsPerDay.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.productionDays.isEmpty().or(mold.productionDays.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.contractedCycleTime.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.cycleTimeLimit1.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.cycleTimeLimit2.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.uptimeLimitL1.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.uptimeLimitL2.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.preventCycle.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.preventOverdue.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(
										JPAExpressions
												.select(fileStorage.id.min())
												.from(fileStorage)
												.where(
														fileStorage.refId.eq(mold.id)
																.and(fileStorage.storageType.eq(StorageType.MOLD_MAINTENANCE_DOCUMENT)))
												.isNull())
								.then(0)
								.otherwise(1))
						.add(new CaseBuilder().when(
										JPAExpressions
												.select(fileStorage.id.min())
												.from(fileStorage)
												.where(
														fileStorage.refId.eq(mold.id)
																.and(fileStorage.storageType.eq(StorageType.MOLD_INSTRUCTION_VIDEO)))
												.isNull())
								.then(0)
								.otherwise(1))
//						.add(new CaseBuilder().when(mold.moldParts.isEmpty()).then(0).otherwise(1))
						.add(1)
//						.add(new CaseBuilder().when(mold.moldParts.any().cavity.isNull()).then(0).otherwise(1))
						.add(1)
                        .add(JPAExpressions
                                .select(customFieldValue.id.count())
                                .from(customFieldValue)
                                .where(customFieldValue.objectId.eq(mold.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
                        );

        JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.TOOLING));
        long totalCustomField = queryCountCustomField.fetchCount();

		NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.TOOLING + totalCustomField)).avg();
		if (query.fetchCount() > 0){
			query.select(Projections.constructor(CompletionRateData.class, mold.location.companyId, rateValue, mold.updatedAt));
			return (CompletionRateData) query.fetchFirst();
		} else {
			return new CompletionRateData(companyId, 0D);
		}
	}

	@Override
	public List<Long> findUsedMoldIds(Long partId, String fromDate, String toDate) {
		Asserts.notNull(partId, "partId");
		Asserts.notEmpty(fromDate, "fromDate");
		QStatistics sta = QStatistics.statistics;
		QMold mld = QMold.mold;
		QMoldPart mpt = QMoldPart.moldPart;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(mpt.partId.eq(partId));
		builder.and(sta.day.goe(fromDate));
		if (ObjectUtils.isEmpty(toDate)) {
			builder.and(sta.day.loe(toDate));
		}
		JPQLQuery query = from(sta).innerJoin(mld).on(mld.id.eq(sta.moldId)).innerJoin(mpt).on(mpt.moldId.eq(mld.id)).where(builder).select(sta.moldId).distinct();
		return query.fetch();
	}



	@Override
	public Long countByLastShotAt(Instant from, Instant to, List<Long> filteredIds)
	{
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		JPQLQuery query = from(mold);
		builder.and(mold.deleted.isFalse().or(mold.deleted.isNull()));
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		if (CollectionUtils.isNotEmpty(filteredIds)) {
			builder.and(mold.id.in(filteredIds));
		} else builder.and(mold.id.isNull()); // fake condition for case 0 molds

		if(from != null) {
			BooleanExpression booleanExpression = new CaseBuilder()
					.when(mold.lastShotAt.isNull().and(mold.createdAt.goe(from))).then(true)
					.when(mold.lastShotAt.isNotNull().and(mold.lastShotAt.goe(from))).then(true)
					.otherwise(false);
			builder.and(booleanExpression.isTrue());
		}
		if(to != null) {
			BooleanExpression booleanExpression = new CaseBuilder()
					.when(mold.lastShotAt.isNull().and(mold.createdAt.loe(to))).then(true)
					.when(mold.lastShotAt.isNotNull().and(mold.lastShotAt.loe(to))).then(true)
					.otherwise(false);
			builder.and(booleanExpression.isTrue());
		}
		query.where(builder);

		return query.fetchCount();
	}

	@Override
	public Long countByProduct(Long productId, Long partId, List<Long> supplierId) {
		JPQLQuery query = toQueryByProduct(productId, partId, supplierId, false);
		return query.fetchCount();
	}

	@Override
	public Long countByBrand(Long brandId, Long partId, List<Long> supplierId) {
		JPQLQuery query = toQueryByProduct(brandId, partId, supplierId, true);
		return query.fetchCount();
	}

	@Override
	public Page<Mold> findByProject(Long productId, Long partId, List<Long> supplierId, Pageable pageable) {
		JPQLQuery query = toQueryByProduct(productId, partId, supplierId, false);
		long total = query.fetchCount();
		QMold mold = QMold.mold;
		query.orderBy(mold.id.asc());
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		return new PageImpl<>(query.fetch(), pageable, total);
	}

	@Override
	public Page<Mold> findByBrand(Long brandId, Long partId, List<Long> supplierId, Pageable pageable) {
		JPQLQuery query = toQueryByProduct(brandId, partId, supplierId, true);
		long total = query.fetchCount();
		QMold mold = QMold.mold;
		query.orderBy(mold.id.asc());
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		return new PageImpl<>(query.fetch(), pageable, total);
	}

	private JPQLQuery<Mold> toQueryByProduct(Long productId, Long partId, List<Long> supplierId, boolean brand) {
		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.isMold(filter);
		if (!ObjectUtils.isEmpty(supplierId)) {
			filter.and(Q.mold.companyId.in(supplierId));
		}

		BooleanBuilder subFilter = new BooleanBuilder();
		if (brand) {
			subFilter.and(moldPart.part.categoryId.in(ProductUtils.filterProductByBrand(productId)));
		} else {
			subFilter.and(moldPart.part.categoryId.eq(productId));
		}
		if (partId != null) {
			subFilter.and(moldPart.partId.eq(partId));
		}

		filter.and(Q.mold.id.in(JPAExpressions.select(moldPart.moldId).from(moldPart).where(subFilter)));

		JPQLQuery<Mold> query = from(Q.mold).where(filter);
		return query;
	}

	@Override
	public List<StatisticsPartData> getProjectTotalProduced(Long projectId) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QPart part =QPart.part;

		NumberPath ct;
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

		NumberExpression sumProducePart = (statisticsPart.cavity.multiply(shotCount)).sum();
		NumberExpression quantityRequired = new CaseBuilder().when(part.quantityRequired.isNotNull()).then(part.quantityRequired).otherwise(1L);
		NumberExpression projectProduce = (sumProducePart.divide(quantityRequired)).castToNum(Integer.class);

		JPQLQuery query = from(statistics);
		query.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId));
		query.innerJoin(part).on(part.id.eq(statisticsPart.partId));

		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(statistics.moldId.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		query.where(shotCount.isNotNull().and(shotCount.gt(0)));
		query.where(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
		query.where(ct.gt(0).or(statistics.firstData.isTrue()));
		query.where(statisticsPart.projectId.eq(projectId).and(part.enabled.isTrue()));

		query.groupBy(statisticsPart.partId);

		query.select(Projections.constructor(StatisticsPartData.class, part.id, projectProduce));
		return query.fetch();
	}

	@Override
	public MoldShotData findProducedPartData(Long moldId, String time, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		JPQLQuery query = from(statistics)
				.innerJoin(statisticsPart)
				.on(statistics.id.eq(statisticsPart.statisticsId));
		StringPath timeStr;
		if (frequent.equals(Frequent.DAILY)) {
			query.where(statistics.day.eq(time));
			timeStr = statistics.day;
		} else if (frequent.equals(Frequent.WEEKLY)) {
			query.where(statistics.week.eq(time));
			timeStr = statistics.week;
		} else {
			query.where(statistics.month.eq(time));
			timeStr = statistics.month;
		}
		query.where(statistics.moldId.isNotNull()
				.and(statistics.shotCount.isNotNull())
				.and(statistics.shotCount.gt(0))
				.and(statisticsPart.cavity.isNotNull())
				.and(statisticsPart.cavity.gt(0))
				.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()))
				.and(statistics.moldId.eq(moldId)));

		NumberExpression<Integer> producedPart = (statistics.shotCount.multiply(statisticsPart.cavity)).sum();
		query.select(Projections.constructor(MoldShotData.class, timeStr, statistics.moldId, producedPart));
		return (MoldShotData) query.fetchOne();
	}

	@Override
	public List<CountLocationMold> countLocationMold(TabbedOverviewGeneralFilterPayload payload) {
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold)
				.select(Projections.constructor(CountLocationMold.class, mold.location.countryCode, mold.equipmentCode.count()))
				.groupBy(mold.location.countryCode);
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(payload.getMoldFilter());
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())).and(mold.operatingStatus.isNotNull());
		builder.and( mold.location.countryCode.isNotEmpty());
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
		}
		query.where(builder);
		return query.fetch();
	}

	@Override
	public List<MapChartData> getMapData(TabbedOverviewGeneralFilterPayload payload) {
		QMold mold = QMold.mold;
		QCompany company = QCompany.company;

		JPQLQuery query = from(mold).innerJoin(company).on(mold.supplier.id.eq(company.id));
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(mold.operatingStatus.isNotNull().and(mold.deleted.isNull().or(mold.deleted.isFalse())));
		builder.and(payload.getMoldFilter());

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		query.groupBy(mold.location.id, mold.operatingStatus);
		query.select(Projections.constructor(MapChartData.class, mold.location.name, mold.location.address, mold.operatingStatus, mold.count()));
		query.where(builder);
		return query.fetch();
	}

	@Override
	public Long countByPredicate(Predicate predicate) {
		QMold mold = QMold.mold;
		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
		builder.and(predicate);
		JPQLQuery query = from(mold).where(builder);
		return query.fetchCount();
	}

	@Override
	public Long countByLastShotAt(Instant from, Instant to, TabbedOverviewGeneralFilterPayload payload) {
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		JPQLQuery query = from(mold);
		builder.and(mold.deleted.isFalse().or(mold.deleted.isNull()));
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
		builder.and(payload.getMoldFilter());
		if(from != null) {
			BooleanExpression booleanExpression = new CaseBuilder()
					.when(mold.lastShotAt.isNull().and(mold.createdAt.goe(from))).then(true)
					.when(mold.lastShotAt.isNotNull().and(mold.lastShotAt.goe(from))).then(true)
					.otherwise(false);
			builder.and(booleanExpression.isTrue());
		}
		if(to != null) {
			BooleanExpression booleanExpression = new CaseBuilder()
					.when(mold.lastShotAt.isNull().and(mold.createdAt.loe(to))).then(true)
					.when(mold.lastShotAt.isNotNull().and(mold.lastShotAt.loe(to))).then(true)
					.otherwise(false);
			builder.and(booleanExpression.isTrue());
		}
		query.where(builder);

		return query.fetchCount();
	}

	@Override
	public List<CountLocationMold> countLocationMold() {
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold)
				.select(Projections.constructor(CountLocationMold.class, mold.location.countryCode, mold.equipmentCode.count()))
				.groupBy(mold.location.countryCode);
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())).and(mold.operatingStatus.isNotNull());
		builder.and( mold.location.countryCode.isNotEmpty());
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
		}
		query.where(builder);
		return query.fetch();
	}

	public List<Long> findMoldIdByMachineIdIn(List<Long> machineIds) {
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold).where(mold.machineId.in(machineIds));
		query.select(mold.id);
		return query.fetch();
	}

	@Override
	public List<Long> findMoldIdByMachineIn(List<Machine> machines) {
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold).where(mold.machine.in(machines));
		query.select(mold.id);
		return query.fetch();
	}

	@Override
	public List<Long> findMoldIdByStartEndAndMachineIn(List<Machine> machines, String start) {
		QMold mold = QMold.mold;
		QMachineMoldMatchingHistory machineMoldMatchingHistory = QMachineMoldMatchingHistory.machineMoldMatchingHistory;
		JPQLQuery query = from(mold).where(mold.machine.in(machines).and(mold.id.in(JPAExpressions
				.select(machineMoldMatchingHistory.mold.id)
				.from(machineMoldMatchingHistory)
				.where(machineMoldMatchingHistory.matchDay.loe(start))
				.orderBy(machineMoldMatchingHistory.matchTime.desc())
				.limit(1))));
		query.select(mold.id);
		return query.fetch();
	}


	@Override
	public Long findMoldIdByMachineAndTime(Long machineId, Instant time) {
		QMold mold = QMold.mold;
		QMachineMoldMatchingHistory machineMoldMatchingHistory = QMachineMoldMatchingHistory.machineMoldMatchingHistory;

		BooleanBuilder sub1 = new BooleanBuilder(machineMoldMatchingHistory.matchTime.loe(time).and(machineMoldMatchingHistory.unmatchTime.goe(time)));
		BooleanBuilder sub2 = new BooleanBuilder(machineMoldMatchingHistory.matchTime.loe(time).and(machineMoldMatchingHistory.unmatchTime.isNull()));

		JPQLQuery query = from(mold).where(mold.id.in(JPAExpressions
				.select(machineMoldMatchingHistory.mold.id)
				.from(machineMoldMatchingHistory)
				.where(machineMoldMatchingHistory.machine.id.eq(machineId).and(sub1.or(sub2)))
				.orderBy(machineMoldMatchingHistory.matchTime.desc())
				.limit(1)));
		query.select(mold.id);
		return query.fetchOne() != null ? (long) query.fetchOne() : null;
	}

	@Override
	public List<Mold> findMoldOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold).where(predicate);
		NumberExpression<Integer> expression = new CaseBuilder()
				.when(mold.operatingStatus.isNull())
				.then(0)
				.when(mold.operatingStatus.eq(OperatingStatus.WORKING))
				.then(1)
				.when(mold.operatingStatus.eq(OperatingStatus.IDLE))
				.then(2)
				.when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING))
				.then(3)
				.otherwise(4);
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<Mold> findMoldOrderByUpperTierCompanies(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		QAccessCompanyRelation accessCompanyRelation = QAccessCompanyRelation.accessCompanyRelation;
		JPQLQuery query = from(mold)
				.leftJoin(accessCompanyRelation).on(mold.companyId.eq(accessCompanyRelation.companyId).and(accessCompanyRelation.id.in(JPAExpressions
						.select(accessCompanyRelation.id.min())
						.from(accessCompanyRelation)
						.groupBy(accessCompanyRelation.companyId))))
				.where(predicate);
		StringExpression name = accessCompanyRelation.accessHierarchyParent.company.companyCode;
		OrderSpecifier<String> order = name.desc();
		if (directions[0].equals(Sort.Direction.ASC)) {
			order = name.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<Mold> findMoldOrderBySlDepreciation(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold).where(predicate);
		NumberExpression<Integer> expression = new CaseBuilder()
				.when(mold.poDate.isNotNull().and(mold.lifeYears.isNotNull()))
				.then(Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", mold.poDate, Instant.now()).divide(3600 * 24 * 365).ceil().divide(mold.lifeYears))
				.otherwise(Expressions.asNumber(0));
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<Mold> findMoldOrderByUpDepreciation(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold).where(predicate);
		NumberExpression<Float> expression = new CaseBuilder()
				.when(mold.poDate.isNotNull().and(mold.lastShot.isNotNull()))
				.then(mold.lastShot.castToNum(Float.class).divide(mold.designedShot))
				.otherwise(Expressions.asNumber(0F));
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldEfficiency> findMoldEfficiencyOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;
		QMold mold = moldEfficiency.mold;
		JPQLQuery query = from(moldEfficiency).where(predicate);
		NumberExpression<Integer> expression = new CaseBuilder()
				.when(mold.operatingStatus.isNull())
				.then(0)
				.when(mold.operatingStatus.eq(OperatingStatus.WORKING))
				.then(1)
				.when(mold.operatingStatus.eq(OperatingStatus.IDLE))
				.then(2)
				.when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING))
				.then(3)
				.otherwise(4);
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<MoldEfficiency> findMoldEfficiencyOrderByStatus(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMoldEfficiency moldEfficiency = QMoldEfficiency.moldEfficiency;
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		JPQLQuery query = from(moldEfficiency)
				.leftJoin(mold).on(moldEfficiency.moldId.eq(mold.id))
				.leftJoin(counter).on(mold.counterId.eq(counter.id))
				.where(predicate);
		StringExpression expression;
		JPQLQuery<Long> counterMatchedIdExpression = JPAExpressions.select(mold.counterId).from(mold).where(mold.counterId.isNotNull());
		if (properties[0].equals("toolingStatus")) {
			expression = new CaseBuilder()
					.when(mold.operatingStatus.eq(OperatingStatus.WORKING).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
					.then("IN_PRODUCTION")
					.when(mold.operatingStatus.eq(OperatingStatus.IDLE).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
					.then("IDLE")
					.when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
					.then("NOT_WORKING")
					.when(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))
					.then("SENSOR_DETACHED")
					.when(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.eq(OperatingStatus.DISCONNECTED).or(mold.operatingStatus.eq(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
					.then("SENSOR_OFFLINE")
					.when(counter.operatingStatus.isNull().and(mold.operatingStatus.isNull()).and(counter.id.in(counterMatchedIdExpression))).then("ON_STANDBY")
					.when(counter.id.notIn(counterMatchedIdExpression))
					.then("NO_SENSOR")
					.otherwise("");
		} else {
			expression = new CaseBuilder().when(mold.counterId.isNull())
					.then("NOT_INSTALLED")
					.when(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))
					.then("DETACHED")
					.otherwise("INSTALLED");
		}
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public List<Location> findLocationByMoldIdIn(List<Long> moldIdList) {
		QMold mold = QMold.mold;
		JPQLQuery<Location> query = from(mold).select(mold.location).where(mold.id.in(moldIdList));
		return query.fetch();
	}

	@Override
	public Optional<Location> findLocationByMoldId(Long id) {
		QMold mold = QMold.mold;
		JPQLQuery<Location> query = from(mold).select(mold.location).where(mold.id.eq(id));
		return Optional.ofNullable(query.fetchOne() == null ? null : query.fetchOne());
	}

	@Override
	public List<Long> findAllIdByPredicate(Predicate predicate) {
		QMold mold = QMold.mold;
		JPQLQuery<Long> query = from(mold).where(predicate).select(mold.id);
		return query.fetch();
	}

	@Override
	public Long countByProductIdIn(List<Long> productIdList) {
		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.isMold(filter);

		BooleanBuilder subFilter = new BooleanBuilder();
		subFilter.and(moldPart.part.categoryId.in(productIdList));

		filter.and(Q.mold.id.in(JPAExpressions.select(moldPart.moldId).from(moldPart).where(subFilter)));

		JPQLQuery<Mold> query = from(Q.mold).where(filter);
		return query.fetchCount();
	}

	@Override
	public List<Mold> findMoldOrderByStatus(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;
		JPQLQuery query = from(mold).leftJoin(counter).on(mold.counterId.eq(counter.id)).where(predicate);
		StringExpression expression;
		if (properties[0].equals("toolingStatus")) {
			expression = new CaseBuilder()
					.when(mold.operatingStatus.eq(OperatingStatus.WORKING).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.isNotNull()))
					.then("IN_PRODUCTION")
					.when(mold.operatingStatus.eq(OperatingStatus.IDLE).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.isNotNull()))
					.then("IDLE")
					.when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.isNotNull()))
					.then("NOT_WORKING")
					.when(counter.equipmentStatus.eq(EquipmentStatus.DETACHED).and(counter.id.isNotNull()))
					.then("SENSOR_DETACHED")
					.when(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.eq(OperatingStatus.DISCONNECTED).or(mold.operatingStatus.eq(OperatingStatus.DISCONNECTED))).and(counter.id.isNotNull()))
					.then("SENSOR_OFFLINE")
					.when(counter.operatingStatus.isNull().and(mold.operatingStatus.isNull()).and(counter.id.isNotNull())).then("ON_STANDBY")
					.when(mold.counterId.isNull())
					.then("NO_SENSOR")
					.otherwise("");
		} else {
			expression = new CaseBuilder().when(mold.counterId.isNull())
					.then("NOT_INSTALLED")
					.when(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))
					.then("DETACHED")
					.otherwise("INSTALLED");
		}

		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}

	@Override
	public Long countAllIncompleteData(List<String> deletedFieldMold) {
		return getQueryIncompleteData(deletedFieldMold).fetchCount();
	}

	@Override
	public List<Mold> getAllIncompleteData(List<String> deletedFieldMold) {
		return getQueryIncompleteData(deletedFieldMold).fetch();
	}

	private JPQLQuery getQueryIncompleteData(List<String> deletedFieldMold) {
		QMold mold = QMold.mold;
		QFileStorage fileStorage = QFileStorage.fileStorage;
		QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
		QCustomField customField = QCustomField.customField;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())).and(mold.location.company.isEmoldino.isFalse());

		NumberExpression<Integer> zeroValue = Expressions.asNumber(0);
		NumberExpression<Integer> numEntered =
				(new CaseBuilder().when(mold.equipmentCode.isEmpty().or(mold.equipmentCode.isNull())).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.designedShot.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.location.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.toolMaker.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.supplier.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.totalCavities.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.contractedCycleTime.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.cycleTimeLimit1.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.cycleTimeLimit2.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.uptimeLimitL1.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.uptimeLimitL2.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.preventCycle.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(mold.preventOverdue.isNull()).then(0).otherwise(1))
						.add(new CaseBuilder().when(
										JPAExpressions
												.select(fileStorage.id.min())
												.from(fileStorage)
												.where(
														fileStorage.refId.eq(mold.id)
																.and(fileStorage.storageType.eq(StorageType.MOLD_MAINTENANCE_DOCUMENT)))
												.isNull())
								.then(0)
								.otherwise(1))
						.add(new CaseBuilder().when(
										JPAExpressions
												.select(fileStorage.id.min())
												.from(fileStorage)
												.where(
														fileStorage.refId.eq(mold.id)
																.and(fileStorage.storageType.eq(StorageType.MOLD_INSTRUCTION_VIDEO)))
												.isNull())
								.then(0)
								.otherwise(1))
//						.add(new CaseBuilder().when(mold.moldParts.isEmpty()).then(0).otherwise(1))
						.add(1)
//						.add(new CaseBuilder().when(mold.moldParts.any().cavity.isNull()).then(0).otherwise(1))
						.add(JPAExpressions
								.select(customFieldValue.id.count())
								.from(customFieldValue)
								.where(customFieldValue.objectId.eq(mold.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
						)
						//BASIC
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.BASIC.toolingLetter) ? (new CaseBuilder().when(mold.toolingLetter.isEmpty().or(mold.toolingLetter.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.BASIC.toolingType) ? (new CaseBuilder().when(mold.toolingType.isEmpty().or(mold.toolingType.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.BASIC.toolingComplexity) ? (new CaseBuilder().when(mold.toolingComplexity.isEmpty().or(mold.toolingComplexity.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.BASIC.madeYear) ? (new CaseBuilder().when(mold.madeYear.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.BASIC.engineers) ? (new CaseBuilder().when(mold.engineersInCharge.isEmpty()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.BASIC.toolDescription) ? (new CaseBuilder().when(mold.toolDescription.isEmpty().or(mold.toolDescription.isNull())).then(0).otherwise(1)) : zeroValue)
						//PHYSICAL
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.PHYSICAL.size) ? (new CaseBuilder().when(mold.size.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.PHYSICAL.weight) ? (new CaseBuilder().when(mold.weight.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.PHYSICAL.injectionMachineId) ? (new CaseBuilder().when(mold.injectionMachineId.isEmpty().or(mold.injectionMachineId.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.PHYSICAL.quotedMachineTonnage) ? (new CaseBuilder().when(mold.quotedMachineTonnage.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(new CaseBuilder().when(
										JPAExpressions
												.select(fileStorage.id.min())
												.from(fileStorage)
												.where(
														fileStorage.refId.eq(mold.id)
																.and(fileStorage.storageType.eq(StorageType.MOLD_CONDITION)))
												.isNull())
								.then(0)
								.otherwise(1))
						//RUNNER SYSTEM
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.runnerType) ? (new CaseBuilder().when(mold.runnerType.isEmpty().or(mold.runnerType.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.runnerMaker) ? (new CaseBuilder().when(mold.runnerMaker.isEmpty().or(mold.runnerMaker.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.weightRunner) ? (new CaseBuilder().when(mold.weightRunner.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.hotRunnerDrop) ? (new CaseBuilder().when(mold.hotRunnerDrop.isEmpty().or(mold.hotRunnerDrop.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.RUNNER_SYSTEM.hotRunnerZone) ? (new CaseBuilder().when(mold.hotRunnerZone.isEmpty().or(mold.hotRunnerZone.isNull())).then(0).otherwise(1)) : zeroValue)
						//COST
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.COST.cost) ? (new CaseBuilder().when(mold.cost.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.COST.memo) ? (new CaseBuilder().when(mold.memo.isEmpty().or(mold.memo.isNull())).then(0).otherwise(1)) : zeroValue)
						//SUPPLIER INFORMATION
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.SUPPLIER.labour) ? (new CaseBuilder().when(mold.labour.isNull()).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.SUPPLIER.shiftsPerDay) ? (new CaseBuilder().when(mold.shiftsPerDay.isEmpty().or(mold.shiftsPerDay.isNull())).then(0).otherwise(1)) : zeroValue)
						.add(!deletedFieldMold.contains(Const.ColumnCode.tooling.SUPPLIER.productionDays) ? (new CaseBuilder().when(mold.productionDays.isEmpty().or(mold.productionDays.isNull())).then(0).otherwise(1)) : zeroValue)
				;

		JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.TOOLING));
		long totalCustomField = queryCountCustomField.fetchCount();

		NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.TOOLING - deletedFieldMold.size() + totalCustomField)).avg();
		builder.and(rateValue.lt(1));

		return from(mold).where(builder);
	}

	public List<Mold> findAllMoldHaveOPMissMatch() {
		QMold mold = QMold.mold;
		QCounter counter = QCounter.counter;

		JPQLQuery query = from(mold).innerJoin(counter)
				.on(mold.counterId.eq(counter.id))
				.where(mold.operatingStatus.ne(counter.operatingStatus)
						.or(mold.operatingStatus.isNull().and(counter.operatingStatus.isNotNull()))
						.or(mold.operatingStatus.isNotNull().and(counter.operatingStatus.isNull()))
				);

		return query.fetch();
	}

	@Override
	public Long countByMasterFilter(Predicate predicate, ActiveStatus activeStatus) {
		if (activeStatus == null) {
			activeStatus = ActiveStatus.ENABLED;
		}
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.id.countDistinct())//
				.from(Q.mold)//
				.where(predicate);

		Set<EntityPathBase<?>> join = new HashSet<>();
		if (ActiveStatus.ENABLED.equals(activeStatus)) {
			QueryUtils.applyMoldFilter(query, join, "COMMON");
		} else if (ActiveStatus.DISPOSED.equals(activeStatus)) {
			QueryUtils.applyMoldDisposedFilter(query, join, "COMMON");
		} else {
			QueryUtils.applyMoldDisabledFilter(query, join, "COMMON");
		}

		return query.fetchOne();
	}

	@Deprecated
	public Long countByMasterFilter(Predicate predicate, Boolean deleted) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.id.countDistinct())//
				.from(Q.mold)//
				.where(predicate);

		Set<EntityPathBase<?>> join = new HashSet<>();
		if (deleted != null && deleted) {
			QueryUtils.applyMoldDisabledFilter(query, join, "COMMON");
		} else {
			QueryUtils.applyMoldFilter(query, join, "COMMON");
		}

		return query.fetchOne();
	}

	@Override
	public Page<Mold> findAllByMasterFilter(Predicate predicate, ActiveStatus activeStatus, Pageable pageable) {
        QMoldPmPlan qMoldPmPlan = QMoldPmPlan.moldPmPlan;
        String property = QueryUtils.getFirstSortProperty(pageable);
		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);

		QCycleTimeDeviation qCtd = QCycleTimeDeviation.cycleTimeDeviation;
		NumberExpression<Float> ctCompliance = Expressions.asNumber(//
				JPAExpressions//
						.select(qCtd.withinL1ToleranceSc.abs().floatValue().sum().divide(qCtd.shotCount.abs().floatValue().sum()).multiply(100d))//
						.from(qCtd)//
						.where(qCtd.moldId.eq(Q.mold.id))//
		).floatValue();

		QMoldMaintenance qMoldMaintenance = QMoldMaintenance.moldMaintenance;

		NumberExpression<Integer> preventCycle = new CaseBuilder().when(Q.mold.preventCycle.isNull()).then(1).otherwise(Q.mold.preventCycle);
		NumberExpression<Integer> lastShot = new CaseBuilder().when(Q.mold.lastShot.isNull()).then(0).otherwise(Q.mold.lastShot);
		NumberExpression<Integer> mAccumulatedShot = new CaseBuilder().when(qMoldMaintenance.accumulatedShot.isNull()).then(0).otherwise(qMoldMaintenance.accumulatedShot).max();
		NumberExpression<Integer> shotSinceCompletion = new CaseBuilder().when(lastShot.goe(mAccumulatedShot)).then(lastShot.subtract(mAccumulatedShot)).otherwise(0);
		NumberExpression<Integer> pmCheckpoint = new CaseBuilder().when(qMoldMaintenance.isNotNull()).then(preventCycle.add(mAccumulatedShot))
				.otherwise(((shotSinceCompletion.divide(preventCycle).add(1)).floor().multiply(preventCycle)).add(mAccumulatedShot));
//		NumberExpression<Integer> countUp = new CaseBuilder().when(shotSinceCompletion.lt(preventCycle)).then(shotSinceCompletion).otherwise(preventCycle);
		NumberExpression<Integer> countUp = new CaseBuilder()
				.when(qMoldMaintenance.isNotNull())
				.then(new CaseBuilder().when(shotSinceCompletion.lt(preventCycle)).then(shotSinceCompletion).otherwise(preventCycle))
				.otherwise(shotSinceCompletion.mod(preventCycle));
		NumberExpression untilNextPmRate = new CaseBuilder().when(qMoldPmPlan.pmStrategy.eq(PM_STRATEGY.TIME_BASED))//check config shot base
				.then(Expressions.numberTemplate(Float.class,"null"))
				.otherwise(countUp.floatValue().divide(preventCycle).multiply(100));
		NumberExpression<Float> lastShotDivCheckpoint =new CaseBuilder().when(qMoldPmPlan.pmStrategy.eq(PM_STRATEGY.TIME_BASED))//check config shot base
				.then(Expressions.numberTemplate(Float.class,"null"))
				.otherwise(lastShot.floatValue().divide(pmCheckpoint));
		NumberExpression dueDate;
		{
			NumberExpression<Integer> activePeriod= Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", //
					Q.mold.operatedStartAt, Instant.now()).divide(24*3600f);
			NumberExpression<Integer> d=new CaseBuilder()
					.when(lastShot.goe(pmCheckpoint))
					.then((shotSinceCompletion.subtract(preventCycle)).multiply(activePeriod).divide(lastShot))
					.otherwise(
							new CaseBuilder()
							.when(qMoldMaintenance.isNotNull().and(qMoldMaintenance.dueDate.isNotNull()))
							.then(qMoldMaintenance.dueDate)
							.otherwise((preventCycle.subtract(countUp)).multiply(activePeriod).divide(lastShot))
					);
			dueDate = new CaseBuilder()
					.when(qMoldPmPlan.pmStrategy.eq(PM_STRATEGY.TIME_BASED))//check config shot base
					.then(Expressions.numberTemplate(Integer.class,"null"))
					.when(lastShot.gt(0).and(Q.mold.operatedStartAt.isNotNull())).then(d)
					.otherwise(Expressions.nullExpression());
		}

/*
		NumberExpression pmCheckPointValue = new CaseBuilder()//
				.when(qMoldMaintenance.periodStart.isNotNull().and(qMoldMaintenance.mold.preventUpcoming.isNotNull()))//
				.then(qMoldMaintenance.periodStart.add(qMoldMaintenance.mold.preventUpcoming))//
				.when(qMoldMaintenance.periodEnd.isNotNull().and(qMoldMaintenance.mold.preventOverdue.isNotNull()))//
				.then(qMoldMaintenance.periodEnd.subtract(qMoldMaintenance.mold.preventOverdue))//
				.when(qMoldMaintenance.preventCycle.isNotNull())//
				.then(qMoldMaintenance.preventCycle)//
				.when(Q.mold.preventCycle.isNotNull())//
				.then(Q.mold.preventCycle)//
				.otherwise(1);
		NumberExpression lastShotCheckpoint = qMoldMaintenance.mold.lastShot.floatValue().divide(pmCheckPointValue);
		NumberExpression utilNextPm = qMoldMaintenance.lastShotMade.floatValue().divide(qMoldMaintenance.mold.preventCycle.coalesce(1));
		NumberExpression dueDate = qMoldMaintenance.dueDate;
*/

		if ("dueDate".equals(property)) {
			Direction direction = QueryUtils.getFirstSortDirection(pageable);
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), direction,  "maintenanceStatus","dueDate");
		}

		JPQLQuery<Tuple> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold
						,ctCompliance.as("ctCompliance")
						,dueDate.as("dueDate")
						,preventCycle
						,mAccumulatedShot
						,lastShot
						,pmCheckpoint
						,countUp
						,untilNextPmRate
						,lastShotDivCheckpoint
						,qMoldPmPlan.pmStrategy
				).from(Q.mold)//
				.distinct()//
				.leftJoin(qMoldMaintenance).on(//
						qMoldMaintenance.moldId.eq(Q.mold.id)//
								.and(qMoldMaintenance.maintenanceStatus.eq(MaintenanceStatus.DONE))//
								.and(qMoldMaintenance.latest.isTrue()))
				.leftJoin(qMoldPmPlan).on(Q.mold.id.eq(qMoldPmPlan.moldId))//check config shot base
				.groupBy(Q.mold.id)
				.where(predicate);

		Set<EntityPathBase<?>> join = new HashSet<>();
		if (ActiveStatus.ENABLED.equals(activeStatus)) {
			QueryUtils.applyMoldFilter(query, join, "COMMON");
		} else if (ActiveStatus.DISPOSED.equals(activeStatus)) {
			QueryUtils.applyMoldDisposedFilter(query, join, "COMMON");
		} else {
			QueryUtils.applyMoldDisabledFilter(query, join, "COMMON");
		}

		if (property != null) {
			if (property.equals("supplier.name")) {
				QueryUtils.leftJoin(query, join, Q.supplier, () -> Q.supplier.id.eq(Q.mold.supplierCompanyId));
			} else if (property.equals("location.company.name")) {
				QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));
				QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.location.companyId));
			} else if (property.equals("location.name")) {
				QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));
			} else if (property.equals("part")) {
				QueryUtils.leftJoin(query, join, Q.moldPart, () -> Q.moldPart.moldId.eq(Q.mold.id));
				QueryUtils.leftJoin(query, join, Q.part, () -> Q.part.id.eq(Q.moldPart.partId));
			} else if (property.equals("machine.machineCode")) {
				QueryUtils.leftJoin(query, join, Q.machine, () -> Q.machine.id.eq(Q.mold.machineId));
			} else if (property.equals("sensorStatus") || property.equals("counter.equipmentCode")) {
				QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
			} else if (property.equals("areaName")) {
				QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.mold.areaId));
			}
		}

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("equipmentCode", Q.mold.equipmentCode)//
				.put("supplierMoldCode", Q.mold.supplierMoldCode)//
				.put("supplier.name", Q.supplier.name)//
				.put("location.company.name", Q.company.name)//
				.put("location.name", Q.mold.location.name)//
				.put("part", Q.part.partCode)//
				.put("machine.machineCode", Q.machine.machineCode)//
				.put("counter.equipmentCode", Q.counter.equipmentCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("sensorStatus", getSensorStatus())//counterStatus
				.put(SpecialSortProperty.operatingStatus, getOperatingStatus(Q.mold))//
				.put("lastShot", Q.mold.lastShot)//
				.put("activatedAt", Q.mold.activatedAt)//
				.put("installedAt", Q.mold.installedAt)//
				.put("installedBy", Q.mold.installedBy)//
				.put("lastCycleTime", Q.mold.lastCycleTime)//
				.put("cycleTimeLimit1", Q.mold.cycleTimeLimit1)//
				.put("cycleTimeLimit2", Q.mold.cycleTimeLimit2)//
				.put("weightedAverageCycleTime", Q.mold.weightedAverageCycleTime)//
				.put("uptimeTarget", Q.mold.uptimeTarget)//
				.put("engineer", Q.mold.engineer)//
				.put("weightRunner", Q.mold.weightRunner)//
				.put("contractedCycleTime", Q.mold.contractedCycleTime)//
				.put("toolmakerContractedCycleTime", Q.mold.toolmakerContractedCycleTime)//
				.put("designedShot", Q.mold.designedShot)//
				.put("hotRunnerDrop", Q.mold.hotRunnerDrop)//
				.put("hotRunnerZone", Q.mold.hotRunnerZone)//
				.put("injectionMachineId", Q.mold.injectionMachineId)//
				.put("labour", Q.mold.labour)//
				.put("lastShotAt", Q.mold.lastShotAt)//
				.put("quotedMachineTonnage", Q.mold.quotedMachineTonnage)//
				.put("preventCycle", Q.mold.preventCycle)//
				.put("runnerMaker", Q.mold.runnerMaker)//
				.put("maxCapacityPerWeek", Q.mold.maxCapacityPerWeek)//
				.put("shiftsPerDay", Q.mold.shiftsPerDay)//
				.put("productionDays", Q.mold.productionDays)//
				.put("toolDescription", Q.mold.toolDescription)//
				.put("size", Q.mold.size)//
				.put("weight", Q.mold.weight)//
				.put("toolingComplexity", Q.mold.toolingComplexity)//
				.put("toolingLetter", Q.mold.toolingLetter)//
				.put("toolingType", Q.mold.toolingType)//
				.put("toolMaker.name", Q.toolmaker.name)//
				.put("runnerType", Q.mold.runnerType)//
				.put("preventUpcoming", Q.mold.preventUpcoming)//
				.put("uptimeLimitL1", Q.mold.uptimeLimitL1)//
				.put("uptimeLimitL2", Q.mold.uptimeLimitL2)//
				.put("utilizationRate", Q.mold.lastShot.floatValue().divide(Q.mold.designedShot)) //Q.mold.utilizationRate
				.put("madeYear", Q.mold.madeYear)//
				.put("memo", Q.mold.memo)//
				.put("cost", Q.mold.cost)//
				.put("accumulatedMaintenanceCost", Q.mold.accumulatedMaintenanceCost)//
				.put("lastMaintenanceDate", Q.mold.lastMaintenanceDate)//
				.put("lastRefurbishmentDate", Q.mold.lastRefurbishmentDate)//
				.put("tco", Q.mold.cost.floatValue().add(Q.mold.accumulatedMaintenanceCost))//
				.put("ctCompliance", Expressions.numberPath(Double.class, "ctCompliance"))//
				.put("lastShotCheckpoint", untilNextPmRate)//Accumulated Shots/PM Checkpoint (% value)
				.put("utilNextPm", untilNextPmRate)//PM checkpoint progress
				.put("dueDate", Expressions.numberPath(Integer.class, "dueDate")) //pmCheckpointPrediction
				.put("areaName", Q.area.name)//

				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.id.desc());
		QueryResults<Tuple> results = query.fetchResults();

		List<Mold> moldList = results.getResults().stream().map(tuple -> {
			int col=0;
			Mold m = tuple.get(col++, Mold.class);
			Double ctc = ValueUtils.toDouble(tuple.get(col++, Float.class), null);
			Integer dueDateVal = ValueUtils.toInteger(tuple.get(col++, Float.class), null);
			Double preventCycleVal = ValueUtils.toDouble(tuple.get(col++, Float.class), null);
			Double mAccumulatedShotVal = ValueUtils.toDouble(tuple.get(col++, Float.class), null);
			col++;//lastShot
			Integer pmCheckPointVal = tuple.get(col++, Integer.class);
			Integer countUpVal = ValueUtils.toInteger(tuple.get(col++, Float.class), null);
			Double untilNextPmVal = ValueUtils.toDouble(tuple.get(col++, Float.class), null);
			Double lastShotDivCheckpointVal = ValueUtils.toDouble(tuple.get(col++, Float.class), null);
			PM_STRATEGY pmStrategy =tuple.get(col++, PM_STRATEGY.class);

			m.setCtCompliance(ctc);
			m.setPmCheckpoint(pmCheckPointVal);
			m.setUntilNextPm(countUpVal);
			m.setPmStrategy(pmStrategy);
			m.setPmCheckpointPrediction(dueDateVal);

			return m;
		}).collect(Collectors.toList());

		return new PageImpl<>(moldList, pageable, results.getTotal());
	}

	@Override
	public List<Long> findAllIdsByMasterFilter(Predicate predicate, ActiveStatus activeStatus) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.id)//
				.from(Q.mold)//
				.distinct()//
				.where(predicate);

		Set<EntityPathBase<?>> join = new HashSet<>();
		if (ActiveStatus.ENABLED.equals(activeStatus)) {
			QueryUtils.applyMoldFilter(query, join, "COMMON");
		} else if (ActiveStatus.DISPOSED.equals(activeStatus)) {
			QueryUtils.applyMoldDisposedFilter(query, join, "COMMON");
		} else {
			QueryUtils.applyMoldDisabledFilter(query, join, "COMMON");
		}

		QueryResults<Long> results = query.fetchResults();
		return results.getResults();
	}

	private static StringExpression getSensorStatus() {
		StringExpression expression = new CaseBuilder()//
				.when(Q.mold.counterId.isNull())//
				.then("NOT_INSTALLED")//
				.when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED))//
				.then("DETACHED")//
				.otherwise("INSTALLED");
		return expression;
	}

	private static NumberExpression<Integer> getOperatingStatus(QMold mold) {
		NumberExpression<Integer> expression = new CaseBuilder()//
				.when(mold.operatingStatus.isNull()).then(0)//
				.when(mold.operatingStatus.eq(OperatingStatus.WORKING)).then(1)//
				.when(mold.operatingStatus.eq(OperatingStatus.IDLE)).then(2)//
				.when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING)).then(3)//
				.otherwise(4);
		return expression;
	}
	@Override
	public List<Mold> findMoldOrderByTco(Predicate predicate, Pageable pageable) {
		String[] properties = {""};
		Sort.Direction[] directions = {Sort.Direction.DESC};
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QMold mold = QMold.mold;
		JPQLQuery query = from(mold).where(predicate);
		NumberExpression<Float> expression=mold.cost.floatValue().add(mold.accumulatedMaintenanceCost);
		OrderSpecifier order = expression.desc();
		if(directions[0].equals(Sort.Direction.ASC)){
			order = expression.asc();
		}
		query.orderBy(order);
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());

		return query.fetch();
	}


private static JPQLQuery<?> getShotCountSum(QStatistics statistics, StringPath viewData) {
		QStatistics subStatistics = new QStatistics("s2");

		JPQLQuery<?> subQuery = JPAExpressions//
				.select(subStatistics.shotCount.sum())//
				.from(subStatistics)//
				.where(subStatistics.moldId.eq(statistics.moldId));//
		if (viewData.equals(statistics.year)) {
			subQuery.where(subStatistics.year.eq(statistics.year));
		} else if (viewData.equals(statistics.month)) {
			subQuery.where(subStatistics.month.eq(statistics.month));
		} else if (viewData.equals(statistics.week)) {
			subQuery.where(subStatistics.week.eq(statistics.week));
		} else if (viewData.equals(statistics.day)) {
			subQuery.where(subStatistics.day.eq(statistics.day));
		} else if (viewData.equals(statistics.hour)) {
			subQuery.where(subStatistics.hour.eq(statistics.hour));
		}

		return subQuery;
	}

}