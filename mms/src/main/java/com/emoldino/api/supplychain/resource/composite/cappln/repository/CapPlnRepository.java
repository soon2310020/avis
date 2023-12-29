package com.emoldino.api.supplychain.resource.composite.cappln.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.CapPlnDetailsGetOut.CapPlnDetails;
import com.emoldino.api.supplychain.resource.composite.cappln.dto.QCapPlnDetailsGetOut_CapPlnDetails;
import com.emoldino.api.supplychain.resource.composite.cappln.util.CapPlnUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DslExpression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class CapPlnRepository {

	public List<CapPlnDetails> findAllDetails(CapPlnDetailsGetIn input, Sort sort) {
		LogicUtils.assertNotNull(input.getPartId(), "partId");

		List<String> weeks = CapPlnUtils.toWeeks(input);

		BooleanExpression prodMoldStatFilter = Q.prodMoldStat.moldId.eq(Q.mold.id).and(Q.prodMoldStat.week.in(weeks));

		// wact = avg(weekly_wact) = sum(weekly_wact) / weeks_count
		DslExpression<Double> wactField = Expressions.asNumber(//
				JPAExpressions//
						.select(Q.prodMoldStat.weightedAvgCycleTime.avg().divide(10d))//
						.from(Q.prodMoldStat)//
						.where(prodMoldStatFilter)//
		).coalesce(0d).as("weightedAvgCycleTime");
		// avg_ct = sum(weekly_avg_ct x weekly_sc) / sum(weekly_sc)
		DslExpression<Double> actField = Expressions.asNumber(//
				JPAExpressions//
						.select((Q.prodMoldStat.avgCycleTime.multiply(Q.prodMoldStat.actualShotCount).sum()).divide(Q.prodMoldStat.actualShotCount.sum()).divide(10d))//
						.from(Q.prodMoldStat)//
						.where(prodMoldStatFilter)//
		).coalesce(0d).as("avgCycleTime");
//		// prod-qty = sum(actual_sc * cavity)
//		DslExpression<Long> prodQtyField = Expressions.asNumber(//
//				JPAExpressions//
//						.select(Q.prodMoldStat.actualShotCount.multiply(Q.prodMoldStat.cavityCount).sum())//
//						.from(Q.prodMoldStat)//
//						.where(prodMoldStatFilter)//
//		).coalesce(0L).as("prodQty");
		// sum(weekly_capa)
		DslExpression<Long> weeklyCapaField = Expressions.asNumber(//
				JPAExpressions//
						.select(Q.prodMoldStat.weeklyCapa.sum())//
						.from(Q.prodMoldStat)//
						.where(prodMoldStatFilter)//
		).coalesce(0L).as("weeklyCapa");

		JPQLQuery<CapPlnDetails> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(new QCapPlnDetailsGetOut_CapPlnDetails( //
						Q.mold.id, //
						Q.mold.equipmentCode, //
						wactField, //
						actField, //
						Expressions.asNumber(0L), //
						weeklyCapaField//
				))//
				.distinct()//
				.from(Q.mold)//
				.groupBy(Q.mold.id);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
		QueryUtils.joinPartByMold(query, join);
		QueryUtils.joinSupplierByMold(query, join);

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.part.id.eq(input.getPartId()));
		if (input.getSupplierId() != null) {
			filter.and(Q.supplier.id.eq(input.getSupplierId()));
		}
		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("weightedAvgCycleTime", Expressions.numberPath(Long.class, "weightedAvgCycleTime"))//
				.put("avgCycleTime", Expressions.numberPath(Long.class, "avgCycleTime"))//
				.put("prodQty", Expressions.numberPath(Long.class, "prodQty"))//
				.put("weeklyCapa", Expressions.numberPath(Long.class, "weeklyCapa"))//
				.build();

		QueryUtils.applySort(query, sort, fieldMap, Q.mold.equipmentCode.asc());

		return query.fetch();
	}

}
