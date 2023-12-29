package com.emoldino.api.asset.resource.composite.wgttolina.repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.api.asset.resource.composite.wgttolina.dto.WgtTolInaGetIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class WgtTolInaRepository {

	public long countByGtAndLoe(WgtTolInaGetIn input, Instant min, Instant max) {
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Q.mold.countDistinct())//
				.from(Q.mold);

		if (min == null) {
			min = DateUtils2.toInstant("20100101", DatePattern.yyyyMMdd, Zone.GMT);
		}

		Set<EntityPathBase<?>> join = new HashSet<>();
		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		BooleanBuilder filter = new BooleanBuilder()//
				.and(Q.mold.toolingStatus.notIn(ToolingStatus.NO_SENSOR, ToolingStatus.SENSOR_DETACHED))//
				.and(QueryUtils.gtAndLoe(new BooleanBuilder(), Q.mold.lastShotAt, min, max).and(Q.mold.lastShotAt.isNotNull())//
						.or(QueryUtils.gtAndLoe(new BooleanBuilder(), Q.mold.operatedStartAt, min, max).and(Q.mold.operatedStartAt.isNotNull().and(Q.mold.lastShotAt.isNull())))//
				);
		query.where(filter);

		return query.fetchOne();
	}

//	private JPQLQuery<Long> toQuery(WgtTolInaGetIn input, Instant min, Instant max) {
//		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
//				.select(Q.mold.count())//
//				.from(Q.mold);
//
//		Set<EntityPathBase<?>> join = new HashSet<>();
//		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());
//
//		BooleanBuilder filter = new BooleanBuilder()//
//				.or(QueryUtils.gtAndLoe(new BooleanBuilder(), Q.mold.lastShotAt, min, max).and(Q.mold.lastShotAt.isNotNull()))//
//				.or(QueryUtils.gtAndLoe(new BooleanBuilder(), Q.mold.activatedAt, min, max).and(Q.mold.activatedAt.isNotNull().and(Q.mold.lastShotAt.isNull())))//
//		;
//		query.where(filter);
//
//		return query;
//	}

}
