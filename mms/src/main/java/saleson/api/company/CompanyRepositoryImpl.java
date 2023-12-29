package saleson.api.company;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.dsl.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.common.config.Const;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.RequestDataType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.data.CompanyData;
import saleson.model.data.MiniGeneralData;
import saleson.model.data.completionRate.CompletionRateData;

public class CompanyRepositoryImpl extends QuerydslRepositorySupport implements CompanyRepositoryCustom {
    public CompanyRepositoryImpl() {
        super(Company.class);
    }

    @Lazy
    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Override
    public List<MiniGeneralData> findCompanyByMoldIdIn(List<Long> moldIds){
        if(moldIds == null || moldIds.size() == 0) return new ArrayList<>();
        QMold mold = QMold.mold;
        QCompany company = QCompany.company;

        JPQLQuery query = from(company)
                .innerJoin(mold).on(mold.companyId.eq(company.id))
                .where(mold.id.in(moldIds))
                .select(Projections.constructor(MiniGeneralData.class, mold.id, company.id, company.name, company.companyCode));

        return query.fetch();
    }

    @Override
    public Page<Company> findAllOrderByName(Predicate predicate, Pageable pageable) {
        QCompany company = QCompany.company;

        BooleanBuilder builder = new BooleanBuilder(predicate);
        builder.and(company.isEmoldino.isFalse());

        List<Long> companyIds = dashboardGeneralFilterUtils.getCompanyIds();
        if (CollectionUtils.isNotEmpty(companyIds)) {
            builder.and(company.id.in(companyIds));
        } else {
            //trick for no company
            builder.and(company.id.isNull());
        }

        JPQLQuery query = from(company).where(builder);

        StringExpression expression = company.name;
        OrderSpecifier order = expression.asc();

        query.orderBy(order);
        query.select(Projections.constructor(CompanyData.class, company.id, company));

        long total = query.fetchCount();

        query.limit(pageable.getPageSize());
        query.offset(pageable.getOffset());
        List<CompanyData> list = query.fetch();
        List<Company> companies = list.stream().map(CompanyData::getCompany).collect(Collectors.toList());

        return new PageImpl<>(companies, pageable, total);
    }

    @Override
    public Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
        QCompany company = QCompany.company;
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;

        BooleanBuilder builder = new BooleanBuilder();


        if (AccessControlUtils.isAccessFilterRequired()) {
            if((!SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER))) {
                builder.and(company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())
                        .or(company.createdBy.eq(SecurityUtils.getUserId())));
            }else if((SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER))){
                builder.and(company.id.eq(SecurityUtils.getCompanyId()));
            }
        }

//        if (payload.getIsDataRequest() != null && payload.getIsDataRequest()) {
//            builder.and(company.id.in(
//                    JPAExpressions.select(dataRequestObject.objectId)
//                    .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
//                            .where(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION)
//                                    .and(dataRequestObject.objectType.eq(ObjectType.COMPANY)))
//            ));
//        }

        if (CollectionUtils.isNotEmpty(payload.getCompanyId())){
            builder.and(company.id.in(payload.getCompanyId()));
        }
        if (payload.getCompanyType() != null) {
            builder.and(company.companyType.eq(payload.getCompanyType()));
        }

        if (payload.getIsDashboard() != null && payload.getIsDashboard()) {
            builder.and(dashboardGeneralFilterUtils.getCompanyFilter(company));
        }

        if (!StringUtils.isEmpty(payload.getQuery())) {
            builder.and(company.name.containsIgnoreCase(payload.getQuery()).or(company.companyCode.containsIgnoreCase(payload.getQuery())));
        }

        if (payload.getDataRequestId() != null) {
            builder.and(company.id.in(
                    JPAExpressions.select(dataRequestObject.objectId)
                            .from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
                            .where(
                                    dataRequestObject.objectType.eq(ObjectType.COMPANY)
                                    .and(dataRequest.id.eq(payload.getDataRequestId()))
                            )
            ));
        }

        if (payload.getObjectId() != null) {
            builder.and(company.id.eq(payload.getObjectId()));
        }

        builder.and(company.enabled.isNull().or(company.enabled.isTrue()))
                .and(company.isEmoldino.isFalse())
        ;

        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(company.companyType.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.name.isEmpty().or(company.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.companyCode.isEmpty().or(company.companyCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.address.isEmpty().or(company.address.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.manager.isEmpty().or(company.manager.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.phone.isEmpty().or(company.phone.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.email.isEmpty().or(company.email.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.memo.isEmpty().or(company.memo.isNull())).then(0).otherwise(1))
                ;

        NumberExpression rateValue;

        if (forAvg){
            rateValue = (numEntered.floatValue().divide(Const.numberLine.COMPANY)).avg();
        } else {
            rateValue = numEntered.floatValue().divide(Const.numberLine.COMPANY);
        }

        if (payload.isUncompletedData()){
            builder.and(rateValue.doubleValue().lt(1));
        }
        if (CollectionUtils.isNotEmpty(payload.getIds())){
            builder.and(company.id.in(payload.getIds()));
        }

        JPQLQuery query = from(company)
                .where(builder);

        if ("rate".equalsIgnoreCase(properties[0])) {
            NumberExpression numberExpression = rateValue;
            OrderSpecifier numberOrder = numberExpression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                numberOrder = numberExpression.asc();
            }
            query.orderBy(numberOrder);
        } else {
            StringExpression expression = company.name;
            OrderSpecifier order = expression.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = expression.asc();
            }
            query.orderBy(order);
        }

        long total = query.fetchCount();
        if (total > 0){
            query.select(Projections.constructor(CompletionRateData.class, company.id, company.companyCode, company.name, numEntered, rateValue, company.updatedAt, company));

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
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(company.id.eq(companyId)).and(company.enabled.isNull().or(company.enabled.isTrue())).and(company.isEmoldino.isFalse());

        NumberExpression<Integer> numEntered =
                (new CaseBuilder().when(company.companyType.isNull()).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.name.isEmpty().or(company.name.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.companyCode.isEmpty().or(company.companyCode.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.address.isEmpty().or(company.address.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.manager.isEmpty().or(company.manager.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.phone.isEmpty().or(company.phone.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.email.isEmpty().or(company.email.isNull())).then(0).otherwise(1))
                        .add(new CaseBuilder().when(company.memo.isEmpty().or(company.memo.isNull())).then(0).otherwise(1))
                ;

        NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.COMPANY)).avg();
        JPQLQuery query = from(company)
                .where(builder);
        query.select(Projections.constructor(CompletionRateData.class, company.id, rateValue, company.updatedAt));
        return (CompletionRateData) query.fetchFirst();
    }

	@Override
	public Page<Company> findAllWithTotalMoldCount(Predicate predicate, Pageable pageable) {
        String[] properties = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            properties[0] = order.getProperty();
            directions[0] = order.getDirection();
        });
		QCompany com = QCompany.company;
        QAccessCompanyRelation accessCompanyRelation = QAccessCompanyRelation.accessCompanyRelation;
		QMold mld = QMold.mold;
		JPQLQuery<Integer> count = JPAExpressions.select(mld.count().intValue()).from(mld).where(new BooleanBuilder().and(mld.companyId.eq(com.id)));
		JPQLQuery<Company> query = from(com).where(predicate).select(Projections.constructor(Company.class, com.id, com.companyType, com.companyCode, com.name, com.address,
				com.manager, com.phone, com.email, com.memo, com.enabled, com.isEmoldino, com.createdAt, Expressions.as(count, "moldCount")));

		OrderSpecifier<Integer> orderBy = null;
        List<OrderSpecifier<?>> orderbyOtherCol = new ArrayList<>(query.getMetadata().getOrderBy());
		if (pageable.getSort() != null && pageable.getSort().isSorted()) {
			org.springframework.data.domain.Sort.Order order = pageable.getSort().getOrderFor("moldCount");
			if (order != null) {
				pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
				orderBy = order.isAscending() ? Expressions.numberPath(Integer.class, "moldCount").asc() : Expressions.numberPath(Integer.class, "moldCount").desc();
			}


		}
        if (properties[0].startsWith(SpecialSortProperty.upperTierCompanies)) {
            query = from(com)
                    .leftJoin(accessCompanyRelation).on(com.id.eq(accessCompanyRelation.companyId).and(accessCompanyRelation.id.in(JPAExpressions
                            .select(accessCompanyRelation.id.min())
                            .from(accessCompanyRelation)
                            .groupBy(accessCompanyRelation.companyId))))
                    .where(predicate).select(Projections.constructor(Company.class, com.id, com.companyType, com.companyCode, com.name, com.address,
                            com.manager, com.phone, com.email, com.memo, com.enabled, com.isEmoldino, com.createdAt, Expressions.as(count, "moldCount")));
            StringExpression name = accessCompanyRelation.accessHierarchyParent.company.companyCode;
            OrderSpecifier<String> order = name.desc();
            if (directions[0].equals(Sort.Direction.ASC)) {
                order = name.asc();
            }
            query.orderBy(order);
        }
        else{
            orderbyOtherCol.addAll(
                    pageable.getSort().stream().map(orderq -> new OrderSpecifier<>(
                            Order.valueOf(orderq.getDirection().toString()),
                            new PathBuilder<>(Object.class, "company").get(orderq.getProperty(), String.class))
                    ).collect(Collectors.toList())
            );
        }

        JPQLQuery<Company> finalQuery = query;
        orderbyOtherCol.forEach(finalQuery::orderBy);
        if (orderBy!=null){
            query.orderBy(orderBy);
        }
        long total = query.fetchCount();
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());
		Page<Company> page = new PageImpl<>(query.fetch(), pageable, total);
		return page;
	}

    @Override
    public List<Company> getListCompany(List<String> companyCodeList, Pageable pageable, String searchText) {
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(company.enabled.isTrue());

        if (companyCodeList != null) {
            builder.and(company.companyCode.in(companyCodeList));
        }

        if (!StringUtils.isEmpty(searchText)) {
            builder.and(company.companyCode.like('%'+searchText+'%'));
        }

        JPQLQuery query = from(company)
                .where(builder).limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        return query.fetch();

    }

	@Override
	public Long countByProduct(Long productId, Long partId, Long moldId) {
		JPQLQuery<Company> query = toQueryByProduct(productId, partId, moldId);
		return query.fetchCount();
	}

    @Override
    public Long countByBrand(Long brandId, Long partId, Long moldId) {
        JPQLQuery<Company> query = toQueryByBrand(brandId, partId, moldId);
        return query.fetchCount();
    }

    @Override
	public Page<Company> findByProject(Long productId, Long partId, Long moldId, Pageable pageable) {
		JPQLQuery<Company> query = toQueryByProduct(productId, partId, moldId);
		long total = query.fetchCount();
		QCompany company = QCompany.company;
		query.orderBy(company.id.asc());
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		return new PageImpl<>(query.fetch(), pageable, total);
	}

    @Override
	public Page<Company> findByBrand(Long brandId, Long partId, Long moldId, Pageable pageable) {
		JPQLQuery<Company> query = toQueryByBrand(brandId, partId, moldId);
		long total = query.fetchCount();
		QCompany company = QCompany.company;
		query.orderBy(company.id.asc());
		query.limit(pageable.getPageSize());
		query.offset(pageable.getOffset());
		return new PageImpl<>(query.fetch(), pageable, total);
	}

	private JPQLQuery<Company> toQueryByProduct(Long productId, Long partId, Long moldId) {
		QCompany company = QCompany.company;
		QMoldPart moldPart = QMoldPart.moldPart;

		JPQLQuery<Long> subQuery;
		{
			BooleanBuilder subFilter = new BooleanBuilder();
			subFilter.and(moldPart.part.categoryId.eq(productId));
			subFilter.and(moldPart.mold.deleted.isFalse());
			subFilter.and(moldPart.mold.equipmentStatus.notIn(EquipmentStatus.FAILURE));
			if (partId != null) {
				subFilter.and(moldPart.partId.eq(partId));
			}
			if (moldId != null) {
				subFilter.and(moldPart.moldId.eq(moldId));
			}
			subQuery = JPAExpressions.select(moldPart.mold.companyId).from(moldPart).where(subFilter);
		}

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(company.id.in(subQuery));

		JPQLQuery<Company> query = from(company).where(filter);
		return query;
	}

	private JPQLQuery<Company> toQueryByBrand(Long brandId, Long partId, Long moldId) {
		QCompany company = QCompany.company;
		QMoldPart moldPart = QMoldPart.moldPart;

		JPQLQuery<Long> subQuery;
		{
			BooleanBuilder subFilter = new BooleanBuilder();
			subFilter.and(moldPart.part.categoryId.in(ProductUtils.filterProductByBrand(brandId)));
			subFilter.and(moldPart.mold.deleted.isFalse());
			subFilter.and(moldPart.mold.equipmentStatus.notIn(EquipmentStatus.FAILURE));
			if (partId != null) {
				subFilter.and(moldPart.partId.eq(partId));
			}
			if (moldId != null) {
				subFilter.and(moldPart.moldId.eq(moldId));
			}
			subQuery = JPAExpressions.select(moldPart.mold.companyId).from(moldPart).where(subFilter);
		}

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(company.id.in(subQuery));

		JPQLQuery<Company> query = from(company).where(filter);
		return query;
	}

    @Override
    public List<Company> findAllByGeneralFilter(boolean isToolmaker, DashboardGeneralFilter filter, boolean isAll) {
        QCompany company = QCompany.company;
        QMold mold = QMold.mold;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(company.enabled.isTrue());
        builder.and(company.isEmoldino.isFalse());
        NumberPath<Long> qCompanyId = mold.supplierCompanyId;
        if (isToolmaker) qCompanyId = mold.toolMakerCompanyId;
        if (AccessControlUtils.isAccessFilterRequired()) {
            if (isToolmaker) builder.and(company.id.eq(SecurityUtils.getCompanyId()));
            else
                builder.and(company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())
                        .or(company.createdBy.eq(SecurityUtils.getUserId())));
        }
        if (filter != null) {
            builder.and(company.id.in(JPAExpressions
                    .select(qCompanyId).distinct()
                    .from(mold)
                    .where(dashboardGeneralFilterUtils.getMoldFilterCustom(mold, filter))));
        } else if (isAll) {
            builder.and(company.id.in(JPAExpressions
                    .select(qCompanyId).distinct()
                    .from(mold)
                    .where(dashboardGeneralFilterUtils.getMoldFilterCommon(mold))));
        } else {
            builder.and(company.id.in(JPAExpressions
                    .select(qCompanyId).distinct()
                    .from(mold)
                    .where(dashboardGeneralFilterUtils.getMoldFilter(mold))));
        }

        JPQLQuery query = from(company).where(builder);
        query.select(company);
        return query.fetch();
    }

    @Override
    public List<Long> findAllIdByPredicate(Predicate predicate) {
        QCompany company = QCompany.company;
        JPQLQuery query = from(company).where(predicate).select(company.id);
        return query.fetch();
    }

    @Override
    public Long countByProductIdIn(List<Long> productIdList) {
        QCompany company = QCompany.company;
        QMoldPart moldPart = QMoldPart.moldPart;

        JPQLQuery<Long> subQuery;
        {
            BooleanBuilder subFilter = new BooleanBuilder();
            subFilter.and(moldPart.part.categoryId.in(productIdList));
            subFilter.and(moldPart.mold.deleted.isFalse());
            subFilter.and(moldPart.mold.equipmentStatus.notIn(EquipmentStatus.FAILURE));
            subQuery = JPAExpressions.select(moldPart.mold.companyId).from(moldPart).where(subFilter);
        }

        BooleanBuilder filter = new BooleanBuilder();
        filter.and(company.id.in(subQuery));

        JPQLQuery<Company> query = from(company).where(filter);
        return query.fetchCount();
    }

    @Override
    public Long countAllIncompleteData() {
        return getQueryAllIncompleteData().fetchCount();
    }

    @Override
    public List<Company> findAllIncompleteData() {
        return getQueryAllIncompleteData().fetch();
    }

    private JPQLQuery getQueryAllIncompleteData() {
        QCompany company = QCompany.company;

        BooleanBuilder builder = new BooleanBuilder();
        BooleanExpression isCompleteData = new CaseBuilder().when(company.companyType.isNull()
                        .or(company.name.isEmpty()).or(company.name.isNull())
                        .or(company.companyCode.isEmpty()).or(company.companyCode.isNull())
                        .or(company.address.isEmpty()).or(company.address.isNull())
                        .or(company.manager.isEmpty()).or(company.manager.isNull())
                        .or(company.phone.isEmpty()).or(company.phone.isNull())
                        .or(company.email.isEmpty()).or(company.email.isNull())
                        .or(company.memo.isEmpty()).or(company.memo.isNull()))
                .then(false).otherwise(true);
        builder.and(isCompleteData.isFalse())
                .and(company.enabled.isNull()
                        .or(company.enabled.isTrue()))
                .and(company.isEmoldino.isFalse());

        return from(company).where(builder);
    }
}
