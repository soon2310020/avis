package saleson.api.endLifeCycle;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.batch.payload.IdData;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.*;
import saleson.model.data.MoldMisconfigureExtraData;
import saleson.model.data.MoldRefurbishmentExtraData;

import java.util.List;

public class MoldRefurbishmentRepositoryImpl  extends QuerydslRepositorySupport implements MoldRefurbishmentRepositoryCustom {
    public MoldRefurbishmentRepositoryImpl() {
        super(MoldRefurbishment.class);
    }

    @Override
    public List<MoldRefurbishmentExtraData> findMoldRefurbishmentOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldRefurbishment moldRefurbishment = QMoldRefurbishment.moldRefurbishment;
        QMold mold = QMold.mold;
        QStatistics statistics = QStatistics.statistics;
        BooleanBuilder builder = new BooleanBuilder(predicate);

        if (CollectionUtils.isNotEmpty(moldIds)) {
            builder.and(mold.id.in(moldIds));
        }

        JPQLQuery query;
        NumberExpression sumShotCount;
        if (properties[0].startsWith(SpecialSortProperty.moldAccumulatedShotSort)) {
            String[] sortValues = properties[0].split("\\.");
            if (!"all".equals(sortValues[1])) {
                sumShotCount = (new CaseBuilder().when(statistics.isNull()).then(0).otherwise(statistics.shotCount)).sum();
                query = from(moldRefurbishment).innerJoin(mold).on(moldRefurbishment.mold.eq(mold))
                        .leftJoin(statistics).on(statistics.moldId.eq(mold.id)
                                .and(statistics.year.eq(sortValues[1]))
                                .and(statistics.ct.gt(0).or(statistics.firstData.isTrue())))
                        .where(builder);
            } else {
                sumShotCount = mold.lastShot;
                query = from(moldRefurbishment).innerJoin(mold).on(moldRefurbishment.mold.eq(mold))
                        .leftJoin(statistics).on(statistics.moldId.eq(mold.id))
                        .where(builder);
            }
        } else {
            sumShotCount = mold.lastShot;
            query = from(moldRefurbishment).innerJoin(mold).on(moldRefurbishment.mold.eq(mold))
                    .leftJoin(statistics).on(statistics.moldId.eq(mold.id))
                    .where(builder);
        }
        query.groupBy(mold.id, moldRefurbishment.refurbishmentStatus);

        OrderSpecifier numberOrder = sumShotCount.asc();
        if (directions[0].equals(Sort.Direction.DESC)) {
            numberOrder = sumShotCount.desc();
        }
        query.orderBy(numberOrder);

        if (CollectionUtils.isEmpty(moldIds)) {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        query.select(Projections.constructor(MoldRefurbishmentExtraData.class, moldRefurbishment, sumShotCount));

        return query.fetch();
    }

    @Override
    public List<MoldRefurbishment> findMoldRefurbishmentOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldRefurbishment moldRefurbishment = QMoldRefurbishment.moldRefurbishment;
        QMold mold = moldRefurbishment.mold;
        JPQLQuery query = from(moldRefurbishment).where(predicate);
        NumberExpression<Integer> expression = new CaseBuilder()
                .when(mold.operatingStatus.isNull())
                .then(0)
                .when(mold.operatingStatus.eq(OperatingStatus.WORKING))
                .then(1)
                .when(mold.operatingStatus.eq(OperatingStatus.IDLE))
                .then(2)
                .when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING))
                .then(3)
                .otherwise(4);
        OrderSpecifier order = expression.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public List<MoldRefurbishment> findMoldRefurbishmentOrderByStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldRefurbishment moldRefurbishment = QMoldRefurbishment.moldRefurbishment;
        QMold mold = QMold.mold;
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(moldRefurbishment)
                .leftJoin(mold).on(moldRefurbishment.moldId.eq(mold.id))
                .leftJoin(counter).on(mold.counterId.eq(counter.id))
                .where(predicate);
        StringExpression expression;
        JPQLQuery<Long> counterMatchedIdExpression = JPAExpressions.select(mold.counterId).from(mold).where(mold.counterId.isNotNull());
        if (properties[0].equals("toolingStatus")) {
            expression = new CaseBuilder()
                    .when(mold.operatingStatus.eq(OperatingStatus.WORKING).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
                    .then("IN_PRODUCTION")
                    .when(mold.operatingStatus.eq(OperatingStatus.IDLE).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
                    .then("IDLE")
                    .when(mold.operatingStatus.eq(OperatingStatus.NOT_WORKING).and(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.ne(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
                    .then("NOT_WORKING")
                    .when(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))
                    .then("SENSOR_DETACHED")
                    .when(counter.equipmentStatus.ne(EquipmentStatus.DETACHED).and(counter.operatingStatus.eq(OperatingStatus.DISCONNECTED).or(mold.operatingStatus.eq(OperatingStatus.DISCONNECTED))).and(counter.id.in(counterMatchedIdExpression)))
                    .then("SENSOR_OFFLINE")
                    .when(counter.operatingStatus.isNull().and(mold.operatingStatus.isNull()).and(counter.id.in(counterMatchedIdExpression))).then("ON_STANDBY")
                    .when(counter.id.notIn(counterMatchedIdExpression))
                    .then("NO_SENSOR")
                    .otherwise("");
        } else {
            expression = new CaseBuilder().when(mold.counterId.isNull())
                    .then("NOT_INSTALLED")
                    .when(counter.equipmentStatus.eq(EquipmentStatus.DETACHED))
                    .then("DETACHED")
                    .otherwise("INSTALLED");
        }
        OrderSpecifier order = expression.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public List<IdData> getAllIds(Predicate predicate) {
        QMoldRefurbishment table = QMoldRefurbishment.moldRefurbishment;
        JPQLQuery query = from(table).where(predicate);
        query.select(Projections.constructor(IdData.class, table.moldId, table.id));
        return query.fetch();
    }
}
