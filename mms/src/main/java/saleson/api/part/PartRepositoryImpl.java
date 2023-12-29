package saleson.api.part;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.part.payload.PartPayload;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.common.config.Const;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.StorageType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.Cdata;
import saleson.model.DashboardGeneralFilter;
import saleson.model.Part;
import saleson.model.PartCustomFieldValue;
import saleson.model.QCategory;
import saleson.model.QCompany;
import saleson.model.QDataRequest;
import saleson.model.QDataRequestObject;
import saleson.model.QFileStorage;
import saleson.model.QLocation;
import saleson.model.QMold;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.model.QStatistics;
import saleson.model.QStatisticsPart;
import saleson.model.customField.QCustomField;
import saleson.model.customField.QCustomFieldValue;
import saleson.model.data.ChartData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.MiniGeneralData;
import saleson.model.data.PartWithStatisticsData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.model.data.cycleTime.CycleTimeOverviewData;
import saleson.model.data.dashboard.otd.OTDData;
import saleson.model.data.dashboard.otd.OTDDetailsData;
import saleson.model.data.productivity.ProductivityOverviewData;

public class PartRepositoryImpl extends QuerydslRepositorySupport implements PartRepositoryCustom {
    public PartRepositoryImpl() {
        super(Cdata.class);
    }

    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Override
    public Page<PartWithStatisticsData> findWithSpecialSort(PartPayload payload, Pageable pageable){
        BooleanBuilder predicate = new BooleanBuilder(payload.getPredicate());
        if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
            predicate.and(dashboardGeneralFilterUtils.getPartFilter(QPart.part));
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        QPart part = QPart.part;
        QMoldPart moldPart = QMoldPart.moldPart;

        NumberExpression sortValue = moldPart.count();

        JPQLQuery query = from(part);
        if(properties[0].equalsIgnoreCase("totalMolds")) {
            query.leftJoin(moldPart).on(part.id.eq(moldPart.partId));
            query.where(predicate);
        }else if(properties[0].equalsIgnoreCase("weight")) {
            sortValue =  part.weight.castToNum(Float.class);
            query.where(predicate);
        }else{
            QStatistics statistics = QStatistics.statistics;
            QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

			NumberPath ct;
			NumberPath<Integer> shotCount;
			if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
				ct = statistics.ctVal;
				shotCount = statistics.shotCountVal;
			} else {
				ct = statistics.ct;
				shotCount = statistics.shotCount;
			}

            BooleanBuilder builder = (BooleanBuilder) predicate;
			if (AccessControlUtils.isAccessFilterRequired()) {
				builder.and(statistics.moldId.in(JPQLQueryUtils.getMoldIdsSubquery()));
			}

            // [2021-02-26] Ignore shot with CT == 0
            builder.and(ct.gt(0).or(statistics.firstData.isTrue()));
            builder.and(shotCount.isNotNull().and(shotCount.gt(0))) ;
            builder.and(part.deleted.isNull().or(part.deleted.isFalse()));

//            if(payload != null && payload.getStartTime() != null && payload.getEndTime() != null){
//                builder.and(statistics.createdAt.between(Instant.ofEpochSecond(payload.getStartTime()), Instant.ofEpochSecond(payload.getEndTime())));
//            }
            query.leftJoin(statisticsPart).on(statisticsPart.partId.eq(part.id));
            if(payload != null){
                if(payload.getTimePeriod() != null) {
                    String timePeriod = payload.getTimePeriod();
                    if (timePeriod.startsWith("Y") || timePeriod.startsWith("y")) {
                        query.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder).and(statistics.year.eq(timePeriod.substring(1))));
                    } else if (timePeriod.startsWith("M") || timePeriod.startsWith("m")) {
                        query.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder).and(statistics.month.eq(timePeriod.substring(1))));
                    } else if (timePeriod.startsWith("W") || timePeriod.startsWith("w")) {
                        query.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder).and(statistics.week.eq(timePeriod.substring(1))));
                    }
                }else if(payload.getStartDate() != null && payload.getEndDate() != null){
                    query.leftJoin(statistics)
                            .on(statistics.id.eq(statisticsPart.statisticsId).and(builder)
                                    .and(statistics.day.goe(payload.getStartDate()))
                                    .and(statistics.day.loe(payload.getEndDate())));
                }else {
                    query.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder));
                }
            }else {
                query.leftJoin(statistics).on(statistics.id.eq(statisticsPart.statisticsId).and(builder));
            }
            sortValue = (shotCount.multiply(statisticsPart.cavity)).sum();
        }

        OrderSpecifier order = sortValue.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = sortValue.asc();
        }
        if(properties[0].equalsIgnoreCase("weight")){
        query.select(Projections.constructor(PartWithStatisticsData.class, part));
        }else
        query.select(Projections.constructor(PartWithStatisticsData.class, part, sortValue));
        query.groupBy(part.id).orderBy(order);

        Long total = query.fetchCount();

        query.limit(pageable.getPageSize())
                .offset(pageable.getOffset());

        List<PartWithStatisticsData> resultList = query.fetch();

        Page<PartWithStatisticsData> result = new PageImpl<>(resultList, pageable, total);
        return result;
    }

    @Override
    public ProductivityOverviewData findProducedQuantity(List<Long> moldIds, ProductivitySearchPayload payload){
        QStatistics statistics = QStatistics.statistics;
        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
        QMold mold = QMold.mold;

		NumberPath ct;
		NumberPath<Integer> shotCount;
		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
			ct = statistics.ctVal;
			shotCount = statistics.shotCountVal;
		} else {
			ct = statistics.ct;
			shotCount = statistics.shotCount;
		}

        JPQLQuery query = from(statistics)
                .innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
                .innerJoin(mold).on(mold.id.eq(statistics.moldId));

        BooleanBuilder builder = new BooleanBuilder();
        if(moldIds != null) builder.and(mold.id.in(moldIds));
        if(payload.getPartId() != null) builder.and(statisticsPart.partId.eq(payload.getPartId()));
        if(payload.getStartDate() != null && payload.getEndDate() != null){
            builder.and(statistics.day.between(payload.getStartDate(), payload.getEndDate()));
        }else if(payload.getEndDate() != null){
            builder.and(statistics.day.loe(payload.getEndDate()));
        }

        // [2021-02-26] Ignore shot with CT == 0
        builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

        query.where(builder
                .and(mold.deleted.isNull().or(mold.deleted.isFalse())));
        query.select(Projections.constructor(ProductivityOverviewData.class, (shotCount.multiply(statisticsPart.cavity)).sum().coalesce(0)));
        return (ProductivityOverviewData) query.fetchOne();
    }

    @Override
    public CycleTimeOverviewData findCycleTimeOverviewData(ProductivitySearchPayload payload){
        return null;
    }

    @Override
    public List<String> findCountriesByPartId(Long partId){
        QLocation location = QLocation.location;
        QMoldPart moldPart = QMoldPart.moldPart;
        QMold mold = QMold.mold;

        BooleanBuilder builder = new BooleanBuilder();
        if(partId != null) builder.and(moldPart.partId.eq(partId));

        JPQLQuery query = from(location)
                .innerJoin(mold).on(mold.locationId.eq(location.id))
                .innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
                .where(builder
                        .and(mold.deleted.isNull().or(mold.deleted.isFalse())))
                .select(location.countryCode);
        return query.fetch();
    }

    @Override
    public List<MiniComponentData> findExistsPartCodes(List<String> partCodes) {
        QPart part = QPart.part;
        JPQLQuery query = from(part)
                .select(Projections.constructor(MiniComponentData.class, part.id, part.partCode))
                .where(part.partCode.in(partCodes).and(part.deleted.isNull().or(part.deleted.isFalse())));
        return query.fetch();
    }
    @Override
    public List<MiniComponentData> findExistsPartNames(List<String> names) {
        QPart part = QPart.part;
        JPQLQuery query = from(part)
                .select(Projections.constructor(MiniComponentData.class, part.id, part.name))
                .where(part.name.in(names).and(part.deleted.isNull().or(part.deleted.isFalse())));
        return query.fetch();
    }

    @Override
    public Integer findPartProducedByWeek(Long partId, String week){
        QStatistics statistics = QStatistics.statistics;
        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

		NumberPath ct = statistics.ct;
		NumberPath<Integer> shotCount = statistics.shotCount;
//		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
//			ct = statistics.ctVal;
//			shotCount = statistics.shotCountVal;
//		}


        if(partId == null || week == null) return 0;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(statisticsPart.partId.eq(partId));
        builder.and(statistics.week.eq(week));
        builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

        JPQLQuery query = from(statistics)
                .innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
                .where(builder)
                .select(shotCount.multiply(statisticsPart.cavity).sum().coalesce(0));

        return (Integer) query.fetchFirst();
    }

    @Override
    public OTDData findListDetailsOtd(Long partId, String week, Pageable pageable){
        OTDData result = OTDData.builder().build();

        if(partId == null || week == null) return result;

        QStatistics statistics = QStatistics.statistics;
        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
        QMold mold = QMold.mold;
        QLocation location = QLocation.location;
        QCompany company = QCompany.company;

		NumberPath ct = statistics.ct;
		NumberPath<Integer> shotCount = statistics.shotCount;
//		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
//			ct = statistics.ctVal;
//			shotCount = statistics.shotCountVal;
//		}


        BooleanBuilder builder = new BooleanBuilder();
        builder.and(statisticsPart.partId.eq(partId));
        builder.and(statistics.week.eq(week));
        builder.and(ct.gt(0).or(statistics.firstData.isTrue()));

        JPQLQuery query = from(statistics)
                .innerJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
                .innerJoin(mold).on(statistics.moldId.eq(mold.id))
                .innerJoin(location).on(mold.locationId.eq(location.id))
                .innerJoin(company).on(location.companyId.eq(company.id))
                .groupBy(company.id)
                .where(builder
                        .and(mold.deleted.isNull().or(mold.deleted.isFalse()))
                        .and(dashboardGeneralFilterUtils.getMoldFilter(mold))
                        .and(dashboardGeneralFilterUtils.getSupplierFilter(company)))
                .select(Projections.constructor(OTDDetailsData.class, company.id, company.name,
                        shotCount.multiply(statisticsPart.cavity).sum().coalesce(0)));

        Long totalElements = query.fetchCount();
        Integer totalPages = totalElements.intValue() / pageable.getPageSize() + 1;
        result.setTotalPages(totalPages);
        result.setNumber(pageable.getPageNumber());

        query.limit(pageable.getPageSize());
        query.offset(pageable.getPageSize() * pageable.getPageNumber());

        List<OTDDetailsData> details = query.fetch();
        result.setDetails(details);

        return result;
    }

    @Override
    public List<ChartData> findSupplierTotalCavityPart(Long partId){
        QMold mold = QMold.mold;
        QMoldPart moldPart = QMoldPart.moldPart;
        QLocation location = QLocation.location;
        QCompany company = QCompany.company;

        JPQLQuery query = from(company)
                .innerJoin(location).on(company.id.eq(location.companyId))
                .innerJoin(mold).on(location.id.eq(mold.locationId))
                .innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
                .where(moldPart.partId.eq(partId)
                        .and(mold.deleted.isNull().or(mold.deleted.isFalse()))
                        .and(dashboardGeneralFilterUtils.getMoldFilter(mold))
                        .and(dashboardGeneralFilterUtils.getSupplierFilter(company)))
                .groupBy(company.id)
                .select(Projections.constructor(ChartData.class, company.id, company.companyCode, company.name, moldPart.cavity.sum()));

        return query.fetch();
    }

    @Override
    public List<ChartData> findWeekRemainingCapacityToolings(Long partId, Integer remainingDays){
        QMold mold = QMold.mold;
        QMoldPart moldPart = QMoldPart.moldPart;
        QLocation location = QLocation.location;
        QCompany company = QCompany.company;

        JPQLQuery query = from(company)
                .innerJoin(location).on(location.companyId.eq(company.id))
                .innerJoin(mold).on(mold.locationId.eq(location.id))
                .innerJoin(moldPart).on(moldPart.moldId.eq(mold.id))
                .where(moldPart.partId.eq(partId)
                        .and(mold.deleted.isNull().or(mold.deleted.isFalse())))
                .groupBy(company.id)
                .select(Projections.constructor(ChartData.class, company.id, company.companyCode, company.name,
                        (mold.maxCapacityPerWeek.floatValue().multiply(remainingDays).divide(7)).sum().intValue()));

        return query.fetch();
    }

    @Override
    public List<ChartData> findToolingProducedPart(Long partId, Long companyId, String week){
        QMold mold = QMold.mold;
        QMoldPart moldPart = QMoldPart.moldPart;
        QStatistics statistics = QStatistics.statistics;
        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;

//		NumberPath ct= statistics.ct;
		NumberPath<Integer> shotCount = statistics.shotCount;
//		if (OptionUtils.isEnabled(ConfigCategory.DATA_FILTER)) {
//			ct = statistics.ctVal;
//			shotCount = statistics.shotCountVal;
//		}

        JPQLQuery query = from(mold)
                .innerJoin(moldPart).on(mold.id.eq(moldPart.moldId))
                .leftJoin(statistics).on(statistics.moldId.eq(mold.id).and(statistics.week.eq(week)))
                .leftJoin(statisticsPart).on(statistics.id.eq(statisticsPart.statisticsId))
                .where(moldPart.partId.eq(partId).and(mold.companyId.eq(companyId))
                        .and(mold.deleted.isNull().or(mold.deleted.isFalse())))
                .groupBy(mold.id)
                .select(Projections.constructor(ChartData.class, mold.id, mold.equipmentCode, shotCount.multiply(statisticsPart.cavity).sum().coalesce(0)));

        return query.fetch();
    }

    @Override
    public List<MiniComponentData> findListPart(List<String> partCodeList, String searchText, Long page, Long size){
        QPart part = QPart.part;
        BooleanBuilder builder = new BooleanBuilder();
        if (partCodeList != null) {
            builder.and(part.partCode.in(partCodeList));
        }
        if (StringUtils.isNotBlank(searchText)) {
            builder.and(part.partCode.like('%'+searchText+'%').or(part.name.like('%'+searchText+'%')));
        }
        builder.and(part.deleted.isNull().or(part.deleted.isFalse()));

        JPQLQuery query = from(part)
                .where(builder)
                .select(Projections.constructor(MiniComponentData.class, part.id, part.partCode, part.name));

        if (page != null && size != null) {
            query.offset(size * page);
            query.limit(size);
        }

        return query.fetch();
    }
    @Override
    public List<MiniComponentData> findListPartName(){
        QPart part = QPart.part;

        JPQLQuery query = from(part)
                .where(part.deleted.isNull().or(part.deleted.isFalse()))
                .select(Projections.constructor(MiniComponentData.class, part.id, part.name));

        return query.fetch();
    }

    @Override
    public List<PartCustomFieldValue> findPartCustomFieldValue(Predicate predicate, Pageable pageable)
    {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        Long customFieldId = Long.valueOf((properties[0].split("-"))[1]);

        QPart part = QPart.part;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;

        JPQLQuery query = from(part)
                .leftJoin(customFieldValue).on(part.id.eq(customFieldValue.objectId));
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
        StringExpression expression2 = part.partCode;
        OrderSpecifier order2 = expression2.desc();
        if(directions[0].equals(Sort.Direction.ASC)){
            order = expression.asc();
        }

        query.orderBy(order, order2);
        query.select(Projections.constructor(PartCustomFieldValue.class, part, customFieldValue.id));
        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());

        return query.fetch();
    }

    @Override
    public List<MiniGeneralData> findPartByMoldIdIn(List<Long> moldIds){
        if(moldIds == null || moldIds.size() == 0) return new ArrayList<>();
        QMold mold = QMold.mold;
        QMoldPart moldPart = QMoldPart.moldPart;
        QPart part = QPart.part;

        JPQLQuery query = from(part)
                .innerJoin(moldPart).on(part.id.eq(moldPart.partId))
                .innerJoin(mold).on(moldPart.moldId.eq(mold.id))
                .where(mold.id.in(moldIds).and(part.deleted.isNull().or(part.deleted.isFalse())))
                .select(Projections.constructor(MiniGeneralData.class, mold.id, part.id, part.name, part.partCode));

        return query.fetch();
    }

    @Override
    public Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        QPart part = QPart.part;
        QMoldPart moldPart = QMoldPart.moldPart;
        QCustomField customField = QCustomField.customField;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
        QFileStorage fileStorage = QFileStorage.fileStorage;
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;
        BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}

        if (!saleson.common.util.StringUtils.isEmpty(payload.getQuery())) {
            builder.and(part.partCode.containsIgnoreCase(payload.getQuery()));
        }

//        if (payload.getIsDataRequest() != null && payload.getIsDataRequest()) {
//            builder.and(part.id.in(
//                    JPAExpressions.select(dataRequestObject.objectId)
//                            .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
//                            .where(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION)
//                                    .and(dataRequestObject.objectType.eq(ObjectType.PART)))
//            ));
//        }

        if (payload.getDataRequestId() != null) {
            builder.and(part.id.in(
                    JPAExpressions.select(dataRequestObject.objectId)
                            .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
                            .where(
                                    dataRequestObject.objectType.eq(ObjectType.PART)
                                            .and(dataRequest.id.eq(payload.getDataRequestId()))
                            )
            ));
        }

        if (payload.getObjectId() != null) {
            builder.and(part.id.eq(payload.getObjectId()));
        }

        if (CollectionUtils.isNotEmpty(payload.getCompanyId()) && (payload.getIsDashboard() == null || !payload.getIsDashboard())) {
            builder.and(part.id.in(
                    JPAExpressions
                            .select(moldPart.partId)
                            .from(moldPart)
                            .where(
                                    moldPart.mold.location.companyId.isNotNull()
                                            .and(moldPart.mold.location.companyId.in(payload.getCompanyId()))
                                            .and(moldPart.mold.deleted.isNull().or(moldPart.mold.deleted.isFalse()))))
            );
        }
//        else {
//            builder.and(part.id.in(
//                    JPAExpressions
//                            .select(moldPart.partId)
//                            .from(moldPart)));
//        }
        builder.and(part.deleted.isNull().or(part.deleted.isFalse())).and(part.enabled.isNull().or(part.enabled.isTrue()));

        NumberExpression<Integer> zeroValue = Expressions.asNumber(0);
        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(part.partCode.isEmpty().or(part.partCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.partCode.isEmpty().or(part.partCode.isNull())).then(0).otherwise(1))
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.category) ? (new CaseBuilder().when(part.category.isNull()).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.resinCode) ? (new CaseBuilder().when(part.resinCode.isEmpty().or(part.resinCode.isNull())).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.resinGrade) ? (new CaseBuilder().when(part.resinGrade.isEmpty().or(part.resinGrade.isNull())).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.designRevision) ? (new CaseBuilder().when(part.designRevision.isEmpty().or(part.designRevision.isNull())).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.size) ? (new CaseBuilder().when(part.size.isEmpty().or(part.size.isNull())).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.weight) ? (new CaseBuilder().when(part.weight.isEmpty().or(part.weight.isNull())).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.weeklyDemand) ? (new CaseBuilder().when(part.weeklyDemand.isNull()).then(0).otherwise(1)) : zeroValue)
                        .add(!payload.getDeletedFields().contains(Const.ColumnCode.part.partPictureFile) ? (new CaseBuilder().when(
                                        JPAExpressions
                                                .select(fileStorage.id.min())
                                                .from(fileStorage)
                                                .where(
                                                        fileStorage.refId.eq(part.id)
                                                                .and(fileStorage.storageType.eq(StorageType.PART_PICTURE)))
                                                .isNull())
                                .then(0)
                                .otherwise(1)) : zeroValue)
                        .add(JPAExpressions
                                .select(customFieldValue.id.count())
                                .from(customFieldValue)
                                .where(customFieldValue.objectId.eq(part.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
                        );

        JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.PART));
        long totalCustomField = queryCountCustomField.fetchCount();
        NumberExpression rateValue;

        if (forAvg){
            rateValue = (numEntered.floatValue().divide(Const.numberLine.PART - payload.getDeletedFields().size() + totalCustomField)).avg();
        } else {
            rateValue = numEntered.floatValue().divide(Const.numberLine.PART - payload.getDeletedFields().size() + totalCustomField);
        }

        if (payload.isUncompletedData()){
            builder.and(rateValue.doubleValue().lt(1));
        }
        if (CollectionUtils.isNotEmpty(payload.getIds())){
            builder.and(part.id.in(payload.getIds()));
        }

        if (payload.getIsDashboard() != null && payload.getIsDashboard()) {
            builder.and(dashboardGeneralFilterUtils.getPartFilter(part));

            List<Long> supplierIds = dashboardGeneralFilterUtils.getSupplierIds_Old();
            List<Long> toolmakerIds = dashboardGeneralFilterUtils.getToolMakerIds_Old();
            if (CollectionUtils.isNotEmpty(payload.getCompanyId())) {
                if (CollectionUtils.isNotEmpty(supplierIds) && supplierIds.contains(payload.getCompanyId().get(0))) {
                    builder.and(part.id.in(
                                    JPAExpressions
                                            .select(moldPart.partId)
                                            .from(moldPart)
                                            .where(moldPart.mold.supplierCompanyId.in(payload.getCompanyId()))
                            )
                    );
                }
                if (CollectionUtils.isNotEmpty(toolmakerIds) && toolmakerIds.contains(payload.getCompanyId().get(0))) {
                    builder.and(part.id.in(
                                    JPAExpressions
                                            .select(moldPart.partId)
                                            .from(moldPart)
                                            .where(moldPart.mold.toolMakerCompanyId.in(payload.getCompanyId()))
                            )
                    );
                }
            }
        }
        JPQLQuery query = from(part)
                .where(builder);
        if ("rate".equalsIgnoreCase(properties[0])) {
            NumberExpression numberExpression = rateValue;
            OrderSpecifier numberOrder = numberExpression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                numberOrder = numberExpression.asc();
            }
            query.orderBy(numberOrder);
        } else {
            StringExpression expression = part.name;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        }
        long total = query.fetchCount();

        if (total > 0) {
            query.select(Projections.constructor(CompletionRateData.class, part.id, part.partCode, part.name, numEntered, rateValue, part.updatedAt, part));

            if (!forAvg) {
                query.offset(pageable.getOffset());
                query.limit(pageable.getPageSize());
            }
            List<CompletionRateData> list= query.fetch();
            return new PageImpl<>(list, pageable, total);
        } else {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    @Override
    public CompletionRateData getCompanyCompletionRate(Long companyId) {
        QPart part = QPart.part;
        QMoldPart moldPart = QMoldPart.moldPart;
        QCustomField customField = QCustomField.customField;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(part.id.in(
                JPAExpressions
                        .select(moldPart.partId)
                        .from(moldPart)
                        .where(
                                moldPart.mold.location.companyId.isNotNull()
                                        .and(moldPart.mold.location.companyId.eq(companyId))
                                        .and(moldPart.mold.deleted.isNull().or(moldPart.mold.deleted.isFalse()))))
        );
        builder.and(part.deleted.isNull().or(part.deleted.isFalse())).and(part.enabled.isNull().or(part.enabled.isTrue()));

        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(part.category.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.partCode.isEmpty().or(part.partCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.name.isEmpty().or(part.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.resinCode.isEmpty().or(part.resinCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.resinGrade.isEmpty().or(part.resinGrade.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.designRevision.isEmpty().or(part.designRevision.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.size.isEmpty().or(part.size.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.weight.isEmpty().or(part.weight.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(part.weeklyDemand.isNull()).then(0).otherwise(1))
                        .add(JPAExpressions
                                .select(customFieldValue.id.count())
                                .from(customFieldValue)
                                .where(customFieldValue.objectId.eq(part.id).and(customFieldValue.value.isNotNull().and(customFieldValue.value.isNotEmpty())))
                        );

        JPQLQuery queryCountCustomField = from(customField).where(customField.objectType.eq(ObjectType.PART));
        long totalCustomField = queryCountCustomField.fetchCount();

        NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.PART + totalCustomField)).avg();
        JPQLQuery query = from(part)
                .where(builder);
        if (query.fetchCount() > 0){
            query.select(Projections.constructor(CompletionRateData.class, part.id, rateValue, part.updatedAt));
            return (CompletionRateData) query.fetchFirst();
        } else {
            return new CompletionRateData(companyId, 0D);
        }
    }

    @Override
    public Long countByProduct(Long productId, List<Long> supplierId, Long moldId) {
        JPQLQuery<Part> query = toQueryByProduct(productId, supplierId, moldId, null, false);
        return query.fetchCount();
    }

    @Override
    public Long countByBrand(Long brandId, List<Long> supplierId, Long moldId) {
        JPQLQuery<Part> query = toQueryByProduct(brandId, supplierId, moldId, null, true);
        return query.fetchCount();
    }

    @Override
	public Page<Part> findAllByProduct(Long productId, List<Long> supplierId, Long moldId, Pageable pageable) {
		return findAllByProduct(productId, supplierId, moldId, null, pageable);
	}

    @Override
	public Page<Part> findAllByBrand(Long productId, List<Long> supplierId, Long moldId, Pageable pageable) {
		return findAllByBrand(productId, supplierId, moldId, null, pageable);
	}

	@Override
	public Page<Part> findAllByProduct(Long productId, List<Long> supplierId, Long moldId, String searchWord, Pageable pageable) {
		return findAllByProduct(productId, supplierId, moldId, searchWord, pageable, false);
	}

	@Override
	public Page<Part> findAllByBrand(Long productId, List<Long> supplierId, Long moldId, String searchWord, Pageable pageable) {
		return findAllByProduct(productId, supplierId, moldId, searchWord, pageable, true);
	}

	private Page<Part> findAllByProduct(Long productId, List<Long> supplierId, Long moldId, String searchWord, Pageable pageable, boolean brand) {
		JPQLQuery<Part> query = toQueryByProduct(productId, supplierId, moldId, searchWord, brand);
		long total = query.fetchCount();
		query.orderBy(Q.part.name.asc());
		QueryUtils.applyPagination(query, pageable);
		List<Part> content = query.fetch();
		return new PageImpl<>(content, pageable, total);
	}

	private JPQLQuery<Part> toQueryByProduct(Long productId, List<Long> supplierId, Long moldId, String searchWord, boolean brand) {
		QPart part = QPart.part;

		BooleanBuilder filter = new BooleanBuilder();
		if (brand) {
			filter.and(part.categoryId.in(ProductUtils.filterProductByBrand(productId)));
		} else {
			filter.and(part.categoryId.eq(productId));
		}
		filter.and(part.enabled.isTrue());
		filter.and(part.deleted.isFalse());

		if (!ObjectUtils.isEmpty(searchWord)) {
			String str = "%" + searchWord + "%";
			filter.and(part.name.likeIgnoreCase(str).or(part.partCode.likeIgnoreCase(str)));
		}

		if (moldId != null || !ObjectUtils.isEmpty(supplierId)) {
			QMoldPart moldPart = QMoldPart.moldPart;
			BooleanBuilder subFilter = new BooleanBuilder();
			if (moldId != null) {
				subFilter.and(moldPart.moldId.eq(moldId));
			}
			if (!ObjectUtils.isEmpty(supplierId)) {
				subFilter.and(moldPart.mold.companyId.in(supplierId));
			}
			filter.and(part.id.in(JPAExpressions.select(moldPart.partId).from(moldPart).where(subFilter)));
		}

		JPQLQuery<Part> query = from(part).where(filter);
		return query;
	}

    @Override
    public boolean checkIfSomePartsNotProducedByProjectId(Long projectId) {
        QPart part = QPart.part;
        QStatisticsPart statisticsPart = QStatisticsPart.statisticsPart;
        JPQLQuery query = from(part).where(part.id.notIn(JPAExpressions
                .select(statisticsPart.partId).distinct()
                .from(statisticsPart)).and(part.categoryId.eq(projectId)).and(part.enabled.isTrue()));
        List result = query.fetch();
        return CollectionUtils.isNotEmpty(result);
    }

    @Override
    public List<Part> findAllByGeneralFilter(boolean isAll) {
        if (isAll) {
            return findAllByGeneralFilter(new DashboardGeneralFilter(true, true, true, true));
        }
        QPart part = QPart.part;

        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}
        builder.and(part.enabled.isTrue());
        builder.and(dashboardGeneralFilterUtils.getPartFilter(part));


        JPQLQuery query = from(part).where(builder);
        query.select(part);
        return query.fetch();
    }

    @Override
    public List<Part> findAllByGeneralFilter(DashboardGeneralFilter filter) {
        QPart part = QPart.part;

        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}
        builder.and(part.enabled.isTrue());
        builder.and(dashboardGeneralFilterUtils.getPartFilterCustom(part, filter));


        JPQLQuery query = from(part).where(builder);
        query.select(part);
        return query.fetch();
    }

    @Override
    public Long countByFilter(Predicate predicate) {
        QPart part = QPart.part;

        JPQLQuery query = from(part).where(predicate);
        return query.fetchCount();
    }

    @Override
    public List<Long> findAllIdByPredicate(Predicate predicate) {
        QPart part = QPart.part;

        JPQLQuery query = from(part).select(part.id).where(predicate);
        return query.fetch();
    }

    @Override
    public Page<Part> getPartOrderByMachineId(Predicate predicate, Pageable pageable) {
        QPart part = QPart.part;
        QMoldPart moldPart = QMoldPart.moldPart;

        String property  = pageable.getSort().get().findFirst().get().getProperty();
        Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
        JPQLQuery query = from(part)
                .leftJoin(moldPart).on(part.id.eq(moldPart.partId).and(moldPart.id.in(JPAExpressions
                        .select(moldPart.id)
                        .from(moldPart)
                        .where(moldPart.mold.machine.isNotNull())
                        .groupBy(moldPart.partId))
                ))
                .where(predicate);
        StringExpression machineCode = moldPart.mold.machine.machineCode;
        OrderSpecifier<String> order = isAsc ? machineCode.asc() : machineCode.desc();
        query.orderBy(order);
        long total = query.fetchCount();

        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        return new PageImpl<>(query.fetch(), pageable, total);
    }

    @Override
    public Page<Part> getPartOrderByCategoryName(Predicate predicate, Pageable pageable) {
        QPart part = QPart.part;
        QCategory category = QCategory.category;
        String property  = pageable.getSort().get().findFirst().get().getProperty();
        Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
        StringExpression categoryName = new CaseBuilder().when(category.grandParentId.isNotNull())
                .then(category.grandParent.name).otherwise(category.parent.parent.name);
        OrderSpecifier<String> order = isAsc ? categoryName.asc() : categoryName.desc();

        JPQLQuery query = from(part)
                .leftJoin(category).on(part.categoryId.eq(category.id))
                .where(predicate);
        query.orderBy(order);
        long total = query.fetchCount();

        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        return new PageImpl<>(query.fetch(), pageable, total);
    }

    @Override
    public Long countByProductIdIn(List<Long> productIdList) {
        QPart part = QPart.part;
        BooleanBuilder filter = new BooleanBuilder();
        filter.and(part.categoryId.in(productIdList));
        filter.and(part.enabled.isTrue());
        filter.and(part.deleted.isFalse());

        JPQLQuery<Part> query = from(part).where(filter);
        return query.fetchCount();
    }

    @Override
    public Long countAllIncompleteData() {
        return getQueryIncompleteData().fetchCount();
    }

    @Override
    public List<Part> getAllIncompleteData() {
        return getQueryIncompleteData().fetch();
    }

    private JPQLQuery getQueryIncompleteData() {
        QPart part = QPart.part;
        QCustomFieldValue customFieldValue = QCustomFieldValue.customFieldValue;
        BooleanBuilder builder = new BooleanBuilder();
        BooleanExpression isCompleteData =
                new CaseBuilder().when(part.category.isNull()
                                .or(part.partCode.isEmpty()).or(part.partCode.isNull())
                                .or(part.resinCode.isEmpty()).or(part.resinCode.isNull())
                                .or(part.resinGrade.isEmpty()).or(part.resinGrade.isNull())
                                .or(part.designRevision.isEmpty()).or(part.designRevision.isNull())
                                .or(part.size.isEmpty()).or(part.size.isNull())
                                .or(part.weight.isEmpty()).or(part.weight.isNull()).or(part.weeklyDemand.isNull())
                                .or(JPAExpressions
                                        .select(customFieldValue.id.count())
                                        .from(customFieldValue)
                                        .where(customFieldValue.objectId.eq(part.id).and(customFieldValue.value.isNull().and(customFieldValue.value.isEmpty()))).gt(0L)
                                ))
                        .then(false).otherwise(true);

        builder.and(part.deleted.isNull().or(part.deleted.isFalse())).and(part.enabled.isNull().or(part.enabled.isTrue()));
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}

        builder.and(isCompleteData.isFalse());
        return from(part).where(builder);
    }
}
