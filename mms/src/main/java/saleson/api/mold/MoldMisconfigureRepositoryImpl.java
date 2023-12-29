package saleson.api.mold;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.batch.payload.IdData;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.*;
import saleson.model.data.MoldMisconfigureExtraData;

import java.time.Instant;
import java.util.List;

public class MoldMisconfigureRepositoryImpl  extends QuerydslRepositorySupport implements MoldMisconfigureRepositoryCustom {
    public MoldMisconfigureRepositoryImpl() {
        super(MoldMisconfigure.class);
    }

    @Override
    public List<MoldMisconfigureExtraData> findMoldMisconfigureOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldMisconfigure moldMisconfigure = QMoldMisconfigure.moldMisconfigure;
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
                query = from(moldMisconfigure).innerJoin(mold).on(moldMisconfigure.mold.eq(mold))
                        .leftJoin(statistics).on(statistics.moldId.eq(mold.id)
                                .and(statistics.year.eq(sortValues[1]))
                                .and(statistics.ct.gt(0).or(statistics.firstData.isTrue())))
                        .where(builder);
            } else {
                sumShotCount = mold.lastShot;
                query = from(moldMisconfigure).innerJoin(mold).on(moldMisconfigure.mold.eq(mold))
                        .leftJoin(statistics).on(statistics.moldId.eq(mold.id))
                        .where(builder);
            }
        } else {
            sumShotCount = mold.lastShot;
            query = from(moldMisconfigure).innerJoin(mold).on(moldMisconfigure.mold.eq(mold))
                    .leftJoin(statistics).on(statistics.moldId.eq(mold.id))
                    .where(builder);
        }
        query.groupBy(mold.id, moldMisconfigure.misconfigureStatus);

        OrderSpecifier numberOrder = sumShotCount.asc();
        if (directions[0].equals(Sort.Direction.DESC)) {
            numberOrder = sumShotCount.desc();
        }
        query.orderBy(numberOrder);

        if (CollectionUtils.isEmpty(moldIds)) {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        query.select(Projections.constructor(MoldMisconfigureExtraData.class, moldMisconfigure, sumShotCount));

        return query.fetch();
    }

    @Override
    public Page<MoldMisconfigure> findAllSortByPreset(Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        QMoldMisconfigure moldMisconfigure = QMoldMisconfigure.moldMisconfigure;
        JPQLQuery query = from(moldMisconfigure).where(builder);
        Long total = query.fetchCount();

        query.limit(pageable.getPageSize()).offset(pageable.getOffset());

        NumberExpression<Long> durationExpression = Expressions.numberTemplate(Long.class, "CAST({0} AS int)",moldMisconfigure.preset);
        boolean isAsc = pageable.getSort().getOrderFor("preset").isAscending();
        OrderSpecifier orderBy = isAsc ? durationExpression.asc() : durationExpression.desc();
        query.orderBy(orderBy);
        List<MoldMisconfigure> moldMisconfigureList = query.fetch();

        return new PageImpl<> (moldMisconfigureList, pageable, total);
    }

    @Override
    public List<MoldMisconfigure> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldMisconfigure moldMisconfigure = QMoldMisconfigure.moldMisconfigure;
        QMold mold = moldMisconfigure.mold;
        JPQLQuery query = from(moldMisconfigure).where(predicate);
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
    public List<MoldMisconfigure> findAllOrderByStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldMisconfigure moldMisconfigure = QMoldMisconfigure.moldMisconfigure;
        QMold mold = QMold.mold;
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(moldMisconfigure)
                .leftJoin(mold).on(moldMisconfigure.mold.id.eq(mold.id))
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
        QMoldMisconfigure table = QMoldMisconfigure.moldMisconfigure;
        JPQLQuery query = from(table).where(predicate);
        query.select(Projections.constructor(IdData.class, table.mold.id, table.id));
        return query.fetch();
    }
}
