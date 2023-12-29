package com.emoldino.api.common.resource.composite.mchstp.repository;

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

import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpGetIn;
import com.emoldino.api.common.resource.composite.mchstp.dto.MchStpItem;
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
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.util.DataUtils;
import saleson.model.QMachine;
import saleson.model.QWorkOrder;
import saleson.model.QWorkOrderAsset;
import saleson.model.TabTable;
import saleson.model.customField.QCustomFieldValue;

@Repository
public class MchStpRepository {
	public long count(MchStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.MACHINE, input.getTabName());

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(QMachine.machine.id.countDistinct())//
				.from(QMachine.machine);

		applyFilter(query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	public Page<MchStpItem> findAll(MchStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.MACHINE, input.getTabName());

		QWorkOrder qWorkOrder = QWorkOrder.workOrder;
		QWorkOrderAsset qWorkOrderAsset = QWorkOrderAsset.workOrderAsset;

		JPQLQuery<MchStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(MchStpItem.class, //
						Q.machine, //
						qWorkOrder.completedOn.max().as("lastWorkOrderAt"), //
						qWorkOrder.countDistinct().as("workOrderCount")//
				))//))//
				.distinct()//
				.from(Q.machine)

				.leftJoin(qWorkOrderAsset).on(//
						qWorkOrderAsset.type.eq(ObjectType.MACHINE).and(qWorkOrderAsset.assetId.eq(Q.machine.id))//
				)//
				.leftJoin(qWorkOrder).on(//
						qWorkOrder.id.eq(qWorkOrderAsset.workOrderId)//
								.and(qWorkOrder.status.in(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)))//
				.groupBy(Q.machine);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin, tabTable);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("machineCode", Q.machine.machineCode)//
				.put("mold", Q.mold.equipmentCode)//
				.put("company.name", Q.company.name)//
				.put("location.name", Q.location.name)//
				.put("line", Q.machine.line)//
				.put("machineMaker", Q.machine.machineMaker)//
				.put("machineType", Q.machine.machineType)//
				.put("machineModel", Q.machine.machineModel)//
				.put("enabled", Q.machine.enabled)//
				.put("mold.id", Q.mold.id)//
				.build();
		String property = QueryUtils.getFirstSortProperty(pageable);
		if (property != null && property.startsWith(SpecialSortProperty.customFieldSort)) {
			Long customFieldId = Long.valueOf((property.split("-"))[1]);
			QCustomFieldValue qCustFieldValue = QCustomFieldValue.customFieldValue;
			query.leftJoin(qCustFieldValue).on(qCustFieldValue.customField.id.eq(customFieldId).and(qCustFieldValue.objectId.eq(Q.machine.id)));
			fieldMap.put(property, qCustFieldValue.value);
		}

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.machine.id.desc());
		QueryResults<MchStpItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private BooleanBuilder addQueryFilters(BooleanBuilder filter, String query) {
		if (ObjectUtils.isEmpty(query)) {
			return filter;
		}

		filter.or(Q.machine.machineCode.containsIgnoreCase(query))//
				.or(Q.mold.equipmentCode.containsIgnoreCase(query));
		List<String> selectedFields = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.MACHINE_SETTING);
		List<String> remainFields = new ArrayList<>();
		selectedFields.forEach(field -> {
			switch (field) {
			case "company":
				filter.or(Q.company.name.containsIgnoreCase(query));
				filter.or(Q.company.companyCode.containsIgnoreCase(query));
				break;
			case "location":
				filter.or(Q.location.locationCode.containsIgnoreCase(query));
				filter.or(Q.location.name.containsIgnoreCase(query));
				break;
			case "line":
				filter.or(Q.machine.line.containsIgnoreCase(query));
				break;
			case "machineMaker":
				filter.or(Q.machine.machineMaker.containsIgnoreCase(query));
				break;
			case "machineType":
				filter.or(Q.machine.machineType.containsIgnoreCase(query));
				break;
			case "machineModel":
				filter.or(Q.machine.machineModel.containsIgnoreCase(query));
				break;
			default:
				remainFields.add(field);
				break;
			}
		});

		if (!remainFields.isEmpty()) {
			List<Long> customFieldIds = DataUtils.getNumericElements(selectedFields);

			QCustomFieldValue qCustFieldValue = QCustomFieldValue.customFieldValue;
			filter.or(Q.machine.id.in(//
					JPAExpressions.select(qCustFieldValue.objectId).distinct().from(qCustFieldValue).where(//
							qCustFieldValue.customField.objectType.eq(ObjectType.MACHINE)//
									.and(qCustFieldValue.customField.id.in(customFieldIds))//
									.and(qCustFieldValue.value.containsIgnoreCase(query))//
					)//
			));
		}

		return filter;
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, MchStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, Q.machine.id);
		QueryUtils.includeDisabled(Q.mold);
		QueryUtils.includeDisabled(Q.location);
		QueryUtils.includeDisabled(Q.company);

		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applyMachineFilter(query, join, input.getFilterCode());
		} else {
			QueryUtils.applyMachineDisabledFilter(query, join, input.getFilterCode());
		}

		QueryUtils.leftJoin(query, join, Q.mold, () -> QueryUtils.isMold().and(Q.mold.machineId.eq(Q.machine.id)));
		QueryUtils.leftJoin(query, join, Q.location, () -> QueryUtils.isLocation().and(Q.location.id.eq(Q.machine.locationId)));
		QueryUtils.leftJoin(query, join, Q.company, () -> QueryUtils.isCompany().and(Q.company.id.eq(Q.machine.companyId)));

		BooleanBuilder filter = new BooleanBuilder();
		addQueryFilters(filter, input.getQuery());
		if (input.getMatchedWithTooling() != null) {
			if (input.getMatchedWithTooling()) {
				filter.and(Q.mold.machineId.isNotNull());
			} else {
				filter.and(Q.mold.machineId.isNull());
			}
		}
		if (CollectionUtils.isNotEmpty(input.getId())) {
			filter.and(Q.machine.id.in(input.getId()));
		}
		QueryUtils.applyBatchFilter(filter, batchin, Q.machine.id);
		query.where(filter);
	}

}
