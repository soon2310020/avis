package saleson.api.machine;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.machine.payload.DetailOEE;
import saleson.common.util.StringUtils;
import saleson.model.MachineStatistics;
import saleson.model.QMachine;
import saleson.model.QMachineStatistics;
import saleson.service.util.DateTimeUtils;

import java.util.List;

public class MachineStatisticsRepositoryImpl extends QuerydslRepositorySupport implements MachineStatisticsRepositoryCustom {

    public MachineStatisticsRepositoryImpl() {
        super(MachineStatistics.class);
    }

    @Override
    public Page<DetailOEE> findMachineStatisticsForOEE(String start, String end, String line, Long companyId, Pageable pageable, boolean isAll) {
        QMachineStatistics machineStatistics = QMachineStatistics.machineStatistics;
        QMachine machine = QMachine.machine;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(machine.companyId.eq(companyId)).and(machine.line.isNotNull());
        BooleanBuilder machineStatisticBuilder = new BooleanBuilder();

        NumberExpression<Integer> duration = Expressions.asNumber(DateTimeUtils.diffBetweenTwoDates(start, end, true));
        if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end)) {
            machineStatisticBuilder.and(machineStatistics.day.between(start, end));
        }

        NumberExpression<Double> fa = new CaseBuilder()
                .when(machineStatistics.fa.isNotNull()).then(machineStatistics.fa.doubleValue())
                .otherwise(0D);

        NumberExpression<Double> fp = new CaseBuilder()
                .when(machineStatistics.fp.isNotNull()).then(machineStatistics.fp.doubleValue())
                .otherwise(0D);

        NumberExpression<Double> fq = new CaseBuilder()
                .when(machineStatistics.fq.isNotNull()).then(machineStatistics.fq.doubleValue())
                .otherwise(0D);

        NumberExpression<Double> oee = new CaseBuilder()
                .when(machineStatistics.oee.isNotNull()).then(machineStatistics.oee.doubleValue())
                .otherwise(0D);


        NumberExpression<Double> avgFA = fa.sum().divide(duration).divide(machine.id.countDistinct());
        NumberExpression<Double> avgFP = fp.sum().divide(duration).divide(machine.id.countDistinct());
        NumberExpression<Double> avgFQ = fq.sum().divide(duration).divide(machine.id.countDistinct());
        NumberExpression<Double> avgOEE = oee.sum().divide(duration).divide(machine.id.countDistinct());

        StringExpression name;
        if ("all".equalsIgnoreCase(line)) {
            name = new CaseBuilder()
                    .when(machine.line.isNotNull()).then(machine.line)
                    .otherwise("");
        } else {
            builder.and(machine.line.eq(line));
            name = machine.machineCode;
        }

        Expression sortValue;
        OrderSpecifier orderSpecifier = name.asc();
        if (pageable != null) {
            Sort.Direction[] directions = {Sort.Direction.DESC};
            String[] orderProperty = {""};
            pageable.getSort().forEach(order -> {
                directions[0] = order.getDirection();
                orderProperty[0] = order.getProperty();
            });
            if (orderProperty[0].equalsIgnoreCase("oee")) {
                sortValue = avgOEE;
            } else if (orderProperty[0].equalsIgnoreCase("quality")) {
                sortValue = avgFQ;
            } else if (orderProperty[0].equalsIgnoreCase("availability")) {
                sortValue = avgFA;
            } else if (orderProperty[0].equalsIgnoreCase("performance")) {
                sortValue = avgFP;
            } else {
                sortValue = name;
            }
            orderSpecifier = new OrderSpecifier(Order.valueOf(directions[0].toString()), sortValue);

        }

        JPQLQuery query = from(machine)
                .leftJoin(machineStatistics).on(machine.id.eq(machineStatistics.machine.id).and(machineStatisticBuilder))
                .where(builder);


        long total;
        if (isAll){
            query.select(Projections.constructor(DetailOEE.class, name, avgOEE, avgFA, avgFP, avgFQ));
            total = query.fetchCount();
        } else {
            query.orderBy(orderSpecifier);
            query.select(Projections.constructor(DetailOEE.class, name, avgOEE, avgFA, avgFP, avgFQ));
            query.groupBy(name);
            total = query.fetchCount();

            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }

        List<DetailOEE> details = query.fetch();
        return new PageImpl<>(details, pageable, total);
    }
}
