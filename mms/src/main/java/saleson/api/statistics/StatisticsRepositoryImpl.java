package saleson.api.statistics;

import static saleson.api.mold.MoldRepositoryImpl.divideCycleTime;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.util.Pair;

import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAUpdateClause;

import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.machine.payload.PerformanceRawData;
import saleson.api.statistics.payload.StatisticsDaily;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.FrequentUsage;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.StringUtils;
import saleson.dto.TransactionDTO;
import saleson.model.Mold;
import saleson.model.QCdata;
import saleson.model.QCompany;
import saleson.model.QMold;
import saleson.model.QMoldDownTime;
import saleson.model.QMoldPart;
import saleson.model.QMoldStandardValue;
import saleson.model.QPart;
import saleson.model.QStatistics;
import saleson.model.QStatisticsPart;
import saleson.model.QStatisticsPreset;
import saleson.model.QVCompanyTargetUptimeHour;
import saleson.model.QVPartTargetUptimeHour;
import saleson.model.Statistics;
import saleson.model.StatisticsAccumulatingShot;
import saleson.model.data.ChartData;
import saleson.model.data.ChartDataOte;
import saleson.model.data.MoldCapacityReportData;
import saleson.model.data.ProductionQuantityData;
import saleson.model.data.QStatisticsSummary;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioDetails;
import saleson.model.data.dashboard.uptimeRatio.UptimeRatioTooling;
import saleson.model.rejectedPartRate.QProducedPart;
import saleson.service.util.DateTimeUtils;

public class StatisticsRepositoryImpl extends QuerydslRepositorySupport implements StatisticsRepositoryCustom {

	@PersistenceContext
	EntityManager em;

    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    public StatisticsRepositoryImpl() {
        super(Statistics.class);
    }

	@Modifying
	public void resetStoredAllValidShotVals() {
		QStatistics statistics = QStatistics.statistics;
		QCdata cdata = QCdata.cdata;
		QMoldStandardValue moldStandardValue = QMoldStandardValue.moldStandardValue;

		new JPAUpdateClause(em, cdata).where(cdata.id.gt(0)).setNull(cdata.ctVal).setNull(cdata.cttVal).setNull(cdata.llctVal).setNull(cdata.ulctVal).execute();

		new JPAUpdateClause(em, statistics).where(statistics.id.gt(0)).setNull(statistics.ctVal).setNull(statistics.shotCountVal).setNull(statistics.llctVal)
				.setNull(statistics.ulctVal).execute();

		new JPADeleteClause(em, moldStandardValue).where(moldStandardValue.id.gt(0)).execute();
	}

	@Modifying
	public void updateValsAsOriginWhenIsNull() {
		QStatistics statistics = QStatistics.statistics;
		QCdata cdata = QCdata.cdata;

		new JPAUpdateClause(em, cdata).where(cdata.ctVal.isNull()).set(cdata.ctVal, cdata.ct).set(cdata.cttVal, cdata.ctt).set(cdata.llctVal, cdata.llct)
				.set(cdata.ulctVal, cdata.ulct).execute();

		new JPAUpdateClause(em, statistics).where(statistics.shotCountVal.isNull()).set(statistics.ctVal, statistics.ct)
				.set(statistics.shotCountVal, Expressions.numberTemplate(Integer.class, "IF({0} is null or {0} < 0, 0, {0})", statistics.shotCount))
				.set(statistics.llctVal, statistics.llct).set(statistics.ulctVal, statistics.ulct).execute();

	}

	@Override
	public List<ChartData> findChartData(DashboardFilterPayload payload) {
		QStatistics statistics = QStatistics.statistics;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));

		JPQLQuery<ChartData> query;
		if (payload.getStartTime() != null && payload.getEndTime() != null) {
			QStatistics table = QStatistics.statistics;

			if (payload.getStartTime() != null && payload.getEndTime() != null) {
				builder.and(table.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
			}

			query = from(table).innerJoin(mold).on(table.moldId.eq(mold.id)).where(builder).groupBy(table.moldId, table.day)
					.select(Projections.constructor(ChartData.class, table.moldId, table.day, mold.createdAt, table.uptimeSeconds.sum(),
							(table.ct.divide(10).multiply(table.shotCount)).sum(), mold.uptimeTarget, mold.contractedCycleTime)); // ct 100ms -> s
		} else {
			QStatisticsSummary table = QStatisticsSummary.statisticsSummary;

			query = from(table).innerJoin(mold).on(table.moldId.eq(mold.id)).where(builder).groupBy(table.moldId, table.day)
					.select(Projections.constructor(ChartData.class, table.moldId, table.day.stringValue(), mold.createdAt, table.uptimeSeconds.sum(),
							(table.ct.divide(10).multiply(table.shotCount)).sum(), mold.uptimeTarget, mold.contractedCycleTime)); // ct 100ms -> s
		}
		List<ChartData> list = query.fetch();
		// TODO OCT
		list.forEach(item -> {
			double act = MoldUtils.getOptimalCycleTime(item.getMoldId(), item.getApprovedCycleTime(), null).getValue();
			item.setApprovedCycleTime(ValueUtils.toInteger(act, null));
		});
		return list;
	}

    @Override
    public List<Statistics> findStatisticsByDashboardPayload(DashboardFilterPayload payload){
        QStatistics statistics = QStatistics.statistics;
        QMold mold = QMold.mold;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
        if(payload.getStartTime() != null && payload.getEndTime() != null){
            builder.and(statistics.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
        }
        JPQLQuery<Statistics> query = from(statistics)
                .innerJoin(mold).on(statistics.moldId.eq(mold.id))
                .where(builder);
        return query.fetch();
    }

	private StringExpression getDataView(QStatistics statistics, Frequent frequent) {
		StringExpression dataView = statistics.week;
		if (frequent.equals(Frequent.MONTHLY))
			dataView = statistics.month;
		else if (frequent.equals(Frequent.YEARLY))
			dataView = statistics.year;
		else if (frequent.equals(Frequent.DAILY))
			dataView = statistics.day;
		return dataView;
	}

	@Override
	public List<ProductionQuantityData> findProductionQuantityData(List<Long> spIds, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

		StringExpression dataView = getDataView(statistics, frequent);

		List<ProductionQuantityData> listData = new ArrayList<>();
		for (int i = 0; i < spIds.size();) {
			List<Long> subSpIds = spIds;
			if (i < spIds.size() - 200) {
				subSpIds = spIds.subList(i, i + 200);
			} else {
				subSpIds = spIds.subList(i, spIds.size());
			}
			JPQLQuery query = from(statisticsPart).innerJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId))
					.where(statisticsPart.id.in(subSpIds).and(statistics.ct.gt(0).or(statistics.firstData.isTrue()))).groupBy(dataView).orderBy(dataView.asc())
					.select(Projections.constructor(ProductionQuantityData.class, (statistics.shotCount.multiply(statisticsPart.cavity)).sum(), dataView));
			listData.addAll(query.fetch());
			i += 200;
		}
		List<ProductionQuantityData> result = new ArrayList<>();
		listData.forEach(data -> {
			ProductionQuantityData productionData = result.stream().filter(x -> x.getTitle().equalsIgnoreCase(data.getTitle())).findAny().orElse(null);
			if (productionData == null) {
				result.add(data);
			} else {
				productionData.setQuantity(productionData.getQuantity() + data.getQuantity());
			}
		});
		return result;
	}

	@Override
	public List<ProductionQuantityData> findProductionQuantityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
        if(payload.getStartTime() != null && payload.getEndTime() != null){
            builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
        }

		StringExpression dataView = getDataView(statistics, frequent);

		if (!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(statistics.day.gt(DateUtils.getDate(Instant.now().minus(12, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statistics.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(statistics.week.gt(DateUtils.getYearWeek(Instant.now().minus(12 * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
						.and(statistics.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(statistics.month.gt(DateUtils.getYearMonth(Instant.now().minus(12 * 30, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statistics.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		if (moldIds != null && moldIds.size() > 0) {
			builder.and(mold.id.in(moldIds));
		}

		// [2021-02-26] Ignore shot with CT == 0
		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));

		JPQLQuery query = from(statistics).innerJoin(mold).on(mold.id.eq(statistics.moldId)).innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
				.where(builder).groupBy(mold.id, dataView).orderBy(dataView.asc())
				.select(Projections.constructor(ProductionQuantityData.class, mold.id, (statistics.shotCount.multiply(statisticsPart.cavity)).sum(), dataView));
		return query.fetch();
	}

	@Override
	public List<ProductionQuantityData> findProductionQuantityOfTooling(TabbedOverviewGeneralFilterPayload payload, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(payload.getMoldFilter());

		StringExpression dataView = getDataView(statistics, frequent);

//		if (!frequent.equals(Frequent.YEARLY)) {
//			if (frequent.equals(Frequent.DAILY)) {
//				builder.and(statistics.day.gt(DateUtils.getDate(Instant.now().minus(12, ChronoUnit.DAYS), "yyyyMMdd"))
//						.and(statistics.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
//			} else if (frequent.equals(Frequent.WEEKLY)) {
//				builder.and(statistics.week.gt(DateUtils.getYearWeek(Instant.now().minus(12 * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
//						.and(statistics.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
//			} else if (frequent.equals(Frequent.MONTHLY)) {
//				builder.and(statistics.month.gt(DateUtils.getYearMonth(Instant.now().minus(12 * 30, ChronoUnit.DAYS), "yyyyMMdd"))
//						.and(statistics.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
//			}
//		}

		// [2021-02-26] Ignore shot with CT == 0
		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));
		builder.and(statistics.shotCount.isNotNull().and(statistics.shotCount.gt(0)));

		Pair<String, String> startEnd = payload.getStartEndStringByDuration(true);
		builder.and(statistics.lst.between(startEnd.getFirst(), startEnd.getSecond()));

		JPQLQuery query = from(statistics)
				.innerJoin(mold).on(mold.id.eq(statistics.moldId))
				.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
				.where(builder).groupBy(dataView).orderBy(dataView.asc())
				.select(Projections.constructor(ProductionQuantityData.class, (statistics.shotCount.multiply(statisticsPart.cavity)).sum(), dataView));
		return query.fetch();
	}

	@Override
	public Long countProductionQuantityOfTooling(TabbedOverviewGeneralFilterPayload payload, boolean current) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		builder.and(payload.getMoldFilter());
		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));
		builder.and(statistics.shotCount.isNotNull().and(statistics.shotCount.gt(0)));

		Pair<String, String> startEnd = payload.getStartEndStringByDuration(current);
		builder.and(statistics.lst.between(startEnd.getFirst(), startEnd.getSecond()));

		JPQLQuery query = from(statistics)
				.innerJoin(mold).on(mold.id.eq(statistics.moldId))
				.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
				.where(builder)
				.select((statistics.shotCount.multiply(statisticsPart.cavity)).sum().longValue());
		return query.fetchOne() != null ? (long) query.fetchOne() : 0;
	}

	// @Override
	private List<MoldCapacityReportData> findOutputCapacityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QMold mold = QMold.mold;
//        QMoldPart moldPart = QMoldPart.moldPart;
        int rangTime = 7;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
//        if (payload.getStartTime() != null && payload.getEndTime() != null) {
//            builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//        }

		StringExpression dataView = getDataView(statistics, frequent);

		if (!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(statistics.day.gt(DateUtils.getDate(Instant.now().minus(rangTime, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statistics.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(statistics.week.gt(DateUtils.getYearWeek(Instant.now().minus(rangTime * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
						.and(statistics.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(statistics.month.gt(DateUtils.getYearMonth(Instant.now().minus(rangTime * 30, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statistics.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		Expression<Long> moldIdExp = Expressions.asNumber(0L);
		Expression<?>[] group = new Expression[1];
		group[0] = dataView;
		if (moldIds != null && moldIds.size() > 0) {
			builder.and(mold.id.in(moldIds));
			moldIdExp = mold.id;
			group = new Expression[2];
			group[0] = moldIdExp;
			group[1] = dataView;
		}
		/*
		 * Expression maxCapacityExp = (mold.shiftsPerDay.castToNum(Integer.class).multiply(moldPart.cavity).sum().multiply(36000))
		 * .multiply(mold.uptimeTarget.divide(mold.contractedCycleTime.coalesce(1))); if (frequent.equals(Frequent.WEEKLY)) { maxCapacityExp =
		 * (mold.shiftsPerDay.castToNum(Integer.class).multiply(moldPart.cavity).multiply(36000).multiply(mold.productionDays.castToNum(Integer.class)))
		 * .sum().multiply(mold.uptimeTarget.divide(mold.contractedCycleTime.coalesce(1))); }
		 */

		// [2021-02-26] Ignore shot with CT == 0
		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));

		JPQLQuery<MoldCapacityReportData> query = from(statistics).innerJoin(mold).on(mold.id.eq(statistics.moldId))
//                    .innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
				.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId)).where(builder).groupBy(group)
				.select(Projections.constructor(MoldCapacityReportData.class, moldIdExp, dataView, (statistics.shotCount.multiply(statisticsPart.cavity)).sum()))
				.orderBy(dataView.asc());

		return query.fetch();
	}

	// @Override
	private List<MoldCapacityReportData> findOutputCapacityOfTooling_New(TabbedOverviewGeneralFilterPayload payload, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QMold mold = QMold.mold;
        int rangTime = 7;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(payload.getMoldFilter());

		StringExpression dataView = getDataView(statistics, frequent);

		if (!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(statistics.day.gt(DateUtils.getDate(Instant.now().minus(rangTime, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statistics.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(statistics.week.gt(DateUtils.getYearWeek(Instant.now().minus(rangTime * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
						.and(statistics.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(statistics.month.gt(DateUtils.getYearMonth(Instant.now().minus(rangTime * 30, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(statistics.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		Expression<Long> moldIdExp = Expressions.asNumber(0L);
		Expression<?>[] group = new Expression[1];
		group[0] = dataView;
		// [2021-02-26] Ignore shot with CT == 0
		builder.and(statistics.ct.gt(0).or(statistics.firstData.isTrue()));

		Pair<String, String> startEnd = payload.getStartEndStringByDuration(true);
		builder.and(statistics.lst.between(startEnd.getFirst(), startEnd.getSecond()));

		JPQLQuery<MoldCapacityReportData> query = from(statistics).innerJoin(mold).on(mold.id.eq(statistics.moldId))
//                    .innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
				.innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId)).where(builder).groupBy(group)
				.select(Projections.constructor(MoldCapacityReportData.class, moldIdExp, dataView, (statistics.shotCount.multiply(statisticsPart.cavity)).sum()))
				.orderBy(dataView.asc());

		return query.fetch();
	}

	@Override
	public List<MoldCapacityReportData> findMaxCapacityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent, boolean isDyson) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
//        if (payload.getStartTime() != null && payload.getEndTime() != null) {
//            builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//        }

		// Query
//		SELECT
//			mld.ID,
//			'' TITLE,
//			SUM(mld.SHIFT_PER_DAY * mpt.CAVITY * 3600 * Days * COALESCE(mld.UPTIME_TARGET, 90) / (mld.StandardCycleTime / 10)) DATA
//		FROM
//			MOLD mld
//				INNER JOIN MOLD_PART mpt ON
//					mpt.MOLD_ID = mld.
//		WHERE
//			...
//		GROUP BY
//			mld.ID

		Expression<Long> selectId = Expressions.asNumber(0L);
		Expression<Integer> selectAct = Expressions.asNumber(1);
		Expression<?>[] groupBy = new Expression[0];
		if (moldIds != null && moldIds.size() > 0) {
			builder.and(mold.id.in(moldIds));
			selectId = mold.id;
			selectAct = mold.contractedCycleTime.max();
			groupBy = new Expression[1];
			groupBy[0] = selectId;
		}

		Expression<Long> selectData;
		NumberExpression<?> uptimeTarget = isDyson ? mold.uptimeTarget.coalesce(90).asNumber().divide(100) : Expressions.asNumber(1);
		// Replaced for Applying Optimal Cycle Time
		if (frequent.equals(Frequent.WEEKLY)) {
			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7).asNumber())).multiply(uptimeTarget)).sum().castToNum(Long.class);
		} else if (frequent.equals(Frequent.MONTHLY)) {
			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7).asNumber())).multiply(4).multiply(uptimeTarget)).sum().castToNum(Long.class);
		} else {
			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600))
					.multiply(uptimeTarget)).sum().castToNum(Long.class);
		}
//		if (frequent.equals(Frequent.WEEKLY)) {
//			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600)
//					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7).asNumber())).multiply(uptimeTarget)
//							.divide(mold.contractedCycleTime.coalesce(1).asNumber().divide(10))).sum().castToNum(Integer.class);
//		} else if (frequent.equals(Frequent.MONTHLY)) {
//			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600)
//					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7).asNumber())).multiply(4).multiply(uptimeTarget)
//							.divide(mold.contractedCycleTime.coalesce(1).asNumber().divide(10))).sum().castToNum(Integer.class);
//		} else {
//			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600))
//					.multiply(uptimeTarget).divide(mold.contractedCycleTime.coalesce(1).asNumber().divide(10))).sum().castToNum(Integer.class);
//		}

		JPQLQuery<MoldCapacityReportData> query = from(mold).innerJoin(moldPart).on(mold.id.eq(moldPart.moldId)).where(builder).groupBy(groupBy)
				.select(Projections.constructor(MoldCapacityReportData.class, selectId, Expressions.asString(""), selectData, Expressions.asNumber(1), selectAct));

		List<MoldCapacityReportData> list = query.fetch();
		divideCycleTime(list);
		return list;
	}

	@Override
	public List<MoldCapacityReportData> findMaxCapacityOfTooling_New(TabbedOverviewGeneralFilterPayload payload, Frequent frequent, boolean isDyson) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;

		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(payload.getMoldFilter());

		Expression<Long> selectId = Expressions.asNumber(0L);
		Expression<Integer> selectAct = mold.contractedCycleTime.max();

		Expression<Long> selectData;
		NumberExpression<?> uptimeTarget = isDyson ? mold.uptimeTarget.coalesce(90).asNumber().divide(100) : Expressions.asNumber(1);
		// Replaced for Applying Optimal Cycle Time
		if (frequent.equals(Frequent.WEEKLY)) {
			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7).asNumber())).multiply(uptimeTarget)).sum().castToNum(Long.class);
		} else if (frequent.equals(Frequent.MONTHLY)) {
			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600)
					.multiply(mold.productionDays.castToNum(Integer.class).coalesce(7).asNumber())).multiply(4).multiply(uptimeTarget)).sum().castToNum(Long.class);
		} else {
			selectData = ((mold.shiftsPerDay.castToNum(Float.class).coalesce(Float.valueOf(24)).asNumber().multiply(moldPart.cavity.coalesce(1)).multiply(3600))
					.multiply(uptimeTarget)).sum().castToNum(Long.class);
		}

		JPQLQuery<MoldCapacityReportData> query = from(mold).innerJoin(moldPart).on(mold.id.eq(moldPart.moldId)).where(builder)
				.select(Projections.constructor(MoldCapacityReportData.class, selectId, Expressions.asString(""), selectData, Expressions.asNumber(1), selectAct));

		List<MoldCapacityReportData> list = query.fetch();
		divideCycleTime(list);
		return list;
	}


	private StringExpression getDataViewMoldDownTime(QMoldDownTime moldDownTime, Frequent frequent) {
		StringExpression dataView = moldDownTime.week;
		if (frequent.equals(Frequent.MONTHLY))
			dataView = moldDownTime.month;
		else if (frequent.equals(Frequent.YEARLY))
			dataView = moldDownTime.year;
		else if (frequent.equals(Frequent.DAILY))
			dataView = moldDownTime.day;
		return dataView;
	}

	private List<MoldCapacityReportData> findAvailableDowntimeOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QMoldDownTime moldDownTime = QMoldDownTime.moldDownTime;
		int rangTime = 7;

        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
//        if (payload.getStartTime() != null && payload.getEndTime() != null) {
//            builder.and(mold.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//        }

		StringExpression dataView = getDataViewMoldDownTime(moldDownTime, frequent);

		if (!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(moldDownTime.day.gt(DateUtils.getDate(Instant.now().minus(rangTime, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(moldDownTime.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(moldDownTime.week.gt(DateUtils.getYearWeek(Instant.now().minus(rangTime * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
						.and(moldDownTime.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(moldDownTime.month.gt(DateUtils.getYearMonth(Instant.now().minus(rangTime * 30, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(moldDownTime.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		Expression<Long> moldIdExp = Expressions.asNumber(0L);
		Expression<?>[] group = new Expression[1];
		group[0] = dataView;
		if (moldIds != null && moldIds.size() > 0) {
			builder.and(mold.id.in(moldIds));
			moldIdExp = mold.id;

			group = new Expression[2];
			group[0] = moldIdExp;
			group[1] = dataView;
		}
		// TODO OCT
		Expression<Long> dataExp = ((moldDownTime.downTime.coalesce(0L).asNumber().divide(1000).multiply(moldPart.cavity.coalesce(1)))).sum().castToNum(Long.class);
//		Expression<Integer> dataExp = ((moldDownTime.downTime.coalesce(0L).asNumber().divide(1000).multiply(moldPart.cavity.coalesce(1)))
//				.divide(mold.contractedCycleTime.coalesce(1)).divide(10)).sum().castToNum(Integer.class);
////        if (frequent.equals(Frequent.WEEKLY)) {
////            dataExp = (mold.shiftsPerDay.castToNum(Integer.class).multiply(moldPart.cavity).multiply(36000).multiply(mold.productionDays.castToNum(Integer.class)))
////                    .sum().multiply(mold.uptimeTarget.divide(mold.contractedCycleTime.coalesce(1)));
////        }

		JPQLQuery<MoldCapacityReportData> query = from(moldDownTime).innerJoin(mold).on(mold.id.eq(moldDownTime.moldId)).innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
				.select(Projections.constructor(MoldCapacityReportData.class, moldIdExp, dataView, dataExp));
		query.where(builder).groupBy(group);

		List<MoldCapacityReportData> list = query.fetch();
		// TODO OCT
		divideCycleTime(list);
		return list;
	}

	private List<MoldCapacityReportData> findAvailableDowntimeOfTooling_New(TabbedOverviewGeneralFilterPayload payload, Frequent frequent) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QMoldDownTime moldDownTime = QMoldDownTime.moldDownTime;
		int rangTime = 7;

        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(payload.getMoldFilter());

		Pair<Instant, Instant> startEnd = payload.getStartEndByDuration(true);
		builder.and(moldDownTime.createdAt.between(startEnd.getFirst(), startEnd.getSecond()));

		StringExpression dataView = getDataViewMoldDownTime(moldDownTime, frequent);

		if (!frequent.equals(Frequent.YEARLY)) {
			if (frequent.equals(Frequent.DAILY)) {
				builder.and(moldDownTime.day.gt(DateUtils.getDate(Instant.now().minus(rangTime, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(moldDownTime.day.loe(DateUtils.getDate(Instant.now(), "yyyyMMdd"))));
			} else if (frequent.equals(Frequent.WEEKLY)) {
				builder.and(moldDownTime.week.gt(DateUtils.getYearWeek(Instant.now().minus(rangTime * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"))
						.and(moldDownTime.week.loe(DateUtils.getYearWeek(Instant.now(), "yyyyMMddHHmmss"))));
			} else if (frequent.equals(Frequent.MONTHLY)) {
				builder.and(moldDownTime.month.gt(DateUtils.getYearMonth(Instant.now().minus(rangTime * 30, ChronoUnit.DAYS), "yyyyMMdd"))
						.and(moldDownTime.month.loe(DateUtils.getYearMonth(Instant.now(), "yyyyMMdd"))));
			}
		}

		Expression<Long> moldIdExp = Expressions.asNumber(0L);
		Expression<?>[] group = new Expression[1];
		group[0] = dataView;
		// TODO OCT
		Expression<Long> dataExp = ((moldDownTime.downTime.coalesce(0L).asNumber().divide(1000).multiply(moldPart.cavity.coalesce(1)))).sum().castToNum(Long.class);

		JPQLQuery<MoldCapacityReportData> query = from(moldDownTime).innerJoin(mold).on(mold.id.eq(moldDownTime.moldId)).innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
				.select(Projections.constructor(MoldCapacityReportData.class, moldIdExp, dataView, dataExp));
		query.where(builder).groupBy(group);

		List<MoldCapacityReportData> list = query.fetch();
		// TODO OCT
		divideCycleTime(list);
		return list;
	}

	@Override
	public List<MoldCapacityReportData> findMoldCapacityOfTooling(DashboardFilterPayload payload, List<Long> moldIds, Frequent frequent, boolean isDyson) {
		List<MoldCapacityReportData> listRest = new ArrayList<>();
		List<MoldCapacityReportData> listOutputCapacityOfTooling = findOutputCapacityOfTooling(payload, moldIds, frequent);
		List<MoldCapacityReportData> listMaxCapacityOfTooling = findMaxCapacityOfTooling(payload, moldIds, frequent, isDyson);
		List<MoldCapacityReportData> listAvailableDowntimeOfTooling = findAvailableDowntimeOfTooling(payload, moldIds, frequent);
		Map<String, MoldCapacityReportData> mapMaxCapacityOfTooling = new HashMap<>();
		Map<String, MoldCapacityReportData> mapAvailableDowntimeOfTooling = new HashMap<>();
		listMaxCapacityOfTooling.stream().forEach(o -> mapMaxCapacityOfTooling.put(String.valueOf(o.getId()), o));
		listAvailableDowntimeOfTooling.stream().forEach(o -> mapAvailableDowntimeOfTooling.put(o.getKey(), o));
		listOutputCapacityOfTooling.stream().forEach(o -> {
			MoldCapacityReportData m = new MoldCapacityReportData();
			m.setId(o.getId());
			m.setTitle(o.getTitle());
			if (o.getData() != null) {
				m.setOutputCapacity(o.getDataLong());

			} else
				m.setOutputCapacity(0L);

			if (mapMaxCapacityOfTooling.get(String.valueOf(o.getId())) != null)
				m.setMaxCapacity(mapMaxCapacityOfTooling.get(String.valueOf(o.getId())).getData());
			else
				m.setMaxCapacity(0);
			if (mapAvailableDowntimeOfTooling.get(o.getKey()) != null)
				m.setAvailableDowntime(mapAvailableDowntimeOfTooling.get(o.getKey()).getDataLong());
			else
				m.setAvailableDowntime(0L);

			if (m.getMaxCapacity() != null && !m.getMaxCapacity().equals(0)) {

				Long availableOutput = m.getMaxCapacity() - m.getOutputCapacity() - m.getAvailableDowntime();
				m.setAvailableOutput(availableOutput > 0 ? availableOutput : 0);

				Long overCapacity = m.getOutputCapacity() > m.getMaxCapacity() ? m.getOutputCapacity() - m.getMaxCapacity() : 0;
				m.setOverCapacity(overCapacity);
				m.setOutputCapacity(m.getOutputCapacity() - overCapacity);
				m.makePercent();
				/*
				 * m.setOutputCapacityPercent(DataUtils.round(100* Double.valueOf(m.getOutputCapacity()) / m.getMaxCapacity(),2));
				 * m.setAvailableDowntimePercent(DataUtils.round(100* Double.valueOf(m.getAvailableDowntime()) / m.getMaxCapacity(),2));
				 * m.setAvailableOutputPercent(DataUtils.round(100* Double.valueOf(m.getAvailableOutput()) / m.getMaxCapacity(),2));
				 * m.setOverCapacityPercent(DataUtils.round(100* Double.valueOf(m.getOverCapacity()) / m.getMaxCapacity(),2));
				 */
			}
			listRest.add(m);
		});

        List<MoldCapacityReportData> result = addBlankMoldCapacityReportData(listMaxCapacityOfTooling, listRest, frequent);

//        return listRest;
        return result;
    }

	@Override
	public List<MoldCapacityReportData> findProductionCapacityOfTooling(TabbedOverviewGeneralFilterPayload payload, Frequent frequent, boolean isDyson) {
		List<MoldCapacityReportData> listRest = new ArrayList<>();
		List<MoldCapacityReportData> listOutputCapacityOfTooling = findOutputCapacityOfTooling_New(payload, frequent);
		List<MoldCapacityReportData> listMaxCapacityOfTooling = findMaxCapacityOfTooling_New(payload, frequent, isDyson);
		List<MoldCapacityReportData> listAvailableDowntimeOfTooling = findAvailableDowntimeOfTooling_New(payload, frequent);
		Map<String, MoldCapacityReportData> mapMaxCapacityOfTooling = new HashMap<>();
		Map<String, MoldCapacityReportData> mapAvailableDowntimeOfTooling = new HashMap<>();
		listMaxCapacityOfTooling.stream().forEach(o -> mapMaxCapacityOfTooling.put(String.valueOf(o.getId()), o));
		listAvailableDowntimeOfTooling.stream().forEach(o -> mapAvailableDowntimeOfTooling.put(o.getKey(), o));
		listOutputCapacityOfTooling.stream().forEach(o -> {
			MoldCapacityReportData m = new MoldCapacityReportData();
			m.setId(o.getId());
			m.setTitle(o.getTitle());
			if (o.getData() != null) {
				m.setOutputCapacity(o.getDataLong());

			} else
				m.setOutputCapacity(0L);

			if (mapMaxCapacityOfTooling.get(String.valueOf(o.getId())) != null) {
				m.setMaxCapacity(mapMaxCapacityOfTooling.get(String.valueOf(o.getId())).getData());
				m.setMaxCapacityLong(mapMaxCapacityOfTooling.get(String.valueOf(o.getId())).getDataLong());
			}
			else
				m.setMaxCapacity(0);
			if (mapAvailableDowntimeOfTooling.get(o.getKey()) != null)
				m.setAvailableDowntime(mapAvailableDowntimeOfTooling.get(o.getKey()).getDataLong());
			else
				m.setAvailableDowntime(0L);

			if (m.getMaxCapacity() != null && !m.getMaxCapacity().equals(0)) {
				if (m.getMaxCapacity() < 0) {
					Long availableOutput = m.getMaxCapacityLong() - m.getOutputCapacity() - m.getAvailableDowntime();
					m.setAvailableOutput(availableOutput > 0 ? availableOutput : 0);

					Long overCapacity = m.getOutputCapacity() > m.getMaxCapacityLong() ? m.getOutputCapacity() - m.getMaxCapacityLong() : 0;
					m.setOverCapacity(overCapacity);
					m.setOutputCapacity(m.getOutputCapacity() - overCapacity);
					m.makePercent();
				} else {
					Long availableOutput = (long) m.getMaxCapacity() - m.getOutputCapacity() - m.getAvailableDowntime();
					m.setAvailableOutput(availableOutput > 0 ? availableOutput : 0);

					Long overCapacity = m.getOutputCapacity() > m.getMaxCapacity() ? m.getOutputCapacity() - m.getMaxCapacity() : 0;
					m.setOverCapacity(overCapacity);
					m.setOutputCapacity(m.getOutputCapacity() - overCapacity);
					m.makePercent();
				}

				/*
				 * m.setOutputCapacityPercent(DataUtils.round(100* Double.valueOf(m.getOutputCapacity()) / m.getMaxCapacity(),2));
				 * m.setAvailableDowntimePercent(DataUtils.round(100* Double.valueOf(m.getAvailableDowntime()) / m.getMaxCapacity(),2));
				 * m.setAvailableOutputPercent(DataUtils.round(100* Double.valueOf(m.getAvailableOutput()) / m.getMaxCapacity(),2));
				 * m.setOverCapacityPercent(DataUtils.round(100* Double.valueOf(m.getOverCapacity()) / m.getMaxCapacity(),2));
				 */
			}
			listRest.add(m);
		});

		List<MoldCapacityReportData> result = addBlankMoldCapacityReportData(listMaxCapacityOfTooling, listRest, frequent);

//        return listRest;
		return result;
	}

	private List<MoldCapacityReportData> addBlankMoldCapacityReportData(List<MoldCapacityReportData> listMaxCapacityOfTooling, List<MoldCapacityReportData> listData,
			Frequent frequent) {
		int rangeTime = 7;
		List<String> titles = new ArrayList<>();
		for (int i = rangeTime - 1; i >= 0; i--) {
			if (!frequent.equals(Frequent.YEARLY)) {
				if (frequent.equals(Frequent.DAILY)) {
					titles.add(DateUtils.getDate(Instant.now().minus(i, ChronoUnit.DAYS), "yyyyMMdd"));
				} else if (frequent.equals(Frequent.WEEKLY)) {
					titles.add(DateUtils.getYearWeek(Instant.now().minus(i * 7, ChronoUnit.DAYS), "yyyyMMddHHmmss"));
				} else if (frequent.equals(Frequent.MONTHLY)) {
					titles.add(DateUtils.getYearMonth(Instant.now().minus(i * 30, ChronoUnit.DAYS), "yyyyMMdd"));
				}
			}
		}
		List<MoldCapacityReportData> result = new ArrayList<>();
		titles.forEach(title -> {
			List<MoldCapacityReportData> existData = listData.stream().filter(x -> x.getTitle().equals(title)).collect(Collectors.toList());
			if (existData != null && existData.size() > 0)
				result.addAll(existData);
			else {
				listMaxCapacityOfTooling.forEach(maxCapacity -> {
					MoldCapacityReportData moldCapacityReportData = new MoldCapacityReportData(maxCapacity.getId(), title, maxCapacity.getData(), 0);
					/*
					 * moldCapacityReportData.setAvailableDowntime(0); moldCapacityReportData.setAvailableDowntimePercent(0.0);
					 * moldCapacityReportData.setAvailableOutput(maxCapacity.getData()); moldCapacityReportData.setAvailableOutputPercent(100.0);
					 * moldCapacityReportData.setOutputCapacityPercent(0.0); moldCapacityReportData.setOverCapacity(0); moldCapacityReportData.setOverCapacityPercent(0.0);
					 */
					moldCapacityReportData.makeDefault();
					result.add(moldCapacityReportData);
				});
			}
		});
		return result;
	}

    private JPQLQuery createQueryStatisticsAccumulatingShotByDay(Mold moldCheck, String day){
        QStatistics statistics = QStatistics.statistics;
        QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder();
        if(moldCheck!=null){
            builder.and(mold.id.eq(moldCheck.getId()));
        }else{
			if (AccessControlUtils.isAccessFilterRequired()) {
				builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
			}
        }

//        builder.and(dashboardGeneralFilterUtils.getMoldBuilder(mold));
        if (!StringUtils.isEmpty(day)) {
            builder.and(statistics.day.loe(day));
        }

		/*
		 * Expression moldIdExp = Expressions.asNumber(0L); Expression[] group = new Expression[0]; if (mold!=null) { builder.and(mold.id.in(moldIds)); moldIdExp =
		 * mold.id; group = new Expression[2]; group[0] = moldIdExp; group[1] = dataView; }
		 */
		/*
		 * Expression maxCapacityExp = (mold.shiftsPerDay.castToNum(Integer.class).multiply(moldPart.cavity).sum().multiply(36000))
		 * .multiply(mold.uptimeTarget.divide(mold.contractedCycleTime.coalesce(1))); if (frequent.equals(Frequent.WEEKLY)) { maxCapacityExp =
		 * (mold.shiftsPerDay.castToNum(Integer.class).multiply(moldPart.cavity).multiply(36000).multiply(mold.productionDays.castToNum(Integer.class)))
		 * .sum().multiply(mold.uptimeTarget.divide(mold.contractedCycleTime.coalesce(1))); }
		 */

		JPQLQuery query = from(statistics).innerJoin(mold).on(mold.id.eq(statistics.moldId))
//                    .innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
				.where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()))).groupBy(mold.id)
				.select(Projections.constructor(StatisticsAccumulatingShot.class, mold, (statistics.shotCount).sum(), statistics.day.max().eq(day)));
		return query;
	}

	private JPQLQuery createQueryStatisticsAccumulatingShotByDayInStatisticsPreset(Mold moldCheck, String day) {
		QStatisticsPreset statisticsPreset = QStatisticsPreset.statisticsPreset;
		QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder();
        if(moldCheck!=null){
            builder.and(mold.id.eq(moldCheck.getId()));
        }else{
			if (AccessControlUtils.isAccessFilterRequired()) {
				builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
			}
        }

		if (!StringUtils.isEmpty(day)) {
			builder.and(statisticsPreset.day.loe(day));
		}

		JPQLQuery query = from(statisticsPreset).innerJoin(mold).on(mold.id.eq(statisticsPreset.moldId)).where(builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())))
				.groupBy(mold.id).select(Projections.constructor(StatisticsAccumulatingShot.class, mold, (statisticsPreset.shotMissing).sum(), statisticsPreset.day.max().eq(day)));
		return query;
	}

	@Override
	public List<StatisticsAccumulatingShot> statisticsAccumulatingShotByDay(Mold moldCheck, String day, Pageable pageable) {
		List<StatisticsAccumulatingShot> resultList = new ArrayList<>();
		JPQLQuery query = createQueryStatisticsAccumulatingShotByDay(moldCheck, day);
		JPQLQuery queryPreset = createQueryStatisticsAccumulatingShotByDayInStatisticsPreset(moldCheck, day);
		if (pageable != null) {
			query.limit(pageable.getPageSize()).offset(pageable.getOffset());
		}

		List<StatisticsAccumulatingShot> statisticsAccumulatingShotList = query.fetch();
		List<StatisticsAccumulatingShot> statisticsAccumulatingShotPresetList = queryPreset.fetch();
		Map<Long, Integer> mapShotPreset = new HashMap<>();
		statisticsAccumulatingShotPresetList.stream().forEach(s -> {
			if (s.getMold() != null)
				mapShotPreset.put(s.getMold().getId(), s.getAccumulatingShot());
		});

		statisticsAccumulatingShotList.stream().forEach(o -> {
			if (o.getMold() != null && o.getMold().getDesignedShot() != null) {

				Integer accumulatingShot = o.getAccumulatingShot();
				Long moldId = o.getMold().getId();
				if (mapShotPreset.containsKey(moldId) && mapShotPreset.get(moldId) != null) {
					accumulatingShot = accumulatingShot == null ? 0 : accumulatingShot;
					accumulatingShot += mapShotPreset.get(moldId);
				}
				Integer remainingShot = o.getMold().getDesignedShot() - accumulatingShot;
				StatisticsAccumulatingShot sa = new StatisticsAccumulatingShot(o.getMold(), accumulatingShot, remainingShot, day);
				sa.setDesignedShot(o.getMold().getDesignedShot());
				sa.setWorkingInDay(o.getWorkingInDay());
				resultList.add(sa);
			}
		});
		return resultList;
	}

	@Override
	public Long countStatisticsAccumulatingShotByDay(String day) {
		JPQLQuery query = createQueryStatisticsAccumulatingShotByDay(null, day);
		return query.fetchCount();
	}

	@Override
	public List<TransactionDTO> getTransactionByDay(String day) {
		Instant dateTime = Instant.now();
		List<TransactionDTO> transactionDTOList = new ArrayList<>();
//        QTransfer qTransfer;
		QStatistics statistics = QStatistics.statistics;
		QStatistics statisticsSub = QStatistics.statistics;
		BooleanBuilder builder = new BooleanBuilder();
		if (!StringUtils.isEmpty(day)) {
			/*
			 * Instant toDate= DateUtils.getInstant(day + "000000",DateUtils.DEFAULT_DATE_FORMAT).plus(1, ChronoUnit.DAYS);
			 * builder.and(statistics.lst.between(day,DateUtils.getDay(toDate)));
			 */
			builder.and(statistics.day.eq(day));
		}

		JPQLQuery query = from(statistics).where(builder).groupBy(statistics.ti, statistics.ci)
				.select(Projections.constructor(TransactionDTO.class, statistics.ti, statistics.ci, statistics.shotCount.sum(), statistics.shotCount.max())); // ct 100ms -> s
		transactionDTOList = query.fetch();
		for (TransactionDTO t : transactionDTOList) {
			t.setDate(day);
			t.setDatetime(dateTime.toEpochMilli());

			BooleanBuilder builderSub = new BooleanBuilder();
			if (!StringUtils.isEmpty(day)) {
//                Instant toDate= DateUtils.getInstant(day + "000000",DateUtils.DEFAULT_DATE_FORMAT).plus(1, ChronoUnit.DAYS);
//                builderSub.and(statisticsSub.lst.between(day,DateUtils.getDay(toDate)));
				builderSub.and(statistics.day.eq(day));

			}
			JPQLQuery querySub = from(statistics)
					.where(builderSub.and(statisticsSub.shotCount.eq(t.getScMax())).and(statisticsSub.ci.eq(t.getCi())).and(statisticsSub.ti.eq(t.getTi())))
					.select(statisticsSub.ct).limit(1);
			List<Double> listCycle = querySub.fetch();
//            Double cycleSub= JPAExpressions.select(statisticsSub.ct).from(statisticsSub).where(builderSub.and(statisticsSub.shotCount.eq(t.getScMax()))
//                    .and(statisticsSub.ci.eq(t.getCi()))
//                    .and(statisticsSub.ti.eq(t.getTi()))).fetchFirst();
			if (listCycle.size() > 0)
				t.setCt(listCycle.get(0));
		}

		return transactionDTOList;
	}

	@Override
	public List<Statistics> findFirstStatistics() {
		QStatistics statistics = QStatistics.statistics;

		JPQLQuery query = from(statistics).where(statistics.id.in(JPAExpressions.select(statistics.id.min()).from(statistics).groupBy(statistics.moldId)));
		return query.fetch();
	}

	@Override
	public List<ChartDataOte> findChartDataOte(DashboardFilterPayload payload) {
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
		Expression<Integer> targetTimeOfToolingInHours = (mold.shiftsPerDay.castToNum(Integer.class)).multiply(mold.productionDays.castToNum(Integer.class))
				.multiply(mold.uptimeTarget.coalesce(100));

		JPQLQuery<ChartDataOte> query;
		if (payload.getStartTime() != null && payload.getEndTime() != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
			String str = formatter.format(Instant.now().minus(Duration.ofDays(50)));
			String fromStr = formatter.format(Instant.ofEpochSecond(payload.getStartTime()));
			String toStr = formatter.format(Instant.ofEpochSecond(payload.getEndTime()));
			if (str.compareTo(toStr) > 0) {
				QStatisticsSummary table = QStatisticsSummary.statisticsSummary;
				int from = ValueUtils.toInteger(fromStr, 0);
				int to = ValueUtils.toInteger(toStr, 0);
				builder.and(table.day.between(from, to));
				query = from(table).innerJoin(mold).on(table.moldId.eq(mold.id))
						.select(Projections.constructor(ChartDataOte.class, table.moldId, mold.equipmentCode, mold.location.company.companyCode, mold.location.company.name,
								mold.equipmentCode, mold.createdAt, table.uptimeSeconds.sum(), (table.ct.divide(10).multiply(table.shotCount)).sum(), targetTimeOfToolingInHours,
								mold.maxCapacityPerWeek// Theoretical number of parts to be produced
								, mold.contractedCycleTime))
						.where(builder).groupBy(table.moldId); // ct 100ms -> s
			} else {
				QStatistics table = QStatistics.statistics;
				builder.and(table.day.between(fromStr, toStr));
//				builder.and(table.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
				query = from(table).innerJoin(mold).on(table.moldId.eq(mold.id))
						.select(Projections.constructor(ChartDataOte.class, table.moldId, mold.equipmentCode, mold.location.company.companyCode, mold.location.company.name,
								mold.equipmentCode, mold.createdAt, table.uptimeSeconds.sum(), (table.ct.divide(10).multiply(table.shotCount)).sum(), targetTimeOfToolingInHours,
								mold.maxCapacityPerWeek// Theoretical number of parts to be produced
								, mold.contractedCycleTime))
						.where(builder).groupBy(table.moldId); // ct 100ms -> s
			}
		} else {
			QStatisticsSummary table = QStatisticsSummary.statisticsSummary;
			query = from(table).innerJoin(mold).on(table.moldId.eq(mold.id))
					.select(Projections.constructor(ChartDataOte.class, table.moldId, mold.equipmentCode, mold.location.company.companyCode, mold.location.company.name,
							mold.equipmentCode, mold.createdAt, table.uptimeSeconds.sum(), (table.ct.divide(10).multiply(table.shotCount)).sum(), targetTimeOfToolingInHours,
							mold.maxCapacityPerWeek// Theoretical number of parts to be produced
							, mold.contractedCycleTime))
					.where(builder).groupBy(table.moldId); // ct 100ms -> s

		}

		List<ChartDataOte> list = query.fetch();
		// TODO OCT
		list.forEach(item -> {
			double act = MoldUtils.getOptimalCycleTime(item.getMoldId(), item.getApprovedCycleTime(), null).getValue();
			item.setApprovedCycleTime(ValueUtils.toInteger(act, null));
		});
		return list;
	}

	@Override
	public List<ChartDataOte> findChartDataOtePart(DashboardFilterPayload payload) {
//        QStatistics statistics = QStatistics.statistics;
		QProducedPart producedPart = QProducedPart.producedPart;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
        if(payload.getStartTime() != null && payload.getEndTime() != null){
            builder.and(producedPart.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
        }
//        Expression targetTimeOfToolingInHours =
//                (mold.shiftsPerDay.castToNum(Integer.class)).multiply(mold.productionDays.castToNum(Integer.class)).multiply(mold.uptimeTarget.coalesce(100));
		JPQLQuery<ChartDataOte> query = from(producedPart).innerJoin(mold).on(producedPart.moldId.eq(mold.id)).where(builder).groupBy(producedPart.moldId)
				.select(Projections.constructor(ChartDataOte.class, producedPart.moldId, producedPart.mold.equipmentCode, producedPart.mold.equipmentCode,
						producedPart.totalProducedAmount.sum(), producedPart.totalRejectedAmount.sum())); // ct 100ms -> s
		return query.fetch();
	}

	private BooleanBuilder getUptimeRatioGeneralBuilder(QMold mold, QMoldPart moldPart, String type, Long id, String from, String to) {
		BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));

		builder.and(mold.deleted.isNull().or(mold.deleted.isFalse())).and(mold.location.company.isEmoldino.isFalse());
//        builder.and(mold.operatingStatus.isNotNull());

		if (type != null && id != null) {
			if (type.equalsIgnoreCase("SUPPLIER")) {
				builder.and(mold.companyId.eq(id));
			} else {
				builder.and(mold.id.in(JPAExpressions.select(moldPart.moldId).from(moldPart).where(moldPart.partId.eq(id))));
			}
		}
		return builder;
	}

	@Override
	public Page<UptimeRatioTooling> findUptimeRatioTooling(FrequentUsage frequent, String type, Long id, String from, String to, Pageable pageable, boolean getAll) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QStatisticsSummary table = QStatisticsSummary.statisticsSummary;
//		QStatistics table = QStatistics.statistics;
		QCompany company = QCompany.company;
		QPart part = QPart.part;

		BooleanBuilder builder = getUptimeRatioGeneralBuilder(mold, moldPart, type, id, from, to);

		NumberExpression<Integer> duration;

		BooleanBuilder statisticsBuilder = new BooleanBuilder();

		if (!StringUtils.isEmpty(from) && !StringUtils.isEmpty(to)) {
			statisticsBuilder.and(table.day.between(ValueUtils.toInteger(from, 0), ValueUtils.toInteger(to, 0)));
//			statisticsBuilder.and(table.day.between(from, to));

			duration = Expressions.asNumber(DateTimeUtils.diffBetweenTwoDates(from, to, true));
		} else {
			duration = new CaseBuilder().when(mold.passedDays.isNotNull().and(mold.passedDays.gt(0))).then(mold.passedDays).otherwise(1);
		}

		NumberExpression<Integer> hoursPerDay = new CaseBuilder().when(mold.shiftsPerDay.isNotNull()).then(mold.shiftsPerDay.castToNum(Integer.class)).otherwise(24);

		NumberExpression<Float> uptimeSeconds = new CaseBuilder().when(table.uptimeSeconds.isNotNull()).then(table.uptimeSeconds.floatValue()).otherwise(0f);

		NumberExpression<Float> totalUptimeHour = (uptimeSeconds.sum()).divide(3600);
		NumberExpression<Integer> targetUptimeHour = hoursPerDay.multiply(duration);

		NumberExpression<Float> ratio = new CaseBuilder().when(totalUptimeHour.lt(targetUptimeHour)).then((totalUptimeHour.multiply(100)).divide(targetUptimeHour)).otherwise(100f);

		Expression sortValue = mold.equipmentCode;
		OrderSpecifier orderSpecifier = mold.equipmentCode.desc();
		if (pageable != null) {
			Sort.Direction[] directions = { Sort.Direction.DESC };
			String[] orderProperty = { "moldCode" };
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
				orderProperty[0] = order.getProperty();
			});
			if (!orderProperty[0].equalsIgnoreCase("moldCode")) {
				if (orderProperty[0].equalsIgnoreCase("ratio")) {
					sortValue = ratio;
				} else if (orderProperty[0].equalsIgnoreCase("madeYear")) {
					sortValue = mold.madeYear;
				} else if (type != null && type.equalsIgnoreCase("SUPPLIER") && orderProperty[0].equalsIgnoreCase("code")) {
					sortValue = company.companyCode;
				} else if (orderProperty[0].equalsIgnoreCase("code")) {
					sortValue = part.partCode;
				}
			}
			orderSpecifier = new OrderSpecifier(Order.valueOf(directions[0].toString()), sortValue);
		}

//        JPQLQuery queryFrequently = from(mold)
//                .select(mold.id.countDistinct())
//                .where(mold.id.in(
//                        JPAExpressions.select(mold.id)
//                        .from(mold)
//                        .leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(builder))
//                        .groupBy(mold.id, mold.shiftsPerDay, mold.passedDays)
//                        .having(ratio.gt(50))
//                ));
//
//        JPQLQuery queryOccasionally = from(mold)
//                .select(mold.id.countDistinct())
//                .where(mold.id.in(
//                        JPAExpressions.select(mold.id)
//                                .from(mold)
//                                .leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(builder))
//                                .groupBy(mold.id, mold.shiftsPerDay, mold.passedDays)
//                                .having(ratio.gt(20).and(ratio.loe(50)))
//                ));
//
//        JPQLQuery queryRarely = from(mold)
//                .select(mold.id.countDistinct())
//                .where(mold.id.in(
//                        JPAExpressions.select(mold.id)
//                                .from(mold)
//                                .leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(builder))
//                                .groupBy(mold.id, mold.shiftsPerDay, mold.passedDays)
//                                .having(ratio.gt(0).and(ratio.loe(20)))
//                ));
//
//        JPQLQuery queryNever = from(mold)
//                .select(mold.id.countDistinct())
//                .where(mold.id.in(
//                        JPAExpressions.select(mold.id)
//                                .from(mold)
//                                .leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(builder))
//                                .groupBy(mold.id, mold.shiftsPerDay, mold.passedDays)
//                                .having(ratio.eq(0L))
//                ));
//
//        long countFrequently = (long) queryFrequently.fetchOne();
//        long countOccasionally = (long) queryOccasionally.fetchOne();
//        long countRarely = (long) queryRarely.fetchOne();
//        long countNever = (long) queryNever.fetchOne();

		builder.and(mold.location.company.isEmoldino.isFalse());
		if (type != null && type.equalsIgnoreCase("SUPPLIER")) {
			builder.and(dashboardGeneralFilterUtils.getSupplierFilter(mold.location.company));
		}

		JPQLQuery query = from(mold).leftJoin(table).on(mold.id.eq(table.moldId).and(statisticsBuilder)).where(builder);
		long total = 100L;
		if (!getAll) {
			// Using having function to calculate data
			if (frequent != null) {
				if (frequent.equals(FrequentUsage.FREQUENTLY))
					query.where(mold.id.in(JPAExpressions.select(mold.id).from(mold).leftJoin(table).on(mold.id.eq(table.moldId).and(statisticsBuilder))
							.groupBy(mold.id, mold.shiftsPerDay, mold.passedDays).having(ratio.gt(50))));
				else if (frequent.equals(FrequentUsage.OCCASIONALLY))
					query.where(mold.id.in(JPAExpressions.select(mold.id).from(mold).leftJoin(table).on(mold.id.eq(table.moldId).and(statisticsBuilder))
							.groupBy(mold.id, mold.shiftsPerDay, mold.passedDays).having(ratio.gt(20).and(ratio.loe(50)))));
				else if (frequent.equals(FrequentUsage.RARELY))
					query.where(mold.id.in(JPAExpressions.select(mold.id).from(mold).leftJoin(table).on(mold.id.eq(table.moldId).and(statisticsBuilder))
							.groupBy(mold.id, mold.shiftsPerDay, mold.passedDays).having(ratio.gt(0).and(ratio.loe(20)))));
				else
					query.where(mold.id.in(JPAExpressions.select(mold.id).from(mold).leftJoin(table).on(mold.id.eq(table.moldId).and(statisticsBuilder))
							.groupBy(mold.id, mold.shiftsPerDay, mold.passedDays).having(ratio.eq(0f))));
			}
			total = (long) query.select(mold.id.countDistinct()).fetchOne();

			// Join with company or part for sorting purpose
			if (type != null && type.equalsIgnoreCase("SUPPLIER")) {
				query.innerJoin(company).on(mold.companyId.eq(company.id).and(company.isEmoldino.isFalse()));
			} else if (type != null && type.equalsIgnoreCase("PART")) {
				query.innerJoin(moldPart).on(mold.id.eq(moldPart.moldId).and(moldPart.id.in(JPAExpressions.select(moldPart.id.min()).from(moldPart).groupBy(moldPart.moldId))));
				query.innerJoin(part).on(moldPart.partId.eq(part.id));
			}
			query.orderBy(orderSpecifier);
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());
		}

		query.select(Projections.constructor(UptimeRatioTooling.class, mold.id, mold.equipmentCode, mold.madeYear, totalUptimeHour, targetUptimeHour, ratio));
		query.groupBy(mold.id, mold.shiftsPerDay, mold.passedDays);
		List<UptimeRatioTooling> listTooling = query.fetch();
		return new PageImpl<>(listTooling, pageable, total);
	}

/*
	@Override
	public Page<UptimeRatioDetails> findUptimeRatioDetails(String type, Long id, String from, String to, Pageable pageable) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QStatistics statistics = QStatistics.statistics;
		QCompany company = QCompany.company;
		QPart part = QPart.part;

		BooleanBuilder builder = getUptimeRatioGeneralBuilder(mold, moldPart, type, id, from, to);

		NumberExpression<Integer> duration;

		BooleanBuilder statisticsBuilder = new BooleanBuilder();

		if (!StringUtils.isEmpty(from) && !StringUtils.isEmpty(to)) {
			statisticsBuilder.and(statistics.day.between(from, to));

			duration = Expressions.asNumber(DateTimeUtils.diffBetweenTwoDates(from, to, true));
		} else {
			duration = new CaseBuilder().when(mold.passedDays.isNotNull().and(mold.passedDays.gt(0))).then(mold.passedDays).otherwise(1);
		}

		NumberExpression<Integer> hoursPerDay = new CaseBuilder().when(mold.shiftsPerDay.isNotNull()).then(mold.shiftsPerDay.castToNum(Integer.class)).otherwise(24);

		NumberExpression<Float> uptimeSeconds = new CaseBuilder().when(statistics.uptimeSeconds.isNotNull()).then(statistics.uptimeSeconds.floatValue()).otherwise(0f);

		NumberExpression<Float> totalUptimeHour = (uptimeSeconds.sum()).divide(3600);
		NumberExpression<Integer> targetUptimeHour = hoursPerDay.multiply(duration).sum();

		NumberExpression<Float> ratio = new CaseBuilder().when(totalUptimeHour.lt(targetUptimeHour)).then((totalUptimeHour.multiply(100)).divide(targetUptimeHour)).otherwise(100f);

		NumberExpression<Long> numberOfTooling = mold.id.countDistinct();

		Expression sortValue = ratio;
		OrderSpecifier orderSpecifier = ratio.desc();
		if (pageable != null) {
			Sort.Direction[] directions = { Sort.Direction.DESC };
			String[] orderProperty = { "ratio" };
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
				orderProperty[0] = order.getProperty();
			});
			if (!orderProperty[0].equalsIgnoreCase("ratio")) {
				if (orderProperty[0].equalsIgnoreCase("numberOfTooling")) {
					sortValue = numberOfTooling;
				} else if (type != null && type.equalsIgnoreCase("SUPPLIER") && orderProperty[0].equalsIgnoreCase("code")) {
					sortValue = company.companyCode;
				} else if (orderProperty[0].equalsIgnoreCase("code")) {
					sortValue = part.partCode;
				}
			}
			orderSpecifier = new OrderSpecifier(Order.valueOf(directions[0].toString()), sortValue);
		}

		Page<UptimeRatioDetails> result;
		if (type.equalsIgnoreCase("SUPPLIER")) {
			JPQLQuery query = from(mold).innerJoin(company).on(mold.companyId.eq(company.id)).leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(statisticsBuilder))
					.where(builder).groupBy(company.id);

			long total = query.fetchCount();

			query.select(
					Projections.constructor(UptimeRatioDetails.class, company.id, company.name, company.companyCode, numberOfTooling, totalUptimeHour, targetUptimeHour, ratio));
			query.orderBy(orderSpecifier);
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());

			List<UptimeRatioDetails> details = query.fetch();

			result = new PageImpl<>(details, pageable, total);
		} else {
			JPQLQuery query = from(mold).innerJoin(moldPart).on(mold.id.eq(moldPart.moldId)).innerJoin(part).on(moldPart.partId.eq(part.id)).leftJoin(statistics)
					.on(mold.id.eq(statistics.moldId).and(statisticsBuilder)).where(builder.and(part.deleted.isNull().or(part.deleted.isFalse())))
//                    .select(Projections.constructor(UptimeRatioDetails.class, part.id, part.name, part.partCode,
//                            numberOfTooling, totalUptimeHour.doubleValue(), targetUptimeHour.doubleValue(), ratio.doubleValue()))
					.groupBy(part.id);

			long total = query.fetchCount();

			query.select(Projections.constructor(UptimeRatioDetails.class, part.id, part.name, part.partCode, numberOfTooling, totalUptimeHour, targetUptimeHour, ratio));
			query.orderBy(orderSpecifier);
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());

			List<UptimeRatioDetails> details = query.fetch();

			result = new PageImpl<>(details, pageable, total);
		}
		return result;
	}
*/

	@Override
	public Page<UptimeRatioDetails> findUptimeRatioDetailsNew(String type, Long id, String from, String to, Pageable pageable) {
		QMold mold = QMold.mold;
		QMoldPart moldPart = QMoldPart.moldPart;
		QStatistics statistics = QStatistics.statistics;
		QCompany company = QCompany.company;
		QPart part = QPart.part;
		QVPartTargetUptimeHour vPartTargetUptimeHour = QVPartTargetUptimeHour.vPartTargetUptimeHour;
		QVCompanyTargetUptimeHour vCompanyTargetUptimeHour = QVCompanyTargetUptimeHour.vCompanyTargetUptimeHour;

		BooleanBuilder builder = getUptimeRatioGeneralBuilder(mold, moldPart, type, id, from, to);

		NumberExpression<Integer> duration;

		BooleanBuilder statisticsBuilder = new BooleanBuilder();

		if (!StringUtils.isEmpty(from) && !StringUtils.isEmpty(to)) {
			statisticsBuilder.and(statistics.day.between(from, to));

			duration = Expressions.asNumber(DateTimeUtils.diffBetweenTwoDates(from, to, true));
		} else {
			duration = new CaseBuilder().when(mold.passedDays.isNotNull().and(mold.passedDays.gt(0))).then(mold.passedDays).otherwise(1);
		}

		NumberExpression<Integer> hoursPerDay = new CaseBuilder().when(mold.shiftsPerDay.isNotNull()).then(mold.shiftsPerDay.castToNum(Integer.class)).otherwise(24);

		NumberExpression<Float> uptimeSeconds = new CaseBuilder().when(statistics.uptimeSeconds.isNotNull()).then(statistics.uptimeSeconds.floatValue()).otherwise(0f);

		NumberExpression<Float> totalUptimeHour = (uptimeSeconds.sum()).divide(3600);
		NumberExpression<Integer> targetUptimeHour;
		if (type != null && type.equalsIgnoreCase("SUPPLIER")) {
			targetUptimeHour = vCompanyTargetUptimeHour.hourPerDay.intValue().multiply(duration);
		} else {
			targetUptimeHour = vPartTargetUptimeHour.hourPerDay.intValue().multiply(duration);
		}
		NumberExpression<Float> ratio = new CaseBuilder().when(totalUptimeHour.lt(targetUptimeHour)).then((totalUptimeHour.multiply(100)).divide(targetUptimeHour)).otherwise(100f);

		NumberExpression<Long> numberOfTooling = mold.id.countDistinct();

		Expression sortValue = ratio;
		OrderSpecifier orderSpecifier = ratio.desc();
		if (pageable != null) {
			Sort.Direction[] directions = { Sort.Direction.DESC };
			String[] orderProperty = { "ratio" };
			pageable.getSort().forEach(order -> {
				directions[0] = order.getDirection();
				orderProperty[0] = order.getProperty();
			});
			if (!orderProperty[0].equalsIgnoreCase("ratio")) {
				if (orderProperty[0].equalsIgnoreCase("numberOfTooling")) {
					sortValue = numberOfTooling;
				} else if (type != null && type.equalsIgnoreCase("SUPPLIER") && orderProperty[0].equalsIgnoreCase("code")) {
					sortValue = company.companyCode;
				} else if (orderProperty[0].equalsIgnoreCase("code")) {
					sortValue = part.partCode;
				}
			}
			orderSpecifier = new OrderSpecifier(Order.valueOf(directions[0].toString()), sortValue);
		}

		Page<UptimeRatioDetails> result;
		if (type != null && type.equalsIgnoreCase("SUPPLIER")) {
            builder.and(dashboardGeneralFilterUtils.getSupplierFilter(company));
            JPQLQuery query = from(mold).innerJoin(company).on(mold.companyId.eq(company.id)).leftJoin(vCompanyTargetUptimeHour).on(company.id.eq(vCompanyTargetUptimeHour.id))
					.leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(statisticsBuilder)).where(builder.and(mold.location.company.isEmoldino.isFalse()))
					.groupBy(company.id);

			long total = query.fetchCount();

			query.select(
					Projections.constructor(UptimeRatioDetails.class, company.id, company.name, company.companyCode, numberOfTooling, totalUptimeHour, targetUptimeHour, ratio));
			query.orderBy(orderSpecifier);
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());

			List<UptimeRatioDetails> details = query.fetch();

			result = new PageImpl<>(details, pageable, total);
		} else {
            builder.and(dashboardGeneralFilterUtils.getPartFilter(part));
            if (id != null) {
				builder.and(part.id.eq(id));
			}
			JPQLQuery query = from(mold).innerJoin(moldPart).on(mold.id.eq(moldPart.moldId)).innerJoin(part).on(moldPart.partId.eq(part.id)).innerJoin(vPartTargetUptimeHour)
					.on(part.id.eq(vPartTargetUptimeHour.id)).leftJoin(statistics).on(mold.id.eq(statistics.moldId).and(statisticsBuilder))
					.where(builder.and(part.deleted.isNull().or(part.deleted.isFalse())).and(mold.location.company.isEmoldino.isFalse()))
//                    .select(Projections.constructor(UptimeRatioDetails.class, part.id, part.name, part.partCode,
//                            numberOfTooling, totalUptimeHour.doubleValue(), targetUptimeHour.doubleValue(), ratio.doubleValue()))
					.groupBy(part.id);

			long total = query.fetchCount();

			query.select(Projections.constructor(UptimeRatioDetails.class, part.id, part.name, part.partCode, numberOfTooling, totalUptimeHour, targetUptimeHour, ratio));
			query.orderBy(orderSpecifier);
			query.limit(pageable.getPageSize());
			query.offset(pageable.getOffset());

			List<UptimeRatioDetails> details = query.fetch();

			result = new PageImpl<>(details, pageable, total);
		}
		return result;
	}

	@Override
	public List<PerformanceRawData> getPerformanceRawData(long moldId, String day) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCdata cdata = QCdata.cdata;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(statistics.moldId.eq(moldId).and(cdata.day.eq(day)));

		NumberExpression<Integer> cavity = new CaseBuilder().when(statisticsPart.cavity.isNotNull()).then(statisticsPart.cavity).otherwise(0);
		NumberExpression<Integer> shotCount = new CaseBuilder().when(statistics.shotCount.isNotNull()).then(statistics.shotCount).otherwise(0);

		JPQLQuery query = from(statistics).leftJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId)).leftJoin(cdata).on(statistics.cdataId.eq(cdata.id))
				.where(builder);

		query.select(Projections.constructor(PerformanceRawData.class, cavity, shotCount, cdata.ctt, cdata.ct));

		return query.fetch();
	}

	@Override
	public List<PerformanceRawData> getPerformanceRawDataAfter(long moldId, String day, String time) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCdata cdata = QCdata.cdata;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(statistics.moldId.eq(moldId).and(cdata.day.eq(day)).and(cdata.lst.goe(time)));

		NumberExpression<Integer> cavity = new CaseBuilder().when(statisticsPart.cavity.isNotNull()).then(statisticsPart.cavity).otherwise(0);
		NumberExpression<Integer> shotCount = new CaseBuilder().when(statistics.shotCount.isNotNull()).then(statistics.shotCount).otherwise(0);

		JPQLQuery query = from(statistics).leftJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId)).leftJoin(cdata).on(statistics.cdataId.eq(cdata.id))
				.where(builder);

		query.select(Projections.constructor(PerformanceRawData.class, cavity, shotCount, cdata.ctt, cdata.ct));

		return query.fetch();
	}

	@Override
	public List<PerformanceRawData> getPerformanceRawDataBefore(long moldId, String day, String time) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
		QCdata cdata = QCdata.cdata;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(statistics.moldId.eq(moldId).and(cdata.day.eq(day)).and(cdata.lst.loe(time)));

		NumberExpression<Integer> cavity = new CaseBuilder().when(statisticsPart.cavity.isNotNull()).then(statisticsPart.cavity).otherwise(0);
		NumberExpression<Integer> shotCount = new CaseBuilder().when(statistics.shotCount.isNotNull()).then(statistics.shotCount).otherwise(0);

		JPQLQuery query = from(statistics).leftJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId)).leftJoin(cdata).on(statistics.cdataId.eq(cdata.id))
				.where(builder);

		query.select(Projections.constructor(PerformanceRawData.class, cavity, shotCount, cdata.ctt, cdata.ct));

		return query.fetch();
	}

	@Override
	public List<StatisticsDaily> findAllGroupByYearAndMonthAndDayAndWeekAndMoldIdAndCi(String after, String before, String day, Long moldId, String ci, Pageable pageable) {
		QStatistics table = QStatistics.statistics;
		BooleanBuilder filter = new BooleanBuilder();
		if (after != null) {
			filter.and(table.day.gt(after + ""));
		}
		if (before != null) {
			filter.and(table.day.lt(before));
		}
		if (day != null) {
			BooleanBuilder subfilter = new BooleanBuilder();
			subfilter.or(table.day.gt(day));
			subfilter.or(table.day.eq(day).and(table.moldId.gt(moldId)));
			subfilter.or(table.day.eq(day).and(table.moldId.eq(moldId)).and(table.ci.gt(ci)));
			filter.and(subfilter);
		}
		filter.and(table.moldId.isNotNull());
		filter.and(table.ci.isNotNull());
		filter.and(table.shotCount.gt(0));

		JPQLQuery<StatisticsDaily> query = from(table).select(Projections.constructor(StatisticsDaily.class, table.year, table.month, table.day, table.week, table.moldId, table.ci,
				table.uptimeSeconds.sum(), table.ct.sum(), table.ctVal.sum(), table.shotCount.sum().longValue(), table.shotCountVal.sum().longValue()));
		query.where(filter);
		query.groupBy(table.year, table.month, table.day, table.week, table.moldId, table.ci);
		query.orderBy(table.year.asc(), table.month.asc(), table.day.asc(), table.week.asc(), table.moldId.asc(), table.ci.asc());
		getQuerydsl().applyPagination(pageable, query);
		return query.fetch();
	}

	@Override
	public Integer getPartProjectProduced(Long partId, Long projectId) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

		NumberPath ct = statistics.ct;
		NumberPath shotCount = statistics.shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}
		NumberExpression sumProducePart = (statisticsPart.cavity.multiply(shotCount)).sum();

		JPQLQuery query = from(statistics)
				.innerJoin(statisticsPart)
				.on(statistics.id.eq(statisticsPart.statisticsId));

		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(statistics.moldId.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

		query.where(statisticsPart.partId.eq(partId).and(statisticsPart.projectId.eq(projectId)));
		query.where(shotCount.isNotNull().and(shotCount.gt(0)));
		query.where(statisticsPart.cavity.isNotNull().and(statisticsPart.cavity.gt(0)));
		query.where(ct.gt(0).or(statistics.firstData.isTrue()));
		query.groupBy(statisticsPart.partId, statisticsPart.projectId);
		query.select(sumProducePart);

		List result = query.fetch();
		return result.isEmpty() ? 0 : (result.get(0) != null ? (int) result.get(0) : 0);
	}

	@Override
	public boolean checkHaveDataProductionQuantityData(List<Long> spIds, Frequent frequent) {
		QStatistics statistics = QStatistics.statistics;
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

		StringExpression dataView = getDataView(statistics, frequent);

		for (int i = 0; i < spIds.size();) {
			List<Long> subSpIds = spIds;
			if (i < spIds.size() - 200) {
				subSpIds = spIds.subList(i, i + 200);
			} else {
				subSpIds = spIds.subList(i, spIds.size());
			}
			JPQLQuery query = from(statisticsPart).innerJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId))
					.where(statisticsPart.id.in(subSpIds).and(statistics.ct.gt(0).or(statistics.firstData.isTrue()))).groupBy(dataView).orderBy(dataView.asc())
					.select(Projections.constructor(ProductionQuantityData.class, (statistics.shotCount.multiply(statisticsPart.cavity)).sum(), dataView));
			List<ProductionQuantityData> productionQuantityDataList = query.fetch();
			if (productionQuantityDataList.stream().anyMatch(productionQuantityData -> productionQuantityData.getQuantity() > 0)) return true;
			i += 200;
		}
		return false;
	}

	@Override
	public Double getPartWACTBefore(Long partId, String time) {
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
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

		NumberExpression<Double> totalTime = (ct.multiply(shotCount)).sum();
		NumberExpression<Integer> totalShot = shotCount.sum();
		NumberExpression<Double> wact = totalTime.divide(totalShot);

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(ct.gt(0).or(statistics.firstData.isTrue()));
		builder.and(shotCount.isNotNull().and(shotCount.gt(0)));
		builder.and(statistics.lst.lt(time));
		builder.and(statisticsPart.partId.eq(partId));

		JPQLQuery query = from(statisticsPart)
				.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder));
		query.select(wact);

		return query.fetchOne() != null ? (double) query.fetchOne() : 0D;
	}

	@Override
	public Double getPartWACTBetween(Long partId, String start, String end) {
		QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
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

		NumberExpression<Double> totalTime = (ct.multiply(shotCount)).sum();
		NumberExpression<Integer> totalShot = shotCount.sum();
		NumberExpression<Double> wact = totalTime.divide(totalShot);

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(ct.gt(0).or(statistics.firstData.isTrue()));
		builder.and(shotCount.isNotNull().and(shotCount.gt(0)));
		builder.and(statistics.lst.between(start, end));
		builder.and(statisticsPart.partId.eq(partId));

		JPQLQuery query = from(statisticsPart)
				.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder));
		query.select(wact);

		return query.fetchOne() != null ? (double) query.fetchOne() : 0D;
	}
}
