package saleson.api.user;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.user.payload.UserParam;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.RoleUserData;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.QUser;
import saleson.model.User;

public class UserRepositoryImpl extends QuerydslRepositorySupport implements UserRepositoryCustom<User, UserParam> {
	public UserRepositoryImpl(JPAQueryFactory queryFactory) {
		super(User.class);
		this.queryFactory = queryFactory;
	}

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<User> findAll(UserParam searchParam, Pageable pageable) {

		QUser user = QUser.user;

		JPQLQuery query = from(user);
		//filter by role
		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(user.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(user.id.eq(SecurityUtils.getUserId()))//
					.or(user.isPublic.isTrue()));//
		}
		if (!StringUtils.isEmpty(searchParam.getQuery())) {
			query.where(user.name.like('%' + searchParam.getQuery() + '%')//
					.or(user.email.like('%' + searchParam.getQuery() + '%'))//
					.or(user.company.name.contains(searchParam.getQuery())));
		}
		if (!StringUtils.isEmpty(searchParam.getStatus())) {
			String status = searchParam.getStatus();

			if ("active".equalsIgnoreCase(status)) {
				query.where(user.enabled.isTrue().and(user.requested.isNull().or(user.requested.isFalse())));

			} else if ("disabled".equalsIgnoreCase(status)) {
				query.where(user.enabled.isFalse().and(user.requested.isNull().or(user.requested.isFalse())));

			} else if ("admins".equalsIgnoreCase(status)) {
				query.where(user.admin.isTrue().and(user.requested.isNull().or(user.requested.isFalse())));
			} else if ("requested".equalsIgnoreCase(status)) {
				query.where(user.requested.isTrue());

			}
		}
		if (!StringUtils.isEmpty(searchParam.getCompanyType())) {
			String status = searchParam.getCompanyType();

			if ("in_house".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.IN_HOUSE));

			} else if ("tool_maker".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.TOOL_MAKER));

			} else if ("supplier".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER));

			} else if ("non_admin".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER).or(user.company.companyType.eq(CompanyType.TOOL_MAKER)));
			} else if ("non_inhouse".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER)
						.and(user.company.companyType.eq(CompanyType.TOOL_MAKER).and(user.company.companyType.eq(CompanyType.IN_HOUSE))));
			}

		}

		if (!StringUtils.isEmpty(searchParam.getAccessLevel())) {
			String status = searchParam.getAccessLevel();

			if ("rest_user".equalsIgnoreCase(status)) {
				query.where(user.roleUserData.eq(RoleUserData.ROLE_REST_USER));

			} else if ("regular".equalsIgnoreCase(status)) {
				query.where(user.admin.isFalse());
			} else if ("all_admins".equalsIgnoreCase(status)) {
				query.where(user.admin.isTrue());
			}
		}

		if (searchParam.getId() != null) {
			query.where(user.id.eq(searchParam.getId()));
		}
		//other filter
		if (searchParam.getUserIds() != null || (searchParam.getIsDefaultTab() != null && !searchParam.getIsDefaultTab())) {
			query.where(user.id.in(searchParam.getUserIds()));
		}
		//add filter delete
		query.where(user.deleted.isFalse());

		List<User> users = getQuerydsl().applyPagination(pageable, query).fetch();
		long totalCount = query.fetchCount();

		return new PageImpl<>(users, pageable, totalCount);
	}

	@Transactional(isolation = Isolation.READ_UNCOMMITTED)
	public void saveLastLogin(String username) {
		QUser user = QUser.user;
		update(user).where(user.loginId.eq(username)).set(user.lastLogin, Instant.now()).execute();
	}

	public List<User> findByIdInAndCompanyType(List<Long> ids, CompanyType companyType) {
		QUser user = QUser.user;
		JPQLQuery query = from(user)//
				.where(user.id.in(ids)//
						.and(user.company.companyType.eq(companyType))//
						.and(user.deleted.isFalse()));
		return query.fetch();
	}

	@Override
	public List<User> findAllUserActiveByEmailIn(List<String> emailList, String searchText, Pageable pageable) {
		QUser user = QUser.user;
		BooleanBuilder builder = new BooleanBuilder();
		if (emailList != null) {
			builder.and(user.email.in(emailList));
		}
		if (!StringUtils.isEmpty(searchText)) {
			builder.and(user.email.like('%' + searchText + '%'));
		}
		builder.and(user.enabled.isTrue());
		//add filter delete
		builder.and(user.deleted.isFalse());

		JPQLQuery query = from(user).where(builder);
		query.offset(pageable.getOffset()).limit(pageable.getPageSize());
		return query.fetch();
	}

	@Override
	public long countAll(UserParam searchParam) {
		JPQLQuery query = getQueryByParam(searchParam);
		return query.fetchCount();
	}

	@Override
	public List<Long> findAllIdByParam(UserParam searchParam) {
		QUser user = QUser.user;
		JPQLQuery query = getQueryByParam(searchParam);
		query.select(user.id);
		return query.fetch();
	}

	@Override
	public List<User> findByCompanyTypeAndEnabledIsTrue(CompanyType companyType) {
		QUser user = QUser.user;
		JPQLQuery query =  from(user).where(
				user.enabled.isTrue()
						.and(user.deleted.isNull().or(user.deleted.isFalse()))
						.and(user.company.companyType.eq(companyType))
						.and(user.admin.isTrue()));
		query.select(user);
		List<User> result = query.fetch();
		return result;
	}

	@Override
	public List<User> findByCompanyTypeAndIsEmoldinoIsTrueAndEnabledIsTrue(CompanyType companyType) {
		QUser user = QUser.user;
		JPQLQuery query =  from(user).where(
				user.enabled.isTrue()
						.and(user.deleted.isNull().or(user.deleted.isFalse()))
						.and(user.company.companyType.eq(companyType))
						.and(user.company.isEmoldino.isTrue())
						.and(user.admin.isTrue()));
		query.select(user);
		List<User> result = query.fetch();
		return result;
	}

	private JPQLQuery getQueryByParam(UserParam searchParam) {
		QUser user = QUser.user;

		JPQLQuery query = from(user);
		//filter by role
		if (AccessControlUtils.isAccessFilterRequired()) {
			query.where(user.company.id.in(AccessControlUtils.getAllAccessibleCompanyIds())//
					.or(user.id.eq(SecurityUtils.getUserId()))//
					.or(user.isPublic.isTrue()));
		}
		if (!StringUtils.isEmpty(searchParam.getQuery())) {
			query.where(user.name.like('%' + searchParam.getQuery() + '%')//
					.or(user.email.like('%' + searchParam.getQuery() + '%'))//
					.or(user.company.name.contains(searchParam.getQuery())));
		}
		if (!StringUtils.isEmpty(searchParam.getStatus())) {
			String status = searchParam.getStatus();

			if ("active".equalsIgnoreCase(status)) {
				query.where(user.enabled.isTrue().and(user.requested.isNull().or(user.requested.isFalse())));

			} else if ("disabled".equalsIgnoreCase(status)) {
				query.where(user.enabled.isFalse().and(user.requested.isNull().or(user.requested.isFalse())));

			} else if ("admins".equalsIgnoreCase(status)) {
				query.where(user.admin.isTrue().and(user.requested.isNull().or(user.requested.isFalse())));
			} else if ("requested".equalsIgnoreCase(status)) {
				query.where(user.requested.isTrue());

			}
		}
		if (!StringUtils.isEmpty(searchParam.getCompanyType())) {
			String status = searchParam.getCompanyType();

			if ("in_house".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.IN_HOUSE));

			} else if ("tool_maker".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.TOOL_MAKER));

			} else if ("supplier".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER));

			} else if ("non_admin".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER).or(user.company.companyType.eq(CompanyType.TOOL_MAKER)));
			} else if ("non_inhouse".equalsIgnoreCase(status)) {
				query.where(user.company.companyType.eq(CompanyType.SUPPLIER)
						.and(user.company.companyType.eq(CompanyType.TOOL_MAKER).and(user.company.companyType.eq(CompanyType.IN_HOUSE))));
			}

		}

		if (!StringUtils.isEmpty(searchParam.getAccessLevel())) {
			String status = searchParam.getAccessLevel();

			if ("rest_user".equalsIgnoreCase(status)) {
				query.where(user.roleUserData.eq(RoleUserData.ROLE_REST_USER));

			} else if ("regular".equalsIgnoreCase(status)) {
				query.where(user.admin.isFalse());
			} else if ("all_admins".equalsIgnoreCase(status)) {
				query.where(user.admin.isTrue());
			}
		}

		if (searchParam.getId() != null) {
			query.where(user.id.eq(searchParam.getId()));
		}
		//other filter
		if (searchParam.getUserIds() != null) {
			query.where(user.id.in(searchParam.getUserIds()));
		}
		//add filter delete
		query.where(user.deleted.isFalse());
		return query;
	}

//	/*
//	 * 2022.05.17 Add by Mickey Park
//	 */
//	@Override
//	public Page<UserLite> findByCompanyIdAndQueryAndAvailble(Long companyId, String searchWord, List<Long> userIds, Pageable pageable) {
//
//		BooleanBuilder filter = new BooleanBuilder();
//		QUser table = QUser.user;
//
//		JPQLQuery<UserLite> query = from(table).select(Projections.fields(UserLite.class, table.id, table.name));
//
//		filter.and(table.companyId.eq(companyId));
//
//		if (!ObjectUtils.isEmpty(searchWord)) {
//			filter.and(table.name.contains(searchWord));
//		}
//
//		if (!ObjectUtils.isEmpty(userIds)) {
//			filter.and(table.id.notIn(userIds));
//		}
//
//		query.where(filter);
//
//		// Adjust Pageable
//		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
//			query.orderBy(table.name.asc());
//		}
//
//		getQuerydsl().applyPagination(pageable, query);
//		List<UserLite> result = query.fetch();
//		return new PageImpl<UserLite>(result, pageable, query.fetchCount());
//	}
//
//	/*
//	 * 2022.05.19 Add by Mickey Park
//	 * Test BooleanExpression
//	 */
//	@Override
//	public Page<UserLite> findByCompanyIdAndQueryAndAvailble2(Long companyId, String searchWord, List<Long> userIds, Pageable pageable) {
//
//		QUser table = QUser.user;
//
//		QueryResults<UserLite> result = queryFactory//
//				.select(Projections.fields(UserLite.class, table.id.as("id"), table.name.as("name")))//
//				.from(table)//
//				.where(table.companyId.eq(companyId), searchWordContains(searchWord), userIdsNotIn(userIds))//
//				.orderBy(table.name.asc())//
//				.offset(pageable.getOffset()).limit(pageable.getPageSize())//
//				.fetchResults();
//
//		return new PageImpl<UserLite>(result.getResults(), pageable, result.getTotal());
//	}

	private BooleanExpression searchWordContains(String searchWord) {
		QUser user = QUser.user;
		return !ObjectUtils.isEmpty(searchWord) ? user.name.contains(searchWord) : null;
	}

	private BooleanExpression userIdsNotIn(List<Long> userIds) {
		QUser user = QUser.user;
		return !ObjectUtils.isEmpty(userIds) ? user.id.notIn(userIds) : null;
	}
}
