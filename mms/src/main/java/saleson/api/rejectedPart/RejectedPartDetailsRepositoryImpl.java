package saleson.api.rejectedPart;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.common.enumeration.Frequent;
import saleson.common.enumeration.RejectedRateStatus;
import saleson.common.util.DateUtils;
import saleson.model.QMachineMoldMatchingHistory;
import saleson.model.QMachineOee;
import saleson.model.data.rejectedPartRate.RejectedPartBreakDownData;
import saleson.model.rejectedPartRate.QProducedPart;
import saleson.model.rejectedPartRate.QRejectedPartDetails;
import saleson.model.rejectedPartRate.RejectedPartDetails;

import java.util.List;

public class RejectedPartDetailsRepositoryImpl extends QuerydslRepositorySupport implements RejectedPartDetailsRepositoryCustom {
    public RejectedPartDetailsRepositoryImpl() {
        super(RejectedPartDetails.class);
    }

    @Override
    public List<RejectedPartBreakDownData> findBreakDownByMoldIdAndPartIdAndPeriod(Long moldId, Long partId, String startDate, String endDate){
        QRejectedPartDetails rejectedPartDetails = QRejectedPartDetails.rejectedPartDetails;

        JPQLQuery query = from(rejectedPartDetails)
                .where(rejectedPartDetails.producedPart.moldId.eq(moldId)
                    .and(rejectedPartDetails.producedPart.partId.eq(partId))
                    .and(rejectedPartDetails.producedPart.day.between(startDate, endDate))
                    .and(rejectedPartDetails.producedPart.rejectedRateStatus.eq(RejectedRateStatus.COMPLETED)))
                .groupBy(rejectedPartDetails.reason)
                .select(Projections.constructor(RejectedPartBreakDownData.class, rejectedPartDetails.reason, rejectedPartDetails.rejectedAmount.sum()));
        return query.fetch();
    }

    @Override
    public List<RejectedPartBreakDownData> findBreakDownByMoldIdAndPartIdAndFrequent(Long moldId, Long partId, Frequent frequent, String selectedTime) {
        QRejectedPartDetails rejectedPartDetails = QRejectedPartDetails.rejectedPartDetails;
        BooleanBuilder builder = new BooleanBuilder();
        if (frequent.equals(Frequent.WEEKLY)) {
            builder.and(rejectedPartDetails.producedPart.frequent.eq(Frequent.WEEKLY).and(rejectedPartDetails.producedPart.week.eq(selectedTime)));
        } else if (frequent.equals(Frequent.MONTHLY)) {
            builder.and(rejectedPartDetails.producedPart.frequent.eq(Frequent.MONTHLY).and(rejectedPartDetails.producedPart.month.eq(selectedTime)));
        } else {
            builder.and(rejectedPartDetails.producedPart.frequent.eq(Frequent.DAILY).and(rejectedPartDetails.producedPart.day.eq(selectedTime)));
        }
        JPQLQuery query = from(rejectedPartDetails)
                .where(rejectedPartDetails.producedPart.moldId.eq(moldId)
                        .and(rejectedPartDetails.producedPart.partId.eq(partId))
                        .and(rejectedPartDetails.producedPart.rejectedRateStatus.eq(RejectedRateStatus.COMPLETED))
                        .and(builder))
                .groupBy(rejectedPartDetails.reason)
                .select(Projections.constructor(RejectedPartBreakDownData.class, rejectedPartDetails.reason, rejectedPartDetails.rejectedAmount.sum()));
        return query.fetch();
    }

    @Override
    public List<RejectedPartBreakDownData> findBreakDownByMoldIdAndPartIdAndFrequentAndPeriod(Long moldId, Long partId, Frequent frequent, String start, String end) {
        QRejectedPartDetails rejectedPartDetails = QRejectedPartDetails.rejectedPartDetails;
        BooleanBuilder builder = new BooleanBuilder();
        if (frequent.equals(Frequent.WEEKLY)) {
            builder.and(rejectedPartDetails.producedPart.frequent.eq(Frequent.WEEKLY).and(rejectedPartDetails.producedPart.week.between(start, end)));
        } else if (frequent.equals(Frequent.MONTHLY)) {
            builder.and(rejectedPartDetails.producedPart.frequent.eq(Frequent.MONTHLY).and(rejectedPartDetails.producedPart.month.between(start, end)));
        } else {
            builder.and(rejectedPartDetails.producedPart.frequent.eq(Frequent.DAILY).and(rejectedPartDetails.producedPart.day.between(start, end)));
        }
        JPQLQuery query = from(rejectedPartDetails)
                .where(rejectedPartDetails.producedPart.moldId.eq(moldId)
                        .and(rejectedPartDetails.producedPart.partId.eq(partId))
                        .and(rejectedPartDetails.producedPart.rejectedRateStatus.eq(RejectedRateStatus.COMPLETED))
                        .and(builder))
                .groupBy(rejectedPartDetails.reason)
                .select(Projections.constructor(RejectedPartBreakDownData.class, rejectedPartDetails.reason, rejectedPartDetails.rejectedAmount.sum()));
        return query.fetch();
    }

    @Override
    public List<RejectedPartDetails> getRejectedPartDetailsByMachineIdAndDay(Long machineId, String day, Pageable pageable, String startHour, String endHour) {
        JPQLQuery<RejectedPartDetails> query = getQueryRejectedPartDetailsByMachineIdAndDay(machineId, day, startHour, endHour);
        query.limit(pageable.getPageSize()).offset(pageable.getOffset());
        return query.fetch();
    }

    @Override
    public long countRejectedPartDetailsByMachineIdAndDay(Long machineId, String day, String startHour, String endHour) {
        JPQLQuery<RejectedPartDetails> query = getQueryRejectedPartDetailsByMachineIdAndDay(machineId, day, startHour, endHour);
        return query.fetch().size();
    }

    private JPQLQuery<RejectedPartDetails> getQueryRejectedPartDetailsByMachineIdAndDay(Long machineId, String day, String startHour, String endHour){

        QRejectedPartDetails rejectedPartDetails = QRejectedPartDetails.rejectedPartDetails;
        QProducedPart producedPart = QProducedPart.producedPart;
        QMachineOee machineOee = QMachineOee.machineOee;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(machineOee.machineId.eq(machineId))
                .and(producedPart.frequent.eq(Frequent.HOURLY))
                .and(machineOee.periodType.eq(Frequent.HOURLY))
                .and(producedPart.hour.between(startHour, endHour));
        return from(rejectedPartDetails)
                .innerJoin(producedPart).on(rejectedPartDetails.producedPartId.eq(producedPart.id))
                .innerJoin(machineOee).on(machineOee.hour.eq(producedPart.hour).and(machineOee.moldId.eq(producedPart.moldId)))
                .where(builder)
                .select(Projections.constructor(RejectedPartDetails.class, rejectedPartDetails.reason, rejectedPartDetails.rejectedAmount.sum()))
                .groupBy(rejectedPartDetails.reason)
                .having(rejectedPartDetails.rejectedAmount.sum().gt(0))
                .orderBy(rejectedPartDetails.rejectedAmount.sum().desc());
    }
}
