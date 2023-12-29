package com.emoldino.api.common.resource.composite.tolstp.repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.maintenance.enumeration.PM_STRATEGY;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.tolstp.dto.QTolStpItem;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpGetIn;
import com.emoldino.api.common.resource.composite.tolstp.dto.TolStpItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.repository.Qs;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.TabType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.util.DataUtils;
import saleson.common.util.StringUtils;
import saleson.model.QAccessCompanyRelation;
import saleson.model.TabTable;
import saleson.model.User;

@Repository
public class TolStpRepository {

	public long count(TolStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.TOOLING, input.getTabName());

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.id.countDistinct())//
				.distinct()//
				.from(Q.mold)
				.leftJoin(Q.counter).on(Q.mold.counterId.eq(Q.counter.id));//for filter

		applyFilter(query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TolStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, Q.mold.id);
		//todo: modify to check deleted
		if (TabType.DISPOSED.name().equalsIgnoreCase(input.getTabName())) {
			QueryUtils.applyMoldDisposedFilter(query, join, "COMMON");
		} else if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
		} else {
			QueryUtils.applyMoldDisabledFilter(query, join, input.getFilterCode());
		}

		QueryUtils.leftJoin(query, join, Q.location, Q.location.id.eq(Q.mold.locationId));
		QueryUtils.leftJoin(query, join, Q.company, Q.company.id.eq(Q.location.companyId));
		leftJoinPartByMold(query, join);
		QueryUtils.leftJoinCategoryByPart(query, join);
		QueryUtils.leftJoin(query, join, Q.supplier, (Q.supplier.id.eq(Q.mold.supplierCompanyId)));// remove QueryUtils.isSupplier() because current data is linking to multiple company types
		QueryUtils.leftJoin(query, join, Q.toolmaker, (Q.toolmaker.id.eq(Q.mold.toolMakerCompanyId)));// remove QueryUtils.isToolmaker() because current data is linking to multiple company types
		QueryUtils.leftJoin(query, join, Q.machine, QueryUtils.isMachine().and(Q.mold.machineId.eq(Q.machine.id)));
		QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.mold.areaId));

		BooleanBuilder filter = new BooleanBuilder();

		addQueryFilters(filter, input.getQuery(), query, join);

		if (TabType.DIGITAL.name().equalsIgnoreCase(input.getTabName())) {
			filter.and(Q.mold.counterId.isNotNull());
		} else if (TabType.NON_DIGITAL.name().equalsIgnoreCase(input.getTabName())) {
			filter.and(Q.mold.counterId.isNull());
		} else if (NotificationStatus.DISAPPROVED.name().equalsIgnoreCase(input.getTabName())) {//dyson
			filter.and(Q.mold.dataSubmission.isNull().or(Q.mold.dataSubmission.eq(NotificationStatus.DISAPPROVED)));
		} else if (NotificationStatus.PENDING.name().equalsIgnoreCase(input.getTabName())) {
			filter.and(Q.mold.dataSubmission.eq(NotificationStatus.PENDING));
		} else if (NotificationStatus.APPROVED.name().equalsIgnoreCase(input.getTabName())) {
			filter.and(Q.mold.dataSubmission.eq(NotificationStatus.APPROVED));
		}
		if (CollectionUtils.isNotEmpty(input.getId())) {
			filter.and(Q.mold.id.in(input.getId()));
		}
		if (input.getPartId() != null) {
			filter.and(Q.part.id.eq(input.getPartId()));
		}
		if (input.getCompanyId() != null) {
			filter.and(Q.company.id.eq(input.getCompanyId()));
		}

		if (input.getTabName() == null || !Arrays.asList(TabType.DISPOSED.name(), TabType.DISABLED.name()).contains(input.getTabName().toUpperCase())) {
			checkDataStatus(input, filter);
		}
		QueryUtils.applyBatchFilter(filter, batchin, Q.mold.id);
		query.where(filter);

	}

	public Page<TolStpItem> findAll(TolStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.TOOLING, input.getTabName());
		// TODO filterInactive
		// TODO filter with dashboard redirect, tabbedDashboardRedirected

		//data filter
		String property = QueryUtils.getFirstSortProperty(pageable);

		NumberExpression<Float> ctCompliance = Expressions.asNumber(//
				JPAExpressions//
						.select(Q.ctd.withinL1ToleranceSc.abs().floatValue().sum().divide(Q.ctd.shotCount.abs().floatValue().sum()).multiply(100d))//
						.from(Q.ctd)//
						.where(Q.ctd.moldId.eq(Q.mold.id))//
		).floatValue();

		NumberExpression<Integer> preventCycle = new CaseBuilder().when(Q.mold.preventCycle.isNull()).then(1).otherwise(Q.mold.preventCycle);
		NumberExpression<Integer> lastShot = new CaseBuilder().when(Q.mold.lastShot.isNull()).then(0).otherwise(Q.mold.lastShot);
		NumberExpression<Integer> mAccumulatedShot = new CaseBuilder().when(Q.moldMaintenance.accumulatedShot.isNull()).then(0).otherwise(Q.moldMaintenance.accumulatedShot).max();
		NumberExpression<Integer> shotSinceCompletion = new CaseBuilder().when(lastShot.goe(mAccumulatedShot)).then(lastShot.subtract(mAccumulatedShot)).otherwise(0);
		NumberExpression<Integer> pmCheckpoint = new CaseBuilder().when(Q.moldMaintenance.isNotNull()).then(preventCycle.add(mAccumulatedShot))
				.otherwise(((shotSinceCompletion.divide(preventCycle).add(1)).floor().multiply(preventCycle)).add(mAccumulatedShot));
//		NumberExpression<Integer> countUp = new CaseBuilder().when(shotSinceCompletion.lt(preventCycle)).then(shotSinceCompletion).otherwise(preventCycle);
		NumberExpression<Integer> countUp = new CaseBuilder()//
				.when(Q.moldMaintenance.isNotNull())//
				.then(new CaseBuilder()//
						.when(shotSinceCompletion.lt(preventCycle)).then(shotSinceCompletion)//
						.otherwise(preventCycle)//
				)//
				.otherwise(shotSinceCompletion.mod(preventCycle));
		NumberExpression untilNextPmRate = new CaseBuilder()//
				.when(Q.moldPmPlan.pmStrategy.eq(PM_STRATEGY.TIME_BASED))//check config shot base
				.then(Expressions.numberTemplate(Float.class, "null"))//
				.otherwise(countUp.floatValue().divide(preventCycle).multiply(100));
		NumberExpression<Float> lastShotDivCheckpoint = new CaseBuilder()//
				.when(Q.moldPmPlan.pmStrategy.eq(PM_STRATEGY.TIME_BASED))//check config shot base
				.then(Expressions.numberTemplate(Float.class, "null"))//
				.otherwise(lastShot.floatValue().divide(pmCheckpoint));

		NumberExpression dueDate;
		{
			NumberExpression<Integer> activePeriod = Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", //
					Q.mold.operatedStartAt, Instant.now()).divide(24 * 3600f);
			NumberExpression<Integer> d = new CaseBuilder()//
					.when(lastShot.goe(pmCheckpoint))//
					.then((shotSinceCompletion.subtract(preventCycle)).multiply(activePeriod).divide(lastShot))//
					.otherwise(new CaseBuilder()//
							.when(Q.moldMaintenance.isNotNull().and(Q.moldMaintenance.dueDate.isNotNull()))//
							.then(Q.moldMaintenance.dueDate)//
							.otherwise((preventCycle.subtract(countUp)).multiply(activePeriod).divide(lastShot)));
			dueDate = new CaseBuilder()//
					.when(Q.moldPmPlan.pmStrategy.eq(PM_STRATEGY.TIME_BASED))//check config shot base
					.then(Expressions.numberTemplate(Integer.class, "null"))//
					.when(lastShot.gt(0).and(Q.mold.operatedStartAt.isNotNull()))//
					.then(d)//
					.otherwise(Expressions.nullExpression());
		}

		if ("dueDate".equals(property)) {
			Sort.Direction direction = QueryUtils.getFirstSortDirection(pageable);
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), direction, "maintenanceStatus", "dueDate");
		}

		boolean dataFilterEnabled = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER);
		NumberExpression<Integer> inactiveDays = new CaseBuilder()//
				.when(Q.mold.lastShotAt.isNotNull())//
				.then(Expressions.numberTemplate(Integer.class, "datediff({0}, {1})", Instant.now(), Q.mold.lastShotAt))//
				.otherwise(Expressions.numberTemplate(Integer.class, "datediff({0}, {1})", Instant.now(), Q.mold.createdAt));

		String[] properties = { "" };
		Sort.Direction[] directions = { Sort.Direction.DESC };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		if ("lastShotAt".equals(properties[0]) && dataFilterEnabled) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), directions[0], "lastShotAtVal");
		}

		QAccessCompanyRelation accessCompanyRelation = QAccessCompanyRelation.accessCompanyRelation;

		StringExpression upperTierCompanies = accessCompanyRelation.accessHierarchyParent.company.companyCode;
		NumberExpression<Float> slDepreciationDivLifeYears = new CaseBuilder().when(Q.mold.poDate.isNotNull().and(Q.mold.lifeYears.isNotNull()))//
				.then(Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", //
						Q.mold.poDate, Instant.now())//
						.divide(3600 * 24 * 365)//
						.ceil()//
						.floatValue()//
						.divide(Q.mold.lifeYears))//
				.otherwise(Expressions.asNumber(-1f));

		NumberExpression<Float> upDepreciation = new CaseBuilder()//
				.when(Q.mold.poDate.isNotNull().and(Q.mold.lastShot.isNotNull()))//
				.then(Q.mold.lastShot.castToNum(Float.class).divide(Q.mold.designedShot))//
				.otherwise(Expressions.asNumber(-1F));
		/*
		NumberExpression pmCheckPointValue = new CaseBuilder()//
				.when(moldMaintenance.periodStart.isNotNull().and(moldMaintenance.mold.preventUpcoming.isNotNull()))//
				.then(moldMaintenance.periodStart.add(moldMaintenance.mold.preventUpcoming))//
				.when(moldMaintenance.periodEnd.isNotNull().and(moldMaintenance.mold.preventOverdue.isNotNull()))//
				.then(moldMaintenance.periodEnd.subtract(moldMaintenance.mold.preventOverdue))//
				.when(moldMaintenance.preventCycle.isNotNull())//
				.then(moldMaintenance.preventCycle)//
				.otherwise(1);
		NumberExpression lastShotCheckpoint = moldMaintenance.mold.lastShot.divide(pmCheckPointValue);
		NumberExpression utilNextPm = moldMaintenance.lastShotMade.divide(moldMaintenance.mold.preventCycle.coalesce(1));
		NumberExpression dueDate = moldMaintenance.dueDate;
		if ("dueDate".equals(properties[0]) && dataFilterEnabled) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), directions[0], "maintenanceStatus", "dueDate");
		}
		NumberExpression maintenancePeriod = new CaseBuilder().when(moldMaintenance.periodStart.isNotNull().and(Q.mold.preventUpcoming.isNotNull()))
				.then(moldMaintenance.periodStart.add(Q.mold.preventUpcoming)).when(moldMaintenance.periodEnd.isNotNull().and(Q.mold.preventOverdue.isNotNull()))
				.then(moldMaintenance.periodEnd.subtract(Q.mold.preventOverdue)).otherwise(Expressions.nullExpression());
		
		String property = QueryUtils.getFirstSortProperty(pageable);
		*/

		NumberPath<Integer> shotCount;
		NumberExpression<Integer> accumulatedShot;
		if (dataFilterEnabled) {
			shotCount = Q.statistics.shotCountVal;
		} else {
			shotCount = Q.statistics.shotCount;
		}

		String[] sortValues = property != null && property.startsWith(SpecialSortProperty.moldAccumulatedShotSort) ? properties[0].split("\\.") : new String[0];
		if (sortValues.length > 1 && !"all".equals(sortValues[1])) {
			accumulatedShot = Expressions.asNumber(//
					JPAExpressions//
							.select(shotCount.sum().coalesce(0))//
							.from(Q.statistics)//
							.where(Q.statistics.moldId.eq(Q.mold.id).and(Q.statistics.year.eq(sortValues[1])).and(Q.statistics.ct.gt(0).or(Q.statistics.firstData.isTrue())))//
			).intValue();
		} else {
			accumulatedShot = Q.mold.lastShot;
		}
		NumberExpression<Long> remainingPartsCount= new CaseBuilder()
				.when(Q.mold.totalCavities.gt(0).and(Q.mold.designedShot.gt(0)))
				.then(
				(Q.mold.designedShot.subtract(accumulatedShot)).longValue().multiply(Q.mold.totalCavities))
						.otherwise(Expressions.nullExpression());

		JPQLQuery<TolStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(
						//Projections.constructor(TolStpItem.class,
						new QTolStpItem(Q.mold, //todo: convert to using details of each field
								ctCompliance.as("ctCompliance"), //
								dueDate.as("dueDate"), //
//								preventCycle, //
//								mAccumulatedShot, //
//								lastShot, //
								pmCheckpoint, countUp.as("untilNextPm"), //
//								untilNextPmRate, //
//								lastShotDivCheckpoint, //
								Q.moldPmPlan.pmStrategy,
								//	PM_STRATEGY
								Q.moldPmPlan.pmFrequency, //
								Q.moldPmPlan.schedDayOfWeek, //
								Q.moldPmPlan.schedStartDate, //
								Q.moldPmPlan.schedInterval, //
								Q.moldPmPlan.schedOrdinalNum, // month(s) on the
								Q.moldPmPlan.recurrConstraintType, Q.moldPmPlan.recurrNum, // Number Time
								Q.moldPmPlan.recurrDueDate, // recurrDueDate
								Q.moldPmPlan.schedUpcomingTolerance, // SCHED_UPCOMING_TOLERANCE

								inactiveDays, //
								Q.moldMaintenance.lastShotMade, //
								Q.moldMaintenance.maintenanceStatus, //

								Expressions.asBoolean(dataFilterEnabled), //
								accumulatedShot.intValue().as("accumulatedShot"), //
								remainingPartsCount.as("remainingPartsCount"), //
								Q.workOrder.completedOn.max().as("lastWorkOrderAt"), //
								Q.workOrder.countDistinct().as("workOrderCount")//
						))//
				.distinct()//
				.from(Q.mold)//
				.leftJoin(Q.counter).on(Q.mold.counterId.eq(Q.counter.id))//
//				.leftJoin(Q.location).on(Q.location.id.eq(Q.mold.locationId))//
//				.leftJoin(Q.company).on(Q.company.id.eq(Q.location.companyId))//
				.leftJoin(accessCompanyRelation)
				.on(Q.mold.companyId.eq(accessCompanyRelation.companyId).and(
						accessCompanyRelation.id.in(JPAExpressions.select(accessCompanyRelation.id.min()).from(accessCompanyRelation).groupBy(accessCompanyRelation.companyId))))
				.leftJoin(Q.workOrderAsset).on(//
						Q.workOrderAsset.type.eq(ObjectType.TOOLING).and(Q.workOrderAsset.assetId.eq(Q.counter.id))//
				)//
				.leftJoin(Q.workOrder).on(//
						Q.workOrder.id.eq(Q.workOrderAsset.workOrderId)//
								.and(Q.workOrder.status.in(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)))//
				.leftJoin(Q.moldMaintenance).on(//
						Q.moldMaintenance.moldId.eq(Q.mold.id)//
								.and(Q.moldMaintenance.maintenanceStatus.eq(MaintenanceStatus.DONE))//
								.and(Q.moldMaintenance.latest.isTrue()))
				.leftJoin(Q.moldPmPlan).on(Q.mold.id.eq(Q.moldPmPlan.moldId))//check config shot base
				.groupBy(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin, tabTable);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put(SpecialSortProperty.operatingStatus, getOrderByOperatingStatus())//
				.put("equipmentCode", Q.mold.equipmentCode)//
				.put("supplierMoldCode", Q.mold.supplierMoldCode)//
				.put("supplierMoldCode", Q.mold.supplierMoldCode)//
				.put("location.company.name", Q.mold.location.company.name)//
				.put("location.name", Q.mold.location.name)//
				.put("lastShot", Q.mold.lastShot)//
				.put("activatedAt", Q.mold.activatedAt)//
				.put("installedAt", Q.mold.installedAt)//
				.put("installedBy", Q.mold.installedBy)//
				.put("part", Q.moldPart.part.partCode)//
				.put("productAndCategory", Q.product.name)//
				.put("inactivePeriod", inactiveDays)//
				.put("upperTierCompanies", upperTierCompanies)//
				.put("slDepreciation", slDepreciationDivLifeYears)//
				.put("upDepreciation", upDepreciation)//
				.put("toolingStatus", getToolingStatus())//
				.put("sensorStatus", getSensorStatus())//counterStatus
//				.put("lastShotCheckpoint", lastShotCheckpoint)//
//				.put("utilNextPm", utilNextPm)//
//				.put("dueDate", dueDate) //pmCheckpointPrediction
				.put("maintenanceStatus", Q.moldMaintenance.maintenanceStatus)//
				.put("lastCycleTime", Q.mold.lastCycleTime)//
				.put("cycleTimeLimit1", Q.mold.cycleTimeLimit1)//
				.put("cycleTimeLimit2", Q.mold.cycleTimeLimit2)//
				.put("weightedAverageCycleTime", Q.mold.weightedAverageCycleTime)//
				.put("uptimeTarget", Q.mold.uptimeTarget)//
				.put("engineer", Q.mold.engineer)//
				.put("weightRunner", Q.mold.weightRunner)//
				.put("contractedCycleTime", Q.mold.contractedCycleTime)//
				.put("toolmakerContractedCycleTime", Q.mold.toolmakerContractedCycleTime)//
				.put("counter.equipmentCode", Q.counter.equipmentCode)//
				.put("designedShot", Q.mold.designedShot)//
				.put("hotRunnerDrop", Q.mold.hotRunnerDrop)//
				.put("hotRunnerZone", Q.mold.hotRunnerZone)//
				.put("injectionMachineId", Q.mold.injectionMachineId)//
				.put("labour", Q.mold.labour)//
				.put("lastShotAt", Q.mold.lastShotAt)//
				.put("lastShotAtVal", Q.mold.lastShotAtVal)//
				.put("quotedMachineTonnage", Q.mold.quotedMachineTonnage)//
				.put("preventCycle", Q.mold.preventCycle)//
				.put("runnerMaker", Q.mold.runnerMaker)//
				.put("maxCapacityPerWeek", Q.mold.maxCapacityPerWeek)//
				.put("shiftsPerDay", Q.mold.shiftsPerDay)//
				.put("productionDays", Q.mold.productionDays)//
				.put("supplier.name", Q.supplier.name)//
				.put("toolDescription", Q.mold.toolDescription)//
				.put("size", Q.mold.size)//
				.put("weight", Q.mold.weight)//
				.put("shotSize", Q.mold.shotSize)//
				.put("toolingComplexity", Q.mold.toolingComplexity)//
				.put("toolingLetter", Q.mold.toolingLetter)//
				.put("toolingType", Q.mold.toolingType)//
				.put("toolMaker.name", Q.toolmaker.name)//
				.put("runnerType", Q.mold.runnerType)//
				.put("preventUpcoming", Q.mold.preventUpcoming)//
				.put("uptimeLimitL1", Q.mold.uptimeLimitL1)//
				.put("uptimeLimitL2", Q.mold.uptimeLimitL2)//
				.put("utilizationRate", Q.mold.lastShot.floatValue().divide(Q.mold.designedShot))//
				.put("madeYear", Q.mold.madeYear)//
				.put("memo", Q.mold.memo)//
				.put("cost", Q.mold.cost)//
				.put("accumulatedMaintenanceCost", Q.mold.accumulatedMaintenanceCost)//
				.put("lastMaintenanceDate", Q.mold.lastMaintenanceDate)//
				.put("lastRefurbishmentDate", Q.mold.lastRefurbishmentDate)//
				.put("machine.machineCode", Q.machine.machineCode)//
				.put("tco", Q.mold.cost.floatValue().add(Q.mold.accumulatedMaintenanceCost))//
				.put("ctCompliance", Expressions.numberPath(Double.class, "ctCompliance"))//
				.put("lastShotCheckpoint", untilNextPmRate)//Accumulated Shots/PM Checkpoint (% value)
				.put("utilNextPm", untilNextPmRate)//PM checkpoint progress
				.put("dueDate", Expressions.numberPath(Integer.class, "dueDate")) //pmCheckpointPrediction
				.put("areaName", Q.area.name)//
				.put("remainingPartsCount", Expressions.numberPath(Long.class, "remainingPartsCount"))

				.build();

		if (property != null && property.startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
			fieldMap.put(property, Expressions.numberPath(Long.class, "accumulatedShot"));
		}

		if (property != null && property.startsWith(SpecialSortProperty.customFieldSort)) {
			Long customFieldId = Long.valueOf((property.split("-"))[1]);
			query.leftJoin(Qs.customFieldValue).on(Qs.customFieldValue.customField.id.eq(customFieldId).and(Qs.customFieldValue.objectId.eq(Q.mold.id)));
			fieldMap.put(property, Qs.customFieldValue.value);
		}
		//
		/*
		if (property != null && property.startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
		    NumberPath shotCount;
		    NumberExpression sumShotCount;
		    if (dataFilterEnabled) {
		        shotCount = Q.statistics.shotCountVal;
		    } else {
		        shotCount = Q.statistics.shotCount;
		    }
		    String[] sortValues = properties[0].split("\\.");
		    if (!"all".equals(sortValues[1])) {
		        sumShotCount = shotCount.sum();
		        query = query
		            .leftJoin(Q.statistics).on(Q.statistics.moldId.eq(Q.mold.id)
		                .and(Q.statistics.year.eq(sortValues[1]))
		                .and(Q.statistics.ct.gt(0).or(Q.statistics.firstData.isTrue())));
		    } else {
		        sumShotCount = Q.mold.lastShot;
		    }
		    fieldMap.put(property, sumShotCount);//todo: accumulatedShot is incorrect because of duplicate values due to join with moldPart(one-to-many)
		}
		*/

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.id.desc());

		QueryResults<TolStpItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private BooleanBuilder addQueryFilters(BooleanBuilder filter, String query, JPQLQuery<?> jpqlQuery, Set<EntityPathBase<?>> join) {
		if (ObjectUtils.isEmpty(query)) {
			return filter;
		}
		//Use when don't join with moldPart
		/*
		QMoldPart moldPart = QMoldPart.moldPart;
		QPart part = QPart.part;
		QCategory category = new QCategory("category");
		QCategory brand = new QCategory("brand");
		*/

		filter.or(Q.mold.equipmentCode.containsIgnoreCase(query));
		//todo: check when skip join with moldPart
		filter.or((Q.part.partCode.contains(query).or(Q.part.name.contains(query))).and(Q.part.deleted.isNull().or(Q.part.deleted.isFalse())));
		//Use when don't join with moldPart
		/*
		filter.or(Q.mold.id.in(
		    JPAExpressions
		        .select(moldPart.mold.id)
		        .from(moldPart)
		        .innerJoin(part).on(moldPart.partId.eq(part.id))
		        .where(part.partCode.contains(query).or(part.name.contains(query)).and(part.deleted.isNull().or(part.deleted.isFalse())))
		));
		*/

		List<String> selectedFields = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.TOOLING_SETTING);
		List<String> remainFields = new ArrayList<>();

		if (CollectionUtils.isEmpty(selectedFields)) {
			filter.or(Q.mold.equipmentCode.containsIgnoreCase(query)).or(Q.company.companyCode.containsIgnoreCase(query)).or(Q.company.name.containsIgnoreCase(query));
		} else {
			selectedFields.forEach(selectedField -> {
				switch (selectedField) {
				case "equipmentStatus":
					filter.or(Q.mold.equipmentStatus.stringValue().containsIgnoreCase(query));
					break;
				case "company":
					filter.or(Q.company.companyCode.contains(query).or(Q.company.name.contains(query)));
					break;
				case "location":
					filter.or(Q.location.name.contains(query).or(Q.location.locationCode.contains(query)));
					break;
				case "op":
					filter.or(Q.mold.operatingStatus.stringValue().containsIgnoreCase(query));
					break;

				case "counterCode":
					filter.or(Q.counter.equipmentCode.contains(query));
					break;
				case "hotRunnerDrop":
					filter.or(Q.mold.hotRunnerDrop.contains(query));
					break;
				case "hotRunnerZone":
					filter.or(Q.mold.hotRunnerZone.contains(query));
					break;
				case "injectionMachineId":
					filter.or(Q.mold.injectionMachineId.contains(query));
					break;
				case "runnerMaker":
					filter.or(Q.mold.runnerMaker.contains(query));
					break;
				case "runnerTypeTitle":
					filter.or(Q.mold.runnerType.contains(query));
					break;
				case "supplierCompanyName":
					filter.or(Q.mold.supplier.name.contains(query));
					break;
				case "toolMakerCompanyName":
					filter.or(Q.mold.toolMaker.name.contains(query));
					break;
				case "toolDescription":
					filter.or(Q.mold.toolDescription.contains(query));
					break;
				case "toolingType":
					filter.or(Q.mold.toolingType.contains(query));
					break;
				case "toolingLetter":
					filter.or(Q.mold.toolingLetter.contains(query));
					break;
				case "toolingComplexity":
					filter.or(Q.mold.toolingComplexity.contains(query));
					break;
				case "supplierMoldCode":
					filter.or(Q.mold.supplierMoldCode.contains(query));
					break;
				case "areaName":
					filter.or(Q.area.name.contains(query));
					break;
				case "productAndCategory":
					//todo: check when skip join with moldPart
//					QueryUtils.leftJoinCategoryByPart(jpqlQuery, join);
					filter.or(Q.category.name.contains(query).or(Q.product.name.contains(query)));
					//Use when don't join with moldPart
					/*
					    filter.or(Q.mold.id.in(
					        JPAExpressions
					            .select(moldPart.mold.id)
					            .from(moldPart)
					            .innerJoin(part).on(moldPart.partId.eq(part.id))
					            .where(part.category.name.contains(query)
					                .or(part.category.parentId.in(JPAExpressions.select(brand.id).from(brand).where(brand.parent.name.contains(query).and(brand.level.eq(2)))))
					                .or(part.category.grandParentId.in(JPAExpressions.select(category.id).from(category).where(category.name.contains(query).and(category.level.eq(1)))))
					                .and(part.deleted.isNull().or(part.deleted.isFalse())))
					    ));
					
					*/
					break;
				default:
					remainFields.add(selectedField);
					break;

				}
			});
		}
		if (!remainFields.isEmpty()) {
			List<Long> customFieldIds = DataUtils.getNumericElements(remainFields);

			filter.or(Q.mold.id.in(//
					JPAExpressions.select(Qs.customFieldValue.objectId).distinct()//
							.from(Qs.customFieldValue)//
							.where(//
									Qs.customFieldValue.customField.objectType.eq(ObjectType.TOOLING)//
											.and(Qs.customFieldValue.customField.id.in(customFieldIds))//
											.and(Qs.customFieldValue.value.containsIgnoreCase(query))//
							)//
			));
		}

		return filter;
	}

	private void checkDataStatus(TolStpGetIn input, BooleanBuilder filter) {
		QueryUtils.in(filter, Q.mold.toolingStatus, input.getToolingStatus());

		if (!ObjectUtils.isEmpty(input.getCounterStatus()) //
				&& input.getCounterStatus().size() < 3) {
			BooleanBuilder counterStatusFilter = new BooleanBuilder();
			for (CounterStatus counterStatus : input.getCounterStatus()) {
				switch (counterStatus) {
				case NOT_INSTALLED: {
					counterStatusFilter.or(Q.counter.isNull());
					break;
				}
				case DETACHED: {
					counterStatusFilter.or(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED));
					break;
				}
				case INSTALLED: {
					counterStatusFilter.or(Q.counter.isNotNull().and(Q.counter.equipmentStatus.ne(EquipmentStatus.DETACHED)));
					break;
				}
				}
			}
			filter.and(counterStatusFilter);
		}
	}

	private static NumberExpression<Integer> getOrderByOperatingStatus() {
		NumberExpression<Integer> expression = new CaseBuilder()//
				.when(Q.mold.operatingStatus.isNull()).then(0)//
				.when(Q.mold.operatingStatus.eq(OperatingStatus.WORKING)).then(1)//
				.when(Q.mold.operatingStatus.eq(OperatingStatus.IDLE)).then(2)//
				.when(Q.mold.operatingStatus.eq(OperatingStatus.NOT_WORKING)).then(3)//
				.otherwise(4);
		return expression;
	}

	//Todo: move to QueryUtils
	public static void leftJoinPartByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoin(query, join, Q.moldPart, Q.moldPart.moldId.eq(Q.mold.id));
		QueryUtils.leftJoin(query, join, Q.part, QueryUtils.isPart().and(Q.part.id.eq(Q.moldPart.partId)));
	}

	private static StringExpression getToolingStatus() {
		StringExpression expression = new CaseBuilder()
				.when(Q.mold.operatingStatus.eq(OperatingStatus.WORKING)
						.and(Q.counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(Q.counter.id.isNotNull()))//
				.then("IN_PRODUCTION")//
				.when(Q.mold.operatingStatus.eq(OperatingStatus.IDLE)
						.and(Q.counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(Q.counter.id.isNotNull()))//
				.then("IDLE")//
				.when(Q.mold.operatingStatus.eq(OperatingStatus.NOT_WORKING)
						.and(Q.counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(Q.counter.id.isNotNull()))//
				.then("NOT_WORKING").when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED).and(Q.counter.id.isNotNull())).then("SENSOR_DETACHED")//
				.when(Q.counter.equipmentStatus.ne(EquipmentStatus.DETACHED)
						.and(Q.counter.operatingStatus.eq(OperatingStatus.DISCONNECTED).or(Q.mold.operatingStatus.eq(OperatingStatus.DISCONNECTED))).and(Q.counter.id.isNotNull()))//
				.then("SENSOR_OFFLINE")//
				.when(Q.counter.operatingStatus.isNull().and(Q.mold.operatingStatus.isNull()).and(Q.counter.id.isNotNull()))//
				.then("ON_STANDBY")//
				.when(Q.mold.counterId.isNull()).then("NO_SENSOR")//
				.otherwise("");
		return expression;
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

	/*
	public List<TolStpItem.AccumulatedShot> findMoldAccumulatedShot(String year, List<Long> moldIds) {
	    QMold mold = QMold.mold;
	    QStatistics statistics = QStatistics.statistics;
	    BooleanBuilder builder = new BooleanBuilder();
	
	    if (CollectionUtils.isNotEmpty(moldIds)) {
	        builder.and(mold.id.in(moldIds));
	    }
	
	    NumberPath shotCount;
	    if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
	        shotCount = Q.statistics.shotCountVal;
	    } else {
	        shotCount = Q.statistics.shotCount;
	    }
	
	    JPQLQuery query;
	    if (!StringUtils.isEmpty(year)) {
	        builder.and(Q.statistics.ct.gt(0).or(Q.statistics.firstData.isTrue()));
	        query = BeanUtils.get(JPAQueryFactory.class).from(mold)
	            .leftJoin(Q.statistics).on(Q.statistics.moldId.eq(mold.id).and(Q.statistics.year.eq(year)))
	            .where(builder);
	    } else {
	        query = BeanUtils.get(JPAQueryFactory.class).from(mold)
	            .where(builder);
	    }
	    query.groupBy(mold.id);
	
	    NumberExpression sumShotCount = shotCount.sum();
	    if (!StringUtils.isEmpty(year)) {
	        query.select(Projections.constructor(TolStpItem.AccumulatedShot.class, mold.id, sumShotCount));
	    } else {
	        query.select(Projections.constructor(TolStpItem.AccumulatedShot.class, mold.id, mold.lastShot));
	    }
	
	    return query.fetch();
	}
	*/

	public TolStpItem.AccumulatedShot findMoldAccumulatedShotByLstLessThan(String lst, Long moldId) {
		BooleanBuilder builder = new BooleanBuilder();

		builder.and(Q.mold.id.eq(moldId));
		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = Q.statistics.shotCountVal;
		} else {
			shotCount = Q.statistics.shotCount;
		}

		JPQLQuery query;
		if (!StringUtils.isEmpty(lst)) {
			builder.and(Q.statistics.ct.gt(0).or(Q.statistics.firstData.isTrue()));
			builder.and(shotCount.isNotNull().and(shotCount.gt(0)));
			query = BeanUtils.get(JPAQueryFactory.class)//
					.from(Q.mold)//
					.leftJoin(Q.statistics).on(Q.statistics.moldId.eq(Q.mold.id).and(Q.statistics.lst.lt(lst)))//
					.where(builder);
		} else {
			query = BeanUtils.get(JPAQueryFactory.class).from(Q.mold).where(builder);
		}

		NumberExpression sumShotCount = Q.statistics.sc.max();
		if (!StringUtils.isEmpty(lst)) {
			query.select(Projections.constructor(TolStpItem.AccumulatedShot.class, Q.mold.id, sumShotCount));
		} else {
			query.select(Projections.constructor(TolStpItem.AccumulatedShot.class, Q.mold.id, Q.mold.lastShot));
		}

		return (TolStpItem.AccumulatedShot) query.fetchOne();
	}

	public List<User> getEngineersInCharge(Long moldId,List<Long> allowedEngineerIds){
		return BeanUtils.get(JPAQueryFactory.class).select(Q.user).from(Q.user)
				.innerJoin(Q.mold).on(Q.mold.id.eq(moldId).and(Q.mold.engineersInCharge.contains(Q.user)))
				.where(Q.user.id.in(allowedEngineerIds)).fetch();
	}
	public List<User> getPlantEngineersInCharge(Long moldId,List<Long> allowedEngineerIds){
		return BeanUtils.get(JPAQueryFactory.class).select(Q.user).from(Q.user)
				.innerJoin(Q.mold).on(Q.mold.id.eq(moldId).and(Q.mold.plantEngineersInCharge.contains(Q.user)))
				.where(Q.user.id.in(allowedEngineerIds)).fetch();
	}

}
