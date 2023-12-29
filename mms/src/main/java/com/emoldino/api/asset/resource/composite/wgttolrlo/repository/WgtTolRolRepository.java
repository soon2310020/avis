package com.emoldino.api.asset.resource.composite.wgttolrlo.repository;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.emoldino.api.asset.resource.base.mold.enumeration.RelocationType;
import com.emoldino.api.asset.resource.composite.wgttolrlo.dto.WgtTolRolGetIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class WgtTolRolRepository {

	public long count(WgtTolRolGetIn input) {
		LogicUtils.assertNotNull(input.getMoldLocationStatus(), "moldLocationStatus");

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.moldLocation.id.countDistinct())//
				.from(Q.moldLocation);

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.join(query, join, Q.mold, () -> Q.mold.id.eq(Q.moldLocation.moldId));

		QueryUtils.setAutoDistinct(false);
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		query.where(new BooleanBuilder()//
				.and(Q.moldLocation.relocationType.eq(RelocationType.PLANT))//
				.and(Q.moldLocation.moldLocationStatus.in(input.getMoldLocationStatus()))//
				.and(Q.moldLocation.createdAt.gt(DateUtils2.getInstant().minus(Duration.ofDays(365L))))//
		);

		return query.fetchFirst();
	}

}
