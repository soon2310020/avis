package com.emoldino.api.supplychain.resource.base.product.repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductDemandByTime;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductDemandByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartDemandByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartPlanByWeeksGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductPartStatGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductSupplierGetIn;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStat;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.QueryUtils.QueryRelation;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.CompanyType;
import saleson.model.Company;
import saleson.model.Part;

@Repository
public class ProductRepository {

	public long countParts(ProductPartGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.part.countDistinct())//
				.from(Q.part)//
				.distinct();
		applyPartFilter(query, input);
		return query.fetchOne();
	}

	public List<Part> findAllParts(ProductPartGetIn input, Pageable pageable) {
		JPQLQuery<Part> query = BeanUtils.get(JPAQueryFactory.class)//
				.selectFrom(Q.part)//
				.distinct();

		applyPartFilter(query, input);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("partCode", Q.part.partCode)//
				.put("partName", Q.part.name)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.part.partCode.asc());

		return query.fetch();
	}

	private void applyPartFilter(JPQLQuery<?> query, ProductPartGetIn input) {
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyPartFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();

		if (input.getBrandId() != null) {
			QueryUtils.joinBrandByPart(query, join);
			filter.and(Q.brand.id.eq(input.getBrandId()));
		}

		if (input.getProductId() != null) {
			QueryUtils.joinProductByPart(query, join);
			filter.and(Q.product.id.eq(input.getProductId()));
		}

		QueryUtils.eq(filter, Q.part.id, input.getPartId());

		if (!ObjectUtils.isEmpty(input.getSupplierId())) {
			QueryUtils.joinSupplierByPart(query, join);
			filter.and(Q.supplier.id.in(input.getSupplierId()));
		}

		if (input.isProducibleOnly()) {
			applyProducibleFilter(query, join, filter, input.getDate());
		}

		query.where(filter);
	}

	public List<Company> findAllSuppliersByPart(Long partId, ProductSupplierGetIn input, Pageable pageable) {
		LogicUtils.assertNotNull(partId, "partId");

		JPQLQuery<Company> query = BeanUtils.get(JPAQueryFactory.class)//
				.selectFrom(Q.supplier)//
				.distinct();
		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyCompanyFilter(query, join, input.getFilterCode(), CompanyType.SUPPLIER, CompanyType.IN_HOUSE);

		BooleanBuilder filter = new BooleanBuilder();

		QueryUtils.joinPartBySupplier(query, join);
		filter.and(Q.part.id.eq(partId));

		if (input.isProducibleOnly()) {
			applyProducibleFilter(query, join, filter, input.getDate());
		}

		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("supplierCode", Q.supplier.companyCode)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, Q.supplier.name.asc());

		return query.fetch();
	}

	private void applyProducibleFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, BooleanBuilder filter, String date) {
		Instant instant;
		if (ObjectUtils.isEmpty(date)) {
			instant = DateUtils2.getInstant();
			date = DateUtils2.format(instant, DatePattern.yyyy_MM_dd, Zone.GMT);
		} else {
			if (date.contains("-")) {
				instant = DateUtils2.toInstant(date, DatePattern.yyyy_MM_dd, Zone.GMT);
			} else {
				instant = DateUtils2.toInstant(date, DatePattern.yyyyMMdd, Zone.GMT);
				date = DateUtils2.format(instant, DatePattern.yyyy_MM_dd, Zone.GMT);
			}
		}

		QueryUtils.joinMoldByPart(query, join);
		filter.and(//
				(Q.mold.counter.installedAt.isNotNull().and(Q.mold.counter.installedAt.isNotEmpty()).and(Q.mold.counter.installedAt.loe(date)))//
						.or((Q.mold.counter.installedAt.isNull().or(Q.mold.counter.installedAt.isEmpty())).and(Q.mold.counter.activatedAt.loe(instant)))//
		);
		filter.and(Q.mold.maxCapacityPerWeek.gt(0));
		filter.and(Q.mold.supplierCompanyId.isNotNull());
	}

	public List<ProductDemandByTime> findDemands(ProductDemandByWeeksGetIn input) {
		JPQLQuery<ProductDemandByTime> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProductDemandByTime.class, //
						Expressions.asEnum(TimeScale.WEEK), //
						Q.productDemand.periodValue, //
						Q.productDemand.quantity.sum().coalesce(0L)//
				))//
				.from(Q.productDemand)//
				.groupBy(Q.productDemand.periodValue);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = toFilter(query, join, input);
		query.where(filter);
		query.limit(3000);
		List<ProductDemandByTime> demands = query.fetch();
		return demands;
	}

	private static BooleanBuilder toFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, ProductDemandByWeeksGetIn input) {
		LogicUtils.assertNotNull(input.getProductId(), "productId");
		LogicUtils.assertNotEmpty(input.getWeeks(), "weeks");
		QueryUtils.join(query, join, Q.product, () -> QueryUtils.isProduct().and(Q.product.id.eq(Q.productDemand.productId)));
		QueryUtils.applyProductFilter(query, join, input.getFilterCode());
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.productDemand.productId, input.getProductId());
		QueryUtils.eq(filter, Q.productDemand.periodType, "WEEKLY");
		QueryUtils.in(filter, Q.productDemand.periodValue, input.getWeeks());
		return filter;
	}

	public List<ProductDemandByTime> findPartDemands(ProductPartDemandByWeeksGetIn input) {
		BooleanBuilder filter = toFilter(input);
		JPQLQuery<ProductDemandByTime> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProductDemandByTime.class, //
						Expressions.asEnum(TimeScale.WEEK), //
						Q.partDemand.periodValue, //
						Q.partDemand.quantity.sum().coalesce(0L)//
				))//
				.from(Q.partDemand)//
				.where(filter)//
				.groupBy(Q.partDemand.periodValue);
		query.limit(3000);
		List<ProductDemandByTime> demands = query.fetch();
		return demands;
	}

	public Long findPartDemandQty(ProductPartDemandByWeeksGetIn input) {
		BooleanBuilder filter = toFilter(input);
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.partDemand.quantity.sum().coalesce(0L))//
				.from(Q.partDemand)//
				.where(filter);
		long demand = query.fetchOne();
		return demand;
	}

	private static BooleanBuilder toFilter(ProductPartDemandByWeeksGetIn input) {
		LogicUtils.assertNotNull(input.getPartId(), "partId");
		LogicUtils.assertNotEmpty(input.getWeeks(), "weeks");
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.partDemand.productId, input.getProductId());
		QueryUtils.eq(filter, Q.partDemand.partId, input.getPartId());
		QueryUtils.eq(filter, Q.partDemand.periodType, "WEEKLY");
		QueryUtils.in(filter, Q.partDemand.periodValue, input.getWeeks());
		return filter;
	}

	public List<ProductDemandByTime> findPartPlans(ProductPartPlanByWeeksGetIn input) {
		JPQLQuery<ProductDemandByTime> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProductDemandByTime.class, //
						Expressions.asEnum(TimeScale.WEEK), //
						Q.partPlan.periodValue, //
						Q.partPlan.quantity.sum().coalesce(0L)//
				))//
				.from(Q.partPlan)//
				.groupBy(Q.partPlan.periodValue);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = toFilter(query, join, input);
		query.where(filter);
		query.limit(3000);
		List<ProductDemandByTime> demands = query.fetch();
		return demands;
	}

	public Long findPartPlanQty(ProductPartPlanByWeeksGetIn input) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.partPlan.quantity.sum().coalesce(0L))//
				.from(Q.partPlan);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = toFilter(query, join, input);
		query.where(filter);
		long demand = query.fetchOne();
		return demand;
	}

	private static BooleanBuilder toFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, ProductPartPlanByWeeksGetIn input) {
		LogicUtils.assertNotNull(input.getPartId(), "partId");
		LogicUtils.assertNotEmpty(input.getWeeks(), "weeks");
		QueryUtils.join(query, join, Q.part, () -> QueryUtils.isPart().and(Q.part.id.eq(Q.partPlan.partId)));
		QueryUtils.applyPartFilter(query, join, input.getFilterCode());
		QueryUtils.joinSupplierByPart(query, join);
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.partPlan.productId, input.getProductId());
		QueryUtils.eq(filter, Q.partPlan.partId, input.getPartId());
		QueryUtils.in(filter, Q.partPlan.supplierId, input.getSupplierId());
		QueryUtils.eq(filter, Q.partPlan.periodType, "WEEKLY");
		QueryUtils.in(filter, Q.partPlan.periodValue, input.getWeeks());
		return filter;
	}

	public Long findProdQty(ProductPartStatGetIn input, TimeSetting timeSetting) {
		List<PartStat> list = toPartStatSummaryQuery(input, timeSetting).fetch();
		return ObjectUtils.isEmpty(list) ? 0L : list.get(0).getProduced();
	}

	public List<PartStat> findAllPartStatsGroupByPartAndTime(ProductPartStatGetIn input, TimeSetting timeSetting) {
		JPQLQuery<PartStat> query = toPartStatSummaryQuery(input, timeSetting);
		if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
			query.groupBy(Q.partStat.partId, Q.partStat.month)//
					.orderBy(Q.partStat.partId.asc()).orderBy(Q.partStat.month.asc());
		} else {
			query.groupBy(Q.partStat.productId, Q.partStat.partId, Q.partStat.day)//
					.orderBy(Q.partStat.partId.asc()).orderBy(Q.partStat.day.asc());
		}
		return query.fetch();
	}

	public List<PartStat> findAllPartStatsGroupByPartAndMold(ProductPartStatGetIn input, TimeSetting timeSetting) {
		JPQLQuery<PartStat> query = toPartStatSummaryQuery(input, timeSetting)//
				.groupBy(Q.partStat.partId, Q.partStat.moldId)//
				.orderBy(Q.partStat.partId.asc());
		return query.fetch();
	}

	public List<PartStat> findAllPartStatsGroupByPartAndDay(ProductPartStatGetIn input, TimeSetting timeSetting) {
		JPQLQuery<PartStat> query = toPartStatSummaryQuery(input, timeSetting)//
				.groupBy(Q.partStat.partId, Q.partStat.day)//
				.orderBy(Q.partStat.partId.asc(), Q.partStat.day.asc());
		return query.fetch();
	}

	public List<PartStat> findAllPartStatsGroupByPartAndWeek(ProductPartStatGetIn input, TimeSetting timeSetting) {
		JPQLQuery<PartStat> query = toPartStatSummaryQuery(input, timeSetting)//
				.groupBy(Q.partStat.partId, Q.partStat.week)//
				.orderBy(Q.partStat.partId.asc(), Q.partStat.week.asc());
		return query.fetch();
	}

	public List<PartStat> findAllPartStatsGroupBySupplierAndDay(ProductPartStatGetIn input, TimeSetting timeSetting) {
		JPQLQuery<PartStat> query = toPartStatSummaryQuery(input, timeSetting)//
				.groupBy(Q.partStat.supplierId, Q.partStat.day)//
				.orderBy(Q.partStat.supplierId.asc(), Q.partStat.day.asc());
		return query.fetch();
	}

	public List<PartStat> findAllPartStatsGroupBySupplierAndWeek(ProductPartStatGetIn input, TimeSetting timeSetting) {
		JPQLQuery<PartStat> query = toPartStatSummaryQuery(input, timeSetting)//
				.groupBy(Q.partStat.supplierId, Q.partStat.week)//
				.orderBy(Q.partStat.supplierId.asc(), Q.partStat.week.asc());
		return query.fetch();
	}

	public static JPQLQuery<PartStat> toPartStatSummaryQuery(ProductPartStatGetIn input, TimeSetting timeSetting) {
		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.partStat.productId, input.getProductId());
		QueryUtils.eq(filter, Q.partStat.partId, input.getPartId());
		QueryUtils.eq(filter, Q.partStat.moldId, input.getMoldId());
		QueryUtils.in(filter, Q.partStat.supplierId, input.getSupplierId());

		if (TimeScale.WEEK.equals(timeSetting.getTimeScale())) {
			filter.and(Q.partStat.week.eq(timeSetting.getTimeValue()));
		} else if (TimeScale.MONTH.equals(timeSetting.getTimeScale())) {
			filter.and(Q.partStat.month.eq(timeSetting.getTimeValue()));
		} else if (TimeScale.QUARTER.equals(timeSetting.getTimeScale())) {
			filter.and(Q.partStat.month.in(DateUtils2.toMonths(timeSetting)));
		} else if (TimeScale.YEAR.equals(timeSetting.getTimeScale())) {
			filter.and(Q.partStat.year.eq(timeSetting.getTimeValue()));
		} else if (TimeScale.CUSTOM.equals(timeSetting.getTimeScale())) {
			filter.and(Q.partStat.day.between(timeSetting.getFromDate(), timeSetting.getToDate()));
		}

		if (!ObjectUtils.isEmpty(input.getMonth())) {
			filter.and(Q.partStat.month.in(input.getMonth()));
		} else if (!ObjectUtils.isEmpty(input.getWeek())) {
			filter.and(Q.partStat.week.in(input.getWeek()));
		}

		JPQLQuery<PartStat> query = BeanUtils.get(JPAQueryFactory.class).from(Q.partStat)//
				.where(filter)//
				.select(Projections.constructor(PartStat.class, //
						Q.partStat.productId.max(), //
						Q.partStat.partId.max(), //
						Q.partStat.supplierId.max(), //
						Q.partStat.locationId.max(), //
						Q.partStat.moldId.max(), //
						Q.partStat.week.max().as("week"), //
						Q.partStat.year.max().as("year"), //
						Q.partStat.month.max().as("month"), //
						Q.partStat.day.max().as("day"), //
						Q.partStat.produced.sum().as("produced"), //
						Q.partStat.producedVal.sum().as("producedVal"), //
						Q.partStat.dailyCapacity.sum().as("dailyCapacity")//
				));

//		Set<EntityPathBase<?>> join = new HashSet<>();

		if (!ObjectUtils.isEmpty(input.getFilterCode())) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder().field(Q.partStat.supplierId).build())//
					.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder().field(Q.partStat.mold.toolMakerCompanyId).build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder().field(Q.partStat.locationId).build())//
					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder().field(Q.partStat.moldId).build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder().field(Q.partStat.productId).build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder().field(Q.partStat.partId).build())//
//					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
//							.field(Q.supplier.id)//
//							.join(() -> QueryUtils.isSupplier().and(Q.supplier.id.eq(Q.partStat.supplierId)))//
//							.build())//
//					.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder()//
//							.field(Q.toolmaker.id)//
//							.join(() -> QueryUtils.isToolmaker().and(Q.toolmaker.id.eq(Q.partStat.mold.toolMakerCompanyId)))//
//							.build())//
//					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
//							.field(Q.location.id)//
//							.join(() -> QueryUtils.isLocation().and(Q.location.id.eq(Q.partStat.locationId)))//
//							.build())//
//					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
//							.field(Q.mold.id)//
//							.join(() -> QueryUtils.isMold().and(Q.mold.id.eq(Q.partStat.moldId)))//
//							.build())//
//					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder()//
//							.field(Q.product.id)//
//							.join(() -> QueryUtils.isProduct().and(Q.product.id.eq(Q.partStat.productId)))//
//							.build())//
//					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
//							.field(Q.part.id)//
//							.join(() -> QueryUtils.isPart().and(Q.part.id.eq(Q.partStat.partId)))//
//							.build())//
					.build();
			QueryUtils.applyMasterFilter(query, input.getFilterCode(), relations);
		}

		return query;
	}

}
