package com.emoldino.api.production.resource.composite.alrutm.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmGetIn;
import com.emoldino.api.production.resource.composite.alrutm.dto.AlrUtmItem;
import com.emoldino.api.production.resource.composite.alrutm.enumeration.AlrUtmTab;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.enumeration.AlertTab;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EfficiencyStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.util.SecurityUtils;

@Repository
public class AlrUtmRepository {

	public long count(AlrUtmGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldEfficiency.id.countDistinct())//
				.from(Q.moldEfficiency);
		applyFilter(query, new HashSet<>(), input, null);
		return query.fetchCount();
	}

	public Page<AlrUtmItem> findAll(AlrUtmGetIn input, BatchIn batchin, Pageable pageable) {
		NumberExpression<Double> variance = Q.moldEfficiency.efficiency.subtract(Q.moldEfficiency.baseEfficiency.multiply(100).divide(Q.moldEfficiency.efficiency));

		JPQLQuery<AlrUtmItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrUtmItem.class, //
						Q.moldEfficiency.id, //

						Q.moldEfficiency.notificationStatus, //
						Q.moldEfficiency.createdAt, //
						Q.moldEfficiency.confirmedAt, //

						Q.moldEfficiency.efficiencyStatus, //
						Q.moldEfficiency.baseEfficiency, //
						Q.moldEfficiency.efficiency, //
						variance.as("variance"), //

						Q.mold.id, //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.toolingStatus, //
						Q.mold.operatingStatus, //
						new CaseBuilder()//
								.when(Q.counter.equipmentStatus.isNull())//
								.then(CounterStatus.NOT_INSTALLED.name())//
								.when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED))//
								.then(CounterStatus.DETACHED.name())//
								.otherwise(CounterStatus.INSTALLED.name())//
								.as("sensorStatus"), //

						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.company.companyType, //

						Q.location.id, //
						Q.location.locationCode, //
						Q.location.name, //

						Q.machine.id, //
						Q.machine.machineCode //
				)) //
				.distinct()//
				.from(Q.moldEfficiency);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.moldEfficiency.id)//
				.put("alertStatus", Q.moldEfficiency.notificationStatus)//
				.put("creationDateTime", Q.moldEfficiency.createdAt)//
				.put("confirmedDateTime", Q.moldEfficiency.confirmedAt)//
				.put("uptimeStatus", Q.moldEfficiency.efficiencyStatus)//
				.put("baseEfficiency", Q.moldEfficiency.baseEfficiency)//
				.put("efficiency", Q.moldEfficiency.efficiency)//
				.put("variance", Expressions.numberPath(Double.class, "variance"))//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("operatingStatus", Q.mold.operatingStatus)//
				.put("sensorStatus", Expressions.stringPath("sensorStatus"))//
				.put("companyName", Q.company.name)//
				.put("companyCode", Q.company.companyCode)//
				.put("companyType", Q.company.companyType)//
				.put("locationName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.put("machineId", Q.machine.id)//
				.put("machineCode", Q.machine.machineCode)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldEfficiency.createdAt.desc());
		QueryResults<AlrUtmItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrUtmGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.mold, () -> Q.moldEfficiency.moldId.eq(Q.mold.id));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));
		QueryUtils.leftJoin(query, join, Q.machine, () -> Q.machine.id.eq(Q.mold.machineId));
		QueryUtils.join(query, join, Q.userAlert, () -> Q.userAlert.periodType.eq(Q.moldEfficiency.periodType)
				.and(Q.userAlert.user.id.eq(SecurityUtils.getUserId()).and(Q.userAlert.alertType.eq(AlertType.EFFICIENCY))));

		BooleanBuilder filter = new BooleanBuilder();

		filter.and(Q.mold.operatingStatus.isNotNull());
		if (SpecialAlertType.L2.equals(input.getSpecialAlertType())) {
			filter.and(Q.moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L2));

		} else if (SpecialAlertType.L1L2.equals(input.getSpecialAlertType())) {
			filter.and(Q.moldEfficiency.efficiencyStatus.in(EfficiencyStatus.OUTSIDE_L1, EfficiencyStatus.OUTSIDE_L2));
		}
		if (AlertTab.HISTORY_LOG.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldEfficiency.notificationStatus.eq(NotificationStatus.CONFIRMED));
		} else {
			filter.and(Q.moldEfficiency.notificationStatus.eq(NotificationStatus.ALERT));
			if (AlrUtmTab.OUTSIDE_L1.getTitle().equals(input.getTabName())) {
				filter.and(Q.moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L1));
			} else if (AlrUtmTab.OUTSIDE_L2.getTitle().equals(input.getTabName())) {
				filter.and(Q.moldEfficiency.efficiencyStatus.eq(EfficiencyStatus.OUTSIDE_L2));
			}
		}

		QueryUtils.in(filter, Q.moldEfficiency.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
					Q.mold.equipmentCode.contains(input.getQuery())//
							.or(Q.location.name.contains(input.getQuery()))//
							.or(Q.location.locationCode.contains(input.getQuery()))//
							.or(Q.company.companyCode.contains(input.getQuery()))//
							.or(Q.company.name.contains(input.getQuery()))//
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.moldEfficiency.id);
		query.where(filter);
	}

}
