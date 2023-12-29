package com.emoldino.api.analysis.resource.composite.ovrutl.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetOut.OvrUtlDetail;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetOut.OvrUtlItem;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.common.resource.base.option.dto.UtilizationConfig;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.CompanyType;
import saleson.model.QMold;

@Repository
public class OvrUtlRepository {

	public Page<OvrUtlItem> findAll(OvrUtlGetIn input, UtilizationConfig config, Pageable pageable) {
		String sort = QueryUtils.getFirstSortProperty(pageable);

		JPQLQuery<OvrUtlItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(OvrUtlItem.class, //
						Q.supplier.id.as("supplierId"), //
						Q.supplier.name.max().as("supplierName"), //
						Q.supplier.companyCode.max().as("supplierCode"), //
						!"plants".equals(sort) ? Expressions.asString("")
								: Expressions.asString(//
										JPAExpressions//
												.select(Q.location.name.min())//
												.from(Q.location)//
												.where(Q.location.companyId.eq(Q.supplier.id).and(Q.location.id.eq(Q.mold.locationId)))//
								).as("firstPlantName"), //
						Q.mold.count().as("totalMoldCount"), //								
						new CaseBuilder() //
								.when(Q.mold.utilizationRate.lt(config.getLow()).or(Q.mold.utilizationRate.isNull())).then(1) //
								.otherwise(0).sum().longValue().as("lowMoldCount"),
						new CaseBuilder() //
								.when(Q.mold.utilizationRate.goe(config.getLow()).and(Q.mold.utilizationRate.lt(config.getMedium()))).then(1) //
								.otherwise(0).sum().longValue().as("mediumMoldCount"),
						new CaseBuilder() //
								.when(Q.mold.utilizationRate.goe(config.getMedium()).and(Q.mold.utilizationRate.lt(config.getHigh()))).then(1) //
								.otherwise(0).sum().longValue().as("highMoldCount"),
						new CaseBuilder() //
								.when(Q.mold.utilizationRate.gt(config.getHigh())).then(1) //
								.otherwise(0).sum().longValue().as("prolongedMoldCount")))
				.from(Q.supplier);

		Set<EntityPathBase<?>> join = new HashSet<>();

		QueryUtils.applyCompanyFilter(query, join, input.getFilterCode(), CompanyType.SUPPLIER, CompanyType.IN_HOUSE);
		QueryUtils.joinMoldBySupplier(query, join);

		query.groupBy(Q.supplier.id);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("supplierName", Q.supplier.name)//
				.put("supplierCode", Q.supplier.companyCode)//
				.put("plants", Expressions.stringPath("firstPlantName"))//
				.put("totalMoldCount", Expressions.numberPath(Long.class, "totalMoldCount"))//
				.put("lowMoldCount", Expressions.numberPath(Long.class, "lowMoldCount"))//
				.put("mediumMoldCount", Expressions.numberPath(Long.class, "mediumMoldCount"))//
				.put("highMoldCount", Expressions.numberPath(Long.class, "highMoldCount"))//
				.put("prolongedMoldCount", Expressions.numberPath(Long.class, "prolongedMoldCount"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.supplier.name.asc());
		QueryResults<OvrUtlItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public Page<OvrUtlDetail> findAllDetails(OvrUtlDetailsGetIn input, Pageable pageable) {
		QueryUtils.setAutoDistinct(false);

		// TO-DO: Part Sorting (Part consist of list. So sorting of part standard is not defined.)
		JPQLQuery<OvrUtlDetail> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(OvrUtlDetail.class, //
						Q.mold.id.as("moldId"), //
						Q.mold.equipmentCode.as("moldCode"), //
						Q.mold.lastShot.coalesce(0).as("accumShotCount"), //
						Q.mold.designedShot.coalesce(0).as("designedShotCount"), //
						Q.mold.utilizationRate.coalesce(0d)))//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();
		if (input.getSupplierId() != null) {
			filter.and(Q.supplier.id.eq(input.getSupplierId()));
		}

		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("accumShotCount", Q.mold.lastShot)//
				.put("designedShotCount", Q.mold.designedShot)//
				.put("utilizationRate", Q.mold.utilizationRate)//
				.put("utilizationStatus", Q.mold.utilizationRate)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.supplier.name.asc());
		QueryResults<OvrUtlDetail> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

}
