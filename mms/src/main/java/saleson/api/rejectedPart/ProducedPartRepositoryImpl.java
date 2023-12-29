package saleson.api.rejectedPart;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.rejectedPart.payload.MachineCountEntryRecord;
import saleson.api.rejectedPart.payload.RejectRatePayload;
import saleson.common.enumeration.Frequent;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.RejectPartEntryRecordItemDTO;
import saleson.dto.RejectRateOEEDTO;
import saleson.model.QLocation;
import saleson.model.QMachine;
import saleson.model.QMachineOee;
import saleson.model.QMold;
import saleson.model.rejectedPartRate.ProducedPart;
import saleson.model.rejectedPartRate.QProducedPart;
import saleson.model.rejectedPartRate.QRejectedPartDetails;

import java.util.List;

public class ProducedPartRepositoryImpl extends QuerydslRepositorySupport implements ProducedPartRepositoryCustom  {
    public ProducedPartRepositoryImpl() {
        super(ProducedPart.class);
    }

    @Override
    public List<RejectRateOEEDTO> findRejectRateOEE(RejectRatePayload payload, Pageable pageable, String startHour, String endHour) {
        JPQLQuery query = getQuerySearchOEE(payload, pageable, startHour, endHour);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        return query.fetch();
    }

    @Override
    public long countRejectRateOEE(RejectRatePayload payload, Pageable pageable, String startHour, String endHour) {
        JPQLQuery query = getQuerySearchOEE(payload, pageable, startHour, endHour);
        return query.fetchCount();
    }

    @Override
    public long countEntryRecord(Long machineId, String day, String startHour, String endHour, Pageable pageable) {
        JPQLQuery<RejectPartEntryRecordItemDTO>  query = getQueryEntryRecord(machineId, day, startHour, endHour, pageable);
        return query.fetchCount();
    }

    @Override
    public List<RejectPartEntryRecordItemDTO> findRejectPartEntryRecord(Long machineId, String day, Pageable pageable, String startHour, String endHour) {
        JPQLQuery<RejectPartEntryRecordItemDTO> query = getQueryEntryRecord(machineId, day, startHour, endHour, pageable);
        return query.limit(pageable.getPageSize()).offset(pageable.getOffset()).fetch();
    }

    @Override
    public List<Long> findAllMachineIdRejectRateOEE(RejectRatePayload payload, String startHour, String endHour) {
        QProducedPart producedPart = QProducedPart.producedPart;
        QMachineOee machineOee = QMachineOee.machineOee;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(producedPart.frequent.eq(Frequent.HOURLY))
                .and(machineOee.periodType.eq(Frequent.HOURLY))
                .and(producedPart.hour.between(startHour, endHour))
                .and(producedPart.totalRejectedAmount.gt(0));

        if (CollectionUtils.isNotEmpty(payload.getMachineIdList())) {
            builder.and(machineOee.machineId.in(payload.getMachineIdList()));
        }

        JPQLQuery<Long> query = from(machineOee)
                .innerJoin(producedPart).on(producedPart.hour.eq(machineOee.hour).and(machineOee.moldId.eq(producedPart.moldId)))
                .where(builder).select(machineOee.machineId).distinct();
        return query.fetch();
    }

    @Override
    public List<MachineCountEntryRecord> countMachineRejectRateOEE(List<Long> machineIdList, String day, String startHour, String endHour, Pageable pageable) {
        QProducedPart producedPart = QProducedPart.producedPart;
        QMachineOee machineOee = QMachineOee.machineOee;
        QRejectedPartDetails rejectedPartDetails = QRejectedPartDetails.rejectedPartDetails;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(producedPart.frequent.eq(Frequent.HOURLY))
                .and(machineOee.periodType.eq(Frequent.HOURLY))
                .and(machineOee.machineId.in(machineIdList))
                .and(producedPart.totalRejectedAmount.gt(0))
                .and(producedPart.hour.between(startHour, endHour));

        JPQLQuery<MachineCountEntryRecord> query = from(producedPart).innerJoin(machineOee)
                .on(producedPart.hour.eq(machineOee.hour)
                        .and(producedPart.day.eq(producedPart.day)).and(machineOee.moldId.eq(producedPart.moldId)))
                .leftJoin(rejectedPartDetails).on(rejectedPartDetails.producedPartId.eq(producedPart.id).and(rejectedPartDetails.id.in(JPAExpressions.select(rejectedPartDetails.id.min()).from(rejectedPartDetails).groupBy(rejectedPartDetails.producedPartId))))
                .where(builder)
                .groupBy(machineOee.machineId)
                .select(Projections.constructor(MachineCountEntryRecord.class, machineOee.machineId, producedPart.id.count()));

        return query.fetch();
    }

    private JPQLQuery<RejectPartEntryRecordItemDTO> getQueryEntryRecord(Long machineId, String day, String startHour, String endHour, Pageable pageable) {
        QProducedPart producedPart = QProducedPart.producedPart;
        QMachineOee machineOee = QMachineOee.machineOee;
        QRejectedPartDetails rejectedPartDetails = QRejectedPartDetails.rejectedPartDetails;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(producedPart.frequent.eq(Frequent.HOURLY))
                .and(machineOee.periodType.eq(Frequent.HOURLY))
                .and(machineOee.machineId.eq(machineId))
                .and(producedPart.totalRejectedAmount.gt(0))
                .and(producedPart.hour.between(startHour, endHour));


            OrderSpecifier orderBy = null;
            if (pageable != null && pageable.getSort().isSorted()) {
                String property = pageable.getSort().get().findFirst().get().getProperty();
                Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
                switch (property) {
                    case "shiftNumber":
                    case "period": {
                        StringExpression expression = producedPart.hour;
                        if (isAsc) {
                            orderBy = expression.asc();
                        } else {
                            orderBy = expression.desc();
                        }
                        break;
                    }
                    case "reportedBy": {
                        StringExpression expression = producedPart.reportedBy.name;
                        if (isAsc) {
                            orderBy = expression.asc();
                        } else {
                            orderBy = expression.desc();
                        }
                        break;
                    }
                    case "totalRejectedAmount": {
                        NumberExpression expression = producedPart.totalRejectedAmount;
                        if (isAsc) {
                            orderBy = expression.asc();
                        } else {
                            orderBy = expression.desc();
                        }
                        break;
                    }
                    case "rejectedPartDetails": {
                        StringExpression expression = rejectedPartDetails.reason;
                        if (isAsc) {
                            orderBy = expression.asc();
                        } else {
                            orderBy = expression.desc();
                        }
                        break;
                    }
                }
            }



        JPQLQuery<RejectPartEntryRecordItemDTO> query = from(producedPart).innerJoin(machineOee)
                .on(producedPart.hour.eq(machineOee.hour)
                        .and(producedPart.day.eq(producedPart.day)).and(machineOee.moldId.eq(producedPart.moldId)))
                .leftJoin(rejectedPartDetails).on(rejectedPartDetails.producedPartId.eq(producedPart.id).and(rejectedPartDetails.id.in(JPAExpressions.select(rejectedPartDetails.id.min()).from(rejectedPartDetails).groupBy(rejectedPartDetails.producedPartId))))
                .where(builder)
                .select(Projections.constructor(RejectPartEntryRecordItemDTO.class, producedPart));

        if(orderBy != null) {
            query.orderBy(orderBy);
        }
        return query;
    }

    private JPQLQuery getQuerySearchOEE(RejectRatePayload payload, Pageable pageable, String startHour, String endHour) {
        QProducedPart producedPart = new QProducedPart("dayProducedPart");
        QProducedPart hourProducedPart = new QProducedPart("hourProducedPart");
        QMold mold = QMold.mold;
        QLocation location = QLocation.location;
        QMachine machine = QMachine.machine;
        QMachineOee machineOee = QMachineOee.machineOee;
        QMachineOee hourMachineOee = new QMachineOee("hourMachineOee");
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(producedPart.moldId.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
        if(CollectionUtils.isNotEmpty(payload.getLocationIdList())) {
            builder.and(location.id.in(payload.getLocationIdList()));
        }

        if(CollectionUtils.isNotEmpty(payload.getMachineIdList())) {
            builder.and(machine.id.in(payload.getMachineIdList()));
        }

        builder.and(producedPart.frequent.eq(Frequent.HOURLY))
                .and(machineOee.periodType.eq(Frequent.HOURLY))
                .and(producedPart.hour.between(startHour, endHour))
                .and(producedPart.totalRejectedAmount.gt(0));

//        if (payload.getFrequent() != null)
//            builder.and(producedPart.frequent.eq(payload.getFrequent()));
//        else
//        builder.and(producedPart.frequent.eq(Frequent.DAILY));
//                .and(producedPart.day.eq(payload.getDay()));

//        if (!StringUtils.isEmpty(payload.getDay()))
//            builder.and(producedPart.day.eq(payload.getDay()));
//        if (!StringUtils.isEmpty(payload.getWeek()))
//            builder.and(producedPart.week.eq(payload.getWeek()));
//        if (!StringUtils.isEmpty(payload.getMonth()))
//            builder.and(producedPart.month.eq(payload.getMonth()));
//        if (!StringUtils.isEmpty(payload.getHour()))
//            builder.and(producedPart.hour.eq(payload.getHour()));



        SimpleExpression<Integer> totalProducedAmountExpression = new CaseBuilder()
                .when(JPAExpressions.select(hourProducedPart.totalProducedAmount.sum()).from(hourProducedPart).innerJoin(hourMachineOee).on(hourProducedPart.hour.eq(hourMachineOee.hour).and(hourProducedPart.moldId.eq(hourMachineOee.moldId))).where(hourProducedPart.hour.between(startHour, endHour).and(hourProducedPart.moldId.eq(producedPart.moldId)).and(hourMachineOee.machineId.eq(machine.id)).and(hourProducedPart.frequent.eq(Frequent.HOURLY))).gt(0))
                .then(JPAExpressions.select(hourProducedPart.totalProducedAmount.sum()).from(hourProducedPart).innerJoin(hourMachineOee).on(hourProducedPart.hour.eq(hourMachineOee.hour).and(hourProducedPart.moldId.eq(hourMachineOee.moldId))).where(hourProducedPart.hour.between(startHour, endHour).and(hourProducedPart.moldId.eq(producedPart.moldId)).and(hourMachineOee.machineId.eq(machine.id)).and(hourProducedPart.frequent.eq(Frequent.HOURLY))))
                .otherwise(0);



        SimpleExpression<Integer> totalRejectedAmountExpression = new CaseBuilder()
                .when(JPAExpressions.select(hourProducedPart.totalRejectedAmount.sum()).from(hourProducedPart).innerJoin(hourMachineOee).on(hourProducedPart.hour.eq(hourMachineOee.hour).and(hourProducedPart.moldId.eq(hourMachineOee.moldId))).where(hourProducedPart.hour.between(startHour, endHour).and(hourProducedPart.moldId.eq(producedPart.moldId)).and(hourMachineOee.machineId.eq(machine.id)).and(hourProducedPart.frequent.eq(Frequent.HOURLY))).gt(0))
                .then(JPAExpressions.select(hourProducedPart.totalRejectedAmount.sum()).from(hourProducedPart).innerJoin(hourMachineOee).on(hourProducedPart.hour.eq(hourMachineOee.hour).and(hourProducedPart.moldId.eq(hourMachineOee.moldId))).where(hourProducedPart.hour.between(startHour, endHour).and(hourProducedPart.moldId.eq(producedPart.moldId)).and(hourMachineOee.machineId.eq(machine.id)).and(hourProducedPart.frequent.eq(Frequent.HOURLY))))
                .otherwise(0);


        OrderSpecifier orderBy = null;

        if (pageable.getSort().isSorted()) {
            String property  = pageable.getSort().get().findFirst().get().getProperty();
            Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
            switch (property) {
                case "machineCode": {
                    StringExpression expression = machine.machineCode;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                    break;
                }
                case "locationCode": {
                    StringExpression expression = location.locationCode;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                    break;
                }
                case "totalProducedAmount": {
                    SimpleExpression expression = totalProducedAmountExpression;
                    if (isAsc) {
                        orderBy = expression.count().asc();
                    } else {
                        orderBy = expression.count().desc();
                    }
                    break;
                }
                case "totalRejectedAmount": {
                    SimpleExpression expression = totalRejectedAmountExpression;
                    if (isAsc) {
                        orderBy = expression.count().asc();
                    } else {
                        orderBy = expression.count().desc();
                    }
                    break;
                }
                case "rejectedRate": {
                    NumberExpression expression = producedPart.rejectedRate;
                    if (isAsc) {
                        orderBy = expression.asc();
                    } else {
                        orderBy = expression.desc();
                    }
                    break;
                }
                case "yieldRate": {
                    NumberExpression expression = producedPart.rejectedRate;
                    if (isAsc) {
                        orderBy = expression.desc();
                    } else {
                        orderBy = expression.asc();
                    }
                    break;
                }
                case "moldList": {
                    StringExpression expression = mold.equipmentCode;
                    if (isAsc) {
                        orderBy = expression.desc();
                    } else {
                        orderBy = expression.asc();
                    }
                    break;
                }
            }
        }

        JPQLQuery query = from(machine)
                .leftJoin(machineOee).on(machineOee.machineId.eq(machine.id).and(machineOee.hour.between(startHour, endHour)))
                .leftJoin(mold).on(machineOee.moldId.eq(mold.id))
                .leftJoin(producedPart).on(producedPart.moldId.eq(mold.id).and(producedPart.hour.eq(machineOee.hour)))
                .leftJoin(location).on(machine.locationId.eq(location.id))
                .where(builder)
                .select(Projections.constructor(RejectRateOEEDTO.class, producedPart.id, machine.machineCode, location.locationCode, totalProducedAmountExpression,
                        totalRejectedAmountExpression, producedPart.rejectedRateStatus, machine.id, location.id));
        if(orderBy != null) {
            query.orderBy(orderBy);
        }
        return query;
    }
}
