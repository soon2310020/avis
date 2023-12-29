package com.emoldino.api.integration.resource.composite.inftol.repository;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolDailySummaryIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolGetIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolSummaryItem;
import com.emoldino.api.integration.resource.composite.inftol.enumeration.InfTolGroupBy;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
public class InfTolRepository {

    public Page<InfTolItem> findAll(InfTolGetIn input, Pageable pageable) {
        JPQLQuery<InfTolItem> query = BeanUtils.get(JPAQueryFactory.class)//
                .select(Projections.constructor(InfTolItem.class, Q.mold))//
                .from(Q.mold);

        Set<EntityPathBase<?>> join = new HashSet<>();
        QueryUtils.applyMoldFilter(query, join, null);

        BooleanBuilder filter = new BooleanBuilder();
        QueryUtils.eq(filter, Q.mold.equipmentCode, input.getToolingId());
        QueryUtils.contains(filter, Q.mold.equipmentCode, input.getToolingIdContains());
        if (!ObjectUtils.isEmpty(input.getUpdateDateTimeGoe())) {
            Instant instant = DateUtils2.toInstant(input.getUpdateDateTimeGoe(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
            filter.and(Q.mold.updatedAt.goe(instant));
        }
        query.where(filter);

        Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
                .put("toolingId", Q.mold.equipmentCode)//
                .put("updateDateTime", Q.mold.updatedAt)//
                .build();

        QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc());

        QueryResults<InfTolItem> results = query.fetchResults();
        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    public Page<InfTolSummaryItem> findDailySummary(InfTolDailySummaryIn input, Pageable pageable) {
        LogicUtils.assertNotEmpty(input.getDate(), "date");

        JPQLQuery<InfTolSummaryItem> query;
        if (input.getGroupBy() == null) {
            query = BeanUtils.get(JPAQueryFactory.class)//
                    .select(Projections.constructor(InfTolSummaryItem.class, //
                            Expressions.asString("ALL").as("sensorId"), //
                            Expressions.asString("ALL").as("toolingId"), //
                            Q.statistics.shotCount.sum(), //
                            Q.statistics.ct.multiply(0.1d).avg(), //
                            Q.statistics.uptimeSeconds.sum()//
                    ));
        } else if (InfTolGroupBy.toolingId.equals(input.getGroupBy())) {
            query = BeanUtils.get(JPAQueryFactory.class)//
                    .select(Projections.constructor(InfTolSummaryItem.class, //
                            Q.statistics.moldCode.coalesce("NOT_MATCHED").as("toolingId"), //
                            Q.statistics.shotCount.sum(), //
                            Q.statistics.ct.multiply(0.1d).avg(), //
                            Q.statistics.uptimeSeconds.sum()//
                    ));
        } else {
            query = BeanUtils.get(JPAQueryFactory.class)//
                    .select(Projections.constructor(InfTolSummaryItem.class, //
                            Q.statistics.ci.coalesce("NOT_MATCHED").as("sensorId"), //
                            Q.statistics.moldCode.max().coalesce("NOT_MATCHED").as("toolingId"), //
                            Q.statistics.shotCount.sum(), //
                            Q.statistics.ct.multiply(0.1d).avg(), //
                            Q.statistics.uptimeSeconds.sum()//
                    ));
        }
        query.from(Q.statistics);

        BooleanBuilder filter = new BooleanBuilder()//
                .and(Q.statistics.day.eq(StringUtils.replace(input.getDate(), "-", "")))//
                .and(Q.statistics.ct.gt(10))//
                .and(Q.statistics.ct.lt(999));
        QueryUtils.in(filter, Q.statistics.moldCode, input.getToolingId());

        query.where(filter);

        OrderSpecifier<?> defaultOrder;
        if (input.getGroupBy() == null) {
            defaultOrder = Q.statistics.id.asc();
        } else if (InfTolGroupBy.toolingId.equals(input.getGroupBy())) {
            query.groupBy(Q.statistics.moldCode);
            defaultOrder = Q.statistics.moldCode.asc();
        } else {
            query.groupBy(Q.statistics.ci);
            defaultOrder = Q.statistics.ci.asc();
        }

        Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
                .put("toolingId", Q.statistics.moldCode)//
                .put("sensorId", Q.statistics.ci)//
                .build();

        QueryUtils.applyPagination(query, pageable, fieldMap, defaultOrder);

        QueryResults<InfTolSummaryItem> results = query.fetchResults();
        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

}
