package saleson.api.mold;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.common.enumeration.EquipmentStatus;
import saleson.api.batch.payload.IdData;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.*;

import java.util.List;

public class MoldDetachmentRepositoryImpl extends QuerydslRepositorySupport implements MoldDetachmentRepositoryCustom{

    public MoldDetachmentRepositoryImpl() {
        super(MoldDetachment.class);
    }

    @Override
    public List<MoldDetachment> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldDetachment moldDetachment = QMoldDetachment.moldDetachment;
        QMold mold = moldDetachment.mold;
        JPQLQuery query = from(moldDetachment).where(predicate);
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
    public List<MoldDetachment> findAllOrderByStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMoldDetachment moldDetachment = QMoldDetachment.moldDetachment;
        QMold mold = QMold.mold;
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(moldDetachment)
                .leftJoin(mold).on(moldDetachment.moldId.eq(mold.id))
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
        QMoldDetachment table = QMoldDetachment.moldDetachment;
        JPQLQuery query = from(table).where(predicate);
        query.select(Projections.constructor(IdData.class, table.moldId, table.id));
        return query.fetch();
    }
}
