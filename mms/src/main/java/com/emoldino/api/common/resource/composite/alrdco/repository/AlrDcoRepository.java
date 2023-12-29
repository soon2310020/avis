package com.emoldino.api.common.resource.composite.alrdco.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoGetIn;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTerminal;
import com.emoldino.api.common.resource.composite.alrdco.dto.AlrDcoTooling;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.enumeration.ConnectionStatus;
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
import saleson.common.enumeration.OperatingStatus;

@Repository
public class AlrDcoRepository {

	public long countTerminals(AlrDcoGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.terminalDisconnect.id.countDistinct())//
				.from(Q.terminalDisconnect);

		applyTerminalFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrDcoTerminal> findAllTerminals(AlrDcoGetIn input, BatchIn batchin, Pageable pageable) {

		JPQLQuery<AlrDcoTerminal> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrDcoTerminal.class, //
						Q.terminalDisconnect.id, //
						Q.terminalDisconnect.notificationStatus.as("alertStatus"), //
						Q.terminalDisconnect.createdAt, //
						Q.terminalDisconnect.confirmedBy, //
						Q.terminalDisconnect.confirmedAt, //

						Q.terminal.id, //
						Q.terminal.equipmentCode, //
						Q.terminal.operatingStatus//
								.when(OperatingStatus.WORKING)//
								.then(ConnectionStatus.ONLINE.name())//
								.otherwise(ConnectionStatus.OFFLINE.name())//
								.as("connectionStatus"), //

						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.company.companyType, //

						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode, //
						Q.area.name //
				)) //
				.distinct()//
				.from(Q.terminalDisconnect)//
				.groupBy(Q.terminalDisconnect.id);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyTerminalFilter(query, join, input, batchin);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.terminalDisconnect.id)//
				.put("alertStatus", Q.terminalDisconnect.notificationStatus)//
				.put("creationDateTime", Q.terminalDisconnect.createdAt)//
				.put("confirmedAt", Q.terminalDisconnect.confirmedAt)//
				.put("terminalId", Q.terminal.id)//
				.put("terminalCode", Q.terminal.equipmentCode)//
				.put("connectionStatus", Expressions.stringPath("connectionStatus"))//
				.put("companyId", Q.company.id)//
				.put("companyName", Q.company.name)//
				.put("companyCode", Q.company.companyCode)//
				.put("companyType", Q.company.companyType)//
				.put("locationId", Q.location.id)//
				.put("locationName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.put("areaName", Q.area.name)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.terminalDisconnect.createdAt.desc());
		QueryResults<AlrDcoTerminal> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyTerminalFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrDcoGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.terminal, () -> Q.terminalDisconnect.terminalId.eq(Q.terminal.id));

		QueryUtils.applyTerminalFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.terminal.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.terminal.locationId));
		QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.terminal.areaId));

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.terminalDisconnect.latest.isTrue());
		if ("Terminal Disconnection".equals(input.getTabName())) {
			filter.and(Q.terminalDisconnect.notificationStatus.in(NotificationStatus.PENDING, NotificationStatus.ALERT));
		} else {
			filter.and(Q.terminalDisconnect.notificationStatus.notIn(NotificationStatus.PENDING, NotificationStatus.ALERT));
		}
		QueryUtils.in(filter, Q.terminalDisconnect.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
					Q.terminal.equipmentCode.contains(input.getQuery())//
							.or(Q.location.name.contains(input.getQuery()))//
							.or(Q.location.locationCode.contains(input.getQuery()))//
							.or(Q.company.companyCode.contains(input.getQuery()))//
							.or(Q.company.name.contains(input.getQuery()))//
							.or(Q.area.name.contains(input.getQuery()))//
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.terminalDisconnect.id);
		query.where(filter);
	}

	public long countToolings(AlrDcoGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldDisconnect.id.countDistinct())//
				.from(Q.moldDisconnect);

		applyMoldFilter(query, new HashSet<>(), input, null);

		return query.fetchCount();
	}

	public Page<AlrDcoTooling> findAllToolings(AlrDcoGetIn input, BatchIn batchin, Pageable pageable) {

		JPQLQuery<AlrDcoTooling> query = BeanUtils.get(JPAQueryFactory.class) //
				.select(Projections.constructor(AlrDcoTooling.class, //
						Q.moldDisconnect.id, //
						Q.moldDisconnect.notificationStatus, //
						Q.moldDisconnect.createdAt, //
						Q.moldDisconnect.confirmedBy, //
						Q.moldDisconnect.confirmedAt, //

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
						Q.mold.lastShotAt, //

						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.company.companyType, //

						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode, //
						Q.area.name //
				)) //
				.distinct()//
				.from(Q.moldDisconnect);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyMoldFilter(query, join, input, batchin);

		QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
		QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.mold.areaId));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.moldDisconnect.id)//
				.put("alertStatus", Q.moldDisconnect.notificationStatus)//
				.put("creationDateTime", Q.moldDisconnect.createdAt)//
				.put("confirmedAt", Q.moldDisconnect.confirmedAt)//
				.put("moldId", Q.mold.id)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.put("sensorStatus", Expressions.stringPath("sensorStatus"))//
				.put("accumulatedShot", Expressions.numberPath(Integer.class, "accumulatedShot"))//
				.put("lastShotAt", Q.mold.lastShotAt)//
				.put("lastShotDate", Q.mold.lastShotAt)//
				.put("companyId", Q.company.id)//
				.put("companyName", Q.company.name)//
				.put("companyCode", Q.company.companyCode)//
				.put("companyType", Q.company.companyType)//
				.put("locationId", Q.location.id)//
				.put("locationName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.put("areaName", Q.area.name)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldDisconnect.createdAt.desc());
		QueryResults<AlrDcoTooling> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyMoldFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrDcoGetIn input, BatchIn batchin) {
		QueryUtils.join(query, join, Q.mold, () -> Q.moldDisconnect.moldId.eq(Q.mold.id));

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.moldDisconnect.latest.isTrue());
		if ("Tooling Disconnection".equals(input.getTabName())) {
			filter.and(Q.moldDisconnect.notificationStatus.in(NotificationStatus.PENDING, NotificationStatus.ALERT));
		} else {
			filter.and(Q.moldDisconnect.notificationStatus.notIn(NotificationStatus.PENDING, NotificationStatus.ALERT));
		}
		QueryUtils.in(filter, Q.moldDisconnect.id, input.getId());

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(Q.mold.equipmentCode.contains(input.getQuery())//
					.or(Q.location.name.contains(input.getQuery()))//
					.or(Q.location.locationCode.contains(input.getQuery()))//
					.or(Q.company.companyCode.contains(input.getQuery()))//
					.or(Q.company.name.contains(input.getQuery()))//
			);
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.moldDisconnect.id);
		query.where(filter);
	}

}
