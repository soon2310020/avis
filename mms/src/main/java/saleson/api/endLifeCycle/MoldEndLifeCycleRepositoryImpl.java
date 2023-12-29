package saleson.api.endLifeCycle;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PriorityType;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.*;

public class MoldEndLifeCycleRepositoryImpl extends QuerydslRepositorySupport implements MoldEndLifeCycleRepositoryCustom{
    public MoldEndLifeCycleRepositoryImpl() {
        super(MoldEndLifeCycle.class);
    }

    @Override
    public List<MoldEndLifeCycle> findAllOrderByPriority(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;
        JPQLQuery query = from(moldEndLifeCycle).where(predicate);
        NumberExpression<Integer> zero = Expressions.asNumber(0);
        NumberExpression<Integer> one = Expressions.asNumber(1);
        NumberExpression<Integer> two = Expressions.asNumber(2);
        NumberExpression<Integer> priority = new CaseBuilder()
                .when(moldEndLifeCycle.priority.eq(PriorityType.HIGH))
                .then(two)
                .otherwise(new CaseBuilder()
                        .when(moldEndLifeCycle.priority.eq(PriorityType.MEDIUM))
                        .then(one)
                        .otherwise(zero));

        OrderSpecifier numberOrder = priority.asc();
        if (directions[0].equals(Sort.Direction.DESC)) {
            numberOrder = priority.desc();
        }
        query.orderBy(numberOrder);
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        query.select(moldEndLifeCycle);
        return query.fetch();
    }

    @Override
    public List<MoldEndLifeCycle> findAllOrderByAccumulatedShot(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;
        QMold mold = QMold.mold;
        QStatistics statistics = QStatistics.statistics;
        BooleanBuilder builder = new BooleanBuilder(predicate);

		NumberPath shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			shotCount = statistics.shotCountVal;
		} else {
			shotCount = statistics.shotCount;
		}

        JPQLQuery query;
        NumberExpression sumShotCount;
        if (properties[0].contains(SpecialSortProperty.moldAccumulatedShotSort)) {
            String[] sortValues = properties[0].split("\\.");
            if (!"all".equals(sortValues[1])) {
                sumShotCount = shotCount.sum();
                query = from(moldEndLifeCycle)
                        .innerJoin(mold).on(mold.id.eq(moldEndLifeCycle.moldId))
                        .leftJoin(statistics).on(statistics.moldId.eq(mold.id)
                                .and(statistics.year.eq(sortValues[1]))
                                .and(statistics.ct.gt(0).or(statistics.firstData.isTrue())))
                        .where(builder);
            } else {
                sumShotCount = mold.lastShot;
                query = from(moldEndLifeCycle)
                        .innerJoin(mold).on(mold.id.eq(moldEndLifeCycle.moldId))
                        .where(builder);
            }
        } else {
            sumShotCount = mold.lastShot;
            query = from(moldEndLifeCycle)
                    .innerJoin(mold).on(mold.id.eq(moldEndLifeCycle.moldId))
                    .where(builder);
        }
        query.groupBy(mold.id);
        OrderSpecifier numberOrder = sumShotCount.asc();
        if (directions[0].equals(Sort.Direction.DESC)) {
            numberOrder = sumShotCount.desc();
        }
        query.orderBy(numberOrder);
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        return query.fetch();
    }

    @Override
    public List<MoldEndLifeCycle> findAllOrderByPart(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        QMoldEndLifeCycle moldCycleTime = QMoldEndLifeCycle.moldEndLifeCycle;
        QMold mold = QMold.mold;
        QMoldPart moldPart = QMoldPart.moldPart;

        JPQLQuery query = from(moldCycleTime)
                .innerJoin(mold).on(moldCycleTime.moldId.eq(mold.id))
                .leftJoin(moldPart).on(mold.id.eq(moldPart.moldId));
        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(moldPart.id.in(JPAExpressions
                        .select(moldPart.id.min())
                        .from(moldPart)
                        .groupBy(moldPart.moldId)))
                .and(mold.deleted.isNull().or(mold.deleted.isFalse()));

        query.where(builder);

        StringExpression expression = moldPart.part.partCode;
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
    public List<MoldEndLifeCycle> findAllOrderByStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;
        QMold mold = QMold.mold;
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(moldEndLifeCycle)
                .leftJoin(mold).on(moldEndLifeCycle.moldId.eq(mold.id))
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
    public List<MoldEndLifeCycle> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;
        QMold mold = moldEndLifeCycle.mold;
        JPQLQuery query = from(moldEndLifeCycle).where(predicate);
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
    public Long countByPriorityTypeAndPredicate(PriorityType priorityType, Predicate predicate) {
        QMoldEndLifeCycle moldEndLifeCycle = QMoldEndLifeCycle.moldEndLifeCycle;
        QMold mold = QMold.mold;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
        builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
        builder.and(mold.equipmentStatus.eq(EquipmentStatus.INSTALLED));
        Instant last30Day = Instant.now().minus(30, ChronoUnit.DAYS);
        builder.and(moldEndLifeCycle.mold.operatedStartAt.before(last30Day));
        builder.and(predicate);
        builder.and(moldEndLifeCycle.priority.eq(priorityType));
        JPQLQuery query = from(moldEndLifeCycle).innerJoin(mold)
                .on(moldEndLifeCycle.moldId.eq(mold.id)).where(builder);

        return query.fetchCount();
    }
}
