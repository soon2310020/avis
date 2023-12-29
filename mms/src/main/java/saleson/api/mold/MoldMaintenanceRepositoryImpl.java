package saleson.api.mold;

import com.emoldino.framework.repository.Q;
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
import org.springframework.data.util.Pair;
import saleson.api.batch.payload.IdData;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.MaintenanceStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.*;
import saleson.model.data.MoldMaintenanceExtraData;
import saleson.model.data.MoldMaintenancePartExtraData;
import saleson.model.customField.QCustomFieldValue;
import saleson.model.data.dashboard.maintenance.MaintenanceContinentData;

import java.time.Instant;
import java.util.List;

public class MoldMaintenanceRepositoryImpl  extends QuerydslRepositorySupport implements MoldMaintenanceRepositoryCustom{
    public MoldMaintenanceRepositoryImpl() {
        super(MoldMaintenance.class);
    }

    @Override
    public List<MoldMaintenancePartExtraData> findMoldMaintenanceExtraData(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        QMold mold = QMold.mold;
        QMoldPart moldPart = QMoldPart.moldPart;

        JPQLQuery query = from(moldMaintenance).innerJoin(mold).on(moldMaintenance.moldId.eq(mold.id))
                .leftJoin(moldPart).on(mold.id.eq(moldPart.moldId));
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        booleanBuilder.and(moldPart.id.in(JPAExpressions
                .select(moldPart.id.min())
                .from(moldPart)
                .groupBy(moldPart.moldId)))
                .and(mold.deleted.isNull().or(mold.deleted.isFalse()));

        query.where(booleanBuilder);

        StringExpression expression = moldPart.part.partCode;
        OrderSpecifier order = expression.desc();
        StringExpression expression2 = mold.equipmentCode;
        OrderSpecifier order2 = expression2.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }

        query.orderBy(order, order2);
        query.select(Projections.constructor(MoldMaintenancePartExtraData.class, moldMaintenance, moldPart.part.partCode));
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public List<MoldMaintenanceCustomFieldValue> findMoldMaintenanceCustomFieldValue(Predicate predicate, Pageable pageable)
    {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        Long customFieldId = Long.valueOf((properties[0].split("-"))[1]);

        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        QMold mold = QMold.mold;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;

        JPQLQuery query = from(moldMaintenance).innerJoin(mold).on(moldMaintenance.moldId.eq(mold.id))
                .leftJoin(customFieldValue).on(mold.id.eq(customFieldValue.objectId));
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        booleanBuilder.and(customFieldValue.id
                .in(JPAExpressions
                        .select(customFieldValue.id)
                        .from(customFieldValue)
                        .where(customFieldValue.customField.id.eq(customFieldId)))
                .or(customFieldValue.isNull()));

        query.where(booleanBuilder);

        StringExpression expression = customFieldValue.value;
        OrderSpecifier order = expression.desc();
        StringExpression expression2 = mold.equipmentCode;
        OrderSpecifier order2 = expression2.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }

        query.orderBy(order, order2);
        query.select(Projections.constructor(MoldMaintenanceCustomFieldValue.class, moldMaintenance, customFieldValue.id));
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public List<MoldMaintenanceExtraData> findMoldMaintenanceOrderByAccumulatedShot(Predicate predicate, Pageable pageable, List<Long> moldIds) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
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
                query = from(moldMaintenance).innerJoin(mold).on(moldMaintenance.moldId.eq(mold.id))
                        .leftJoin(statistics).on(statistics.moldId.eq(mold.id)
                                .and(statistics.year.eq(sortValues[1]))
                                .and(statistics.ct.gt(0).or(statistics.firstData.isTrue())))
                        .where(builder);
            } else {
                sumShotCount = mold.lastShot;
                query = from(moldMaintenance).innerJoin(mold).on(moldMaintenance.moldId.eq(mold.id))
                        .where(builder);
            }
        } else {
            sumShotCount = mold.lastShot;
            query = from(moldMaintenance).innerJoin(mold).on(moldMaintenance.moldId.eq(mold.id))
                    .where(builder);
        }
        query.groupBy(mold.id, moldMaintenance.maintenanceStatus);

        OrderSpecifier numberOrder = sumShotCount.asc();
        if (directions[0].equals(Sort.Direction.DESC)) {
            numberOrder = sumShotCount.desc();
        }
        query.orderBy(numberOrder);

        if (CollectionUtils.isEmpty(moldIds)) {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        query.select(Projections.constructor(MoldMaintenanceExtraData.class, moldMaintenance, sumShotCount));

        return query.fetch();
    }

    @Override
    public List<MaintenanceContinentData> findMoldMaintenanceGroupByContinent(TabbedOverviewGeneralFilterPayload payload, MaintenanceStatus status) {
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        QContinent continent = QContinent.continent;
        JPQLQuery query = from(moldMaintenance)
                .innerJoin(continent).on(moldMaintenance.mold.location.countryCode.eq(continent.countryCode))
                .where(payload.getMoldFilter(moldMaintenance.mold).and(moldMaintenance.maintenanceStatus.eq(status)));
        Pair<Instant, Instant> startEnd = payload.getStartEndByDuration(true);
        query.where(moldMaintenance.createdAt.goe(startEnd.getFirst()).and(moldMaintenance.createdAt.loe(startEnd.getSecond())));
        query.where(moldMaintenance.latest.isTrue());
        query.where(moldMaintenance.mold.companyId.isNotNull());
        query.where(moldMaintenance.mold.maintenanced.isTrue());
        query.groupBy(continent.continentName);
        query.select(Projections.constructor(MaintenanceContinentData.class, continent.continentName, moldMaintenance.id.count()));
        return query.fetch();
    }

    @Override
    public Long countMaintenance(TabbedOverviewGeneralFilterPayload payload) {
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        JPQLQuery query = from(moldMaintenance)
                .where(payload.getMoldFilter(moldMaintenance.mold).and(moldMaintenance.maintenanceStatus.ne(MaintenanceStatus.DONE)));
//        Pair<Instant, Instant> startEnd = payload.getStartEndByDuration(true);
//        query.where(moldMaintenance.createdAt.goe(startEnd.getFirst()).and(moldMaintenance.createdAt.loe(startEnd.getSecond())));
        query.where(moldMaintenance.latest.isTrue());
        query.where(moldMaintenance.mold.maintenanced.isTrue());
        return query.fetchCount();
    }

    @Override
    public List<MoldMaintenance> findMoldMaintenanceOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        QMold mold = moldMaintenance.mold;
        JPQLQuery query = from(moldMaintenance).where(predicate);
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
    public List<MoldMaintenance> findMoldMaintenanceOrderByStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        QMold mold = QMold.mold;
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(moldMaintenance)
                .leftJoin(mold).on(moldMaintenance.moldId.eq(mold.id))
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
        QMoldMaintenance table = QMoldMaintenance.moldMaintenance;
        JPQLQuery query = from(table).where(predicate);
        query.select(Projections.constructor(IdData.class, table.moldId, table.id));
        return query.fetch();
    }
}
