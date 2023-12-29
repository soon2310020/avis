package com.emoldino.api.supplychain.resource.composite.demcpl.service.demand;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.base.product.dto.PartPlanYearly;
import com.emoldino.api.supplychain.resource.base.product.dto.PartSupplier;
import com.emoldino.api.supplychain.resource.base.product.repository.productdemand.ProductDemand;
import com.emoldino.api.supplychain.resource.base.product.repository.productdemand.ProductDemandRepository;
import com.emoldino.api.supplychain.resource.base.product.service.partplan.PartPlanService;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplPartDemandsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProductDemand;
import com.emoldino.api.supplychain.resource.composite.demcpl.dto.DemCplProductDemandsGetIn;
import com.emoldino.api.supplychain.resource.composite.demcpl.util.DemCplUtils;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.model.Category;

@Service
@Transactional
public class DemCplDemandService {

	public Page<DemCplProductDemand> getProductDemands(DemCplProductDemandsGetIn input, Pageable pageable) {
		ValueUtils.assertTimeSetting(input, DemCplUtils.TIME_SCALE_DEMAND_SUPPORTED);
		int finalWeek = DateUtils2.getFinalWeekNo(input.getTimeValue());

		QueryResults<Category> results;
		{
			JPQLQuery<Category> query = BeanUtils.get(JPAQueryFactory.class)//
					.selectFrom(Q.product);
			Set<EntityPathBase<?>> join = new HashSet<>();
			BooleanBuilder filter = new BooleanBuilder();

			if (!ObjectUtils.isEmpty(input.getQuery())) {
				String searchWord = input.getQuery();
				filter.and(//
						Q.product.name.containsIgnoreCase(searchWord)//
				);
			}
			query.where(filter);

			QueryUtils.applyProductFilter(query, join, input.getFilterCode());
			Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
					.put("name", Q.product.name)//
					.build();
			QueryUtils.applyPagination(query, pageable, fieldMap, Q.product.name.asc());
			results = query.fetchResults();
		}

		List<DemCplProductDemand> list = results.getResults().stream()//
				.map(product -> {
					Long defaultDemand = ValueUtils.toLong(product.getWeeklyProductionDemand(), 0L);
					Map<String, ProductDemand> demands = getProductDemands(product.getId(), input.getTimeValue());
					DemCplProductDemand demand = new DemCplProductDemand();
					demand.setId(product.getId());
					demand.setName(product.getName());
					for (int i = 1; i <= finalWeek; i++) {
						String week = input.getTimeValue() + ValueUtils.pad(i, 2, "left", "0");
						Long weeklyDemand = demands.containsKey(week) ? ValueUtils.toLong(demands.get(week).getQuantity(), defaultDemand) : defaultDemand;
						try {
							ReflectionUtils.getSetter(demand, "w" + i).invoke(demand, weeklyDemand);
						} catch (RuntimeException e) {
							throw e;
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
					return demand;
				})//
				.collect(Collectors.toList());
		return new PageImpl<>(list, pageable, results.getTotal());
	}

	public void postProductDemands(ListIn<DemCplProductDemand> input, TimeSetting timeSetting) {
		ValueUtils.assertTimeSetting(timeSetting, DemCplUtils.TIME_SCALE_DEMAND_SUPPORTED);
		int finalWeek = DateUtils2.getFinalWeekNo(timeSetting.getTimeValue());

		if (!ObjectUtils.isEmpty(input.getContent())) {
			input.getContent().forEach(demand -> {
				for (int i = 1; i <= finalWeek; i++) {
					String week = timeSetting.getTimeValue() + ValueUtils.pad(i, 2, "left", "0");
					Long weeklyDemand;
					try {
						weeklyDemand = (Long) ReflectionUtils.getGetter(demand, "w" + i).invoke(demand);
					} catch (RuntimeException e) {
						throw e;
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					if (weeklyDemand != null) {
						ProductUtils.saveDemand(demand.getId(), week, weeklyDemand);
					}
				}
			});
		}
	}

	public Page<PartPlanYearly> getPartDemands(DemCplPartDemandsGetIn input, Pageable pageable) {
		ValueUtils.assertTimeSetting(input, DemCplUtils.TIME_SCALE_DEMAND_SUPPORTED);

		QueryResults<PartSupplier> results;
		{
			JPQLQuery<PartSupplier> query = BeanUtils.get(JPAQueryFactory.class)//
					.select(Projections.constructor(PartSupplier.class, //
							Q.part, //
							Q.supplier//
					))//
					.distinct()//
					.from(Q.part);

			Set<EntityPathBase<?>> join = new HashSet<>();
			QueryUtils.joinSupplierByPart(query, join);

			BooleanBuilder filter = new BooleanBuilder();
			if (!ObjectUtils.isEmpty(input.getQuery())) {
				String searchWord = input.getQuery();
				filter.and(//
						Q.part.partCode.containsIgnoreCase(searchWord)//
								.or(Q.part.name.containsIgnoreCase(searchWord))//
				);
			}
			query.where(filter);

			QueryUtils.applyPartFilter(query, join, input.getFilterCode());
			Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
					.put("partName", Q.part.name)//
					.put("supplierName", Q.supplier.name)//
					.build();
			QueryUtils.applyPagination(query, pageable, fieldMap, Q.part.name.asc(), Q.supplier.name.asc());
			results = query.fetchResults();
		}

		List<PartPlanYearly> list = BeanUtils.get(PartPlanService.class).getYearly(results.getResults(), input.getTimeValue());
		return new PageImpl<>(list, pageable, results.getTotal());
	}

	public void postPartDemands(ListIn<PartPlanYearly> input, TimeSetting timeSetting) {
		ValueUtils.assertTimeSetting(timeSetting, DemCplUtils.TIME_SCALE_DEMAND_SUPPORTED);
		BeanUtils.get(PartPlanService.class).saveYearly(input.getContent(), timeSetting.getTimeValue());
	}

	private static Map<String, ProductDemand> getProductDemands(Long productId, String year) {
		LogicUtils.assertNotNull(productId, "productId");
		LogicUtils.assertNotNull(year, "year");
		Map<String, ProductDemand> demands = new LinkedHashMap<>();
		BeanUtils.get(ProductDemandRepository.class).findAll(new BooleanBuilder()//
				.and(Q.productDemand.productId.eq(productId))//
				.and(Q.productDemand.periodType.eq("WEEKLY"))//
				.and(Q.productDemand.periodValue.startsWith(year))//
				.and(Q.productDemand.quantity.isNotNull())//
		).forEach(item -> demands.put(item.getPeriodValue(), item));
		return demands;
	}

}
