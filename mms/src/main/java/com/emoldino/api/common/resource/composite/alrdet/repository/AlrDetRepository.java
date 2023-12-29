package com.emoldino.api.common.resource.composite.alrdet.repository;

import static com.emoldino.framework.enumeration.AlertTab.HISTORY_LOG;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetGetIn;
import com.emoldino.api.common.resource.composite.alrdet.dto.AlrDetItem;
import com.emoldino.framework.dto.BatchIn;
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
import saleson.common.enumeration.NotificationStatus;

@Repository
public class AlrDetRepository {

	public long count(AlrDetGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldDetachment.id.countDistinct())//
				.distinct().from(Q.moldDetachment);

		applyFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrDetItem> findAll(AlrDetGetIn input, BatchIn batchin, Pageable pageable) {
		JPQLQuery<AlrDetItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrDetItem.class, //
						Q.moldDetachment.id, //
						Q.moldDetachment.detachmentTime, // Detachment Time
						Q.moldDetachment.repairTime, //
						Q.moldDetachment.confirmedAt, //  	Confirmation Date

						Q.mold.id, //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.toolingStatus, //
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
						Q.location.name, //
						Q.location.locationCode, //

						Q.machine.id, //
						Q.machine.machineCode //
				)) //
				.distinct()//
				.from(Q.moldDetachment);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.moldDetachment.id) //
				.put("detachmentDateTime", Q.moldDetachment.detachmentTime) //
				.put("repairDateTime", Q.moldDetachment.repairTime) //
				.put("confirmDateTime", Q.moldDetachment.confirmedAt) //

				.put("moldId", Q.mold.id) //
				.put("moldCode", Q.mold.equipmentCode) //
				.put("toolingStatus", Q.mold.toolingStatus) //
				.put("sensorStatus", Expressions.stringPath("sensorStatus"))//

				.put("companyId", Q.company.id) //
				.put("companyName", Q.company.name) //
				.put("companyCode", Q.company.companyCode) //
				.put("companyType", Q.company.companyType) //

				.put("locationId", Q.location.id) //
				.put("locationName", Q.location.name) //
				.put("locationCode", Q.location.locationCode) //

				.put("machineId", Q.machine.id) //
				.put("machineCode", Q.machine.machineCode) //

				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldDetachment.createdAt.desc());
		QueryResults<AlrDetItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrDetGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.mold, () -> Q.mold.id.eq(Q.moldDetachment.moldId).and(QueryUtils.isMold()));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));
		QueryUtils.leftJoin(query, join, Q.machine, () -> Q.machine.id.eq(Q.mold.machineId));

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.moldDetachment.latest.isTrue());//improve late
		if (HISTORY_LOG.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldDetachment.notificationStatus.notIn(NotificationStatus.PENDING, NotificationStatus.ALERT));
		} else {
			filter.and(Q.moldDetachment.notificationStatus.in(NotificationStatus.PENDING, NotificationStatus.ALERT));
		}
		QueryUtils.in(filter, Q.moldDetachment.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(Q.mold.equipmentCode.contains(input.getQuery())//
					.or(Q.location.name.contains(input.getQuery()))//
					.or(Q.location.locationCode.contains(input.getQuery()))//
					.or(Q.company.companyCode.contains(input.getQuery()))//
					.or(Q.company.name.contains(input.getQuery()))//
					.or(Q.machine.machineCode.contains(input.getQuery()))//
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.moldDetachment.id);
		query.where(filter);
	}
}
