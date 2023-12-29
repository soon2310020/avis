package com.emoldino.api.supplychain.resource.composite.demcpl.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplDetailsGetOut.DemCplDetails;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class DemCplRepository {

	public Long findPlanQtyByPartAndWeek(Long partId, String week) {
		LogicUtils.assertNotNull(partId, "partId");
		LogicUtils.assertNotNull(week, "week");

		long demand = ValueUtils.toLong(//
				BeanUtils.get(JPAQueryFactory.class)//
						.select(Q.partPlan.quantity.sum())//
						.from(Q.partPlan)//
						.where(new BooleanBuilder()//
								.and(Q.partPlan.partId.eq(partId))//
								.and(Q.partPlan.periodType.eq("WEEKLY"))//
								.and(Q.partPlan.periodValue.eq(week)))//
						.fetchOne(), //
				0L);
		return demand;
	}

	public QueryResults<DemCplDetails> findAllDetailsByPartAndSupplier(DemCplDetailsGetIn input, Pageable pageable) {
		int days = 7;
		// use floatValue because doubleValue don't work in some version of the database(Example: mysql 5.7.33)
		JPAQuery<DemCplDetails> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(DemCplDetails.class, //
						Q.mold.id, Q.mold.equipmentCode, Q.mold.toolingStatus, Q.mold.equipmentStatus, //
						Q.supplier.id, Q.supplier.name, Q.supplier.companyCode, //
						Q.mold.utilizationRate.coalesce(0d), Q.mold.contractedCycleTime.castToNum(Float.class).coalesce(0f), Q.mold.weightedAverageCycleTime.coalesce(0d), //
						Expressions.asNumber(0L), Q.mold.maxCapacityPerWeek.divide(days).castToNum(Long.class).coalesce(0L)//
				))//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
		QueryUtils.joinPartByMold(query, join);
		QueryUtils.joinSupplierByMold(query, join);

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.part.id, input.getPartId());
		QueryUtils.in(filter, Q.supplier.id, input.getSupplierId());
		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("supplierName", Q.supplier.name)//
				.put("equipmentStatus", Q.mold.equipmentStatus)//
				.put("moldStatus", Q.mold.equipmentStatus)//
				.put("utilizationRate", Q.mold.utilizationRate)//
				.put("approvedCycleTime", Q.mold.contractedCycleTime)//
				.put("averageCycleTime", Q.mold.weightedAverageCycleTime)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc(), Q.supplier.name.asc());
		QueryResults<DemCplDetails> results = query.fetchResults();
		return results;
	}

}
