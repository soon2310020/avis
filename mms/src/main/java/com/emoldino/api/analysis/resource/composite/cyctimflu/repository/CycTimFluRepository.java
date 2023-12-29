package com.emoldino.api.analysis.resource.composite.cyctimflu.repository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.cycletimedeviation.QCycleTimeDeviation;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluItem;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.OutsideUnit;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.QCompany;
import saleson.model.QMoldPart;

@Component
public class CycTimFluRepository {
	private static QCycleTimeDeviation table = QCycleTimeDeviation.cycleTimeDeviation;

	public Page<CycTimFluItem> findAll(CycTimFluGetIn input, Pageable pageable) {
		QueryResults<CycTimFluItem> results = findAll(input, pageable, false);
		List<CycTimFluItem> contents = results.getResults();
		calcCtfTrend(contents, input);
		return new PageImpl<>(contents, pageable, results.getTotal());
	}

	private QueryResults<CycTimFluItem> findAll(CycTimFluGetIn input, Pageable pageable, boolean isCount) {

		/*
		BooleanBuilder filter = new BooleanBuilder();
		addFilters(filter, input.getSupplierId(), input.getPartId());
		*/
//		CycTimDevRepository.addTimeFilters(table,filter, input);

//		NumberExpression<Long> moldCount = table.moldId.countDistinct();
		NumberExpression<Long> moldCount = Q.mold.id.countDistinct();

		// use floatValue because doubleValue don't work in some version of the database(Example: mysql 5.7.33)
		/*
		NumberExpression<Float> averageCycleTime = table.averageCycleTime.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue());
		NumberExpression<Float> ctFluctuation = table.ctFluctuation.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue());
		NumberExpression<Float> l1Limit = table.l1Limit.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue());
		// NumberExpression<Float> nctf = ctFluctuation.divide(new CaseBuilder().when(l1Limit.isNotNull().or(l1Limit.ne(0f))).then(l1Limit).otherwise(1f));
		NumberExpression<Float> nctf = (table.nctf.floatValue().multiply(table.shotCount.floatValue())).sum().divide(table.shotCount.sum().floatValue());
		*/

		BooleanBuilder filterInnerQuery = new BooleanBuilder();
		filterInnerQuery.and(table.moldId.eq(Q.mold.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		NumberExpression<Float> averageCycleTime = Expressions.asNumber(//
				JPAExpressions//
						.select(table.averageCycleTime.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();
		NumberExpression<Float> ctFluctuation = Expressions.asNumber(//
				JPAExpressions//
						.select(table.ctFluctuation.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();
		NumberExpression<Float> l1Limit = Expressions.asNumber(//
				JPAExpressions//
						.select(table.l1Limit.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();
		NumberExpression<Float> nctf = Expressions.asNumber(//
				JPAExpressions//
						.select((table.nctf.floatValue().multiply(table.shotCount.floatValue())).sum().divide(table.shotCount.sum().floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		/*
		BooleanBuilder filterJoinData = new BooleanBuilder().and(Q.mold.id.eq(table.moldId));
		applyTimeFilter(filterJoinData, input);
		*/

		final JPQLQuery<CycTimFluItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(//
						CycTimFluItem.class, //
						Q.supplier.id, //
						Q.supplier.name, //
						Q.supplier.companyCode, //
						moldCount.as("moldCount"), //
						averageCycleTime.as("averageCycleTime"), //
						ctFluctuation.as("ctFluctuation"), //
						l1Limit.as("l1Limit"), //
						nctf.as("nctf"), //
						Expressions.asNumber(0D)//
				))//
				.from(Q.supplier)//
				.groupBy(Q.supplier.id);

		QueryUtils.includeDisabled(Q.mold);
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.leftJoin(query, join, table, () -> table.supplierId.eq(Q.supplier.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		QueryUtils.leftJoin(query, join, Q.mold, () -> Q.mold.id.eq(table.moldId));
		QueryUtils.applyCompanyFilter(query, join, input.getFilterCode(), CompanyType.SUPPLIER, CompanyType.IN_HOUSE);

		BooleanBuilder filter = new BooleanBuilder();
		if (!ObjectUtils.isEmpty(input.getSupplierId())) {
			filter.and(Q.supplier.id.in(input.getSupplierId()));//previous filter
		}
		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("supplierId", Q.supplier.id)//
				.put("supplierName", Q.supplier.name)//
				.put("supplierCode", Q.supplier.companyCode)//
				.put("moldCount", Expressions.numberPath(Integer.class, "moldCount"))//
				.put("averageCycleTime", Expressions.numberPath(Float.class, "averageCycleTime"))//
				.put("ctFluctuation", Expressions.numberPath(Float.class, "ctFluctuation"))//
				.put("nctf", Expressions.numberPath(Float.class, "nctf"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Expressions.numberPath(Float.class, "nctf").desc());

		QueryResults<CycTimFluItem> results = query.fetchResults();
		return results;
	}

	private static BooleanBuilder applyTimeFilter(BooleanBuilder filter, TimeSetting input) {
		if (!ObjectUtils.isEmpty(input.getTimeValue())) {
			if (TimeScale.YEAR.equals(input.getTimeScale())) {
				filter.and(table.year.eq(input.getTimeValue()));
			} else if (TimeScale.MONTH.equals(input.getTimeScale())) {
				filter.and(table.month.eq(input.getTimeValue()));
			} else if (TimeScale.WEEK.equals(input.getTimeScale())) {
				filter.and(table.week.eq(input.getTimeValue()));
			} else if (TimeScale.DATE.equals(input.getTimeScale())) {
				filter.and(table.day.eq(input.getTimeValue()));
			} else if (TimeScale.CUSTOM.equals(input.getTimeScale())) {
				filter.and(table.day.between(input.getFromDate(), input.getToDate()));
			}
		}
		return filter;
	}

	@Deprecated
	private static void addFilters(BooleanBuilder filter, List<Long> supplierIds, Long partId) {
		QCompany qCompany = QCompany.company;

		// Default Filter
		if (AccessControlUtils.isAccessFilterRequired()) {
			filter.and(Q.mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
			if ((!SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER))) {
				filter.and(qCompany.id.in(AccessControlUtils.getAllAccessibleCompanyIds()));
			}
		}
		filter.and(qCompany.enabled.isTrue());
		filter.and(qCompany.isEmoldino.isFalse());
		filter.and(Q.mold.deleted.isFalse().or(Q.mold.deleted.isNull()));

		// Supplier
		if (!ObjectUtils.isEmpty(supplierIds)) {
			filter.and(Q.mold.location.companyId.in(supplierIds));
		}

		// Part
		if (!ObjectUtils.isEmpty(partId)) {
//			filter.and(table.partId.eq(partId));
			QMoldPart moldPart = QMoldPart.moldPart;
			filter.and(Q.mold.id.in(//
					JPAExpressions.select(moldPart.moldId).from(moldPart).where(moldPart.partId.eq(partId))//
			));
		}
	}

	public Page<CycTimFluDetailsGetOut.CycTimFluDetails> findDetailsAll(CycTimFluDetailsGetIn input, Pageable pageable) {
		QueryResults<CycTimFluDetailsGetOut.CycTimFluDetails> results = findDetailsAll(input, pageable, false);
		List<CycTimFluDetailsGetOut.CycTimFluDetails> contents = results.getResults();
		calcDetailsCtdTrend(contents, input);
		return new PageImpl<>(contents, pageable, results.getTotal());
	}

	private QueryResults<CycTimFluDetailsGetOut.CycTimFluDetails> findDetailsAll(CycTimFluDetailsGetIn input, Pageable pageable, boolean isCount) {
		LogicUtils.assertNotNull(input.getSupplierId(), "supplierId");

		/*
		QCompany qsupplier = QCompany.company;
		
		BooleanBuilder filter = new BooleanBuilder();
		addFilters(filter, input.getSupplierId() == null ? null : Arrays.asList(input.getSupplierId()), input.getPartId());
		*/

		NumberExpression<Double> approvedCycleTimeData = table.approvedCycleTime.max();
		// use floatValue because doubleValue don't work in some version of the database(Example: mysql 5.7.33)
		NumberExpression<Float> averageCycleTimeData = table.averageCycleTime.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue());
		/*
		NumberExpression<Double> ctFluctuation = table.ctFluctuation.multiply(table.shotCount).sum().divide(table.shotCount.sum());
		*/

		NumberExpression<Double> l1LimitData = table.l1Limit.max();
		/*
		NumberExpression<Double> nctf = (table.nctf.multiply(table.shotCount)).sum().divide(table.shotCount.sum());
		*/

		//new
		NumberExpression<Integer> approvedCycleTimeDefault = new CaseBuilder().when(Q.mold.contractedCycleTime.isNull()).then(Q.mold.toolmakerContractedCycleTime)
				.otherwise(Q.mold.contractedCycleTime);
		NumberExpression<Float> approvedCycleTime = new CaseBuilder().when(approvedCycleTimeData.isNull()).then(approvedCycleTimeDefault.floatValue())
				.otherwise(approvedCycleTimeData.floatValue());
		/*
		NumberExpression<Float> averageCycleTime = new CaseBuilder().when(averageCycleTimeData.isNull()).then(Q.mold.weightedAverageCycleTime.floatValue())
				.otherwise(averageCycleTimeData.floatValue());
		*/

		NumberExpression<Float> l1Limit = new CaseBuilder().when(l1LimitData.isNull())
				.then(new CaseBuilder().when(Q.mold.cycleTimeLimit1Unit.isNull().or(Q.mold.cycleTimeLimit1Unit.eq(OutsideUnit.PERCENTAGE)))
						.then(approvedCycleTimeDefault.floatValue().multiply(Q.mold.cycleTimeLimit1).multiply(0.01)).otherwise(Q.mold.cycleTimeLimit1.multiply(10).floatValue()))
				.otherwise(l1LimitData.floatValue());

		BooleanBuilder filterInnerQuery = new BooleanBuilder();
		filterInnerQuery.and(table.moldId.eq(Q.mold.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		NumberExpression<Float> averageCycleTime = Expressions.asNumber(//
				JPAExpressions//
						.select(new CaseBuilder().when(averageCycleTimeData.isNull()).then(Q.mold.weightedAverageCycleTime.floatValue())
								.otherwise(averageCycleTimeData.floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();
		NumberExpression<Float> ctFluctuation = Expressions.asNumber(//
				JPAExpressions//
						.select(table.ctFluctuation.multiply(table.shotCount).sum().divide(table.shotCount.sum()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();
		NumberExpression<Float> nctf = Expressions.asNumber(//
				JPAExpressions//
						.select((table.nctf.multiply(table.shotCount)).sum().divide(table.shotCount.sum()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		//filter join
		BooleanBuilder filterJoinData = new BooleanBuilder().and(Q.mold.id.eq(table.moldId));
		applyTimeFilter(filterJoinData, input);

		final JPQLQuery<CycTimFluDetailsGetOut.CycTimFluDetails> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(//
						CycTimFluDetailsGetOut.CycTimFluDetails.class, //
						Q.mold.id, //
						Q.mold.equipmentCode, //
						approvedCycleTime, //
						averageCycleTime.as("averageCycleTime"), //
						ctFluctuation.as("ctFluctuation"), //
						l1Limit, //
						nctf.as("nctf"), //
						Expressions.asNumber(0D)//
				))//
				.from(Q.mold)//
				.groupBy(Q.mold.id);
		QueryUtils.includeDisabled(Q.mold);
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.leftJoin(query, join, table, () -> table.moldId.eq(Q.mold.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		QueryUtils.leftJoin(query, join, Q.supplier, () -> Q.supplier.id.eq(table.supplierId));
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		String property = pageable != null ? QueryUtils.getFirstSortProperty(pageable) : null;
		if (property != null) {
			if (property.equals("part")) {
				QueryUtils.leftJoin(query, join, Q.moldPart, () -> Q.moldPart.moldId.eq(Q.mold.id));
				QueryUtils.leftJoin(query, join, Q.part, () -> Q.part.id.eq(Q.moldPart.partId));
			}
		}
		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.supplier.id.eq(input.getSupplierId()));
		// Tooling for previous filter
		if (!ObjectUtils.isEmpty(input.getMoldId())) {
			filter.and(table.moldId.in(input.getMoldId()));
		}

		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("moldId", Q.mold.id)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("approvedCycleTime", approvedCycleTime)//
				.put("averageCycleTime", Expressions.numberPath(Float.class, "averageCycleTime"))//
				.put("ctFluctuation", Expressions.numberPath(Float.class, "ctFluctuation"))//
				.put("l1Limit", l1Limit)//
				.put("nctf", Expressions.numberPath(Float.class, "nctf"))//
				.put("parts", Q.moldPart.part.partCode)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Expressions.numberPath(Float.class, "nctf").desc());

		QueryResults<CycTimFluDetailsGetOut.CycTimFluDetails> results = query.fetchResults();
		return results;
	}

	private void calcCtfTrend(List<CycTimFluItem> contents, CycTimFluGetIn input) {
		if (ObjectUtils.isEmpty(contents) || input.getTimeScale() == null) {
			return;
		}

		CycTimFluGetIn inputPrev = ValueUtils.map(input, CycTimFluGetIn.class);
		if (!TimeScale.CUSTOM.equals(input.getTimeScale())) {
			String prevTimeValue = DateUtils.getPrevTimeValue(input.getTimeValue(), input.getTimeScale());
			inputPrev.setTimeValue(prevTimeValue);
		} else if (!StringUtils.isEmpty(input.getFromDate()) && !StringUtils.isEmpty(input.getToDate())) {
			Pair<String, String> prevTimeRange = DateUtils.getPrevTimeRange(input.getFromDate(), input.getToDate());
			inputPrev.setFromDate(prevTimeRange.getFirst());
			inputPrev.setToDate(prevTimeRange.getSecond());
		}

		if (ObjectUtils.isEmpty(inputPrev.getSupplierId())) {
			List<Long> supplierIds = contents.stream().map(item -> item.getSupplierId()).collect(Collectors.toList());
			inputPrev.setSupplierId(supplierIds);
		}

		Map<Long, Double> ctfPrevMap = new HashMap<>();
		findAll(inputPrev, null, false).getResults().forEach(item -> ctfPrevMap.put(item.getSupplierId(), item.getCtFluctuation()));
		contents.forEach(item -> {
			if (!ctfPrevMap.containsKey(item.getSupplierId())) {
				item.setCtfTrend(null);
			} else {
				Double ctfPrev = ctfPrevMap.get(item.getSupplierId());
				if (ctfPrev != null && ctfPrev != 0) {
					Double ctfCurrent = item.getCtFluctuation() != null ? item.getCtFluctuation() : 0;
					Double trend = (ctfCurrent - ctfPrev) / ctfPrev * 100;
					item.setCtfTrend(trend);
				}
			}
		});
	}

	private void calcDetailsCtdTrend(List<CycTimFluDetailsGetOut.CycTimFluDetails> contents, CycTimFluDetailsGetIn input) {
		if (ObjectUtils.isEmpty(contents) || input.getTimeScale() == null) {
			return;
		}

		CycTimFluDetailsGetIn inputPrev = ValueUtils.map(input, CycTimFluDetailsGetIn.class);
		String timeValue = input.getTimeValue();
		if (!TimeScale.CUSTOM.equals(input.getTimeScale())) {
			String prevTimeValue = DateUtils.getPrevTimeValue(timeValue, input.getTimeScale());
			inputPrev.setTimeValue(prevTimeValue);
		} else if (!StringUtils.isEmpty(input.getFromDate()) && !StringUtils.isEmpty(input.getToDate())) {
			Pair<String, String> prevTimeRange = DateUtils.getPrevTimeRange(input.getFromDate(), input.getToDate());
			inputPrev.setFromDate(prevTimeRange.getFirst());
			inputPrev.setToDate(prevTimeRange.getSecond());
		}

		List<Long> moldIds = contents.stream().map(details -> details.getMoldId()).collect(Collectors.toList());
		inputPrev.setMoldId(moldIds);

		Map<Long, Double> ctfsPrev = new HashMap<>();
		findDetailsAll(inputPrev, null, false).getResults().forEach(details -> ctfsPrev.put(details.getMoldId(), details.getCtFluctuation()));
		contents.forEach(details -> {
			if (!ctfsPrev.containsKey(details.getMoldId())) {
				details.setCtfTrend(null);
			} else {
				Double ctfPrev = ctfsPrev.get(details.getMoldId());
				if (ctfPrev != null && ctfPrev != 0) {
					Double ctfCurrent = details.getCtFluctuation() != null ? details.getCtFluctuation() : 0;
					Double trend = (ctfCurrent - ctfPrev) / ctfPrev * 100;
					details.setCtfTrend(trend);
				}
			}
		});
	}

}
