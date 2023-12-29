package com.emoldino.api.common.resource.composite.tmnstp.repository;

import java.time.Instant;
import java.util.ArrayList;
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

import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpGetIn;
import com.emoldino.api.common.resource.composite.tmnstp.dto.TmnStpItem;
import com.emoldino.api.common.resource.composite.tmnstp.enumeration.TmnStpConnectionStatus;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.model.QWorkOrder;
import saleson.model.QWorkOrderAsset;
import saleson.model.TabTable;

@Repository
public class TmnStpRepository {

	public long count(TmnStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.TERMINAL, input.getTabName());

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.terminal.id.countDistinct())//
				.from(Q.terminal);

		applyFilter(query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	public Page<TmnStpItem> findAll(TmnStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.TERMINAL, input.getTabName());

		QWorkOrder qWorkOrder = QWorkOrder.workOrder;
		QWorkOrderAsset qWorkOrderAsset = QWorkOrderAsset.workOrderAsset;

		JPQLQuery<TmnStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(TmnStpItem.class, //
						Q.terminal.id, //
						Q.terminal.equipmentCode, //
						Q.terminal.equipmentStatus, //
						Q.terminal.operatingStatus//
								.when(OperatingStatus.WORKING)//
								.then(TmnStpConnectionStatus.ONLINE.name())//
								.otherwise(TmnStpConnectionStatus.OFFLINE.name())//
								.as("connectionStatus"), //
						Q.terminal.operatedAt, //
						Q.counter.id.countDistinct().as("counterCount"), //
						Q.company.id, //
						Q.company.name, //
						Q.company.companyCode, //
						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode, //
						Q.location.enabled, //
						Q.area.id, //
						Q.area.name, //
						Q.area.areaType, //
						Q.terminal.installationArea, //
						Q.terminal.installedBy, //
						Q.terminal.installedAt, //
						Q.terminal.activatedAt, //
						Q.terminal.memo, //
						Q.terminal.ipType, //
						Q.terminal.ipAddress, //
						Q.terminal.subnetMask, //
						Q.terminal.gateway, //
						Q.terminal.dns, //
						Q.terminal.createdAt, //
						qWorkOrder.completedOn.max().as("lastWorkOrderAt"), //
						qWorkOrder.countDistinct().as("workOrderCount")//
				))//
				.distinct()//
				.from(Q.terminal)//
				.leftJoin(qWorkOrderAsset).on(//
						qWorkOrderAsset.type.eq(ObjectType.TERMINAL).and(qWorkOrderAsset.assetId.eq(Q.terminal.id))//
				)//
				.leftJoin(qWorkOrder).on(//
						qWorkOrder.id.eq(qWorkOrderAsset.workOrderId)//
								.and(qWorkOrder.status.in(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)))//
				.groupBy(Q.terminal.id);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin, tabTable);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("equipmentCode", Q.terminal.equipmentCode)//
				.put("equipmentStatus", Q.terminal.equipmentStatus)//
				.put("connectionStatus", Expressions.stringPath("connectionStatus"))//
				.put("operationDateTime", Q.terminal.operatedAt)//
				.put("counterCount", Expressions.numberPath(Long.class, "counterCount"))//
				.put("company", Q.company.name)//
				.put("companyName", Q.company.name)//
				.put("location", Q.location.name)//
				.put("locationName", Q.location.name)//
				.put("areaName", Q.area.name)//
				.put("installedBy", Q.terminal.installedBy)//
				.put("installationDate", Q.terminal.installedAt)//
				.put("activationDate", Q.terminal.activatedAt)//
				.put("memo", Q.terminal.memo)//
				.put("ipType", Q.terminal.ipType)//
				.put("ipAddress", Q.terminal.ipAddress)//
				.put("subnetMask", Q.terminal.subnetMask)//
				.put("gateway", Q.terminal.gateway)//
				.put("dns", Q.terminal.dns)//
				.put("lastWorkOrderDate", Expressions.dateTimePath(Instant.class, "lastWorkOrderAt"))//
				.put("workOrderCount", Expressions.numberPath(Long.class, "workOrderCount"))//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.terminal.operatedAt.desc());

		QueryResults<TmnStpItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private BooleanBuilder addQueryFilters(BooleanBuilder filter, String query) {
		if (ObjectUtils.isEmpty(query)) {
			return filter;
		}
		filter.or(Q.terminal.equipmentCode.contains(query));
		List<String> selectedFields = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.TERMINAL_SETTING);
		if (CollectionUtils.isEmpty(selectedFields)) {
			filter.or(Q.terminal.equipmentCode.contains(query))//
					.or(Q.company.name.contains(query))//
					.or(Q.company.companyCode.contains(query))//
					.or(Q.location.name.contains(query))//
					.or(Q.location.locationCode.contains(query))//
			;
		} else {
			selectedFields.forEach(selectedField -> {
				switch (selectedField) {
				case "company":
					filter.or(Q.company.companyCode.containsIgnoreCase(query)).or(Q.company.name.containsIgnoreCase(query));
					break;
				case "location":
					filter.or(Q.location.locationCode.containsIgnoreCase(query)).or(Q.location.name.containsIgnoreCase(query));
					break;
				case "area":
					filter.or(Q.area.name.containsIgnoreCase(query));
					break;
				case "installedBy":
					filter.or(Q.terminal.installedBy.containsIgnoreCase(query));
					break;
				case "memo":
					filter.or(Q.terminal.memo.containsIgnoreCase(query));
					break;
				}
			});
		}
		return filter;
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TmnStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, Q.terminal.id);

		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applyTerminalFilter(query, join, input.getFilterCode());
		} else {
			QueryUtils.applyTerminalDisabledFilter(query, join, input.getFilterCode());
		}

		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.terminal.locationId).and(QueryUtils.isLocation()));
		QueryUtils.leftJoin(query, join, Q.area, () -> Q.area.id.eq(Q.terminal.areaId));
		QueryUtils.leftJoin(query, join, Q.counter, () -> {
			return Q.counter.lastTerminalId.eq(Q.terminal.id)//
					.and(Q.counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))//
					.and(QueryUtils.isCounter());
		});
		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.terminal.companyId).and(QueryUtils.isCompany()));

		BooleanBuilder filter = new BooleanBuilder();
		addQueryFilters(filter, input.getQuery());
		QueryUtils.eq(filter, Q.company.companyType, input.getCompanyType());
		if (!ObjectUtils.isEmpty(input.getConnectionStatus())) {
			List<OperatingStatus> operStats = new ArrayList<>();
			if (input.getConnectionStatus().contains(TmnStpConnectionStatus.ONLINE)) {
				operStats.add(OperatingStatus.WORKING);
			}
			if (input.getConnectionStatus().contains(TmnStpConnectionStatus.OFFLINE)) {
				operStats.add(OperatingStatus.IDLE);
				operStats.add(OperatingStatus.NOT_WORKING);
				operStats.add(OperatingStatus.DISCONNECTED);
			}
			if (input.getConnectionStatus().contains(TmnStpConnectionStatus.OFFLINE)) {
				filter.and(Q.terminal.operatingStatus.in(operStats).or(Q.terminal.operatingStatus.isNull()));
			} else {
				filter.and(Q.terminal.operatingStatus.in(operStats));
			}
		}
		QueryUtils.in(filter, Q.terminal.equipmentStatus, input.getEquipmentStatus());
		QueryUtils.in(filter, Q.terminal.areaId, input.getAreaId());
		QueryUtils.in(filter, Q.terminal.id, input.getId());

		QueryUtils.applyBatchFilter(filter, batchin, Q.terminal.id);

		query.where(filter);
	}

}
