package com.emoldino.api.common.resource.composite.comstp.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetIn;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpItem;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.common.enumeration.ObjectType;
import saleson.model.QAccessCompanyRelation;
import saleson.model.QCompany;
import saleson.model.TabTable;

@Repository
public class ComStpRepository {

	public long count(ComStpGetIn input) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.COMPANY, input.getTabName());

		QCompany qCompany = Q.company(input.getCompanyType() == null ? null : Arrays.asList(input.getCompanyType()));

		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(qCompany.id.countDistinct())//
				.from(qCompany);

		applyFilter(qCompany, query, new HashSet<>(), input, null, tabTable);

		return query.fetchCount();
	}

	public Page<ComStpItem> findAll(ComStpGetIn input, BatchIn batchin, Pageable pageable) {
		TabTable tabTable = QueryUtils.applyTabInput(input, ObjectType.COMPANY, input.getTabName());

		QCompany qCompany = Q.company(input.getCompanyType() == null ? null : Arrays.asList(input.getCompanyType()));

		JPQLQuery<ComStpItem> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(ComStpItem.class, //
						qCompany, //
						qCompany.manager, //
						qCompany.email, //
						qCompany.phone, //
						qCompany.memo, //
						Expressions.asNumber(//
								JPAExpressions//
										.select(Q.mold.id.count())//
										.from(Q.mold)//
										.where(Q.mold.companyId.eq(qCompany.id).and(QueryUtils.isMold()))//
						).as("moldCount")//
				))//
				.from(qCompany);

		Set<EntityPathBase<?>> join = new HashSet<>();

		applyFilter(qCompany, query, join, input, batchin, tabTable);

		QAccessCompanyRelation qCompanyRel = QAccessCompanyRelation.accessCompanyRelation;
		String sort = QueryUtils.getFirstSortProperty(pageable);
		if (sort != null && sort.equals("upperTierCompanies")) {
			QueryUtils.leftJoin(query, join, qCompanyRel, () -> qCompanyRel.companyId.eq(qCompany.id)//
					.and(qCompanyRel.id.in(JPAExpressions//
							.select(qCompanyRel.id.min())//
							.from(qCompanyRel)//
							.where(qCompanyRel.companyId.eq(qCompany.id))//
							.groupBy(qCompanyRel.companyId)//
					)));
		}
		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("name", qCompany.name)//
				.put("companyCode", qCompany.companyCode)//
				.put("companyType", qCompany.companyType)//
				.put("address", qCompany.address)//
				.put("manager", qCompany.manager)//
				.put("phone", qCompany.phone)//
				.put("email", qCompany.email)//
				.put("memo", qCompany.memo)//
				.put("moldCount", Expressions.numberPath(Long.class, "moldCount"))//
				.put("upperTierCompanies", qCompanyRel.accessHierarchyParent.company.name)//
				.build();

		QueryUtils.applyPagination(query, pageable, fieldMap, qCompany.name.asc());
		QueryResults<ComStpItem> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	private void applyFilter(QCompany qCompany, JPQLQuery<?> query, Set<EntityPathBase<?>> join, ComStpGetIn input, BatchIn batchin, TabTable tabTable) {
		QueryUtils.applyTabFilter(query, join, tabTable, qCompany.id);

		if (ValueUtils.toBoolean(input.getEnabled(), true)) {
			QueryUtils.applyCompanyFilter(query, join, input.getFilterCode(), input.getCompanyType());
		} else {
			QueryUtils.applyCompanyDisabledFilter(query, join, input.getFilterCode(), input.getCompanyType());
		}

		BooleanBuilder filter = new BooleanBuilder();
		QueryUtils.eq(filter, qCompany.companyType, input.getCompanyType());
		QueryUtils.eq(filter, qCompany.enabled, input.getEnabled());
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(//
					qCompany.name.containsIgnoreCase(input.getQuery())//
							.or(qCompany.companyCode.containsIgnoreCase(input.getQuery()))//
							.or(qCompany.address.containsIgnoreCase(input.getQuery()))//
			);
		}
		if (CollectionUtils.isNotEmpty(input.getId())) {
			filter.and(qCompany.id.in(input.getId()));
		}
		QueryUtils.applyBatchFilter(filter, batchin, qCompany.id);

		query.where(filter);
	}

}
