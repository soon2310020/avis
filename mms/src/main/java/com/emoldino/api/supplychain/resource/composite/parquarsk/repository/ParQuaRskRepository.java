package com.emoldino.api.supplychain.resource.composite.parquarsk.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetIn;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetOut.ParQuaRskChartItem;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskHeatmapItem;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskItem;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskMold;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.repository.Qai;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class ParQuaRskRepository {

	public String findPrevWeek(ParQuaRskGetIn input, TimeSetting timeSetting, Long moldId, Long partId, Long supplierId, Long locationId) {
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.setAutoDistinct(false);
		JPQLQuery<String> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.statistics.week), //
				join, null, input.getFilterCode() //
		);
		where(query, input);
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.mold.id.eq(moldId))//
				.and(Q.part.id.eq(partId))//
				.and(Q.supplier.id.eq(supplierId))//
				.and(Q.location.id.eq(locationId))//
				.and(Q.statistics.week.lt(timeSetting.getTimeValue()));
		query.where(filter).orderBy(Q.statistics.week.desc());
		return query.fetchFirst();
	}

	public Double findEstimYieldRate(ParQuaRskGetIn input, String week, Long moldId, Long partId, Long supplierId, Long locationId) {
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.setAutoDistinct(false);
		JPQLQuery<Float> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
				.select(estimYieldRate().coalesce(0f).as("estimYieldRate")), //
				join, //
				TimeSetting.builder().timeScale(TimeScale.WEEK).timeValue(week).build(), //
				input.getFilterCode() //
		);
		where(query, input);
		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.mold.id.eq(moldId))//
				.and(Q.part.id.eq(partId))//
				.and(Q.supplier.id.eq(supplierId))//
				.and(Q.location.id.eq(locationId));
		query.where(filter);
		return ValueUtils.toDouble(query.fetchOne(), 0d);
	}

	public Page<ParQuaRskItem> findAll(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable) {

		long total = 0L;
		{
			QueryUtils.setAutoDistinct(false);
			Set<EntityPathBase<?>> join = new HashSet<>();
			JPQLQuery<Tuple> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
					.select(//
							Q.part.id, //
							Q.mold.id, //
							Q.supplier.id, //
							Q.location.id//
					), //
					join, timeSetting, input.getFilterCode() //
			).groupBy(Q.part.id, Q.mold.id, Q.supplier.id, Q.location.id);
			where(query, input);
			int page = 0;
			List<Tuple> content;
			while (!(content = QueryUtils.applyPagination(query, PageRequest.of(page++, 10000)).fetch()).isEmpty()) {
				total += content.size();
				if (content.size() < 10000) {
					break;
				}
			}
		}

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.setAutoDistinct(false);
		JPQLQuery<ParQuaRskItem> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ParQuaRskItem.class, //
						Q.part.id, //
						Q.part.name.max().as("partName"), //
						Q.part.partCode.max(), //

						Q.mold.id, //
						Q.mold.equipmentCode.max().as("moldCode"), //

						Q.supplier.id, //
						Q.supplier.name.max().as("supplierName"), //
						Q.supplier.companyCode.max(), //

						Q.location.id, //
						Q.location.name.max().as("locationName"), //
						Q.location.locationCode.max(), //

						prodQtySum().coalesce(0).as("prodQty"), //
						goodProdQtyDoubleSum().intValue().coalesce(0).as("goodProdQty"), //
						estimYieldRate().as("estimYieldRate")//
				)), //
				join, timeSetting, input.getFilterCode() //
		).groupBy(Q.part.id, Q.mold.id, Q.supplier.id, Q.location.id);

		where(query, input);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("partName", Expressions.stringPath("partName"))//
				.put("moldCode", Expressions.stringPath("moldCode"))//
				.put("supplierName", Expressions.stringPath("supplierName"))//
				.put("locationName", Expressions.stringPath("locationName"))//
				.put("prodQty", Expressions.numberPath(Long.class, "prodQty"))//
				.put("estimYieldRate", Expressions.numberPath(Double.class, "estimYieldRate"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Expressions.numberPath(Double.class, "estimYieldRate").asc(), Expressions.stringPath("partName").asc());
		List<ParQuaRskItem> content = query.fetch();

		return new PageImpl<>(content, pageable, total);
	}

	public Page<ParQuaRskMold> findAllMolds(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable) {

		long total = 0L;
		{
			QueryUtils.setAutoDistinct(false);
			Set<EntityPathBase<?>> join = new HashSet<>();
			JPQLQuery<Tuple> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
					.select(//
							Q.mold.id, //
							Q.supplier.id, //
							Q.location.id//
					), //
					join, timeSetting, input.getFilterCode() //
			).groupBy(Q.mold.id, Q.supplier.id, Q.location.id);
			if (!ObjectUtils.isEmpty(input.getQuery())) {
				String searchWord = input.getQuery();
				query.where(Q.mold.equipmentCode.containsIgnoreCase(searchWord));
			}
			int page = 0;
			List<Tuple> content;
			while (!(content = QueryUtils.applyPagination(query, PageRequest.of(page++, 10000)).fetch()).isEmpty()) {
				total += content.size();
				if (content.size() < 10000) {
					break;
				}
			}
		}

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.setAutoDistinct(false);
		JPQLQuery<ParQuaRskMold> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ParQuaRskMold.class, //
						Q.part.name.min().as("partName"), //

						Q.mold.id, //
						Q.mold.equipmentCode.max().as("moldCode"), //

						Q.supplier.id, //
						Q.supplier.name.max().as("supplierName"), //

						Q.location.id, //
						Q.location.name.max().as("locationName"), //

						prodQtySum().coalesce(0).as("prodQty"), //
						estimYieldRate().as("estimYieldRate")//
				)), //
				join, timeSetting, input.getFilterCode() //
		).groupBy(Q.mold.id, Q.supplier.id, Q.location.id);
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			query.where(Q.mold.equipmentCode.containsIgnoreCase(searchWord));
		}
		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("partName", Expressions.stringPath("partName"))//
				.put("moldCode", Expressions.stringPath("moldCode"))//
				.put("supplierName", Expressions.stringPath("supplierName"))//
				.put("locationName", Expressions.stringPath("locationName"))//
				.put("prodQty", Expressions.numberPath(Long.class, "prodQty"))//
				.put("estimYieldRate", Expressions.numberPath(Float.class, "estimYieldRate"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Expressions.numberPath(Double.class, "estimYieldRate").asc());
		List<ParQuaRskMold> content = query.fetch();

		return new PageImpl<>(content, pageable, total);
	}

	public List<ParQuaRskChartItem> findAllChartItems(ParQuaRskGetIn input, TimeSetting timeSetting) {
		Set<EntityPathBase<?>> join = new HashSet<>();

		JPQLQuery<ParQuaRskChartItem> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ParQuaRskChartItem.class, //
						Q.statistics.day, //
						prodQtySum().coalesce(0).as("prodQty"), //
						goodProdQtyDoubleSum().intValue().coalesce(0).as("goodProdQty")//
				)), //
				join, timeSetting, input.getFilterCode() //
		).groupBy(Q.statistics.day).orderBy(Q.statistics.day.asc());

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		return query.fetch();
//		return Collections.emptyList();
	}

	public List<ParQuaRskHeatmapItem> findAllHeatmapItems(Long moldId, TimeSetting timeSetting) {
		Set<EntityPathBase<?>> join = new HashSet<>();

		JPQLQuery<ParQuaRskHeatmapItem> query = fromJoin(BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ParQuaRskHeatmapItem.class, //
						Q.statistics.hour, //
						prodQtySum().coalesce(0).as("prodQty"), //
						Qai.pqResult.ppaStatus.max()//
				)), //
				join, timeSetting, "COMMON"//
		).groupBy(Q.statistics.hour).orderBy(Q.statistics.hour.asc());

		query.where(Q.mold.id.eq(moldId));

		return query.fetch();
//		return Collections.emptyList();
	}

	private NumberExpression<Float> estimYieldRate() {
		return goodProdQtyDoubleSum().divide(prodQtySum().floatValue()).multiply(100f);
//		return Q.statistics.shotCount.sum().doubleValue();
	}

	private NumberExpression<Float> goodProdQtyDoubleSum() {
		return new CaseBuilder()//
				.when(Qai.pqResult.qdStatus.eq("GOOD"))//
				.then(prodQty().floatValue())//
				.when(Qai.pqResult.qdStatus.isNull())//
				.then(prodQty().floatValue().multiply(1d))//
				.otherwise(0f)//
				.sum();
//		return Q.statistics.shotCount.sum();
	}

//	private NumberExpression<Integer> goodProdQtySum() {
//		return new CaseBuilder()//
//				.when(Qai.pqResult.qdStatus.eq("GOOD"))//
//				.then(prodQty())//
//				.when(Qai.pqResult.qdStatus.isNull())//
//				.then(prodQty().multiply(1d))//
//				.otherwise(0)//
//				.sum();
////		return Q.statistics.shotCount.sum();
//	}

	private NumberExpression<Integer> prodQty() {
		return Q.statistics.shotCount.multiply(Q.statisticsPart.cavity);
//		return Q.statistics.shotCount;
	}

	private NumberExpression<Integer> prodQtySum() {
		return prodQty().sum();
//		return Q.statistics.shotCount.sum();
	}

	private <T> JPQLQuery<T> fromJoin(JPQLQuery<T> query, Set<EntityPathBase<?>> join, TimeSetting timeSetting, String filterCode) {
		query.from(Q.statistics).where(Q.statistics.shotCount.gt(0));//
		if (timeSetting != null) {
			query.where(Q.statistics.week.eq(timeSetting.getTimeValue()));
		}
		QueryUtils.join(query, join, Q.mold, Q.mold.id.eq(Q.statistics.moldId));
		QueryUtils.join(query, join, Q.terminal, Q.terminal.equipmentCode.eq(Q.statistics.ti));
		QueryUtils.join(query, join, Q.supplier, Q.supplier.id.eq(Q.terminal.companyId));
		QueryUtils.join(query, join, Q.location, Q.location.id.eq(Q.terminal.locationId));
		QueryUtils.join(query, join, Q.statisticsPart, Q.statisticsPart.statisticsId.eq(Q.statistics.id));
		QueryUtils.join(query, join, Q.part, Q.part.id.eq(Q.statisticsPart.partId));
		QueryUtils.leftJoin(query, join, Qai.pqResult, Qai.pqResult.statisticsId.eq(Q.statistics.id));
		QueryUtils.applyMoldFilter(query, join, filterCode);

		query.where(Q.mold.counter.equipmentCode.startsWith("EM"));

		return query;
	}

	private void where(JPQLQuery<?> query, ParQuaRskGetIn input) {
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			query.where(//
					Q.mold.equipmentCode.containsIgnoreCase(searchWord)//
							.or(Q.part.name.containsIgnoreCase(searchWord))//
							.or(Q.supplier.name.containsIgnoreCase(searchWord))//
							.or(Q.location.name.containsIgnoreCase(searchWord))//
			);
		}
	}

}
