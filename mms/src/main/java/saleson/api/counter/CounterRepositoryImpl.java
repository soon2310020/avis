package saleson.api.counter;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.util.DateUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.*;
import saleson.model.data.CounterToolingCode;
import saleson.model.data.CounterToolingData;
import saleson.model.data.DashboardChartData;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CounterRepositoryImpl extends QuerydslRepositorySupport implements CounterRepositoryCustom {

    public CounterRepositoryImpl() {
        super(Counter.class);
    }

    @Autowired
    DashboardGeneralFilterUtils dashboardGeneralFilterUtils;
    @Override
    public List<DashboardChartData> findImplementationStatus(DashboardFilterPayload payload) {
        QCounter counter = QCounter.counter;
        QMold mold = QMold.mold;
        QLocation location = QLocation.location;
        QCompany company = QCompany.company;

        BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}

        builder.and(dashboardGeneralFilterUtils.getMoldFilter(mold));
        if(payload != null && payload.getCompanyId() != null){
            if(payload.getCompanyId() == -1000L){
                builder.and(counter.locationId.isNull());
            }else {
                builder.and(location.companyId.eq(payload.getCompanyId()));
            }
            JPQLQuery query = from(counter)
                    .leftJoin(location).on(location.id.eq(counter.locationId))
                    .where(builder)
                    .groupBy(counter.equipmentStatus)
                    .select(Projections.constructor(DashboardChartData.class, counter.equipmentStatus, counter.id.count()));
            return query.fetch();
        }

        JPQLQuery query = from(counter)
//                .leftJoin(mold).on(mold.counterId.eq(counter.id))
                .leftJoin(location).on(counter.locationId.eq(location.id))
                .leftJoin(company).on(location.companyId.eq(company.id))
                .where(builder)
                .groupBy(company)
                .select(Projections.constructor(DashboardChartData.class, company, counter.id.count()));
        return query.fetch();
    }

    @Override
    public List<CounterToolingData> getListCounter(List<String> counterCodeList, Pageable pageable, String searchText, Boolean isUnmatched)
    {
        QCounter counter = QCounter.counter;
        QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(mold.id.in(JPQLQueryUtils.getMoldIdsSubquery()));
		}
        if (counterCodeList != null) {
            builder.and(counter.equipmentCode.in(counterCodeList));
        }

        if (StringUtils.isNotBlank(searchText)) {
            builder.and(counter.equipmentCode.like('%'+searchText+'%'));
        }
        if (isUnmatched != null && isUnmatched) {
            builder.and(counter.equipmentStatus.in(Arrays.asList(EquipmentStatus.AVAILABLE, EquipmentStatus.DISCARDED)));
        }
        builder.and(counter.enabled.isTrue().and(counter.equipmentCode.isNotNull()).and(counter.equipmentCode.isNotEmpty()));
        JPQLQuery query = from(counter).where(builder)
                .leftJoin(mold).on(counter.id.eq(mold.counterId))
                .select(Projections.constructor(CounterToolingData.class, counter.id, counter.equipmentCode, mold.equipmentCode));
        if (pageable != null && counterCodeList == null) {
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        return query.fetch();
    }

    @Override
    public List<Counter> findAllOrderByOperatingStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(counter).where(predicate);
        NumberExpression<Integer> expression = new CaseBuilder()
                .when(counter.operatingStatus.isNull())
                .then(0)
                .when(counter.operatingStatus.eq(OperatingStatus.WORKING))
                .then(1)
                .when(counter.operatingStatus.eq(OperatingStatus.IDLE))
                .then(2)
                .when(counter.operatingStatus.eq(OperatingStatus.NOT_WORKING))
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
    public List<Location> findAllLocationByCounterId(List<Long> counterIds) {
        QCounter counter = QCounter.counter;
        JPQLQuery<Location> query = from(counter).select(counter.location).where(counter.id.in(counterIds));
        return query.fetch();
    }

    @Override
    public Optional<Location> findLocationByCounterId(Long id) {
        QCounter counter = QCounter.counter;
        JPQLQuery<Location> query = from(counter).select(counter.location).where(counter.id.eq(id));
        return Optional.ofNullable(query.fetchOne() == null ? null : query.fetchOne());
    }

    @Override
    public List<Counter> findAllOrderByStatus(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QCounter counter = QCounter.counter;
        QMold mold = QMold.mold;
        JPQLQuery query = from(counter).leftJoin(mold).on(counter.id.eq(mold.counterId)).where(predicate);
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
    public List<Long> findAllIdByPredicate(Predicate predicate) {
        QCounter counter = QCounter.counter;
        JPQLQuery query = from(counter).where(predicate).select(counter.id);
        return query.fetch();
    }
    @Override
    public List<Counter> findAllOrderBySpecialField(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        if (properties[0].contains(SpecialSortProperty.operatingStatus)) {
            return findAllOrderByOperatingStatus(predicate, pageable);
        }
        if (SpecialSortProperty.moldCounterStatusProperties.contains(properties[0])) {
            return findAllOrderByStatus(predicate, pageable);
        }

        QCounter counter = QCounter.counter;
        JPQLQuery query = from(counter).where(predicate);

        NumberExpression numberExpression = counter.id;
        DateExpression expireDate = Expressions.dateTemplate(Long.class, "DATE_ADD({0}, INTERVAL {1} DAY) ", counter.activatedAt,
            counter.subscriptionTerm.multiply(365)
        );
        if (properties[0].equals("activePeriod")) {
            Expression<Instant> lastActiveDate = new CaseBuilder().when(expireDate.isNotNull().and(expireDate.before(Instant.now())))
                .then(expireDate).otherwise(Instant.now());

            numberExpression = Expressions.numberTemplate(Long.class, "TIMESTAMPDIFF(SECOND, {0}, {1}) ", counter.activatedAt,
                lastActiveDate
            );
        } else if (properties[0].equals("subscriptionStatus")) {
            numberExpression = new CaseBuilder()
                .when((expireDate.before(Instant.now())))
                .then(1)
                .when((expireDate.after(Instant.now())))
                .then(-1)
                .otherwise(0);
        } else if (properties[0].equals("subscriptionExpiry")) {
            numberExpression = Expressions.numberTemplate(Integer.class, "TIMESTAMPDIFF(DAY, {0}, {1}) ", counter.activatedAt,
                Instant.now()
            ).mod(counter.subscriptionTerm.multiply(365));
        }
        OrderSpecifier order = directions[0].equals(Sort.Direction.ASC) ? numberExpression.asc() : numberExpression.desc();
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

}
