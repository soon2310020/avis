package com.emoldino.api.common.resource.composite.parstp.repository;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpGetIn;
import com.emoldino.api.common.resource.composite.parstp.dto.ParStpItem;
import com.emoldino.api.common.resource.composite.parstp.dto.QParStpItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PageType;
import saleson.common.util.DataUtils;
import saleson.model.TabTable;
import saleson.model.customField.QCustomFieldValue;

@Repository
public class ParStpRepository {

	public long count(ParStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.PART, input.getTabName());

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.part.id.countDistinct())//
				.distinct()//
				.from(Q.part);

		applyFilter(query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	public Page<ParStpItem> findAll(ParStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.PART, input.getTabName());

		if (input.getTimeScale() == null) {
			input.setTimeScale(TimeScale.WEEK);
		}
		if (input.getTimeValue() == null) {
			input.setTimeValue(DateUtils2.format(DateUtils2.getInstant(), DatePattern.YYYYww, Zone.GMT));
		}
		ValueUtils.assertTimeSetting(ValueUtils.map(input, TimeSetting.class), Arrays.asList(TimeScale.WEEK));

		// TODO filter with dashboard redirect, tabbedDashboardRedirected

		NumberPath<Long> prodQtyField = OptionUtils.isEnabled(ConfigCategory.DATA_FILTER) ? Q.partStat.producedVal : Q.partStat.produced;
		NumberExpression<Long> producedQuantity = Expressions.asNumber(//
				JPAExpressions//
						.select(prodQtyField.sum().coalesce(0L))//
						.from(Q.partStat)//
						.where(Q.partStat.partId.eq(Q.part.id).and(Q.partStat.week.eq(input.getTimeValue())))//
		).as("producedQuantity");
		NumberExpression<Integer> moldCount = Q.mold.count().intValue();
		NumberExpression<Integer> activeMoldCount = new CaseBuilder().when(Q.mold.operatingStatus.eq(OperatingStatus.WORKING)).then(1).otherwise(0).sum();
		NumberExpression<Integer> idleMoldCount = new CaseBuilder().when(Q.mold.operatingStatus.eq(OperatingStatus.IDLE)).then(1).otherwise(0).sum();
		NumberExpression<Integer> inactiveMoldCount = new CaseBuilder().when(Q.mold.operatingStatus.eq(OperatingStatus.NOT_WORKING)).then(1).otherwise(0).sum();
		NumberExpression<Integer> disconMoldCount = new CaseBuilder().when(Q.mold.operatingStatus.eq(OperatingStatus.DISCONNECTED)).then(1).otherwise(0).sum();
		NumberExpression<Integer> machineCount = Q.mold.machineId.count().intValue();

		JPQLQuery<ParStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(new QParStpItem(//
						Q.part.id, //
						Q.part.name, //
						Q.part.partCode, //
						Q.part.enabled, //
						Q.product.id, //
						Q.product.name, //
						Q.category.name, //
						Q.part.quantityRequired, //
						Q.part.weeklyDemand, //
						producedQuantity, //
						moldCount, //
						activeMoldCount, //
						idleMoldCount, //
						inactiveMoldCount, //
						disconMoldCount, //
						machineCount, //
						Q.part.resinCode, //
						Q.part.resinGrade, //
						Q.part.designRevision, //
						Q.part.size, //
						Q.part.sizeUnit, //
						Q.part.weight, //
						Q.part.weightUnit, //
//						Q.part.dataUpdatedOn, //
						Q.part.createdAt//
				))//
				.distinct()//
				.from(Q.part)//
				.groupBy(Q.part.id);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin, tabTable);
//		QueryUtils.applyTabFilter(query, join, tabTable, Q.part.id);
//		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
//			QueryUtils.applyPartFilter(query, join, input.getFilterCode());
//		} else {
//			QueryUtils.applyPartDisabledFilter(query, join, input.getFilterCode());
//		}
//
//		QueryUtils.leftJoinCategoryByPart(query, join);
//		QueryUtils.leftJoinMoldByPart(query, join);
//
//		BooleanBuilder filter = new BooleanBuilder();
//		addQueryFilters(filter, input.getQuery());
//
//		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("name", Q.part.name)//
				.put("partCode", Q.part.partCode)//
				.put("enabled", Q.part.enabled)//
				.put("product", Q.product.name)//category=>product
				.put("categoryName", Q.category.name)//category.parent.name
				.put("productName", Q.product.name)//category.name => product
				.put("moldCount", moldCount)// totalMolds
				.put("activeMoldCount", activeMoldCount)//
				.put("idleMoldCount", idleMoldCount)//
				.put("inactiveMoldCount", inactiveMoldCount)//
				.put("disconMoldCount", disconMoldCount)//
				.put("producedQuantity", Expressions.numberPath(Long.class, "producedQuantity"))//totalProduced
				.put("designRevision", Q.part.designRevision)//
				.put("resinCode", Q.part.resinCode)//
				.put("resinGrade", Q.part.resinGrade)//
				.put("size", Q.part.size)//
				.put("weight", Q.part.weight)//
				.put("machineCount", machineCount)//
				.put("createdAt", Q.part.createdAt)//
				.put("quantityRequired", Q.part.quantityRequired)//
				.build();
		String property = QueryUtils.getFirstSortProperty(pageable);
		if (property != null && property.startsWith(SpecialSortProperty.customFieldSort)) {
			Long customFieldId = Long.valueOf((property.split("-"))[1]);
			QCustomFieldValue qCustFieldValue = QCustomFieldValue.customFieldValue;
			query.leftJoin(qCustFieldValue).on(qCustFieldValue.customField.id.eq(customFieldId).and(qCustFieldValue.objectId.eq(Q.part.id)));
			fieldMap.put(property, qCustFieldValue.value);
		}

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.part.name.asc());
		QueryResults<ParStpItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private BooleanBuilder addQueryFilters(BooleanBuilder filter, String query) {
		if (ObjectUtils.isEmpty(query)) {
			return filter;
		}

		filter.or(Q.part.name.containsIgnoreCase(query).or(Q.part.partCode.containsIgnoreCase(query)));
		List<String> selectedFields = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.PART_SETTING);
		List<String> remainFields = new ArrayList<>();
		selectedFields.forEach(field -> {
			switch (field) {
			case "name":
				filter.or(Q.part.name.containsIgnoreCase(query));
				break;
			case "category":
				filter.or(Q.category.name.containsIgnoreCase(query));
				break;
			case "projectName":
			case "productName":
				filter.or(Q.product.name.containsIgnoreCase(query));
				break;
			case "resinCode":
				filter.or(Q.part.resinCode.containsIgnoreCase(query));
				break;
			case "resinGrade":
				filter.or(Q.part.resinGrade.containsIgnoreCase(query));
				break;
			default:
				remainFields.add(field);
				break;
			}
		});

		if (!remainFields.isEmpty()) {
			List<Long> customFieldIds = DataUtils.getNumericElements(remainFields);

			QCustomFieldValue qCustFieldValue = QCustomFieldValue.customFieldValue;
			filter.or(Q.part.id.in(//
					JPAExpressions.select(qCustFieldValue.objectId).distinct().from(qCustFieldValue).where(//
							qCustFieldValue.customField.objectType.eq(ObjectType.PART)//
									.and(qCustFieldValue.customField.id.in(customFieldIds))//
									.and(qCustFieldValue.value.containsIgnoreCase(query))//
					)//
			));
		}

		return filter;
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, ParStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, Q.part.id);
		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applyPartFilter(query, join, input.getFilterCode());
		} else {
			QueryUtils.applyPartDisabledFilter(query, join, input.getFilterCode());
		}

		QueryUtils.leftJoinCategoryByPart(query, join);
		QueryUtils.leftJoinMoldByPart(query, join);

		BooleanBuilder filter = new BooleanBuilder();
		addQueryFilters(filter, input.getQuery());

		if (CollectionUtils.isNotEmpty(input.getId())) {
			filter.and(Q.part.id.in(input.getId()));
		}

		QueryUtils.applyBatchFilter(filter, batchin, Q.part.id);
		query.where(filter);
	}
}
