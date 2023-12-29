package com.emoldino.api.analysis.resource.composite.tolrpt.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptGetIn;
import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.model.Part;

@Repository
public class TolRptRepository {

	public Page<TolRptItem> findAll(TolRptGetIn input, BatchIn batchin, Pageable pageable) {
		JPQLQuery<TolRptItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(TolRptItem.class, //
						Q.mold.id, //
						Q.mold.equipmentCode, //
						Q.mold.toolingStatus, //
						Q.location.id, //
						Q.location.name, //
						Q.location.locationCode, //
						Q.area.name, //
						Q.mold.utilizationRate, //
						Q.mold.lastShot, //
						Q.mold.designedShot, //
						Q.mold.lastShotAt, //
						Q.mold.lastCycleTime.divide(10d), //
						Q.mold.weightedAverageCycleTime.divide(10d), //
						Q.supplier.id, //
						Q.supplier.name, //
						Q.supplier.companyCode, //
						Q.mold.productionDays, //
						Q.mold.totalCavities, //
						Q.mold.currentMachineTonnage, //
						Q.mold.quotedMachineTonnage, //
						Q.mold.weightRunner //
				))//
				.from(Q.mold);

		//Set Join
		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin);

		// Set Parameter
		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>() //
				.put("moldCode", Q.mold.equipmentCode)//
				.put("locationName", Q.location.name)//
				.build();

		// Query Result
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc());
		QueryResults<TolRptItem> queryResults = query.fetchResults();

		// Set Tooling Custom Field
		Page<TolRptItem> page = new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());

		return page;
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TolRptGetIn input, BatchIn batchin) {
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		QueryUtils.leftJoinSupplierByMold(query, join);
		QueryUtils.leftJoinLocationByMold(query, join);
		QueryUtils.leftJoinAreaByMold(query, join);

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.applyBatchFilter(filter, batchin, Q.mold.id);
		query.where(filter);
	}

	public List<Part> findPartsByMoldId(Long moldId) {
		JPQLQuery<Part> query = BeanUtils.get(JPAQueryFactory.class)//
				.selectFrom(Q.part);

		//Set Join
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.leftJoinMoldByPart(query, join);

		query.where(Q.mold.id.eq(moldId));
		query.orderBy(Q.part.name.asc());

		return query.fetch();
	}

}
