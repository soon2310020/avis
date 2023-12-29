package com.emoldino.api.asset.resource.composite.alreol.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolGetIn;
import com.emoldino.api.asset.resource.composite.alreol.dto.AlrEolItem;
import com.emoldino.api.asset.resource.composite.alreol.enumeration.AlrEolTab;
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
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.RefurbishmentStatus;
import saleson.common.enumeration.SpecialAlertType;

@Repository
public class AlrEolRepository {

	public long count(AlrEolGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldRefurbishment.id.countDistinct())//
				.from(Q.moldRefurbishment);

		applyFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrEolItem> findAll(AlrEolGetIn input, BatchIn batchin, Pageable pageable) {

		JPQLQuery<AlrEolItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrEolItem.class, //
						Q.moldRefurbishment.id, //
						Q.moldRefurbishment.refurbishmentStatus, //
						Q.moldRefurbishment.priority, //
						Q.moldRefurbishment.createdAt, //

						Q.mold.id, //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.toolingStatus, //

						Q.counter.id, //
						Q.counter.equipmentCode.as("counterCode"), //
						new CaseBuilder()//
								.when(Q.counter.equipmentStatus.isNull())//
								.then(CounterStatus.NOT_INSTALLED.name())//
								.when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED))//
								.then(CounterStatus.DETACHED.name())//
								.otherwise(CounterStatus.INSTALLED.name())//
								.as("sensorStatus"), //

						Q.mold.lastShot, //
						Q.moldRefurbishment.estimateExtendedLife, //
						Q.moldRefurbishment.endLifeAt, //

						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.company.companyType, //

						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode//
				)) //
				.distinct()//
				.from(Q.moldRefurbishment);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		String property = QueryUtils.getFirstSortProperty(pageable);

		if ("alertStatus".equals(property)) {
			Sort.Direction direction = QueryUtils.getFirstSortDirection(pageable);
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), direction, "alertStatus", "priority");
		}

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.moldRefurbishment.id)//
				.put("alertStatus", Q.moldRefurbishment.refurbishmentStatus)//
				.put("priority", Q.moldRefurbishment.priority)//
				.put("createdAt", Q.moldRefurbishment.createdAt)//
				.put("creationDate", Q.moldRefurbishment.createdAt)//

				.put("moldId", Q.mold.id)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("counterCode", Q.counter.equipmentCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("sensorStatus", Expressions.stringPath("sensorStatus"))//
				.put("resetValue", Expressions.numberPath(Long.class, "resetValue"))//
				.put("accumShotCount", Q.mold.lastShot)//
				.put("estimateExtendedLife", Q.moldRefurbishment.estimateExtendedLife)//
				.put("endLifeDate", Q.moldRefurbishment.endLifeAt)//

//				.put("lastShotAt", Q.mold.lastShotAt)//
//				.put("lastShotDate", Q.mold.lastShotAt)//
				.put("companyId", Q.company.id)//
				.put("companyName", Q.company.name)//
				.put("companyCode", Q.company.companyCode)//
				.put("companyType", Q.company.companyType)//
				.put("locationId", Q.location.id)//
				.put("locationName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldRefurbishment.createdAt.desc());
		QueryResults<AlrEolItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrEolGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.mold, () -> Q.moldRefurbishment.mold.id.eq(Q.mold.id).and(QueryUtils.isMold()));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));

		BooleanBuilder filter = new BooleanBuilder();

		filter.and(Q.mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));

		//improve later
		if (!AlertTab.HISTORY_LOG.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldRefurbishment.latest.isTrue());
		}
		if (input.getSpecialAlertType() != null) {
			if (SpecialAlertType.MEDIUM_HIGH.equals(input.getSpecialAlertType())) {
				filter.and(Q.moldRefurbishment.priority.in(Arrays.asList(//
						PriorityType.MEDIUM, //
						PriorityType.HIGH//
				)));
			} else if (SpecialAlertType.HIGH.equals(input.getSpecialAlertType())) {
				filter.and(Q.moldRefurbishment.priority.eq(PriorityType.HIGH));
			}
		}

		if (AlertTab.ALERT.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldRefurbishment.refurbishmentStatus.in(Arrays.asList(//
					RefurbishmentStatus.END_OF_LIFECYCLE, //
					RefurbishmentStatus.REQUESTED//
			)));
		} else if (AlrEolTab.APPROVED.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldRefurbishment.refurbishmentStatus.in(RefurbishmentStatus.APPROVED));
		} else if (AlrEolTab.DISAPPROVED.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldRefurbishment.refurbishmentStatus.in(RefurbishmentStatus.DISAPPROVED));
		} else if (AlertTab.HISTORY_LOG.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldRefurbishment.lastChecked.isTrue());//improve later

			filter.and(Q.moldRefurbishment.refurbishmentStatus.in(//
					RefurbishmentStatus.DISCARDED, //
					RefurbishmentStatus.COMPLETED, //
					RefurbishmentStatus.REPAIRED, //
					RefurbishmentStatus.DISAPPROVED, //
					RefurbishmentStatus.APPROVED//
			));
		}

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
					Q.mold.equipmentCode.contains(input.getQuery())//
							.or(Q.counter.equipmentCode.contains(input.getQuery()))//
							.or(Q.location.name.contains(input.getQuery()))//
							.or(Q.location.locationCode.contains(input.getQuery()))//
							.or(Q.company.companyCode.contains(input.getQuery()))//
							.or(Q.company.name.contains(input.getQuery()))//
			);
		}

		QueryUtils.in(filter, Q.moldRefurbishment.id, input.getId());
		QueryUtils.applyBatchFilter(filter, batchin, Q.moldRefurbishment.id);
		query.where(filter);
	}

}
