package saleson.api.machineDowntimeAlert;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.querydsl.core.types.dsl.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.code.repository.codedata.QCodeData;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.batch.payload.IdData;
import saleson.api.machineDowntimeAlert.payload.MachineDowntimeAlertData;
import saleson.api.machineDowntimeAlert.payload.SearchMachineDowntimePayload;
import saleson.common.enumeration.MachineAvailabilityType;
import saleson.common.enumeration.MachineDowntimeAlertStatus;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.*;

public class MachineDowntimeAlertRepositoryImpl extends QuerydslRepositorySupport implements MachineDowntimeAlertRepositoryCustom {
    public MachineDowntimeAlertRepositoryImpl() {
        super(MachineDowntimeAlert.class);
    }

    @Override
    public List<MachineDowntimeAlertData> getMachineDowntime(SearchMachineDowntimePayload payload, Pageable pageable) {
        JPQLQuery query = getQueryMachineDowntime(payload, pageable);

        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public long countMachineDowntime(SearchMachineDowntimePayload payload, Pageable pageable) {
        JPQLQuery query = getQueryMachineDowntime(payload, pageable);
        return query.fetchCount();
    }

    @Override
    public List<MachineDowntimeAlert> findByMachineInAndDowntimeOverlapped(List<Machine> machines, Instant start, Instant end) {
        QMachineDowntimeAlert machineDowntimeAlert = QMachineDowntimeAlert.machineDowntimeAlert;
        BooleanBuilder builder = new BooleanBuilder();
        //hide negative downtime
        builder.and(machineDowntimeAlert.endTime.isNull().or(machineDowntimeAlert.endTime.gt(machineDowntimeAlert.startTime)));
        builder.and(machineDowntimeAlert.machine.in(machines));
        builder.and(machineDowntimeAlert.startTime.between(start, end)
                .or(machineDowntimeAlert.endTime.between(start, end))
                .or(machineDowntimeAlert.startTime.loe(start).and(machineDowntimeAlert.endTime.isNull()))
                .or(machineDowntimeAlert.startTime.loe(start).and(machineDowntimeAlert.endTime.goe(end))));

        JPQLQuery query = from(machineDowntimeAlert).where(builder);
        return query.fetch();
    }

    @Override
    public List<MachineDowntimeAlert> findByMachineInAndDowntimeTypeAndDowntimeOverlapped(List<Machine> machines, Instant start, Instant end, MachineAvailabilityType downtimeType) {
        QMachineDowntimeAlert machineDowntimeAlert = QMachineDowntimeAlert.machineDowntimeAlert;
        BooleanBuilder builder = new BooleanBuilder();
        //hide negative downtime
        builder.and(machineDowntimeAlert.endTime.isNull().or(machineDowntimeAlert.endTime.gt(machineDowntimeAlert.startTime)));
        builder.and(machineDowntimeAlert.machine.in(machines));
        builder.and(machineDowntimeAlert.downtimeType.eq(downtimeType));
        builder.and(machineDowntimeAlert.startTime.between(start, end)
                .or(machineDowntimeAlert.endTime.between(start, end))
                .or(machineDowntimeAlert.startTime.loe(start).and(machineDowntimeAlert.endTime.isNull()))
                .or(machineDowntimeAlert.startTime.loe(start).and(machineDowntimeAlert.endTime.goe(end))));

        JPQLQuery query = from(machineDowntimeAlert).where(builder);
        return query.fetch();
    }

    private JPQLQuery getQueryMachineDowntime(SearchMachineDowntimePayload payload,Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMachineDowntimeAlert machineDowntimeAlert = QMachineDowntimeAlert.machineDowntimeAlert;
        QMachine machine = QMachine.machine;
        QLocation location = QLocation.location;
        QMachineDowntimeReason machineDowntimeReason = QMachineDowntimeReason.machineDowntimeReason;
        QUser reportBy = new QUser("reportBy");
        QUser confirmBy = new QUser("confirmBy");
        QCodeData codeData =  QCodeData.codeData;
        QMold mold = QMold.mold;
        OrderSpecifier orderBy = null;
        BooleanBuilder builder = new BooleanBuilder();
        //hide negative downtime
        builder.and(machineDowntimeAlert.endTime.isNull().or(machineDowntimeAlert.endTime.gt(machineDowntimeAlert.startTime)));

        if (StringUtils.isNotBlank(payload.getTab())) {
            if ("HISTORY".equals(payload.getTab())) {
                orderBy = machineDowntimeAlert.createdAt.desc();
                builder.and(machineDowntimeAlert.downtimeStatus.in(Arrays.asList(MachineDowntimeAlertStatus.CONFIRMED, MachineDowntimeAlertStatus.UNCONFIRMED)));
            } else if("HISTORY_MOBILE".equals(payload.getTab())) {
                orderBy = machineDowntimeAlert.createdAt.desc();
                builder.and(machineDowntimeAlert.downtimeStatus.eq(MachineDowntimeAlertStatus.UNCONFIRMED));
            } else if("ALERT".equals(payload.getTab())) {
                builder.and(machineDowntimeAlert.downtimeStatus.in(Arrays.asList(MachineDowntimeAlertStatus.REGISTERED, MachineDowntimeAlertStatus.DOWNTIME)));
            } else if("OEE".equals(payload.getTab())) {
                builder.and(machineDowntimeAlert.downtimeStatus.in(Arrays.asList(MachineDowntimeAlertStatus.REGISTERED, MachineDowntimeAlertStatus.DOWNTIME, MachineDowntimeAlertStatus.CONFIRMED, MachineDowntimeAlertStatus.UNCONFIRMED)));
            }
        }

        if (CollectionUtils.isNotEmpty(payload.getStatus())) {
            builder.and(machineDowntimeAlert.downtimeStatus.in(payload.getStatus()));
        }

        if (CollectionUtils.isNotEmpty(payload.getMachineIdList())) {
            builder.and(machine.id.in(payload.getMachineIdList()));
        }

        if(CollectionUtils.isNotEmpty(payload.getLocationIdList())) {
            builder.and(location.id.in(payload.getLocationIdList()));
        }

        if(payload.getFromDate() != null && payload.getToDate() != null) {
                Instant fromDate = DateUtils2.toInstant(payload.getFromDate(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(payload.getLocationIdList() != null &&
                        payload.getLocationIdList().size() > 0 ? payload.getLocationIdList().get(0) : null));
                Instant toDate = DateUtils2.toInstant(payload.getToDate(), DateUtils2.DatePattern.yyyyMMddHH, LocationUtils.getZoneIdByLocationId(payload.getLocationIdList() != null &&
                        payload.getLocationIdList().size() > 0 ? payload.getLocationIdList().get(0) : null));

                builder.and(machineDowntimeAlert.startTime.between(fromDate, toDate)
                        .or(machineDowntimeAlert.endTime.between(fromDate, toDate))
                        .or(machineDowntimeAlert.startTime.loe(fromDate).and(machineDowntimeAlert.endTime.isNull()))
                        .or(machineDowntimeAlert.startTime.loe(fromDate).and(machineDowntimeAlert.endTime.goe(toDate))));
        }

        //TODO unknown logic
        if(CollectionUtils.isNotEmpty(payload.getShiftNumberList())) {

        }

        if (payload.getLastAlert() != null && payload.getLastAlert()) {
            builder.and(machineDowntimeAlert.latest.isTrue());
        }

        if(StringUtils.isNotBlank(payload.getQuery())) {
            String searchText = '%'+payload.getQuery()+'%';
            builder.and(machine.machineCode.like(searchText).or(location.locationCode.like(searchText).or(location.name.like(searchText))));
        }
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(machine.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(location.company.id.in(SecurityUtils.getUserId())));
		}


        if("OEE".equals(payload.getTab()) || StringUtils.isBlank(payload.getTab())) {
            DateTimeExpression<Instant> expression = machineDowntimeAlert.createdAt;
            orderBy = expression.desc();
        } else {
            if (pageable.getSort().isSorted()) {

                String property  = pageable.getSort().get().findFirst().get().getProperty();
                Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
                if ("machineCode".equals(property)) {
                    StringExpression expression = machine.machineCode;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("startTime".equals(property)) {
                    DateTimeExpression<Instant> expression = machineDowntimeAlert.startTime;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("endTime".equals(property)) {
                    DateTimeExpression<Instant> expression = machineDowntimeAlert.endTime;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("downtimeStatus".equals(property)) {
                    EnumExpression<MachineDowntimeAlertStatus> expression = machineDowntimeAlert.downtimeStatus;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("location".equals(property)) {
                    StringExpression expression = location.locationCode;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("downtimeType".equals(property)) {
                    EnumExpression<MachineAvailabilityType> expression = machineDowntimeAlert.downtimeType;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("confirmBy".equals(property)) {
                    StringExpression expression = confirmBy.name;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("reportedBy".equals(property)) {
                    StringExpression expression = reportBy.name;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("downtimeReason".equals(property)) {
                    StringExpression expression = codeData.title;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                } else if ("duration".equals(property)) {
                    NumberExpression<Long> durationExpression = new CaseBuilder()
                            .when(machineDowntimeAlert.endTime.isNull())
                            .then(Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", machineDowntimeAlert.startTime, Instant.now()))
                            .when(machineDowntimeAlert.endTime.after(Instant.now()))
                            .then(Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", machineDowntimeAlert.startTime, Instant.now()))
                            .otherwise(Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1})", machineDowntimeAlert.startTime, machineDowntimeAlert.endTime));
                    if (isAsc) {
                        orderBy = durationExpression.asc();
                    } else {
                        orderBy = durationExpression.desc();
                    }
                }

            }
        }


        JPQLQuery query = from(machineDowntimeAlert)
                .leftJoin(machine).on(machine.id.eq(machineDowntimeAlert.machineId))
                .leftJoin(machineDowntimeReason).on(machineDowntimeReason.machineDowntimeAlertId.eq(machineDowntimeAlert.id)
                        .and(machineDowntimeReason.id.eq(JPAExpressions.select(machineDowntimeReason.id.max())
                                        .from(machineDowntimeReason).where(machineDowntimeReason.machineDowntimeAlertId.eq(machineDowntimeAlert.id))
                                        .orderBy(machineDowntimeReason.startTime.desc()))))
                .leftJoin(codeData).on(codeData.id.eq(machineDowntimeReason.codeDataId))
                .leftJoin(location).on(location.id.eq(machine.locationId))
                .leftJoin(confirmBy).on(confirmBy.id.eq(machineDowntimeAlert.confirmedBy))
                .leftJoin(reportBy).on(reportBy.id.eq(machineDowntimeAlert.reportedBy))
                .leftJoin(mold).on(machineDowntimeAlert.moldId.eq(mold.id))
                .where(builder)
                .select(Projections.constructor(MachineDowntimeAlertData.class, machineDowntimeAlert.id, machine.machineCode,
                        location.locationCode, location.name, machineDowntimeAlert.startTime, machineDowntimeAlert.endTime,
                        machineDowntimeAlert.downtimeStatus, machineDowntimeAlert.updatedAt, machineDowntimeAlert.downtimeType,
                        confirmBy, reportBy, machine.id, machineDowntimeAlert.createdAt,
                        mold));
        if (orderBy != null) {
            query.orderBy(orderBy);
        }
        return query;
    }

    @Override
    public List<IdData> getAllIds(SearchMachineDowntimePayload payload, Pageable pageable) {
        QMachineDowntimeAlert table = QMachineDowntimeAlert.machineDowntimeAlert;
        JPQLQuery query = getQueryMachineDowntime(payload, pageable);
        query.select(Projections.constructor(IdData.class, table.machineId, table.id));
        return query.fetch();
    }
}
