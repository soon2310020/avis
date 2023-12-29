package saleson.api.machine;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.machine.payload.MachineMoldData;
import saleson.common.config.Const;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.RequestDataType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.data.MiniComponentData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import saleson.model.customProperty.ObjectCustomFieldValue;
import saleson.model.data.MachineData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.customField.QCustomField;
import saleson.model.customField.QCustomFieldValue;

public class MachineRepositoryImpl extends QuerydslRepositorySupport implements MachineRepositoryCustom {

    public MachineRepositoryImpl() {
        super(Machine.class);
    }

    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Override
    public List<MiniComponentData> findMachinesForMatchWithMold() {

        QMachine machine = QMachine.machine;
        QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(machine.id.notIn(JPAExpressions.select(mold.machineId)
                .from(mold)
                .where(mold.machineId.isNotNull())));

        JPQLQuery query = from(machine)
                .where(builder.and(machine.deleted.isNull().or(machine.deleted.isFalse())))
                .select(Projections.constructor(MiniComponentData.class, machine.id, machine.machineCode));

        return query.fetch();
    }

    @Override
    public Page<MiniComponentData> findMachineNotMatched(Predicate predicate, Pageable pageable) {
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            directions[0] = order.getDirection();
        });

        QMachine machine = QMachine.machine;
        QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder(predicate);

        builder.and(machine.id.notIn(JPAExpressions.select(mold.machineId)
                .from(mold)
                .where(mold.machineId.isNotNull())));

        JPQLQuery query = from(machine)
                .where(builder)
                .select(Projections.constructor(MiniComponentData.class, machine.id, machine.machineCode));
        long total = query.fetchCount();

        StringExpression expression = machine.machineCode;
        OrderSpecifier order = expression.desc();
        if (directions[0].equals(Sort.Direction.ASC)) {
            order = expression.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        List<MiniComponentData> list = query.fetch();

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public Page<MachineMoldData> findMachineToMatch(Predicate predicate, Pageable pageable) {
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            directions[0] = order.getDirection();
        });

        QMachine machine = QMachine.machine;
        QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder(predicate);
        JPQLQuery query = from(machine).leftJoin(mold).on(mold.machineId.eq(machine.id))
                .where(builder)
                .select(Projections.constructor(MachineMoldData.class, machine.id, machine.machineCode, mold.id, mold.equipmentCode));
        long total = query.fetchCount();

        StringExpression expression = machine.machineCode;
        OrderSpecifier order = expression.desc();
        if (directions[0].equals(Sort.Direction.ASC)) {
            order = expression.asc();
        }
        query.orderBy(order);
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        List<MachineMoldData> list = query.fetch();

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public List<ObjectCustomFieldValue> findAndSortWithCustomFieldValue(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        Long customFieldId = Long.valueOf((properties[0].split("-"))[1]);

        QMachine machine = QMachine.machine;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;

        JPQLQuery query = from(machine)
                .leftJoin(customFieldValue).on(machine.id.eq(customFieldValue.objectId));
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
        StringExpression expression2 = machine.machineCode;
        OrderSpecifier order2 = expression2.desc();
        if (directions[0].equals(Sort.Direction.ASC)) {
            order = expression.asc();
        }

        query.orderBy(order, order2);
        query.select(Projections.constructor(ObjectCustomFieldValue.class, machine, customFieldValue.id));
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public Page<MachineData> findAllMachineForStatistics(Predicate predicate, Pageable pageable, String day) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });


        QMachine machine = QMachine.machine;
        QMachineStatistics machineStatistics = QMachineStatistics.machineStatistics;
        BooleanBuilder booleanBuilder = new BooleanBuilder(predicate);
        NumberExpression<Integer> dailyWorkHour = (new CaseBuilder().when(machineStatistics.isNull()).then(24).otherwise(
                new CaseBuilder().when(machineStatistics.dailyWorkingHour.isNotNull()).then(machineStatistics.dailyWorkingHour).otherwise(24)
        ));
        NumberExpression<Integer> plannedDowntime = (new CaseBuilder().when(machineStatistics.isNull()).then(0).otherwise(
                new CaseBuilder().when(machineStatistics.plannedDowntime.isNotNull()).then(machineStatistics.plannedDowntime).otherwise(0)
        ));
        NumberExpression<Integer> unplannedDowntime = (new CaseBuilder().when(machineStatistics.isNull()).then(0).otherwise(
                new CaseBuilder().when(machineStatistics.unplannedDowntime.isNotNull()).then(machineStatistics.unplannedDowntime).otherwise(0)
        ));
        NumberExpression<Integer> actualWorkHour = (new CaseBuilder().when(machineStatistics.isNull()).then(24).otherwise(
                (new CaseBuilder().when(machineStatistics.dailyWorkingHour.isNotNull()).then(machineStatistics.dailyWorkingHour).otherwise(24))
                        .subtract(new CaseBuilder().when(machineStatistics.plannedDowntime.isNotNull()).then(machineStatistics.plannedDowntime).otherwise(0))
                        .subtract(new CaseBuilder().when(machineStatistics.unplannedDowntime.isNotNull()).then(machineStatistics.unplannedDowntime).otherwise(0))
        ));

        JPQLQuery query = from(machine)
                .leftJoin(machineStatistics).on(machine.id.eq(machineStatistics.machine.id).and(machineStatistics.day.eq(day)))
                .where(booleanBuilder);
        if ("dailyWorkingHour".equalsIgnoreCase(properties[0])) {
            NumberExpression expression = dailyWorkHour;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        } else if ("plannedDowntime".equalsIgnoreCase(properties[0])) {
            NumberExpression expression = plannedDowntime;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        } else if ("unplannedDowntime".equalsIgnoreCase(properties[0])) {
            NumberExpression expression = unplannedDowntime;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        } else if ("actualWorkingHour".equalsIgnoreCase(properties[0])) {
            NumberExpression expression = actualWorkHour;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        } else {
            StringExpression expression = machine.machineCode;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        }

        query.select(Projections.constructor(MachineData.class, machine.id, machine));

        long total = query.fetchCount();

        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        List<MachineData> list = query.fetch();

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QMachine machine = QMachine.machine;
        QCustomField customField = QCustomField.customField;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(machine.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(machine.createdBy.eq(SecurityUtils.getUserId())));
		}

        if (!StringUtils.isEmpty(payload.getQuery())) {
            builder.and(machine.machineCode.containsIgnoreCase(payload.getQuery()));
        }
        if (CollectionUtils.isNotEmpty(payload.getCompanyId())) {
            builder.and(machine.company.id.in(payload.getCompanyId()));
        }

//        if (payload.getIsDataRequest() != null && payload.getIsDataRequest()) {
//            builder.and(machine.id.in(
//                    JPAExpressions.select(dataRequestObject.objectId)
//                            .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
//                            .where(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION)
//                                    .and(dataRequestObject.objectType.eq(ObjectType.MACHINE)))
//            ));
//        }

        if (payload.getDataRequestId() != null) {
            builder.and(machine.id.in(
                    JPAExpressions.select(dataRequestObject.objectId)
                            .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
                            .where(
                                    dataRequestObject.objectType.eq(ObjectType.MACHINE)
                                            .and(dataRequest.id.eq(payload.getDataRequestId()))
                            )
            ));
        }

        if (payload.getObjectId() != null) {
            builder.and(machine.id.eq(payload.getObjectId()));
        }

        builder.and(machine.deleted.isNull().or(machine.deleted.isFalse()))
                .and(machine.enabled.isNull().or(machine.enabled.isTrue()))
                .and(machine.company.isEmoldino.isFalse())
        ;

        if(payload.getIgnoreDashboardFilter() == null || !payload.getIgnoreDashboardFilter()) {
            builder.and(dashboardGeneralFilterUtils.getMachineFilter(machine));
        }


        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(machine.machineCode.isEmpty().or(machine.machineCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.line.isEmpty().or(machine.line.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineMaker.isEmpty().or(machine.machineMaker.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineType.isEmpty().or(machine.machineType.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineModel.isEmpty().or(machine.machineModel.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineTonnage.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.location.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.company.isNull()).then(0).otherwise(1))
                        .add(JPAExpressions
                                .select(customFieldValue.id.count())
                                .from(customFieldValue)
                                .where(customFieldValue.objectId.eq(machine.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
                        );

        JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.MACHINE));
        long totalCustomField = queryCountCustomField.fetchCount();
        NumberExpression rateValue;

        if (forAvg){
            rateValue = (numEntered.floatValue().divide(Const.numberLine.MACHINE + totalCustomField)).avg();
        } else {
            rateValue = numEntered.floatValue().divide(Const.numberLine.MACHINE + totalCustomField);
        }

        if (payload.isUncompletedData()){
            builder.and(rateValue.doubleValue().lt(1));
        }
        if (CollectionUtils.isNotEmpty(payload.getIds())){
            builder.and(machine.id.in(payload.getIds()));
        }

        JPQLQuery query = from(machine)
                .where(builder);

        if ("rate".equalsIgnoreCase(properties[0])) {
            NumberExpression numberExpression = rateValue;
            OrderSpecifier numberOrder = numberExpression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                numberOrder = numberExpression.asc();
            }
            query.orderBy(numberOrder);
        } else {
            StringExpression expression = machine.machineCode;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        }
        long total = query.fetchCount();
        if (total > 0) {
            query.select(Projections.constructor(CompletionRateData.class, machine.id, machine.machineCode, machine.machineCode, numEntered, rateValue, machine.updatedAt, machine));

            if (!forAvg) {
                query.offset(pageable.getOffset());
                query.limit(pageable.getPageSize());
            }
            List<CompletionRateData> list = query.fetch();
            return new PageImpl<>(list, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public CompletionRateData getCompanyCompletionRate(Long companyId) {
        QMachine machine = QMachine.machine;
        QCustomField customField = QCustomField.customField;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(machine.company.id.eq(companyId))
                .and(machine.deleted.isNull().or(machine.deleted.isFalse()))
                .and(machine.enabled.isNull().or(machine.enabled.isTrue()))
                .and(machine.company.isEmoldino.isFalse());

        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(machine.machineCode.isEmpty().or(machine.machineCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.line.isEmpty().or(machine.line.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineMaker.isEmpty().or(machine.machineMaker.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineType.isEmpty().or(machine.machineType.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineModel.isEmpty().or(machine.machineModel.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.machineTonnage.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.location.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(machine.company.isNull()).then(0).otherwise(1))
                        .add(JPAExpressions
                                .select(customFieldValue.id.count())
                                .from(customFieldValue)
                                .where(customFieldValue.objectId.eq(machine.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
                        );

        JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.MACHINE));
        long totalCustomField = queryCountCustomField.fetchCount();

        NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.MACHINE + totalCustomField)).avg();
        JPQLQuery query = from(machine)
                .where(builder);
        if (query.fetchCount() > 0) {
            query.select(Projections.constructor(CompletionRateData.class, machine.companyId, rateValue, machine.updatedAt));
            return (CompletionRateData) query.fetchFirst();
        } else {
            return new CompletionRateData(companyId, 0D);
        }
    }

    @Override
    public List<Machine> findMachineMatchedWithTooling() {
        QMachine machine = QMachine.machine;
        QMold mold = QMold.mold;

        JPQLQuery query = from(machine).where(machine.id.in(JPAExpressions
                .select(mold.machineId)
                .from(mold)
                .where(mold.machineId.isNotNull())));
        query.select(machine);
        return query.fetch();
    }

    @Override
    public List<Location> findAllLocationByMachineId(List<Long> machineIdList) {
        QMachine machine = QMachine.machine;
        JPQLQuery<Location> query = from(machine).select(machine.location).where(machine.id.in(machineIdList));
        return query.fetch();
    }

    @Override
    public Optional<Location> findLocationByMachineId(Long id) {
        QMachine machine = QMachine.machine;
        JPQLQuery<Location> query = from(machine).select(machine.location).where(machine.id.eq(id));
        return Optional.ofNullable(query.fetchOne() == null ? null : query.fetchOne());
    }

    @Override
    public Page<Machine> findAllOrderBySpecial(String machineCode, Predicate predicate, Pageable pageable) {
        QMachine machine = QMachine.machine;


        JPQLQuery query = from(machine).where(predicate);
        long total = query.fetchCount();

        OrderSpecifier orderBy = machine.machineCode.asc();
        if (StringUtils.isEmpty(machineCode)) {
            query.orderBy(orderBy);
        }
        else {
            NumberExpression<Integer> sortByExactMatched = new CaseBuilder().when(machine.machineCode.eq(machineCode)).then(1).otherwise(0);
            query.orderBy(sortByExactMatched.desc(), orderBy);
        }
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        List<Machine> list = query.fetch();

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public Long countAllIncompleteData() {

        return getQueryIncompleteData().fetchCount();
    }

    @Override
    public List<Machine> getAllIncompleteData() {
        return getQueryIncompleteData().fetch();
    }

    private JPQLQuery getQueryIncompleteData() {
        QMachine machine = QMachine.machine;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;

        BooleanBuilder builder = new BooleanBuilder();
        BooleanExpression isCompleteData =
                new CaseBuilder().when(
                        machine.machineCode.isEmpty().or(machine.machineCode.isNull())
                                .or(machine.line.isEmpty()).or(machine.line.isNull())
                                .or(machine.machineMaker.isEmpty()).or(machine.machineMaker.isNull())
                                .or(machine.machineType.isEmpty()).or(machine.machineType.isNull())
                                .or(machine.machineModel.isEmpty()).or(machine.machineModel.isNull())
                                .or(machine.machineTonnage.isNull()).or(machine.location.isNull())
                                .or(machine.company.isNull())
                                .or(JPAExpressions
                                        .select(customFieldValue.id.count())
                                        .from(customFieldValue)
                                        .where(customFieldValue.objectId.eq(machine.id).and(customFieldValue.value.isNull().or(customFieldValue.value.isEmpty()))).gt(0L)
                                )
                ).then(false).otherwise(true);

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(machine.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(machine.createdBy.eq(SecurityUtils.getUserId())));
		}

        builder.and(machine.deleted.isNull().or(machine.deleted.isFalse()))
                .and(machine.enabled.isNull().or(machine.enabled.isTrue()))
                .and(machine.company.isEmoldino.isFalse())
        ;

        builder.and(dashboardGeneralFilterUtils.getMachineFilter(machine));
        builder.and(isCompleteData.isFalse());
        return from(machine).where(builder);
    }

    @Override
    public List<Long> findAllIdByPredicate(Predicate predicate) {
       QMachine machine = QMachine.machine;
        JPQLQuery query = from(machine).where(predicate).select(machine.id);
        return query.fetch();
    }
}
