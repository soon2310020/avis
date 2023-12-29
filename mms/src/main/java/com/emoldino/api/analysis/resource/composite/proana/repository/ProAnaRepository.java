package com.emoldino.api.analysis.resource.composite.proana.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaPartsItem;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class ProAnaRepository {

	public List<ProAnaPartsItem> findAllPartsByMoldAndDay(Long moldId, String day) {
		LogicUtils.assertNotNull(moldId, "moldId");
		LogicUtils.assertNotEmpty(day, "day");

		JPAQuery<ProAnaPartsItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ProAnaPartsItem.class, //
						Q.part.id, //
						Q.part.name, //
						Q.part.partCode)//
				).distinct()//
				.from(Q.part)//
				.where(Q.mold.id.eq(moldId).and(Q.statistics.day.eq(day)));

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.joinMoldByPart(query, join);
		QueryUtils.join(query, join, Q.statistics, () -> Q.statistics.moldId.eq(Q.mold.id));
		QueryUtils.join(query, join, Q.statisticsPart, () -> Q.statisticsPart.statisticsId.eq(Q.statistics.id).and(Q.statisticsPart.partId.eq(Q.part.id)));

		return query.fetch();
	}

}
