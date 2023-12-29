package com.emoldino.api.asset.resource.composite.alrrlo.repository;

import static saleson.common.enumeration.EquipmentStatus.DISCARDED;
import static saleson.common.enumeration.EquipmentStatus.DISPOSED;
import static saleson.common.enumeration.MoldLocationStatus.APPROVED;
import static saleson.common.enumeration.MoldLocationStatus.DISAPPROVED;
import static saleson.common.enumeration.MoldLocationStatus.UNAPPROVED;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.base.mold.enumeration.RelocationType;
import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloGetIn;
import com.emoldino.api.asset.resource.composite.alrrlo.dto.AlrRloItem;
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
import saleson.common.enumeration.MoldLocationStatus;
import saleson.model.QLocation;

@Repository
public class AlrRloRepository {
	private final QLocation qPreviousLocation = new QLocation("previousLocation");

	public long count(AlrRloGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldLocation.id.countDistinct())//
				.distinct().from(Q.moldLocation);

		applyFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrRloItem> findAll(AlrRloGetIn input, BatchIn batchin, Pageable pageable) {
		JPQLQuery<AlrRloItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrRloItem.class, //
						Q.moldLocation.id, //
						Q.moldLocation.moldLocationStatus, //
						Q.moldLocation.createdAt, //
						Q.moldLocation.confirmedAt, //

						Q.mold.id, //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.toolingStatus, //
						new CaseBuilder()//
								.when(Q.counter.equipmentStatus.isNull()) //
								.then(CounterStatus.NOT_INSTALLED.name()) //
								.when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED)) //
								.then(CounterStatus.DETACHED.name()) //
								.otherwise(CounterStatus.INSTALLED.name()) //
								.as("sensorStatus"), //

						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.company.companyType, //

						qPreviousLocation.id.as("previousLocationId"), //
						qPreviousLocation.name.as("previousLocationName"), //
						qPreviousLocation.locationCode.as("previousLocationCode"), //

						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode //
				)) //
				.distinct()//
				.from(Q.moldLocation);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//

				.put("id", Q.moldLocation.id) //
				.put("moldLocationStatus", Q.moldLocation.moldLocationStatus)//
				.put("creationDateTime", Q.moldLocation.createdAt) //
				.put("confirmDateTime", Q.moldLocation.confirmedAt) //

				.put("moldId", Q.mold.id) //
				.put("moldCode", Q.mold.equipmentCode) //
				.put("toolingStatus", Q.mold.toolingStatus) //
				.put("sensorStatus", Expressions.stringPath("sensorStatus"))//

				.put("companyId", Q.company.id) //
				.put("companyName", Q.company.name) //
				.put("companyCode", Q.company.companyCode) //
				.put("companyType", Q.company.companyType) //

				.put("previousLocationId", qPreviousLocation.id) //
				.put("previousLocationName", qPreviousLocation.name) //
				.put("previousLocationCode", qPreviousLocation.locationCode) //

				.put("locationId", Q.location.id) //
				.put("locationName", Q.location.name) //
				.put("locationCode", Q.location.locationCode) //
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldLocation.createdAt.desc());
		QueryResults<AlrRloItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrRloGetIn input, BatchIn batchin) {
		QueryUtils.leftJoin(query, join, Q.mold, () -> Q.moldLocation.moldId.eq(Q.mold.id));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.moldLocation.locationId));
		QueryUtils.leftJoin(query, join, qPreviousLocation, () -> qPreviousLocation.id.eq(Q.moldLocation.previousLocationId));

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.mold.enabled.isTrue().or(Q.mold.enabled.isNull()) //
				.and(Q.mold.equipmentStatus.notIn(DISPOSED, DISCARDED)) //
				.and(Q.moldLocation.relocationType.eq(RelocationType.PLANT)) //
		);
		if (AlertTab.ALERT.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldLocation.moldLocationStatus.eq(MoldLocationStatus.PENDING));
		} else {
			filter.and(Q.moldLocation.moldLocationStatus.in(APPROVED, DISAPPROVED, UNAPPROVED));
		}
		QueryUtils.in(filter, Q.moldLocation.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) { //
			filter.and(Q.mold.equipmentCode.contains(input.getQuery()) //
					.or(qPreviousLocation.locationCode.contains(input.getQuery())) //
					.or(qPreviousLocation.name.contains(input.getQuery())) //
					.or(Q.location.name.contains(input.getQuery())) //
					.or(Q.location.locationCode.contains(input.getQuery())) //
					.or(Q.company.companyCode.contains(input.getQuery())) //
					.or(Q.company.name.contains(input.getQuery())) //
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.moldLocation.id);
		query.where(filter);
	}
}
