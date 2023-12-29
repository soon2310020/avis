package saleson.api.location;

import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.types.dsl.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.common.config.Const;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.RequestDataType;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.data.LocationData;
import saleson.model.data.completionRate.CompletionRateData;

public class LocationRepositoryImpl extends QuerydslRepositorySupport implements LocationRepositoryCustom {

	public LocationRepositoryImpl() {
		super(Location.class);
	}

	@Lazy
	@Autowired
	private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

	@Override
	public List<LocationData> findLocationData(Predicate predicate, Pageable pageable, List<String> locationCodeList) {
		String[] properties = { "" };
		Sort.Direction[] directions = { Sort.Direction.DESC };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});

		QLocation location = QLocation.location;
		QTerminal terminal = QTerminal.terminal;

		NumberExpression sortValue = terminal.id.count();
		OrderSpecifier orderSpecifier = sortValue.desc();
		if (directions[0].equals(Sort.Direction.ASC)) {
			orderSpecifier = sortValue.asc();
		}
		OrderSpecifier orderSpecifierNumberTerminal = orderSpecifier;
		OrderSpecifier[] orderSpecifierArr = pageable.getSort().stream().map(order -> {
			if (SpecialSortProperty.locationSortProperties.contains(order.getProperty()))
				return orderSpecifierNumberTerminal;
			return new OrderSpecifier(//
					Order.valueOf(order.getDirection().toString()), //
					new PathBuilder(Location.class, "location").get(order.getProperty())//
			);
		}).toArray(OrderSpecifier[]::new);
		BooleanBuilder builder = new BooleanBuilder();
		if (locationCodeList != null) {
			builder.and(location.locationCode.in(locationCodeList));
			predicate = builder.and(predicate);
		}

		JPQLQuery query = from(location)//
				.leftJoin(terminal).on(location.id.eq(terminal.locationId))//
				.where(predicate)//
				.groupBy(location.id)//
				.orderBy(orderSpecifierArr)//
				.limit(pageable.getPageSize())//
				.offset(pageable.getOffset())//
				.select(Projections.constructor(LocationData.class, location, sortValue));

		return query.fetch();
	}

	@Override
	public Long countCompletionRateData(DataCompletionRatePayload payload) {
		Long total = 0L;
		return total;
	}

	@Override
	public List<Location> findAllByGeneralFilter(boolean isAll) {
		QLocation location = QLocation.location;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(location.enabled.isTrue());
		builder.and(location.company.isEmoldino.isFalse());
		if (isAll) {
			builder.and(location.id.in(JPAExpressions.select(mold.locationId).distinct()//
					.from(mold)//
					.where(dashboardGeneralFilterUtils.getMoldFilterCommon(mold))));
		} else {
			builder.and(location.id.in(JPAExpressions.select(mold.locationId).distinct()//
					.from(mold)//
					.where(dashboardGeneralFilterUtils.getMoldFilter(mold))));
		}

		JPQLQuery query = from(location).where(builder);
		query.select(location);
		return query.fetch();
	}

	@Override
	public List<Location> findAllByGeneralFilter(DashboardGeneralFilter filter) {
		QLocation location = QLocation.location;
		QMold mold = QMold.mold;

		BooleanBuilder builder = new BooleanBuilder();
		builder.and(location.enabled.isTrue());
		builder.and(location.company.isEmoldino.isFalse());
		builder.and(location.id.in(JPAExpressions.select(mold.locationId).distinct()//
				.from(mold)//
				.where(dashboardGeneralFilterUtils.getMoldFilterCustom(mold, filter))));

		JPQLQuery query = from(location).where(builder);
		query.select(location);
		return query.fetch();
	}

	@Override
	public List<Long> findAllIdByPredicate(Predicate predicate) {
		QLocation location = QLocation.location;
		JPQLQuery query = from(location).where(predicate).select(location.id);
		return query.fetch();
	}

	@Override
	public Long countAllIncompleteData() {

		return getQueryIncompleteData().fetchCount();
	}

	@Override
	public List<Location> getAllIncompleteData() {
		return getQueryIncompleteData().fetch();
	}

	private JPQLQuery getQueryIncompleteData() {
		QLocation location = QLocation.location;

		BooleanBuilder builder = new BooleanBuilder();
		BooleanExpression isCompleteData = new CaseBuilder().when(
						location.locationCode.isEmpty().or(location.locationCode.isNull())
								.or(location.name.isEmpty()).or(location.name.isNull())
								.or(location.address.isEmpty()).or(location.address.isNull())
								.or(location.memo.isEmpty()).or(location.memo.isNull())
								.or(location.company.isNull()))
				.then(false).otherwise(true);

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(location.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(location.createdBy.eq(SecurityUtils.getUserId())));
		}
		if (CompanyType.TOOL_MAKER == SecurityUtils.getCompanyType()) {
			builder.and(location.company.id.eq(SecurityUtils.getCompanyId()));
		}

		builder.and(location.enabled.isNull()
						.or(location.enabled.isTrue()))
				.and(location.company.isEmoldino.isFalse());

		builder.and(isCompleteData.isFalse());
		return from(location).where(builder);
	}

	@Override
	public Page<CompletionRateData> getCompletionRateData(DataCompletionRatePayload payload, Pageable pageable, boolean forAvg) {
		String[] properties = { "" };
		Sort.Direction[] directions = { Sort.Direction.DESC };
		pageable.getSort().forEach(order -> {
			properties[0] = order.getProperty();
			directions[0] = order.getDirection();
		});
		QLocation location = QLocation.location;
		QMold mold = QMold.mold;
        QDataRequest dataRequest = QDataRequest.dataRequest;
        QDataRequestObject dataRequestObject = QDataRequestObject.dataRequestObject;
		BooleanBuilder builder = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(location.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(location.createdBy.eq(SecurityUtils.getUserId())));
		}

		if (!StringUtils.isEmpty(payload.getQuery())) {
			builder.and(location.name.containsIgnoreCase(payload.getQuery()));
		}

//        if (payload.getIsDataRequest() != null && payload.getIsDataRequest()) {
//			builder.and(location.id.in(
//					JPAExpressions.select(dataRequestObject.objectId)
//							.from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
//							.where(dataRequest.requestDataType.eq(RequestDataType.DATA_COMPLETION)
//									.and(dataRequestObject.objectType.eq(ObjectType.LOCATION))))
//            );
//        }

		if (payload.getDataRequestId() != null) {
			builder.and(location.id.in(
					JPAExpressions.select(dataRequestObject.objectId)
							.from(dataRequestObject).innerJoin(dataRequest).on(dataRequestObject.dataRequestId.eq(dataRequest.id))
							.where(
									dataRequestObject.objectType.eq(ObjectType.LOCATION)
											.and(dataRequest.id.eq(payload.getDataRequestId()))
							)
			));
		}

		if (payload.getObjectId() != null) {
			builder.and(location.id.eq(payload.getObjectId()));
		}

		if (SecurityUtils.getCompanyType().equals(CompanyType.TOOL_MAKER)) {
			builder.and(location.company.id.eq(SecurityUtils.getCompanyId()));
		}

		if (CollectionUtils.isNotEmpty(payload.getCompanyId()) && (payload.getIsDashboard() == null || !payload.getIsDashboard())) {
			builder.and(location.company.id.in(payload.getCompanyId()));
		}
		if (payload.getCompanyType() != null) {
			builder.and(location.company.companyType.eq(payload.getCompanyType()));
		}

		if (payload.getIsDashboard() != null && payload.getIsDashboard()) {
			builder.and(dashboardGeneralFilterUtils.getLocationFilter(location));
			List<Long> supplierIds = dashboardGeneralFilterUtils.getSupplierIds_Old();
			List<Long> toolmakerIds = dashboardGeneralFilterUtils.getToolMakerIds_Old();
			if (CollectionUtils.isNotEmpty(payload.getCompanyId())) {
				if (CollectionUtils.isNotEmpty(supplierIds) && supplierIds.contains(payload.getCompanyId().get(0))) {
					builder.and(location.id.in(JPAExpressions//
							.select(mold.locationId).distinct()//
							.from(mold)//
							.where(mold.supplierCompanyId.in(payload.getCompanyId()))));
				}
				if (CollectionUtils.isNotEmpty(toolmakerIds) && toolmakerIds.contains(payload.getCompanyId().get(0))) {
					builder.and(location.id.in(JPAExpressions//
							.select(mold.locationId).distinct()//
							.from(mold)//
							.where(mold.toolMakerCompanyId.in(payload.getCompanyId()))));
				}
			}
		}

		builder.and(location.enabled.isNull()
						.or(location.enabled.isTrue()))
				.and(location.company.isEmoldino.isFalse())
		;
		NumberExpression<Integer> numEntered = //
				(new CaseBuilder().when(location.locationCode.isEmpty().or(location.locationCode.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.name.isEmpty().or(location.name.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.address.isEmpty().or(location.address.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.memo.isEmpty().or(location.memo.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.company.isNull()).then(0).otherwise(1));

		NumberExpression rateValue;

		if (forAvg) {
			rateValue = (numEntered.floatValue().divide(Const.numberLine.LOCATION)).avg();
		} else {
			rateValue = numEntered.floatValue().divide(Const.numberLine.LOCATION);
		}

		if (payload.isUncompletedData()) {
			builder.and(rateValue.doubleValue().lt(1));
		}
		if (CollectionUtils.isNotEmpty(payload.getIds())) {
			builder.and(location.id.in(payload.getIds()));
		}
		JPQLQuery query = from(location).where(builder);
		if ("rate".equalsIgnoreCase(properties[0])) {
			NumberExpression numberExpression = rateValue;
			OrderSpecifier numberOrder = numberExpression.desc();
			if (directions[0].equals(Sort.Direction.ASC)) {
				numberOrder = numberExpression.asc();
			}
			query.orderBy(numberOrder);
		} else {
			StringExpression expression = location.name;
			OrderSpecifier order = expression.desc();
			if (directions[0].equals(Sort.Direction.ASC)) {
				order = expression.asc();
			}
			query.orderBy(order);
		}
		long total = query.fetchCount();

		if (total > 0) {
			query.select(Projections.constructor(CompletionRateData.class, location.id, location.locationCode, location.name, numEntered, rateValue, location.updatedAt, location));

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
		QLocation location = QLocation.location;
		BooleanBuilder builder = new BooleanBuilder();
		builder.and(location.company.id.eq(companyId)).and(location.company.isEmoldino.isFalse());

		NumberExpression<Integer> numEntered = //
				(new CaseBuilder().when(location.locationCode.isEmpty().or(location.locationCode.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.name.isEmpty().or(location.name.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.address.isEmpty().or(location.address.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.memo.isEmpty().or(location.memo.isNull())).then(0).otherwise(1))//
						.add(new CaseBuilder().when(location.company.isNull()).then(0).otherwise(1));

		NumberExpression rateValue = (numEntered.floatValue().divide(Const.numberLine.LOCATION)).avg();
		JPQLQuery query = from(location).where(builder);
		if (query.fetchCount() > 0) {
			query.select(Projections.constructor(CompletionRateData.class, location.companyId, rateValue, location.updatedAt));
			return (CompletionRateData) query.fetchFirst();
		} else {
			return new CompletionRateData(companyId, 0D);
		}
	}

}
