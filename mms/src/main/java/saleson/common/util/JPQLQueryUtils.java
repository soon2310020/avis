package saleson.common.util;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.framework.repository.Q;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

public class JPQLQueryUtils {
	/**
	 * 1. Supplier : 로그인한 회원의 company 와 Mold > Location > company 가 일치하는 데이터
	 * 2. 이외 : MoldAuthority > AUTHORITY에 로그인한 회원의 MOLD 권한과 일치하는 moldId를 조회함.
	 * @return JPQLQuery
	 */
	public static JPQLQuery<Long> getMoldIdsSubquery() {
		JPQLQuery<Long> query = JPAExpressions//
				.select(Q.mold.id)//
				.from(Q.mold)//
				.where(Q.mold.supplierCompanyId.in(AccessControlUtils.getAllAccessibleCompanyIds()));
		return query;
	}

	/**
	 * 권한이 있는 partId를 조회하는 서브쿼리.
	 * @return JPQLQuery
	 */
	public static JPQLQuery<Long> getPartIdsSubquery() {
		JPQLQuery<Long> query = JPAExpressions//
				.select(Q.moldPart.part.id)//
				.from(Q.mold)//
				.innerJoin(Q.moldPart).on(Q.mold.id.eq(Q.moldPart.moldId))//
				.where(Q.mold.supplierCompanyId.in(AccessControlUtils.getAllAccessibleCompanyIds()));
		return query;
	}

	/**
	 * 권한이 있는 counterId를 조회하는 서브쿼리.
	 * @return JPQLQuery
	 */
	public static JPQLQuery<Long> getCounterIdsSubquery() {
		JPQLQuery<Long> query = JPAExpressions//
				.select(Q.counter.id)//
				.from(Q.counter)//
				.leftJoin(Q.mold).on(Q.counter.id.eq(Q.mold.counterId))//
				.where(Q.counter.location.companyId.in(AccessControlUtils.getAllAccessibleCompanyIds()));
		return query;
	}

//	@Deprecated
//	public static BooleanBuilder getMoldFilterBuilder(QMold mold, DashboardFilterPayload payload) {
//		BooleanBuilder builder = new BooleanBuilder();
//		if (payload == null || mold == null) {
//			return builder;
//		}
//		if (payload.getPartIds() != null && payload.getPartIds().size() > 0) {
//			builder.and(mold.moldParts.any().partId.in(payload.getPartIds()));
//		}
//		if (payload.getSupplierIds() != null && payload.getSupplierIds().size() > 0)
//			builder.and(mold.supplier.id.in(payload.getSupplierIds()));
//		if (payload.getToolMakerIds() != null && payload.getToolMakerIds().size() > 0)
//			builder.and(mold.toolMaker.id.in(payload.getToolMakerIds()));
//		if (payload.getLocationIds() != null && payload.getLocationIds().size() > 0)
//			builder.and(mold.location.id.in(payload.getLocationIds()));
//		if (payload.getOps() != null && payload.getOps().size() > 0) {
//			builder.and(mold.operatingStatus.in(payload.getOps()));
//		}
//		builder.and(mold.deleted.isFalse().or(mold.deleted.isNull()));
//		return builder;
//	}

//	@Deprecated
//	public static JPQLQuery<Long> getMoldIdSubCompanySubQuery(String companyName) {
//		QMold mold = QMold.mold;
//		JPQLQuery<Long> query = JPAExpressions//
//				.select(mold.id)//
//				.from(mold);
//		if (companyName.equals("FLEXTRONICS")) {
//			query = JPAExpressions//
//					.select(mold.id)//
//					.from(mold)//
//					.where(mold.location.company.name.in(Arrays.asList("FLEXTRONICS", "MCTRONIC", "Skreen Fabric")));
//		} else if (companyName.equals("SKP")) {
//			query = JPAExpressions//
//					.select(mold.id)//
//					.from(mold)//
//					.where(mold.location.company.name.in(Arrays.asList("SKP", "MyTech", "CGH", "TF", "GOLDPAR", "GOODHART", "SPI", "SKP BP")));
//		}
//		return query;
//	}
//
//	@Deprecated
//	public static JPQLQuery<Long> getSubCompanyIdSubQuery(String companyName) {
//		QCompany company = QCompany.company;
//		JPQLQuery<Long> query = JPAExpressions//
//				.select(company.id)//
//				.from(company);
//		if (companyName.equals("FLEXTRONICS")) {
//			query.where(company.name.in(Arrays.asList("FLEXTRONICS", "MCTRONIC", "Skreen Fabric")));
//		} else if (companyName.equals("SKP")) {
//			query.where(company.name.in(Arrays.asList("SKP", "MyTech", "CGH", "TF", "GOLDPAR", "GOODHART", "SPI", "SKP BP")));
//		}
//		return query;
//	}

//	@Deprecated
//	/**
//	 * @deprecated use AccessControlUtils.getAllAccessibleCompanyIds() instead
//	 */
//	public static List<Long> getSubCompanyIdSubQuery(){
//		MoldService moldService = ContextWrapper.getContext().getBean("moldService", MoldService.class);
//		List<Long> accessCompanyIds = new ArrayList<>();
//		moldService.loadTreeCompanyForPayLoad(accessCompanyIds, null);
//		if(accessCompanyIds.isEmpty()){
//			accessCompanyIds.add(SecurityUtils.getCompanyId());
//		}
//		return accessCompanyIds;
//	}

////	@Deprecated
//	public static OrderSpecifier[] getOrderSpecifiers(@NotNull Pageable pageable, @NotNull Class klass) {
//		// orderVariable must match the variable of FROM
//		String className = klass.getSimpleName();
//		final String orderVariable = String.valueOf(Character.toLowerCase(className.charAt(0))).concat(className.substring(1));
//
//		return pageable.getSort().stream()//
//				.map(order -> new OrderSpecifier(Order.valueOf(order.getDirection().toString()), new PathBuilder(klass, orderVariable).get(order.getProperty())))//
//				.toArray(OrderSpecifier[]::new);
//	}

}
