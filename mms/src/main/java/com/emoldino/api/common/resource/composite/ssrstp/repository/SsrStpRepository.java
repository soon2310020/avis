package com.emoldino.api.common.resource.composite.ssrstp.repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpGetIn;
import com.emoldino.api.common.resource.composite.ssrstp.dto.SsrStpItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.model.QCounter;
import saleson.model.QMold;
import saleson.model.QWorkOrder;
import saleson.model.QWorkOrderAsset;
import saleson.model.TabTable;

@Repository
public class SsrStpRepository {

	public Page<SsrStpItem> findAll(SsrStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.COUNTER, input.getTabName());

		QWorkOrder qWorkOrder = QWorkOrder.workOrder;
		QWorkOrderAsset qWorkOrderAsset = QWorkOrderAsset.workOrderAsset;

		JPQLQuery<SsrStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(SsrStpItem.class, //
						Q.counter, //
						qWorkOrder.completedOn.max().as("lastWorkOrderAt"), //
						qWorkOrder.countDistinct().as("workOrderCount")//
				))//
				.distinct()//
				.from(Q.counter)//
				.leftJoin(qWorkOrderAsset).on(//
						qWorkOrderAsset.type.eq(ObjectType.COUNTER).and(qWorkOrderAsset.assetId.eq(Q.counter.id))//
				)//
				.leftJoin(qWorkOrder).on(//
						qWorkOrder.id.eq(qWorkOrderAsset.workOrderId)//
								.and(qWorkOrder.status.in(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)))//
				.groupBy(Q.counter);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin, tabTable);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("equipmentCode", Q.counter.equipmentCode)//
				.put("sensorStatus", getCounterStatus())//
				.put("batteryStatus", Q.counter.batteryStatus)//
				.put("presetStatus", Q.counter.presetStatus)//
				.put("toolingStatus", getToolingStatus())//fix sort for case no tooling
				.put("operatingStatus", getOperatingStatusOrder())//
				.put("location.company.name", Q.counter.location.company.name)//
				.put("location.name", Q.counter.location.name)//
				.put("mold.equipmentCode", Q.mold.equipmentCode)//
				.put("installedBy", Q.counter.installedBy)//
				.put("installedAt", Q.counter.installedAt)//
				.put("activationDate", Q.counter.activatedAt)//
				.put("shotCount", Q.counter.shotCount)//
				.put("memo", Q.counter.memo)
//				.put("activePeriod", getActivePeriod())// queryDSL error
//				.put("subscriptionStatus", getSubscriptionStatus())//queryDSL error
				.put("subscriptionExpiry", getSubscriptionExpiry())//
				.put("supplierMoldCode", Q.mold.supplierMoldCode)//
				.put("areaName", Q.area.name)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.counter.id.desc());

		QueryResults<SsrStpItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private BooleanBuilder addQueryFilters(BooleanBuilder filter, String query) {
		if (ObjectUtils.isEmpty(query)) {
			return filter;
		}

		BooleanBuilder queryFilter = new BooleanBuilder();

		queryFilter.or(Q.counter.equipmentCode.containsIgnoreCase(query));
		List<String> selectedFields = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.COUNTER_SETTING);
		if (CollectionUtils.isEmpty(selectedFields)) {
			queryFilter.or(Q.counter.equipmentCode.containsIgnoreCase(query))//
					.or(Q.company.companyCode.containsIgnoreCase(query))//
					.or(Q.company.name.containsIgnoreCase(query))//
					.or(Q.location.name.containsIgnoreCase(query))//
					.or(Q.location.locationCode.containsIgnoreCase(query))//
					.or(Q.mold.equipmentCode.containsIgnoreCase(query));
		} else {
			selectedFields.forEach(selectedField -> {
				switch (selectedField) {
				case "mold.equipmentCode":
					queryFilter.or(Q.mold.equipmentCode.containsIgnoreCase(query));
					break;
				case "batteryStatus":
					queryFilter.or(Q.counter.batteryStatus.stringValue().containsIgnoreCase(query));
					break;
				case "memo":
					queryFilter.or(Q.counter.memo.containsIgnoreCase(query));
					break;
				case "installedBy":
					queryFilter.or(Q.counter.installedBy.containsIgnoreCase(query));
					break;
				case "company":
					queryFilter.or(Q.company.companyCode.containsIgnoreCase(query)).or(Q.company.name.containsIgnoreCase(query));
					break;
				case "location":
					queryFilter.or(Q.location.name.containsIgnoreCase(query)).or(Q.location.locationCode.containsIgnoreCase(query));
					break;
				case "supplierMoldCode":
					queryFilter.or(Q.mold.supplierMoldCode.containsIgnoreCase(query));
					break;
				case "areaName":
					queryFilter.or(Q.area.name.containsIgnoreCase(query));
					break;
				}
			});
		}

		filter.and(queryFilter);

		return filter;
	}

	private static StringExpression getCounterStatus() {
		StringExpression counterStatus = new CaseBuilder()//
				.when(Q.mold.isNull())//
				.then("NOT_INSTALLED")//
				.when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED))//
				.then("DETACHED")//
				.otherwise("INSTALLED");
		return counterStatus;
	}

	private static NumberExpression<Integer> getOperatingStatusOrder() {
		NumberExpression<Integer> operatingStatusOrder = new CaseBuilder()//
				.when(Q.counter.operatingStatus.isNull())//
				.then(0)//
				.when(Q.counter.operatingStatus.eq(OperatingStatus.WORKING))//
				.then(1)//
				.when(Q.counter.operatingStatus.eq(OperatingStatus.IDLE))//
				.then(2)//
				.when(Q.counter.operatingStatus.eq(OperatingStatus.NOT_WORKING))//
				.then(3)//
				.otherwise(4);
		return operatingStatusOrder;
	}

	// TODO Fix activePeriod
	private static NumberExpression<Long> getActivePeriod() {
		DateExpression<Instant> expirationDate = getExpirationDate();
		Expression<Instant> lastActiveDate = new CaseBuilder()//
				.when(expirationDate.isNotNull().and(expirationDate.before(Instant.now())))//
				.then(expirationDate)//
				.otherwise(Instant.now());
		NumberExpression<Long> activePeriod = Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1}) ", Q.counter.activatedAt, lastActiveDate);
		return activePeriod;
	}

	// TODO Fix activePeriod
	private static StringExpression getSubscriptionStatus() {
		DateExpression<Instant> expirationDate = getExpirationDate();
		StringExpression subscriptionStatus = new CaseBuilder()//
				.when((expirationDate.before(Instant.now())))//
				.then("Expired")//
				.when((expirationDate.after(Instant.now())))//
				.then("Valid")//
				.otherwise("None");
		return subscriptionStatus;
	}

	private static StringExpression getToolingStatus() {
		StringExpression toolingStatus = new CaseBuilder().when(Q.mold.toolingStatus.eq(ToolingStatus.IN_PRODUCTION)).then(ToolingStatus.IN_PRODUCTION.name())
				.when(Q.mold.toolingStatus.eq(ToolingStatus.IDLE)).then(ToolingStatus.IDLE.name()).when(Q.mold.toolingStatus.eq(ToolingStatus.INACTIVE))
				.then(ToolingStatus.INACTIVE.name()).when(Q.mold.toolingStatus.eq(ToolingStatus.SENSOR_DETACHED)).then(ToolingStatus.SENSOR_DETACHED.name())
				.when(Q.mold.toolingStatus.eq(ToolingStatus.SENSOR_OFFLINE)).then(ToolingStatus.SENSOR_OFFLINE.name()).when(Q.mold.toolingStatus.eq(ToolingStatus.ON_STANDBY))
				.then(ToolingStatus.ON_STANDBY.name()).when(Q.mold.toolingStatus.eq(ToolingStatus.DISABLED)).then(ToolingStatus.DISABLED.name())
				.when(Q.mold.toolingStatus.eq(ToolingStatus.UNKNOWN)).then(ToolingStatus.UNKNOWN.name()).when(Q.mold.toolingStatus.eq(ToolingStatus.NO_SENSOR))
				.then(ToolingStatus.NO_SENSOR.name()).otherwise(ToolingStatus.NO_SENSOR.name());
		return toolingStatus;
	}

	// TODO Fix expirationDate
	private static DateExpression<Instant> getExpirationDate() {
		DateExpression<Instant> expirationDate = Expressions.dateTemplate(Instant.class, "DATE_ADD({0}, INTERVAL {1} DAY) ", //
				Q.counter.activatedAt, Q.counter.subscriptionTerm.multiply(365));
		return expirationDate;
	}

	// TODO Fix subscriptionExpiry
	private static NumberExpression<Integer> getSubscriptionExpiry() {
		NumberExpression<Integer> subscriptionExpiry = Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(DAY, {0}, {1}) ", //
				Q.counter.activatedAt, Instant.now())//
				.mod(Q.counter.subscriptionTerm.multiply(365));
		return subscriptionExpiry;
	}

	public long count(SsrStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.COUNTER, input.getTabName());

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(QCounter.counter.id.countDistinct())//
				.from(QCounter.counter);

		applyFilter(query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, SsrStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, Q.counter.id);

		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applySensorFilter(query, join, input.getFilterCode());
		} else {
			QueryUtils.applySensorDisabledFilter(query, join, input.getFilterCode());
		}

		QueryUtils.leftJoin(query, join, Q.mold, () -> Q.mold.counterId.eq(Q.counter.id));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.counter.locationId).and(QueryUtils.isLocation()));
		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.counter.companyId).and(QueryUtils.isCompany()));
		QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.mold.areaId));

		BooleanBuilder filter = new BooleanBuilder();

		addQueryFilters(filter, input.getQuery());
		QueryUtils.eq(filter, Q.company.companyType, input.getCompanyType());
		//
		BooleanBuilder toolingStatusBuilder = new BooleanBuilder();
		QueryUtils.in(toolingStatusBuilder, Q.mold.toolingStatus, input.getToolingStatus());
		if (input.getToolingStatus() != null && input.getToolingStatus().contains(ToolingStatus.NO_SENSOR)) {
			toolingStatusBuilder.or(Q.mold.isNull());
		}
		filter.and(toolingStatusBuilder);

		if (input.getSensorStatus() != null) {
			BooleanBuilder statusBuilder = new BooleanBuilder();
			for (CounterStatus sensorStatus : input.getSensorStatus()) {
				switch (sensorStatus) {
				case INSTALLED:
					statusBuilder.or(Q.counter.equipmentStatus.eq(EquipmentStatus.INSTALLED).and(Q.mold.isNotNull()));
					break;
				case DETACHED:
					statusBuilder.or(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED).and(Q.mold.isNotNull()));
					break;
				case NOT_INSTALLED:
					statusBuilder.or(Q.mold.isNull());
					break;
				}
			}
			filter.and(statusBuilder);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.counter.id);

		query.where(filter);
	}

}
