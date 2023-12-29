package com.emoldino.api.analysis.resource.composite.proana.service.mold;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaMoldsGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaMoldsItem;
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
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Service
@Transactional
public class ProAnaMoldService {

	public Page<ProAnaMoldsItem> get(ProAnaMoldsGetIn input, Pageable pageable) {
		JPAQuery<ProAnaMoldsItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProAnaMoldsItem.class, Q.mold))//
				.from(Q.mold);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.mold.counter.equipmentCode.startsWith("EM"));
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			if (!ValueUtils.isNumber(input.getQuery()) || input.getQuery().contains(".")) {
				filter.and(//
						Q.mold.equipmentCode.containsIgnoreCase(input.getQuery())//
								.or(Q.mold.toolingStatus.stringValue().equalsIgnoreCase(input.getQuery()))//
				);
			} else {
				filter.and(//
						Q.mold.id.stringValue().eq(input.getQuery())//
								.or(Q.mold.equipmentCode.containsIgnoreCase(input.getQuery()))//
								.or(Q.mold.utilizationRate.stringValue().containsIgnoreCase(input.getQuery()))//
				);
			}
		}
		query.where(filter);

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.mold.id)//
				.put("moldCode", Q.mold.equipmentCode)//
				.put("utilizationRate", Q.mold.utilizationRate)//
				.put("approvedCycleTime", Q.mold.contractedCycleTime)//
				.put("averageCycleTime", Q.mold.weightedAverageCycleTime)//
				.put("toolingStatus", Q.mold.toolingStatus)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc());

		QueryResults<ProAnaMoldsItem> results = query.fetchResults();

		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

}
