package com.emoldino.api.production.resource.composite.alrcyctim.repository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimGetIn;
import com.emoldino.api.production.resource.composite.alrcyctim.dto.AlrCycTimItem;
import com.emoldino.api.production.resource.composite.alrcyctim.enumeration.AlrCycTimTab;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.enumeration.AlertTab;
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
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.CounterStatus;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.util.SecurityUtils;

@Repository
public class AlrCycTimRepository {
    public long count(AlrCycTimGetIn input) {
        JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
                .select(Q.moldCycleTime.id.countDistinct())//
                .from(Q.moldCycleTime);

        applyFilter(query, new HashSet<>(), input, null);

        return query.fetchCount();
    }

    public Page<AlrCycTimItem> findAll(AlrCycTimGetIn input, BatchIn batchin, Pageable pageable) {
        NumberExpression<Double> variance = Q.moldCycleTime.cycleTime.subtract(Q.moldCycleTime.contractedCycleTime.multiply(100).divide(Q.moldCycleTime.contractedCycleTime));

        JPQLQuery<AlrCycTimItem> query = BeanUtils.get(JPAQueryFactory.class)//
                .select(Projections.constructor(AlrCycTimItem.class,//
                        Q.moldCycleTime.id, //

                        Q.moldCycleTime.notificationStatus, // Status
                        Q.moldCycleTime.notificationAt, //
                        Q.moldCycleTime.contractedCycleTime, //

                        Q.moldCycleTime.cycleTime, //
                        Q.moldCycleTime.cycleTimeStatus, //
                        variance.as("variance"), //

                        Q.mold.id, //
                        Q.mold.equipmentCode.as("moldCode"), //
                        Q.mold.toolingStatus, //
                        Q.mold.operatingStatus, // OP

                        new CaseBuilder()//
                                .when(Q.counter.equipmentStatus.isNull())//
                                .then(CounterStatus.NOT_INSTALLED.name())//
                                .when(Q.counter.equipmentStatus.eq(EquipmentStatus.DETACHED))//
                                .then(CounterStatus.DETACHED.name())//
                                .otherwise(CounterStatus.INSTALLED.name())//
                                .as("sensorStatus"), //

                        Q.company.id, //
                        Q.company.name, //
                        Q.company.companyCode, //
                        Q.company.companyType, //

                        Q.location.id, //
                        Q.location.locationCode, //
                        Q.location.name //
                )) //
                .distinct() //
                .from(Q.moldCycleTime);

        Set<EntityPathBase<?>> join = new HashSet<>();
        applyFilter(query, join, input, batchin);

        Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
                .put("id", Q.moldCycleTime.id)//

                .put("alertStatus", Q.moldCycleTime.notificationStatus)//
                .put("creationDateTime", Q.moldCycleTime.notificationAt)//
                .put("contractedCycleTime", Q.moldCycleTime.contractedCycleTime)//

                .put("cycleTime", Q.moldCycleTime.cycleTime)//
                .put("cycleTimeStatus", Q.moldCycleTime.cycleTimeStatus)//
                .put("variance", Expressions.numberPath(Double.class, "variance"))//

                .put("moldCode", Q.mold.equipmentCode)//
                .put("counterCode", Q.counter.equipmentCode)//
                .put("toolingStatus", Q.mold.toolingStatus)//
                .put("operatingStatus", Q.mold.operatingStatus)//
                .put("sensorStatus", Expressions.stringPath("sensorStatus"))//

                .put("companyName", Q.company.name)//
                .put("companyCode", Q.company.companyCode)//
                .put("companyType", Q.company.companyType)//

                .put("locationName", Q.location.name)//
                .put("locationCode", Q.location.locationCode)//
                .build();

        QueryUtils.applyPagination(query, pageable, fieldMap, Q.moldCycleTime.createdAt.desc());
        QueryResults<AlrCycTimItem> results = query.fetchResults();
        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    private void applyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, AlrCycTimGetIn input, BatchIn batchin) {
        QueryUtils.join(query, join, Q.mold, () -> Q.moldCycleTime.moldId.eq(Q.mold.id));
        QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

        QueryUtils.leftJoin(query, join, Q.counter, () -> Q.counter.id.eq(Q.mold.counterId));
        QueryUtils.leftJoin(query, join, Q.company, () -> Q.company.id.eq(Q.mold.companyId));
        QueryUtils.leftJoin(query, join, Q.location, () -> Q.location.id.eq(Q.mold.locationId));
        QueryUtils.join(query, join, Q.userAlert, () -> Q.userAlert.periodType.eq(Q.moldCycleTime.periodType)
                .and(Q.userAlert.user.id.eq(SecurityUtils.getUserId()).and(Q.userAlert.alertType.eq(AlertType.CYCLE_TIME))));

        BooleanBuilder filter = new BooleanBuilder();

        filter.and(Q.mold.operatingStatus.isNotNull());
        if (SpecialAlertType.L2.equals(input.getSpecialAlertType())) {
            filter.and(Q.moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L2));

        } else if (SpecialAlertType.L1L2.equals(input.getSpecialAlertType())) {
            filter.and(Q.moldCycleTime.cycleTimeStatus.in(CycleTimeStatus.OUTSIDE_L1, CycleTimeStatus.OUTSIDE_L2));
        }
        if (AlertTab.HISTORY_LOG.getTitle().equals(input.getTabName())) {
            filter.and(Q.moldCycleTime.notificationStatus.eq(NotificationStatus.CONFIRMED));
        } else {
            filter.and(Q.moldCycleTime.notificationStatus.eq(NotificationStatus.ALERT));
            if (AlrCycTimTab.OUTSIDE_L1.getTitle().equals(input.getTabName())) {
                filter.and(Q.moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L1));
            } else if (AlrCycTimTab.OUTSIDE_L2.getTitle().equals(input.getTabName())) {
                filter.and(Q.moldCycleTime.cycleTimeStatus.eq(CycleTimeStatus.OUTSIDE_L2));
            }
        }

        QueryUtils.in(filter, Q.moldCycleTime.id, input.getId());

        if (!ObjectUtils.isEmpty(input.getQuery())) {
            filter.and(//
                    Q.mold.equipmentCode.contains(input.getQuery())//
                            .or(Q.location.name.contains(input.getQuery()))//
                            .or(Q.location.locationCode.contains(input.getQuery()))//
                            .or(Q.company.companyCode.contains(input.getQuery()))//
                            .or(Q.company.name.contains(input.getQuery()))//
            );
        }

        QueryUtils.applyBatchFilter(filter, batchin, Q.moldCycleTime.id);
        query.where(filter);
    }

}
