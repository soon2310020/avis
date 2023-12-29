package com.emoldino.api.analysis.resource.composite.cyctimdev.repository;

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
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut.CycTimDevDetails;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevItem;
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
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;

@Component
public class CycTimDevRepository {
	private static QCycleTimeDeviation table = QCycleTimeDeviation.cycleTimeDeviation;

	public Page<CycTimDevItem> findAll(CycTimDevGetIn input, Pageable pageable) {
		QueryResults<CycTimDevItem> results = findAll(input, pageable, false);
		List<CycTimDevItem> contents = results.getResults();
		calcNctdTrend(contents, input);
		return new PageImpl<>(contents, pageable, results.getTotal());
	}

	private QueryResults<CycTimDevItem> findAll(CycTimDevGetIn input, Pageable pageable, boolean isCount) {
		NumberExpression<Long> moldCount = Q.mold.id.countDistinct();

		// use floatValue because doubleValue don't work in some version of the database(Example: mysql 5.7.33)
		/*
		NumberExpression<Float> aboveTolerance = table.aboveToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D);
		NumberExpression<Float> withinUpperL2Tolerance = table.withinUpperL2ToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D);
		NumberExpression<Float> withinL1Tolerance = table.withinL1ToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D);
		NumberExpression<Float> withinLowerL2Tolerance = table.withinLowerL2ToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D);
		NumberExpression<Float> belowTolerance = table.belowToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D);
		NumberExpression<Float> nctd = (table.nctd.multiply(table.shotCount)).sum().floatValue().divide(table.shotCount.sum().floatValue());
		*/

		BooleanBuilder filterInnerQuery = new BooleanBuilder();
		filterInnerQuery.and(table.moldId.eq(Q.mold.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		NumberExpression<Integer> shotCount = Expressions.asNumber(//
				JPAExpressions//
						.select(table.shotCount.sum().coalesce(0))//
						.from(table)//
						.where(filterInnerQuery)//
		).intValue();

		NumberExpression<Float> aboveTolerance = Expressions.asNumber(//
				JPAExpressions//
						.select(table.aboveToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		NumberExpression<Float> withinUpperL2Tolerance = Expressions.asNumber(//
				JPAExpressions//
						.select(table.withinUpperL2ToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		NumberExpression<Float> withinL1Tolerance = Expressions.asNumber(//
				JPAExpressions//
						.select(table.withinL1ToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		NumberExpression<Float> withinLowerL2Tolerance = Expressions.asNumber(//
				JPAExpressions//
						.select(table.withinLowerL2ToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		NumberExpression<Float> belowTolerance = Expressions.asNumber(//
				JPAExpressions//
						.select(table.belowToleranceSc.sum().floatValue().divide(table.shotCount.sum().floatValue()).multiply(100D))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		NumberExpression<Float> nctd = Expressions.asNumber(//
				JPAExpressions//
						.select((table.nctd.multiply(table.shotCount)).sum().floatValue().divide(table.shotCount.sum().floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		NumberExpression<Integer> aboveToleranceSc = Expressions.asNumber(//
				JPAExpressions//
						.select(table.aboveToleranceSc.sum())//
						.from(table).where(filterInnerQuery)//
		).intValue();
		NumberExpression<Integer> withinUpperL2ToleranceSc = Expressions.asNumber(//
				JPAExpressions//
						.select(table.withinUpperL2ToleranceSc.sum())//
						.from(table).where(filterInnerQuery)//
		).intValue();
		NumberExpression<Integer> withinL1ToleranceSc = Expressions.asNumber(//
				JPAExpressions//
						.select(table.withinL1ToleranceSc.sum())//
						.from(table).where(filterInnerQuery)//
		).intValue();
		NumberExpression<Integer> withinLowerL2ToleranceSc = Expressions.asNumber(//
				JPAExpressions//
						.select(table.withinLowerL2ToleranceSc.sum())//
						.from(table).where(filterInnerQuery)//
		).intValue();
		NumberExpression<Integer> belowToleranceSc = Expressions.asNumber(//
				JPAExpressions//
						.select(table.belowToleranceSc.sum())//
						.from(table).where(filterInnerQuery)//
		).intValue();

		final JPQLQuery<CycTimDevItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(//
						CycTimDevItem.class, //
						Q.supplier.id, //
						Q.supplier.name, //
						Q.supplier.companyCode, //
						moldCount.as("moldCount"), //
						aboveTolerance.as("aboveTolerance"), //
						withinUpperL2Tolerance.as("withinUpperL2Tolerance"), //
						withinL1Tolerance.as("withinL1Tolerance"), //
						withinLowerL2Tolerance.as("withinLowerL2Tolerance"), //
						belowTolerance.as("belowTolerance"), //
						shotCount, //table.shotCount.sum(), //
						aboveToleranceSc, //table.aboveToleranceSc.sum(), //
						withinUpperL2ToleranceSc, //table.withinUpperL2ToleranceSc.sum(), //
						withinL1ToleranceSc, //table.withinL1ToleranceSc.sum(), //
						withinLowerL2ToleranceSc, //table.withinLowerL2ToleranceSc.sum(), //
						belowToleranceSc, //table.belowToleranceSc.sum(), //
						nctd.as("nctd"), //
						Expressions.asNumber(0D)//
				))//
				.from(Q.supplier)//
				.groupBy(Q.supplier.id);
		QueryUtils.includeDisabled(Q.mold);
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.leftJoin(query, join, table, () -> table.supplierId.eq(Q.supplier.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		QueryUtils.leftJoin(query, join, Q.mold, () -> Q.mold.id.eq(table.moldId));
//		QueryUtils.leftJoin(query, join, Q.part, () -> Q.part.id.eq(table.partId));
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
				.put("aboveTolerance", Expressions.numberPath(Float.class, "aboveTolerance"))//
				.put("withinUpperL2Tolerance", Expressions.numberPath(Float.class, "withinUpperL2Tolerance"))//
				.put("withinL1Tolerance", Expressions.numberPath(Float.class, "withinL1Tolerance"))//
				.put("withinLowerL2Tolerance", Expressions.numberPath(Float.class, "withinLowerL2Tolerance"))//
				.put("belowTolerance", Expressions.numberPath(Float.class, "belowTolerance"))//
				.put("nctd", Expressions.numberPath(Float.class, "nctd"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Expressions.numberPath(Float.class, "nctd").desc());

		QueryResults<CycTimDevItem> results = query.fetchResults();
		return results;
	}

	public Page<CycTimDevDetails> findDetailsAll(CycTimDevDetailsGetIn input, Pageable pageable) {
		QueryResults<CycTimDevDetails> results = findDetailsAll(input, pageable, false);
		List<CycTimDevDetails> contents = results.getResults();
		calcDetailsNctdTrend(contents, input);
		return new PageImpl<>(contents, pageable, results.getTotal());
	}

	private QueryResults<CycTimDevDetails> findDetailsAll(CycTimDevDetailsGetIn input, Pageable pageable, boolean isCount) {
		LogicUtils.assertNotNull(input.getSupplierId(), "supplierId");

		NumberExpression<Double> approvedCycleTimeData = table.approvedCycleTime.max();
		// use floatValue because doubleValue don't work in some version of the database(Example: mysql 5.7.33)
		NumberExpression<Float> averageCycleTimeData = table.averageCycleTime.floatValue().multiply(table.shotCount.floatValue()).sum().divide(table.shotCount.sum().floatValue());
//		NumberExpression nctd = averageCycleTimeData.subtract(approvedCycleTimeData).divide(approvedCycleTimeData.coalesce(1d)).multiply(100);
		/*
		NumberExpression<Double> nctd = (table.nctd.multiply(table.shotCount)).sum().divide(table.shotCount.sum());
		*/

		//new
		NumberExpression<Float> approvedCycleTime = new CaseBuilder()//
				.when(approvedCycleTimeData.isNull())//
				.then(Q.mold.contractedCycleTime.floatValue())//
				.otherwise(approvedCycleTimeData.floatValue());
		/*
		NumberExpression<Float> averageCycleTime = new CaseBuilder()//
				.when(averageCycleTimeData.isNull())//
				.then(Q.mold.weightedAverageCycleTime.floatValue())//
				.otherwise(averageCycleTimeData.floatValue());
		*/

		BooleanBuilder filterInnerQuery = new BooleanBuilder();
		filterInnerQuery.and(table.moldId.eq(Q.mold.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		NumberExpression<Float> nctd = Expressions.asNumber(//
				JPAExpressions//
						.select((table.nctd.multiply(table.shotCount)).sum().divide(table.shotCount.sum()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();
		NumberExpression<Float> averageCycleTime = Expressions.asNumber(//
				JPAExpressions//
						.select(new CaseBuilder()//
								.when(averageCycleTimeData.isNull())//
								.then(Q.mold.weightedAverageCycleTime.floatValue())//
								.otherwise(averageCycleTimeData.floatValue()))//
						.from(table)//
						.where(filterInnerQuery)//
		).floatValue();

		final JPQLQuery<CycTimDevDetails> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(//
						CycTimDevDetails.class, //
						Q.mold.id, //
						Q.mold.equipmentCode, //
						approvedCycleTime, //
						averageCycleTime.as("averageCycleTime"), //
						nctd.as("nctd"), //
						Expressions.asNumber(0D)//
				))//
				.from(Q.mold)//
				.groupBy(Q.mold.id);
		QueryUtils.includeDisabled(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.leftJoin(query, join, table, () -> table.moldId.eq(Q.mold.id).and(applyTimeFilter(new BooleanBuilder(), input)));
		QueryUtils.leftJoin(query, join, Q.supplier, () -> Q.supplier.id.eq(table.supplierId));
//		QueryUtils.leftJoin(query, join, Q.part, () -> Q.part.id.eq(table.partId));
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
				.put("moldId", table.moldId)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("approvedCycleTime", approvedCycleTime)//
				.put("averageCycleTime", Expressions.numberPath(Float.class, "averageCycleTime"))//
				.put("nctd", Expressions.numberPath(Float.class, "nctd"))//
				.put("parts", Q.moldPart.part.partCode)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Expressions.numberPath(Float.class, "nctd").desc());

		QueryResults<CycTimDevDetails> results = query.fetchResults();
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

	private void calcNctdTrend(List<CycTimDevItem> contents, CycTimDevGetIn input) {
		if (ObjectUtils.isEmpty(contents) || input.getTimeScale() == null) {
			return;
		}

		CycTimDevGetIn inputPrev = ValueUtils.map(input, CycTimDevGetIn.class);
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

		Map<Long, Double> nctdsPrev = new HashMap<>();
		findAll(inputPrev, null, false).getResults().forEach(item -> nctdsPrev.put(item.getSupplierId(), item.getNctd()));
		contents.forEach(item -> {
			if (!nctdsPrev.containsKey(item.getSupplierId())) {
				item.setNctdTrend(null);
			} else {
				Double nctdPrev = nctdsPrev.get(item.getSupplierId());
				Double nctdCurrent = item.getNctd() != null ? item.getNctd() : 0;
				if (nctdPrev != null && nctdPrev != 0) {
					item.setNctdTrend((nctdCurrent - nctdPrev) / nctdPrev * 100);
				}
			}
		});
	}

	private void calcDetailsNctdTrend(List<CycTimDevDetailsGetOut.CycTimDevDetails> contents, CycTimDevDetailsGetIn input) {
		if (ObjectUtils.isEmpty(contents) || input.getTimeScale() == null) {
			return;
		}

		CycTimDevDetailsGetIn inputPrev = ValueUtils.map(input, CycTimDevDetailsGetIn.class);
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

		Map<Long, Double> nctdsPrev = new HashMap<>();
		findDetailsAll(inputPrev, null, false).getResults().forEach(details -> nctdsPrev.put(details.getMoldId(), details.getNctd()));
		contents.forEach(details -> {
			if (!nctdsPrev.containsKey(details.getMoldId())) {
				details.setNctdTrend(null);
			} else {
				Double nctdPrev = nctdsPrev.get(details.getMoldId());
				Double nctdCurrent = details.getNctd() != null ? details.getNctd() : 0;
				if (nctdPrev != null && nctdPrev != 0) {
					details.setNctdTrend((nctdCurrent - nctdPrev) / nctdPrev * 100);
				}
			}
		});
	}

}
