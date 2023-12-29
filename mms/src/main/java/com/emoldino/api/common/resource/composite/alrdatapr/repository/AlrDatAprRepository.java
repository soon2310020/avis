package com.emoldino.api.common.resource.composite.alrdatapr.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprGetIn;
import com.emoldino.api.common.resource.composite.alrdatapr.dto.AlrDatAprItem;
import com.emoldino.api.common.resource.composite.alrdatapr.enumeration.DatAprTab;
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

import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.util.SecurityUtils;

@Repository
public class AlrDatAprRepository {

	public long count(AlrDatAprGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldDataSubmission.id.countDistinct())//
				.distinct().from(Q.moldDataSubmission);

		applyFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrDatAprItem> findAll(AlrDatAprGetIn input, BatchIn batchin, Pageable pageable) {
		JPQLQuery<AlrDatAprItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrDatAprItem.class, //
						Q.moldDataSubmission.id, //
						Q.moldDataSubmission.notificationStatus, //
						Q.moldDataSubmission.createdAt, //
						Q.moldDataSubmission.approvedAt, //
						Q.moldDataSubmission.approvedBy, //
						Q.moldDataSubmission.reason, //

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
						Q.location.locationCode //
				)) //
				.distinct()//
				.from(Q.moldDataSubmission);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.companyId));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.moldDataSubmission.id) //
				.put("createDateTime", Q.moldDataSubmission.createdAt) //
				.put("notificationStatus", Q.moldDataSubmission.notificationStatus) //  Status
				.put("approveDateTime", Q.moldDataSubmission.approvedAt) // Approval/Disapproval Date
				.put("approvedBy", Q.moldDataSubmission.approvedBy) // Approval/Disapproval Date
				.put("reason", Q.moldDataSubmission.reason) // Approval/Disapproval Date

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
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldDataSubmission.createdAt.desc());
		QueryResults<AlrDatAprItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrDatAprGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.mold, () -> Q.moldDataSubmission.moldId.eq(Q.mold.id).and(QueryUtils.isMold()));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));

		BooleanBuilder filter = new BooleanBuilder();
//		filter.and(Q.moldDataSubmission.latest.isTrue());//Shows only 1 alert in history for each tooling in list alerts. Can see many row in detail alert
//		filter.and(Q.mold.deleted.isNull().or(Q.mold.deleted.isFalse()));// move to join condition

		if (DatAprTab.APPROVED.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldDataSubmission.notificationStatus.eq(NotificationStatus.APPROVED));
		}
		if (DatAprTab.DISAPPROVED.getTitle().equals(input.getTabName())) {
			filter.and(Q.moldDataSubmission.notificationStatus.eq(NotificationStatus.DISAPPROVED));
		}

		if (Arrays.asList(AlertTab.ALERT.getTitle(), DatAprTab.APPROVED.getTitle(), DatAprTab.DISAPPROVED.getTitle()).contains(input.getTabName())) {
			if (SecurityUtils.isAdmin()) {
				filter.and(Q.moldDataSubmission.notificationStatus.in(NotificationStatus.PENDING, NotificationStatus.ALERT));
			} else {
				filter.and(Q.moldDataSubmission.notificationStatus.in(Arrays.asList(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED))
						.and(Q.moldDataSubmission.confirmed.isFalse().or(Q.moldDataSubmission.confirmed.isNull())));
			}

			QueryUtils.leftJoin(query, join, Q.logUserAlert, //
					() -> Q.moldDataSubmission.id.eq(Q.logUserAlert.alertId));
			filter
					//.and(Q.moldDataSubmission.notificationStatus.in(NotificationStatus.PENDING, NotificationStatus.ALERT) //
					.and(Q.logUserAlert.alertType.eq(AlertType.DATA_SUBMISSION) //
							.and(Q.logUserAlert.userId.eq(SecurityUtils.getUserId())) //
					);
		} else {
//			filter.and(Q.moldDataSubmission.notificationStatus.in(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED));
			if (SecurityUtils.isAdmin()) {
				filter.and(Q.moldDataSubmission.notificationStatus.in(NotificationStatus.APPROVED, NotificationStatus.DISAPPROVED));
			} else {
				filter.and(Q.moldDataSubmission.confirmed.isTrue());
			}
		}

		QueryUtils.in(filter, Q.moldDataSubmission.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(Q.mold.equipmentCode.contains(input.getQuery()) //
					.or(Q.location.name.contains(input.getQuery())) //
					.or(Q.location.locationCode.contains(input.getQuery())) //
					.or(Q.company.companyCode.contains(input.getQuery())) //
					.or(Q.company.name.contains(input.getQuery())) //
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.moldDataSubmission.id);
		query.where(filter);
	}

}
