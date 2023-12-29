package saleson.api.machine;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.machine.payload.AvgOEE;
import saleson.api.machine.payload.OeeByShift;
import saleson.api.machine.payload.OeePayload;
import saleson.common.enumeration.Frequent;
import saleson.common.util.StringUtils;
import saleson.model.*;

import java.util.List;

public class MachineOeeRepositoryImpl extends QuerydslRepositorySupport implements MachineOeeRepositoryCustom{
    public MachineOeeRepositoryImpl() {
        super(MachineOee.class);
    }


    @Override
    public AvgOEE findOeeData(OeePayload payload) {
        QMachineOee machineOee = QMachineOee.machineOee;
        QMachine machine = QMachine.machine;
        JPQLQuery query;
        if (!StringUtils.isEmpty(payload.getStart()) && !StringUtils.isEmpty(payload.getEnd())) {
            query = from(machine)
                    .innerJoin(machineOee).on(machine.id.eq(machineOee.machineId).and(machineOee.day.between(payload.getStart(), payload.getEnd())))
                    .where(payload.getMachinePredicate());
        } else {
            query = from(machine)
                    .innerJoin(machineOee).on(machine.id.eq(machineOee.machineId))
                    .where(payload.getMachinePredicate());
        }
        NumberExpression<Double> fa = new CaseBuilder().when(machineOee.fa.isNotNull()).then(machineOee.fa).otherwise(0D);
        NumberExpression<Double> fp = new CaseBuilder().when(machineOee.fp.isNotNull()).then(machineOee.fp).otherwise(0D);
        NumberExpression<Double> fq = new CaseBuilder().when(machineOee.fq.isNotNull()).then(machineOee.fq).otherwise(0D);
        NumberExpression<Double> avgFa = fa.avg();
        NumberExpression<Double> avgFp = fp.avg();
        NumberExpression<Double> avgFq = fq.avg();

        query.select(Projections.constructor(AvgOEE.class, avgFa, avgFp, avgFq));
        return (AvgOEE) query.fetchOne();
    }

    @Override
    public AvgOEE findOeeDataByHour(OeePayload payload) {
        QMachineOee machineOee = QMachineOee.machineOee;
        QMachine machine = QMachine.machine;
        JPQLQuery query;
        if (!StringUtils.isEmpty(payload.getStart()) && !StringUtils.isEmpty(payload.getEnd())) {
            query = from(machine)
                    .innerJoin(machineOee).on(machine.id.eq(machineOee.machineId).and(machineOee.hour.between(payload.getStart(), payload.getEnd())))
                    .where(machineOee.periodType.eq(Frequent.HOURLY).and(payload.getMachinePredicate()));
        } else {
            query = from(machine)
                    .innerJoin(machineOee).on(machine.id.eq(machineOee.machineId))
                    .where(machineOee.periodType.eq(Frequent.HOURLY).and(payload.getMachinePredicate()));
        }
        NumberExpression<Double> fa = new CaseBuilder().when(machineOee.fa.isNotNull()).then(machineOee.fa).otherwise(0D);
        NumberExpression<Double> fp = new CaseBuilder().when(machineOee.fp.isNotNull()).then(machineOee.fp).otherwise(0D);
        NumberExpression<Double> fq = new CaseBuilder().when(machineOee.fq.isNotNull().and(machineOee.partProduced.lt(0))).then(machineOee.fq).otherwise(100D);
        NumberExpression<Double> avgFa = fa.avg();
        NumberExpression<Double> avgFp = fp.avg();
        NumberExpression<Double> avgFq = fq.avg();

        query.select(Projections.constructor(AvgOEE.class, avgFa, avgFp, avgFq));
        return (AvgOEE) query.fetchOne();
    }

    @Override
    public List<OeeByShift> findAllOeeDataByHour(OeePayload payload) {
        QMachineOee machineOee = QMachineOee.machineOee;
        QMachine machine = QMachine.machine;
        JPQLQuery query;
        if (!StringUtils.isEmpty(payload.getStart()) && !StringUtils.isEmpty(payload.getEnd())) {
            query = from(machine)
                    .innerJoin(machineOee).on(machine.id.eq(machineOee.machineId).and(machineOee.hour.goe(payload.getStart()).and(machineOee.hour.lt(payload.getEnd()))))
                    .where(machineOee.periodType.eq(Frequent.HOURLY).and(payload.getMachinePredicate()));
        } else {
            query = from(machine)
                    .innerJoin(machineOee).on(machine.id.eq(machineOee.machineId))
                    .where(machineOee.periodType.eq(Frequent.HOURLY).and(payload.getMachinePredicate()));
        }
        query.select(Projections.constructor(OeeByShift.class, machineOee.partProduced, machineOee.rejectedPart, machineOee.fa, machineOee.fp, machineOee.fq, machineOee.downtimeDuration, machineOee.hour, machineOee.tenMinute, machineOee.mold));
        return query.fetch();
    }

    @Override
    public String findFirstHourByMachineIdAndPeriodTypeOrderByTenMinuteDesc(Long machineId, Frequent periodType) {
        QMachineOee machineOee = QMachineOee.machineOee;

        JPQLQuery query = from(machineOee)
                .where(machineOee.machineId.eq(machineId).and(machineOee.periodType.eq(periodType)))
                .orderBy(machineOee.tenMinute.desc());
        query.select(machineOee.hour);
        query.limit(1);
        return query.fetchOne() != null ? String.valueOf(query.fetchOne()) : null;
    }

    @Override
    public String findFirstHourByMachineIdAndPeriodTypeOrderByHourDesc(Long machineId) {
        QMachineOee machineOee = QMachineOee.machineOee;

        JPQLQuery query = from(machineOee)
                .where(machineOee.machineId.eq(machineId).and(machineOee.periodType.eq(Frequent.HOURLY)))
                .orderBy(machineOee.hour.desc());
        query.select(machineOee.hour);
        query.limit(1);
        return query.fetchOne() != null ? String.valueOf(query.fetchOne()) : null;
    }

    @Override
    public String findFirstTimeByMachineIdAndPeriodTypeOrderByHourDesc(Long machineId) {
        QMachineOee machineOee = QMachineOee.machineOee;

        JPQLQuery query = from(machineOee)
                .where(machineOee.machineId.eq(machineId).and(machineOee.periodType.eq(Frequent.HOURLY)))
                .orderBy(machineOee.hour.desc());
        query.select(machineOee.tenMinute);
        query.limit(1);
        return query.fetchOne() != null ? String.valueOf(query.fetchOne()) : null;
    }
}
