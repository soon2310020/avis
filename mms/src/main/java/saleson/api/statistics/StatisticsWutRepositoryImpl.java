package saleson.api.statistics;

import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.WUTType;
import saleson.common.enumeration.productivity.CompareType;
import saleson.common.util.DateUtils;
import saleson.model.*;
import saleson.model.data.supplierReport.SupplierProductionData;
import saleson.model.data.supplierReport.SupplierProductionOverviewData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class StatisticsWutRepositoryImpl extends QuerydslRepositorySupport implements StatisticsWutRepositoryCustom {
    public StatisticsWutRepositoryImpl() {
        super(StatisticsWut.class);
    }


    @Override
    public List<SupplierProductionData> findProductionQuantity(List<Long> moldIds, ProductivitySearchPayload payload){
        List<SupplierProductionData> dataList;
        QStatistics statistics = QStatistics.statistics;
//        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
        QMold mold = QMold.mold;
		NumberPath<Integer> shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statistics.shotCountVal;
		} else {
			shotCount = statistics.shotCount;
		}
        JPQLQuery query = from(statistics)
//                .innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
                .innerJoin(mold).on(mold.id.eq(statistics.moldId))
                ;

        BooleanBuilder builder = new BooleanBuilder();
        if(moldIds != null) builder.and(mold.id.in(moldIds));
//        if(payload.getPartId() != null) builder.and(statisticsPart.partId.eq(payload.getPartId()));
        if(payload.getStartDate() != null && payload.getEndDate() != null){
            builder.and(statistics.day.between(DateUtils.getDay(payload.getStartDate()), DateUtils.getDay(payload.getEndDate())));
        }else if(payload.getEndDate() != null){
            builder.and(statistics.day.loe(DateUtils.getDay(payload.getEndDate())));
        }
        Expression<Integer> totalShort= (shotCount).sum().coalesce(0);
        query.where(builder
                .and(mold.deleted.isNull().or(mold.deleted.isFalse())));
        if(payload.isGroupBySuppliers()){
/*
            query.groupBy(mold.supplier.id,mold.supplier.name,mold.supplier.companyCode);
            query.select(Projections.constructor(SupplierProductionData.class,mold.supplier.id,
                    mold.supplier.companyCode,mold.supplier.name, totalShort));
*/

            query.groupBy(mold.location.company.id,mold.location.company.name,mold.location.company.companyCode);
            query.select(Projections.constructor(SupplierProductionData.class,mold.location.company.id,
                    mold.location.company.companyCode,mold.location.company.name, totalShort));

        }else {
            query.select(Projections.constructor(SupplierProductionData.class, totalShort));
        }
        query.orderBy(((Coalesce<Integer>) totalShort).desc());
//        return (ProductivityOverviewData) query.fetchOne();
        dataList = query.fetch();
        return dataList;
    }

    @Override
    public List<SupplierProductionData> findProductionWUTQuantity(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable){
        List<SupplierProductionData> dataList =  new ArrayList<>();
        QStatisticsWut statisticsWut = QStatisticsWut.statisticsWut;
//        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
        QMold mold = QMold.mold;

        JPQLQuery query = from(statisticsWut)
//                .innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
                .innerJoin(mold).on(mold.id.eq(statisticsWut.moldId))
                ;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(statisticsWut.valData.eq(OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)));

        if(moldIds != null) builder.and(mold.id.in(moldIds));
//        if(payload.getPartId() != null) builder.and(statisticsPart.partId.eq(payload.getPartId()));

        DateTimeExpression<Instant> startedAtExpr = Expressions.dateTimeTemplate(Instant.class,"STR_TO_DATE({0},'%Y%m%d%H%i%s') ", statisticsWut.startedAt);
        DateTimeExpression<Instant> endAtExpr = Expressions.dateTimeTemplate(Instant.class,"STR_TO_DATE({0},'%Y%m%d%H%i%s') ", statisticsWut.endAt);

//        NumberExpression<Integer> dueTimeCycleWUTDraw = (endAtExpr.second().subtract(startedAtExpr.second()));
        NumberExpression<Long> dueTimeCycleWUTDraw = Expressions.numberTemplate(Long.class,"TIMESTAMPDIFF(SECOND, {0}, {1}) ", startedAtExpr,endAtExpr);
        NumberExpression<Long> dueTimeCycleWUT = new CaseBuilder().when(dueTimeCycleWUTDraw.gt(0)).then(dueTimeCycleWUTDraw).otherwise(1L);

        NumberExpression<Integer> shotWithTime = statisticsWut.shotCount;

        if(payload.getStartDate() != null && payload.getEndDate() != null){
            Instant startDateParamIns = DateUtils.getInstant(payload.getStartDate(), DateUtils.DEFAULT_DATE_FORMAT);
            Integer startDateParamValue = Long.valueOf(DateUtils.getInstant(payload.getStartDate(), DateUtils.DEFAULT_DATE_FORMAT).toEpochMilli() / 1000).intValue();
            Instant endDateParamIns = DateUtils.getInstant(payload.getEndDate(), DateUtils.DEFAULT_DATE_FORMAT);
            endDateParamIns =  endDateParamIns.plus(1, ChronoUnit.DAYS).toEpochMilli()> Instant.now().toEpochMilli() ? Instant.now() : endDateParamIns.plus(1, ChronoUnit.DAYS);

            String endDateParamFullStr=DateUtils.getDate(endDateParamIns,DateUtils.DEFAULT_DATE_FORMAT);
            Integer endDateParamValue = Long.valueOf(endDateParamIns.toEpochMilli() / 1000).intValue();
            Integer dueTimeCycParam = endDateParamValue - startDateParamValue;
            NumberExpression<Integer> endDateParamValueExp=Expressions.asNumber(endDateParamValue);
            NumberExpression<Integer> dueTimeCycParamExp=Expressions.asNumber(dueTimeCycParam);

            shotWithTime = new CaseBuilder()
                    .when(statisticsWut.day.goe(DateUtils.getDay(payload.getStartDate())).and(statisticsWut.endAt.loe(endDateParamFullStr)))
                    .then(statisticsWut.shotCount)

                    .when(statisticsWut.startedAt.lt(payload.getStartDate())
                            .and(statisticsWut.endAt.gt(payload.getStartDate())).and(statisticsWut.endAt.loe(endDateParamFullStr)))
//                    .then(statisticsWut.shotCount.multiply(endAtExpr.second().subtract(startDateParamValue)).divide(dueTimeCycleWUT))
                    .then(statisticsWut.shotCount.multiply(Expressions.numberTemplate(Long.class,"TIMESTAMPDIFF(SECOND, {0}, {1}) ", DateUtils.getDateTime(startDateParamIns),endAtExpr)).divide(dueTimeCycleWUT))

                    .when(statisticsWut.startedAt.lt(endDateParamFullStr).and(statisticsWut.startedAt.gt(payload.getStartDate()))
                            .and(statisticsWut.endAt.gt(endDateParamFullStr)))
//                    .then(statisticsWut.shotCount.multiply(endDateParamValueExp.subtract(startedAtExpr.second())).divide(dueTimeCycleWUT))
                    .then(statisticsWut.shotCount.multiply(Expressions.numberTemplate(Long.class,"TIMESTAMPDIFF(SECOND, {0}, {1}) ", startedAtExpr,DateUtils.getDateTime(endDateParamIns))).divide(dueTimeCycleWUT))

                    .when(statisticsWut.startedAt.lt(payload.getStartDate()).and(statisticsWut.endAt.gt(endDateParamFullStr)))
                    .then(statisticsWut.shotCount.multiply(dueTimeCycParamExp).divide(dueTimeCycleWUT))

                    .otherwise(0);


            builder.and(statisticsWut.startedAt.lt(endDateParamFullStr));
            builder.and(statisticsWut.endAt.gt(payload.getStartDate()));
        }else if(payload.getEndDate() != null){

            Instant endDateParamIns = DateUtils.getInstant(payload.getEndDate(), DateUtils.DEFAULT_DATE_FORMAT);
            endDateParamIns =  endDateParamIns.plus(1, ChronoUnit.DAYS).toEpochMilli()> Instant.now().toEpochMilli() ? Instant.now() : endDateParamIns.plus(1, ChronoUnit.DAYS);
            String endDateParamFullStr=DateUtils.getDate(endDateParamIns,DateUtils.DEFAULT_DATE_FORMAT);
            Integer endDateParamValue = Long.valueOf(endDateParamIns.toEpochMilli() / 1000).intValue();
            NumberExpression<Integer> endDateParamValueExp=Expressions.asNumber(endDateParamValue);

            shotWithTime = new CaseBuilder()
                    .when(statisticsWut.endAt.loe(endDateParamFullStr))
                    .then(statisticsWut.shotCount)

                    .when(statisticsWut.endAt.gt(endDateParamFullStr))
//                    .then(statisticsWut.shotCount.multiply(endDateParamValueExp.subtract(startedAtExpr.second())).divide(dueTimeCycleWUT))
                    .then(statisticsWut.shotCount.multiply(Expressions.numberTemplate(Long.class,"TIMESTAMPDIFF(SECOND, {0}, {1}) ", startedAtExpr,DateUtils.getDateTime(endDateParamIns))).divide(dueTimeCycleWUT))
                    .otherwise(0);


            builder.and(statisticsWut.day.loe(DateUtils.getDay(payload.getEndDate())));
        }


        Expression<Integer> totalShort= (shotWithTime).sum().coalesce(0);
        NumberExpression<Integer> normalProduction = new CaseBuilder()
                .when(statisticsWut.wutType.eq(WUTType.NORMAL_TIME).or(statisticsWut.wutType.eq(WUTType.CHANGE_IN_NORMAL_TIME)))
                .then(shotWithTime)
                .otherwise(0);
        NumberExpression<Integer> warmUpProduction = new CaseBuilder()
                .when(statisticsWut.wutType.eq(WUTType.WARM_UP_TIME))
                .then(shotWithTime)
                .otherwise(0);
        NumberExpression<Integer> coolDownProduction = new CaseBuilder()
                .when(statisticsWut.wutType.eq(WUTType.COOL_DOWN_TIME))
                .then(shotWithTime)
                .otherwise(0);
        NumberExpression<Integer> coolAbnormalProduction = new CaseBuilder()
                .when(statisticsWut.wutType.eq(WUTType.ABNORMAL_TIME))
                .then(shotWithTime)
                .otherwise(0);


        NumberExpression<Integer> totalNormalProduction = normalProduction.sum();
        NumberExpression<Integer> totalWarmUpProduction = warmUpProduction.sum();
        NumberExpression<Integer> totalCoolDownProduction = coolDownProduction.sum();
        NumberExpression<Integer> totalAbnormalProduction = coolAbnormalProduction.sum();

        query.where(builder
                .and(mold.deleted.isNull().or(mold.deleted.isFalse())));
        if(payload.isGroupBySuppliers()){
/*
            query.groupBy(mold.supplier.id, mold.supplier.name, mold.supplier.companyCode);
            query.select(Projections.constructor(SupplierProductionData.class,mold.supplier.id,
                    mold.supplier.companyCode,mold.supplier.name, totalShort, totalNormalProduction,
                    totalWarmUpProduction, totalCoolDownProduction, totalAbnormalProduction));
*/
            if (CompareType.TOOL.equals(payload.getCompareBy())) {
                query.groupBy(mold.id, mold.equipmentCode);
                query.select(Projections.constructor(SupplierProductionData.class, mold.id, mold.equipmentCode, mold.location.company.name, totalShort, totalNormalProduction,
                        totalWarmUpProduction, totalCoolDownProduction, totalAbnormalProduction,mold));
            } else if (CompareType.TOOLMAKER.equals(payload.getCompareBy())) {
                query.where(builder.and(mold.location.company.isEmoldino.isFalse()).and(mold.toolMaker.isEmoldino.isFalse()));
                query.groupBy(mold.toolMaker.id, mold.toolMaker.name, mold.toolMaker.companyCode);
                query.select(Projections.constructor(SupplierProductionData.class, mold.toolMaker.id, mold.toolMaker.companyCode, mold.toolMaker.name,totalShort, totalNormalProduction,
                        totalWarmUpProduction, totalCoolDownProduction, totalAbnormalProduction));
            } else {
                query.where(builder.and(mold.location.company.isEmoldino.isFalse()).and(mold.location.company.isEmoldino.isFalse()));
                query.groupBy(mold.location.company.id, mold.location.company.name, mold.location.company.companyCode);
                query.select(Projections.constructor(SupplierProductionData.class, mold.location.company.id,
                        mold.location.company.companyCode, mold.location.company.name, totalShort, totalNormalProduction,
                        totalWarmUpProduction, totalCoolDownProduction, totalAbnormalProduction));
            }

        } else {
            query.select(Projections.constructor(SupplierProductionData.class, totalShort, totalNormalProduction,
                    totalWarmUpProduction, totalCoolDownProduction, totalAbnormalProduction));
        }
//        query.orderBy(((Coalesce<Integer>) totalShort).desc());
        if(pageable != null) {
            Sort.Direction[] directions = {Sort.Direction.DESC};
            pageable.getSort().forEach(order -> {
                directions[0] = order.getDirection();
            });
            if (directions[0].equals(Sort.Direction.ASC))
                query.orderBy(normalProduction.asc());
            else
                query.orderBy(normalProduction.desc());
            query.limit(pageable.getPageSize());
            query.offset(pageable.getOffset());
        }
//        return (ProductivityOverviewData) query.fetchOne();
        dataList = query.fetch();
        return dataList;
    }

}
