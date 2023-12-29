package com.emoldino.api.common.resource.composite.alrrst.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstGetIn;
import com.emoldino.api.common.resource.composite.alrrst.dto.AlrRstItem;
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
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.MisconfigureStatus;

@Repository
public class AlrRstRepository {

	public long count(AlrRstGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldMisconfigure.id.countDistinct())//
				.from(Q.moldMisconfigure);

		applyFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrRstItem> findAll(AlrRstGetIn input, BatchIn batchin, Pageable pageable) {

		JPQLQuery<AlrRstItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrRstItem.class, //
						Q.moldMisconfigure.id, //
						Q.moldMisconfigure.misconfigureStatus, //
						Q.moldMisconfigure.triggeredBy, //
						Q.moldMisconfigure.createdAt, //
						Q.moldMisconfigure.confirmedBy, //
						Q.moldMisconfigure.confirmedAt, //

						Q.mold.id, //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.toolingStatus, //

						Q.moldMisconfigure.counterId, 	// counterID
						Q.moldMisconfigure.counterCode, // counterCode
						new CaseBuilder()//
								.when(Q.counter.equipmentStatus.isNull())//
								.then(CounterStatus.NOT_INSTALLED.name())//
								.when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED))//
								.then(CounterStatus.DETACHED.name())//
								.otherwise(CounterStatus.INSTALLED.name())//
								.as("sensorStatus"), //

						Q.moldMisconfigure.lastShot, // Accumulated Shots
						Q.moldMisconfigure.preset.castToNum(Integer.class).as("resetValue"), //
//						Expressions.numberTemplate(Integer.class, "CAST({0} AS int)", Q.moldMisconfigure.preset).as("resetValue"), //

						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.company.companyType, //

						Q.location.id, //
						Q.location.locationCode, //
						Q.location.name //
				)) //
				.distinct()//
				.from(Q.moldMisconfigure);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.moldMisconfigure.id)//
				.put("alertStatus", Q.moldMisconfigure.misconfigureStatus)//
				.put("createdAt", Q.moldMisconfigure.createdAt)//
				.put("creationDate", Q.moldMisconfigure.createdAt)//
				.put("moldId", Q.mold.id)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("counterCode", Q.moldMisconfigure.counterCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("sensorStatus", Expressions.stringPath("sensorStatus"))//
				.put("resetValue", Expressions.numberPath(Long.class, "resetValue"))//
//				.put("accumShotCount", Expressions.numberPath(Integer.class, "accumShotCount"))//
				.put("lastShotAt", Q.mold.lastShotAt)//
				.put("lastShot", Q.moldMisconfigure.lastShot)//
				.put("lastShotDate", Q.mold.lastShotAt)//
				.put("companyId", Q.company.id)//
				.put("companyName", Q.company.name)//
				.put("companyCode", Q.company.companyCode)//
				.put("companyType", Q.company.companyType)//
				.put("locationId", Q.location.id)//
				.put("locationName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldMisconfigure.createdAt.desc());
		QueryResults<AlrRstItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrRstGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.mold, () -> Q.moldMisconfigure.mold.id.eq(Q.mold.id).and(QueryUtils.isMold()));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.moldMisconfigure.latest.isTrue());//improve late

		if (AlertTab.ALERT.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldMisconfigure.misconfigureStatus.in(MisconfigureStatus.MISCONFIGURED));
		} else {
			filter.and(Q.moldMisconfigure.misconfigureStatus.in(MisconfigureStatus.CONFIRMED));
		}
		QueryUtils.in(filter, Q.moldMisconfigure.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
					Q.mold.equipmentCode.contains(input.getQuery())//
							.or(Q.moldMisconfigure.counterCode.contains(input.getQuery()))//
							.or(Q.location.name.contains(input.getQuery()))//
							.or(Q.location.locationCode.contains(input.getQuery()))//
							.or(Q.company.companyCode.contains(input.getQuery()))//
							.or(Q.company.name.contains(input.getQuery()))//
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.moldMisconfigure.id);
		query.where(filter);
	}

}
