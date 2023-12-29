package com.emoldino.api.production.resource.composite.alrmchdtm.repository;

import com.emoldino.api.common.resource.base.code.repository.codedata.QCodeData;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmGetIn;
import com.emoldino.api.production.resource.composite.alrmchdtm.dto.AlrMchDtmItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.enumeration.AlertTab;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.model.QMachineDowntimeAlert;
import saleson.model.QMachineDowntimeReason;
import saleson.model.QUser;

import java.time.Instant;
import java.util.*;

@Repository
public class AlrMchDtmRepository {

	QMachineDowntimeReason qMachineDowntimeReason = QMachineDowntimeReason.machineDowntimeReason;
	QCodeData qcodeData =  QCodeData.codeData;

	public long count(AlrMchDtmGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.machineDowntimeAlert.id.countDistinct())//
				.from(Q.machineDowntimeAlert);

		applyFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrMchDtmItem> findAll(AlrMchDtmGetIn input, BatchIn batchin, Pageable pageable) {
		QUser qUserConfirmBy =new QUser("confirmBy");
		QUser qUserReportedBy =new QUser("reportedBy");

		NumberExpression<Long> duration = new CaseBuilder()
				.when(Q.machineDowntimeAlert.endTime.isNull().or(Q.machineDowntimeAlert.endTime.after(Instant.now())))
				.then(Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", Q.machineDowntimeAlert.startTime, Instant.now()))
				.otherwise(Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", Q.machineDowntimeAlert.startTime, Q.machineDowntimeAlert.endTime));

		JPQLQuery<AlrMchDtmItem> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(
//						new QAlrMchDtmItem(
						Projections.constructor(AlrMchDtmItem.class, //
						Q.machineDowntimeAlert.id, //
						Q.machineDowntimeAlert.downtimeStatus, //
						Q.machineDowntimeAlert.createdAt, //
						qUserConfirmBy, //
						Q.machineDowntimeAlert.downtimeType, //
						Q.machineDowntimeAlert.startTime, //
						Q.machineDowntimeAlert.endTime, //
						Q.machineDowntimeAlert.reportedBy, //
						qUserReportedBy, //
						Q.machine.id, //
						Q.machine.machineCode.as("machineCode"), //

						Q.mold.id, //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.toolingStatus, //

//						Q.company.id, //
//						Q.company.name, //
//						Q.company.companyCode, //
//						Q.company.companyType, //

						Q.location.id, //
						Q.location.locationCode, //
						Q.location.name //
				)) //
				.distinct()//
				.from(Q.machineDowntimeAlert)
				.groupBy(Q.machineDowntimeAlert.id);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);
		QueryUtils.leftJoin(query, join, qMachineDowntimeReason, () -> qMachineDowntimeReason.machineDowntimeAlertId.eq(Q.machineDowntimeAlert.id)
				.and(qMachineDowntimeReason.id.eq(JPAExpressions.select(qMachineDowntimeReason.id.max())
						.from(qMachineDowntimeReason).where(qMachineDowntimeReason.machineDowntimeAlertId.eq(Q.machineDowntimeAlert.id))
						.orderBy(qMachineDowntimeReason.startTime.desc()))));
		QueryUtils.leftJoin(query, join, qcodeData, () -> qcodeData.id.eq(qMachineDowntimeReason.codeDataId));
		QueryUtils.leftJoin(query, join, qUserConfirmBy, () -> qUserConfirmBy.id.eq(Q.machineDowntimeAlert.confirmedBy));
		QueryUtils.leftJoin(query, join, qUserReportedBy, () -> qUserReportedBy.id.eq(Q.machineDowntimeAlert.reportedBy));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.machineDowntimeAlert.id)//
				.put("alertStatus", Q.machineDowntimeAlert.downtimeStatus)//
				.put("creationDateTime", Q.machineDowntimeAlert.createdAt)//
				.put("confirmBy", qUserConfirmBy.name)//
				.put("downtimeType", Q.machineDowntimeAlert.downtimeType)//
				.put("startTime", Q.machineDowntimeAlert.startTime)//
				.put("endTime", Q.machineDowntimeAlert.endTime)//
				.put("duration", duration)//
				.put("downtimeReason", qcodeData.title.min())//
				.put("reportedBy", qUserReportedBy.name)//
				.put("machineCode", Q.machine.machineCode)//

				.put("moldId", Q.mold.id)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//

//				.put("companyId", Q.company.id)//
//				.put("companyName", Q.company.name)//
//				.put("companyCode", Q.company.companyCode)//
//				.put("companyType", Q.company.companyType)//

				.put("locationId", Q.location.id)//
				.put("locationName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.machineDowntimeAlert.createdAt.desc());
		QueryResults<AlrMchDtmItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrMchDtmGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.machine, () -> Q.machineDowntimeAlert.machineId.eq(Q.machine.id));
		QueryUtils.leftJoin(query, join, Q.mold, () -> Q.machineDowntimeAlert.moldId.eq(Q.mold.id));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.machine.locationId));

		QueryUtils.includeDisabled(Q.mold);
		QueryUtils.includeDisabled(Q.location);

		QueryUtils.applyMachineFilter(query, join, input.getFilterCode());


		BooleanBuilder filter = new BooleanBuilder();

		//hide negative downtime
		filter.and(Q.machineDowntimeAlert.endTime.isNull().or(Q.machineDowntimeAlert.endTime.gt(Q.machineDowntimeAlert.startTime)));
		//todo: the condition improve later
// 		filter.and(Q.machineDowntimeAlert.latest.isTrue());//Shows only 1 alert in history for each machine in list alerts. Can see many row in detail alert

		if (AlertTab.ALERT.getTitle().equals(input.getTabName())) {
			filter.and(Q.machineDowntimeAlert.downtimeStatus.in(Arrays.asList(MachineDowntimeAlertStatus.REGISTERED, MachineDowntimeAlertStatus.DOWNTIME)));
		} else {
			filter.and(Q.machineDowntimeAlert.downtimeStatus.in(Arrays.asList(MachineDowntimeAlertStatus.CONFIRMED, MachineDowntimeAlertStatus.UNCONFIRMED)));
		}
		QueryUtils.in(filter, Q.machineDowntimeAlert.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
						Q.mold.equipmentCode.contains(input.getQuery())//
							.or(Q.machine.machineCode.contains(input.getQuery()))//
							.or(Q.location.name.contains(input.getQuery()))//
							.or(Q.location.locationCode.contains(input.getQuery()))//
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.machineDowntimeAlert.id);
		query.where(filter);
	}

	public List<AlrMchDtmItem.MchDtmReason> getMchDtmReasonList(List<Long> mchDtmIds) {
		JPQLQuery<AlrMchDtmItem.MchDtmReason> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(
						Projections.constructor(AlrMchDtmItem.MchDtmReason.class, //
								qMachineDowntimeReason.machineDowntimeAlertId, //
								qMachineDowntimeReason.codeData.title, //
								qMachineDowntimeReason.note
						)) //
				.from(qMachineDowntimeReason)
				.orderBy(qMachineDowntimeReason.id.desc());
		query.where(qMachineDowntimeReason.machineDowntimeAlertId.in(mchDtmIds));
		return query.fetch();
	}
}
