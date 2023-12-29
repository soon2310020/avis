package com.emoldino.api.analysis.resource.composite.prochg.repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.base.data.repository.moldprocchg.QMoldProcChg;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetails;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetOut.ProChgChartItem;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetOut.ProChgTopItem;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgItem;
import com.emoldino.api.analysis.resource.composite.prochg.util.ProChgUtils;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.QPartStat;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class ProChgRepository {
	private static final QMoldProcChg qMoldProcChg = QMoldProcChg.moldProcChg;
	private static final QPartStat qPartStat = QPartStat.partStat;

	public long countTotal(ProChgGetIn input, TimeSetting timeSetting) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(qMoldProcChg.countDistinct())//
				.from(qMoldProcChg);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, timeSetting);

		return query.fetchOne();
	}

	private long count(ProChgGetIn input, TimeSetting timeSetting) {
		JPQLQuery<String> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(//
						qMoldProcChg.hour)//
				.from(qMoldProcChg)//
				.groupBy(//
						qMoldProcChg.hour, //
						qMoldProcChg.moldId//
				);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, timeSetting);

		return query.fetch().size();
	}

	public Page<ProChgItem> findAll(ProChgGetIn input, TimeSetting timeSetting, Pageable pageable) {
		long total = count(input, timeSetting);
		if (total == 0L) {
			return new PageImpl<>(Collections.emptyList());
		}

		JPQLQuery<ProChgItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProChgItem.class, //
						qMoldProcChg.moldId, //
						Q.mold.equipmentCode.max(), //
						qMoldProcChg.hour, //
						qMoldProcChg.count().as("procChgCount"), //
						Q.supplier.id.max(), //
						Q.supplier.name.max(), //
						Q.location.id.max(), //
						Q.location.name.max() //
				)) //
				.from(qMoldProcChg)//
				.groupBy(//
						qMoldProcChg.hour, //
						qMoldProcChg.moldId//
				);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, timeSetting);

		QueryUtils.leftJoin(query, join, Q.supplier, () -> Q.supplier.id.eq(Q.mold.supplierCompanyId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>() //
				.put("moldCode", Q.mold.equipmentCode)//
				.put("procChgHour", qMoldProcChg.hour)//
				.put("procChgCount", Expressions.numberPath(Long.class, "procChgCount"))//
				.put("supplierName", Q.supplier.name)//
				.put("locationName", Q.location.name)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, qMoldProcChg.hour.desc());
		List<ProChgItem> content = query.fetch();

		return new PageImpl<>(content, pageable, total);
	}

	public List<ProChgTopItem> findAllTopItems(ProChgGetIn input, TimeSetting timeSetting) {
		JPQLQuery<ProChgTopItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProChgTopItem.class, //
						qMoldProcChg.moldId, //
						Q.mold.equipmentCode.as("moldCode"), //
						qMoldProcChg.id.count().as("procChgCount") //
				)) //
				.from(qMoldProcChg)//
				.groupBy(qMoldProcChg.moldId)//
				.orderBy(Expressions.numberPath(Long.class, "procChgCount").desc(), Q.mold.equipmentCode.asc());

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, timeSetting);

		query.limit(100);
		return query.fetch();
	}

	public List<ProChgChartItem> findAllProcChgCountItems(ProChgGetIn input, TimeSetting timeSetting) {
		ValueUtils.assertNotEmpty(timeSetting.getTimeScale(), "timeScale");

		StringPath titleField = ProChgUtils.BY_MONTH_SCALES.contains(timeSetting.getTimeScale()) ? qMoldProcChg.month : qMoldProcChg.day;

		JPQLQuery<ProChgChartItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProChgChartItem.class, //
						titleField.as("title"), //
						qMoldProcChg.id.count().as("procChgCount"), //
						Expressions.asNumber(0L).as("prodQty")//
				)) //
				.from(qMoldProcChg) //
				.groupBy(titleField) //
				.orderBy(titleField.asc()) //
				.limit(50); //

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, timeSetting);

		return query.fetch();
	}

	public List<ProChgChartItem> findAllProdQtyItems(ProChgGetIn input, TimeSetting timeSetting) {
		ValueUtils.assertNotEmpty(timeSetting.getTimeScale(), "timeScale");

		StringPath titleField = ProChgUtils.BY_MONTH_SCALES.contains(timeSetting.getTimeScale()) ? qPartStat.month : qPartStat.day;

		NumberPath<Long> prodField = qPartStat.produced;

		JPQLQuery<ProChgChartItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProChgChartItem.class, //
						titleField.as("title"), //
						Expressions.asNumber(0L).as("procChgCount"), //
						prodField.sum().as("prodQty")//
				)) //
				.from(qPartStat) //
				.groupBy(titleField) //
				.orderBy(titleField.asc()) //
				.limit(50); //

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyPartStatFilter(query, join, input, timeSetting);

		return query.fetch();
	}

	public static void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, ProChgGetIn input, TimeSetting timeSetting) {
		QueryUtils.join(query, join, Q.mold, () -> Q.mold.id.eq(qMoldProcChg.moldId));
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, qMoldProcChg.moldId, input.getMoldId());
		QueryUtils.eq(filter, qMoldProcChg.month, input.getMonth());
		QueryUtils.eq(filter, qMoldProcChg.day, input.getDay());
		if (!ObjectUtils.isEmpty(timeSetting.getTimeValue())) {
			if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
				filter.and(qMoldProcChg.year.eq(timeSetting.getTimeValue()));
			} else if (TimeScale.HALF.equals(timeSetting.getTimeScale())) {
				filter.and(qMoldProcChg.month.in(DateUtils2.toMonths(timeSetting)));
			} else if (TimeScale.QUARTER.equals(timeSetting.getTimeScale())) {
				filter.and(qMoldProcChg.month.in(DateUtils2.toMonths(timeSetting)));
			} else if (TimeScale.MONTH.equals(timeSetting.getTimeScale())) {
				filter.and(qMoldProcChg.month.eq(timeSetting.getTimeValue()));
			} else if (TimeScale.WEEK.equals(timeSetting.getTimeScale())) {
				filter.and(qMoldProcChg.week.eq(timeSetting.getTimeValue()));
			} else if (TimeScale.CUSTOM.equals(timeSetting.getTimeScale())) {
				filter.and(qMoldProcChg.day.goe(timeSetting.getFromDate())).and(qMoldProcChg.day.loe(timeSetting.getToDate()));
//			} else if (TimeScale.DATE.equals(timeSetting.getTimeScale())) {
//				filter.and(qMoldProcChg.day.eq(timeSetting.getTimeValue()));
//			} else if (TimeScale.HOUR.equals(timeSetting.getTimeScale())) {
//				filter.and(qMoldProcChg.hour.eq(timeSetting.getTimeValue()));
			} else {
				ValueUtils.assertTimeSetting(timeSetting, Collections.emptyList());
			}
		}

		query.where(filter);
	}

	public static void applyPartStatFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, ProChgGetIn input, TimeSetting timeSetting) {
		QueryUtils.join(query, join, Q.mold, () -> Q.mold.id.eq(qPartStat.moldId));
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.mold.counter.equipmentCode.startsWith("EM"));
		QueryUtils.eq(filter, qPartStat.moldId, input.getMoldId());
		QueryUtils.eq(filter, qPartStat.month, input.getMonth());
		QueryUtils.eq(filter, qPartStat.day, input.getDay());
		if (!ObjectUtils.isEmpty(timeSetting.getTimeValue())) {
			if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
				filter.and(qPartStat.year.eq(timeSetting.getTimeValue()));
			} else if (TimeScale.HALF.equals(timeSetting.getTimeScale())) {
				filter.and(qPartStat.month.in(DateUtils2.toMonths(timeSetting)));
			} else if (TimeScale.QUARTER.equals(timeSetting.getTimeScale())) {
				filter.and(qPartStat.month.in(DateUtils2.toMonths(timeSetting)));
			} else if (TimeScale.MONTH.equals(timeSetting.getTimeScale())) {
				filter.and(qPartStat.month.eq(timeSetting.getTimeValue()));
			} else if (TimeScale.WEEK.equals(timeSetting.getTimeScale())) {
				filter.and(qPartStat.week.eq(timeSetting.getTimeValue()));
			} else if (TimeScale.CUSTOM.equals(timeSetting.getTimeScale())) {
				filter.and(qPartStat.day.goe(timeSetting.getFromDate())).and(qPartStat.day.loe(timeSetting.getToDate()));
//			} else if (TimeScale.DATE.equals(timeSetting.getTimeScale())) {
//				filter.and(qPartStat.day.eq(timeSetting.getTimeValue()));
//			} else if (TimeScale.HOUR.equals(timeSetting.getTimeScale())) {
//				filter.and(qPartStat.hour.eq(timeSetting.getTimeValue()));
			} else {
				ValueUtils.assertTimeSetting(timeSetting, Collections.emptyList());
			}
		}

		query.where(filter);
	}

	public Page<ProChgDetails> findAllDetails(ProChgDetailsGetIn input, Pageable pageable) {
		JPQLQuery<ProChgDetails> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProChgDetails.class, //
						qMoldProcChg.id, //
						qMoldProcChg.procChgTime.max(), //
						Q.location.id.max(), //
						Q.location.name.max(), //
						Q.part.name.min().as("firstPartName") //
				)) //
				.from(qMoldProcChg)//
				.groupBy(qMoldProcChg.id);

		Set<EntityPathBase<?>> join = new HashSet<>();

		QueryUtils.join(query, join, Q.mold, () -> Q.mold.id.eq(qMoldProcChg.moldId));
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoin(query, join, Q.moldPart, () -> Q.moldPart.moldId.eq(Q.mold.id));
		QueryUtils.leftJoin(query, join, Q.part, () -> Q.part.id.eq(Q.moldPart.partId));
		QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, qMoldProcChg.moldId, input.getMoldId());
		QueryUtils.eq(filter, qMoldProcChg.hour, ProChgUtils.toHour(input.getDateHourRange()));
		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>() //
				.put("procChgTime", qMoldProcChg.procChgTime)//
				.put("locationId", Q.location.id)//
				.put("locationName", Q.location.name)//
				.put("parts", Expressions.stringPath("firstPartName"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, qMoldProcChg.procChgTime.desc());
		QueryResults<ProChgDetails> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}
}
