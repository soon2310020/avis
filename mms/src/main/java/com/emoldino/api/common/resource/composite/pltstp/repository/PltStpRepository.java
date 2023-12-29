package com.emoldino.api.common.resource.composite.pltstp.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpGetIn;
import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.ObjectType;
import saleson.model.TabTable;

@Repository
public class PltStpRepository {

	public long count(PltStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.LOCATION, input.getTabName());

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.location.id.countDistinct())//
				.from(Q.location);

		applyFilter(query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	public Page<PltStpItem> findAll(PltStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.LOCATION, input.getTabName());

		JPQLQuery<PltStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(PltStpItem.class, //
						Q.location, //
						Expressions.asNumber(//
								JPAExpressions//
										.select(Q.terminal.id.count())//
										.from(Q.terminal)//
										.where(Q.terminal.locationId.eq(Q.location.id))//
						).as("terminalCount"), //
						Expressions.asString(//
								JPAExpressions//
										.select(Q.area.name.min())//
										.from(Q.area)//
										.where(Q.area.locationId.eq(Q.location.id))//
						).as("firstAreaName")//
				))//
				.distinct()//
				.from(Q.location);

		Set<EntityPathBase<?>> join = new HashSet<>();
		applyFilter(query, join, input, batchin, tabTable);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("name", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.put("companyName", Q.location.company.name)//
				.put("enabled", Q.location.enabled)//
				.put("address", Q.location.address)//
				.put("timeZoneId", Q.location.timeZoneId)//
				.put("memo", Q.location.memo)//
				.put("terminalCount", Expressions.numberPath(Long.class, "terminalCount"))//
				.put("firstAreaName", Expressions.stringPath("firstAreaName"))//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.location.name.asc());
		QueryResults<PltStpItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, PltStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, Q.location.id);

		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applyPlantFilter(query, join, input.getFilterCode());
		} else {
			QueryUtils.applyPlantDisabledFilter(query, join, input.getFilterCode());
		}
		QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.location.companyId).and(QueryUtils.isCompany()));

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, Q.company.companyType, input.getCompanyType());
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
					Q.location.name.containsIgnoreCase(input.getQuery())//
							.or(Q.location.locationCode.containsIgnoreCase(input.getQuery()))//
							.or(Q.company.name.containsIgnoreCase(input.getQuery()))//
							.or(Q.company.companyCode.containsIgnoreCase(input.getQuery()))//
			);
		}
		QueryUtils.in(filter, Q.location.id, input.getId());

		QueryUtils.applyBatchFilter(filter, batchin, Q.location.id);

		query.where(filter);
	}

}
