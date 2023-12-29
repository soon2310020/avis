package com.emoldino.framework.util;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;
import com.emoldino.api.common.resource.base.option.repository.maserfilteritem.QMasterFilterItem;
import com.emoldino.api.common.resource.base.option.repository.masterfilterresource.MasterFilterResourceRepository;
import com.emoldino.api.common.resource.base.option.repository.masterfilterresource.QMasterFilterResource;
import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.enumeration.ActiveStatus;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.util.SecurityUtils;
import saleson.model.QCategory;
import saleson.model.QCompany;
import saleson.model.QLocation;
import saleson.model.QMold;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.model.QWorkOrderAsset;
import saleson.model.TabTable;

public class QueryUtils {

	public static <T> BooleanBuilder andIf(BooleanBuilder filter, SimpleExpression<T> left, T right) {
		if (!ObjectUtils.isEmpty(right)) {
			and(filter, left, right);
		}
		return filter;
	}

	public static <T> BooleanBuilder and(BooleanBuilder filter, SimpleExpression<T> left, T right) {
		return and(filter, left, "=", right);
	}

	public static <T> BooleanBuilder and(BooleanBuilder filter, SimpleExpression<T> left, String operator, T right) {
		if ("=".equals(operator)) {
			if (right == null) {
				filter.and(left.isNull());
			} else {
				filter.and(left.eq(right));
			}
		} else if ("<>".equals(operator) || "!=".equals(operator)) {
			if (right == null) {
				filter.and(left.isNotNull());
			} else {
				filter.and(left.ne(right));
			}
		} else if (">".equals(operator)) {
//			filter.and(left)
		}
		return filter;
	}

	public static <T> void eq(BooleanBuilder filter, SimpleExpression<T> field, T value) {
		if (filter == null || field == null || value == null) {
			return;
		}
		filter.and(field.eq(value));
	}

	public static <T> void ne(BooleanBuilder filter, SimpleExpression<T> field, T value) {
		if (filter == null || field == null || value == null) {
			return;
		}
		filter.and(field.ne(value));
	}

	public static <T> void eqOr(BooleanBuilder filter, SimpleExpression<T> field, T value) {
		if (filter == null || field == null || value == null) {
			return;
		}
		filter.or(field.eq(value));
	}

	public static <T> void in(BooleanBuilder filter, SimpleExpression<T> field, Collection<T> values) {
		if (filter == null || field == null || ObjectUtils.isEmpty(values)) {
			return;
		}
		filter.and(field.in(values));
	}

	public static void startsWith(BooleanBuilder filter, StringPath field, String value) {
		if (filter == null || field == null || ObjectUtils.isEmpty(value)) {
			return;
		}
		filter.and(field.startsWith(value));
	}

	public static void contains(BooleanBuilder filter, StringPath field, String value) {
		if (filter == null || field == null || ObjectUtils.isEmpty(value)) {
			return;
		}
		filter.and(field.contains(value));
	}

	public static void containsOr(BooleanBuilder filter, StringPath field, String value) {
		if (filter == null || field == null || ObjectUtils.isEmpty(value)) {
			return;
		}
		filter.or(field.contains(value));
	}

	public static void after(BooleanBuilder filter, DateTimePath<Instant> field, String value, String zoneId) {
		if (filter == null || field == null || ObjectUtils.isEmpty(value)) {
			return;
		}
		Instant instant = toInstant(value, zoneId);
		filter.and(field.after(instant));
	}

	public static void before(BooleanBuilder filter, DateTimePath<Instant> field, String value, String zoneId) {
		if (filter == null || field == null || ObjectUtils.isEmpty(value)) {
			return;
		}
		Instant instant = toInstant(value, zoneId);
		filter.and(field.before(instant));
	}

	public static <T extends Number & Comparable<?>> BooleanBuilder gtAndLoe(BooleanBuilder filter, NumberExpression<?> field, T greaterThan, T lowerOrEquals) {
		LogicUtils.assertNotNull(filter, "filter");
		LogicUtils.assertNotNull(field, "field");
		if (greaterThan != null || lowerOrEquals != null) {
			if (greaterThan == null) {
				// TODO remove field.isNull() condition
				filter.and(field.isNull().or(field.loe(lowerOrEquals)));
			} else if (lowerOrEquals == null) {
				filter.and(field.gt(greaterThan));
			} else {
				filter.and(field.gt(greaterThan)).and(field.loe(lowerOrEquals));
			}
		}
		return filter;
	}

	public static <T extends Comparable<?>> BooleanBuilder gtAndLoe(BooleanBuilder filter, ComparableExpression<T> field, T greaterThan, T lowerOrEquals) {
		LogicUtils.assertNotNull(filter, "filter");
		LogicUtils.assertNotNull(field, "field");
		if (greaterThan != null || lowerOrEquals != null) {
			if (greaterThan == null) {
				filter.or(field.loe(lowerOrEquals));
			} else if (lowerOrEquals == null) {
				filter.and(field.gt(greaterThan));
			} else {
				filter.and(field.gt(greaterThan)).and(field.loe(lowerOrEquals));
			}
		}
		return filter;
	}

	private static Instant toInstant(String value, String zoneId) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		String pattern;
		if (value.length() == 14) {
			pattern = DatePattern.yyyyMMddHHmmss;
		} else if (value.length() == 12) {
			pattern = DatePattern.yyyyMMddHHmm;
		} else if (value.length() == 10) {
			pattern = DatePattern.yyyyMMddHH;
		} else if (value.length() == 8) {
			pattern = DatePattern.yyyyMMdd;
		} else {
			pattern = DatePattern.yyyyMMddHHmmss;
		}
		Instant instant = DateUtils2.toInstant(value, pattern, zoneId);
		return instant;
	}

	public static void setAutoDistinct(boolean distinct) {
		ThreadUtils.setProp("QueryUtils.autoDistinct", distinct);
	}

	private static void distinct(JPQLQuery<?> query) {
		if (ValueUtils.toBoolean(ThreadUtils.getProp("QueryUtils.autoDistinct"), true)) {
			query.distinct();
		}
	}

	public static void applyBatchFilter(BooleanBuilder filter, BatchIn batchin, NumberPath<Long> idField) {
		if (batchin == null) {
			return;
		}
		LogicUtils.assertNotNull(filter, "filter");
		LogicUtils.assertNotNull(idField, "idField");
		if (batchin.getSelectionMode() == null || MasterFilterMode.SELECTED.equals(batchin.getSelectionMode())) {
			if (ObjectUtils.isEmpty(batchin.getSelectedIds())) {
				filter.and(idField.isNull());
			} else {
				filter.and(idField.in(batchin.getSelectedIds()));
			}
		} else if (!ObjectUtils.isEmpty(batchin.getUnselectedIds())) {
			filter.and(idField.notIn(batchin.getUnselectedIds()));
		}
	}

	public static void applyMasterFilter(JPQLQuery<?> query, String filterCode, Map<MasterFilterResourceType, QueryRelation> relations) {
		QueryUtils.applyMasterFilter(query, null, null, filterCode, relations);
	}

	private static void applyMasterFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, BooleanBuilder filter, String filterCode,
			Map<MasterFilterResourceType, QueryRelation> relations) {
		LogicUtils.assertNotNull(query, "query");
		LogicUtils.assertNotEmpty(filterCode, "filterCode");
		LogicUtils.assertNotEmpty(relations, "relations");

		String apiName = ThreadUtils.getPropApiName();
		boolean fltCtrl = apiName != null && apiName.contains(".FltControllerImpl.");

		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}

		String _filterCode = "COMMON";

		boolean[] skip = { false };
		QMasterFilterResource qResource = QMasterFilterResource.masterFilterResource;
		BeanUtils.get(MasterFilterResourceRepository.class).findAll(new BooleanBuilder()//
				.and(qResource.filterCode.eq(_filterCode))//
				.and(qResource.userId.eq(userId))//
				.and(qResource.resourceType.in(relations.keySet())), //
				Sort.by(Direction.ASC, "position")//
		).forEach(resource -> {
			MasterFilterResourceType resourceType = resource.getResourceType();
			MasterFilterResourceType apiResType = (MasterFilterResourceType) ThreadUtils.getProp("FltService.resourceType");
			if (fltCtrl) {
				if (skip[0]) {
					return;
				} else if (MasterFilterResourceType.SUPPLIER.equals(resourceType)//
						&& (apiName.endsWith(".getSuppliers") || MasterFilterResourceType.SUPPLIER.equals(apiResType))//
						|| MasterFilterResourceType.TOOLMAKER.equals(resourceType) //
								&& (apiName.endsWith(".getToolmakers") || MasterFilterResourceType.TOOLMAKER.equals(apiResType))//
						|| MasterFilterResourceType.PLANT.equals(resourceType)//
								&& (apiName.endsWith(".getPlants") || MasterFilterResourceType.PLANT.equals(apiResType))//
						|| MasterFilterResourceType.TOOLING.equals(resourceType)//
								&& (apiName.endsWith(".getMolds") || MasterFilterResourceType.TOOLING.equals(apiResType))//
						|| MasterFilterResourceType.PRODUCT.equals(resourceType) //
								&& (apiName.endsWith(".getProducts") || MasterFilterResourceType.PRODUCT.equals(apiResType))//
						|| MasterFilterResourceType.PART.equals(resourceType)//
								&& (apiName.endsWith(".getParts") || MasterFilterResourceType.PART.equals(apiResType))) {
					skip[0] = true;
				}
			}

			QueryRelation relation = relations.get(resourceType);
			NumberPath<Long> id = relation.getField();
			if (relation.getJoin() != null) {
				distinct(query);
				relation.getJoin().execute();
			}
			QMasterFilterItem qItem = new QMasterFilterItem(resourceType.name());
			if (MasterFilterMode.SELECTED.equals(resource.getMode())) {
				if (filter == null) {
					if (fltCtrl) {
						QueryUtils.join(query, join, qItem, qItem.filterCode.eq(_filterCode)//
								.and(qItem.userId.eq(userId))//
								.and(qItem.resourceType.eq(resourceType))//
								.and(id.eq(qItem.resourceId)));
					} else {
						QueryUtils.join(query, join, qItem, qItem.filterCode.eq(_filterCode)//
								.and(qItem.userId.eq(userId))//
								.and(qItem.resourceType.eq(resourceType))//
								.and(qItem.selected.isTrue())//
								.and(id.eq(qItem.resourceId)));
					}
				} else {
					if (fltCtrl) {
						QueryUtils.join(query, join, qItem, qItem.filterCode.eq(_filterCode)//
								.and(qItem.userId.eq(userId))//
								.and(qItem.resourceType.eq(resourceType)));
					} else {
						QueryUtils.join(query, join, qItem, qItem.filterCode.eq(_filterCode)//
								.and(qItem.userId.eq(userId))//
								.and(qItem.resourceType.eq(resourceType))//
								.and(qItem.selected.isTrue()));
					}
					filter.and(id.eq(qItem.resourceId));
				}

			} else {
				Predicate predicate = id.notIn(//
						JPAExpressions.select(qItem.resourceId)//
								.from(qItem)//
								.where(//
										fltCtrl ? //
												qItem.userId.eq(userId)//
														.and(qItem.resourceType.eq(resourceType))//
														.and(qItem.selected.isFalse())//
														.and(qItem.temporal.isFalse())
												: qItem.userId.eq(userId)//
														.and(qItem.resourceType.eq(resourceType))//
														.and(qItem.selected.isFalse())//
				));
				if (filter == null) {
					query.where(predicate);
				} else {
					filter.and(predicate);
				}
			}
//			if (relation.getJoin() != null) {
//				distinct(query);
//				relation.getJoin().execute();
//			}
//			QMasterFilterItem qItem = new QMasterFilterItem(resourceType.name());
//			query.innerJoin(qItem).on(//
//					qItem.filterCode.eq(_filterCode)//
//							.and(qItem.userId.eq(userId))//
//							.and(qItem.resourceType.eq(resourceType))//
//							.and(qItem.selected.eq(MasterFilterMode.SELECTED.equals(resource.getMode())))//
//							.and(MasterFilterMode.SELECTED.equals(resource.getMode()) ? id.eq(qItem.resourceId) : id.ne(qItem.resourceId))//
//			);
		});
	}

	public static void join(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Predicate predicate) {
		if (!join(join, entity, predicate)) {
			return;
		}
		query.innerJoin(entity).on(predicate);
	}

	@Deprecated
	// use join(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Predicate predicate) instead
	public static void join(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Closure<Predicate> closure) {
		if (!join(join, entity, closure)) {
			return;
		}
		query.innerJoin(entity).on(closure.execute());
	}

	public static void leftJoin(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Predicate predicate) {
		if (!join(join, entity, predicate)) {
			return;
		}
		query.leftJoin(entity).on(predicate);
	}

	@Deprecated
	// use leftJoin(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Predicate predicate) instead
	public static void leftJoin(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Closure<Predicate> closure) {
		if (!join(join, entity, closure)) {
			return;
		}
		query.leftJoin(entity).on(closure.execute());
	}

	public static void rightJoin(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Predicate predicate) {
		if (!join(join, entity, predicate)) {
			return;
		}
		query.rightJoin(entity).on(predicate);
	}

	@Deprecated
	public static void rightJoin(JPQLQuery<?> query, Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Closure<Predicate> closure) {
		if (!join(join, entity, closure)) {
			return;
		}
		query.rightJoin(entity).on(closure.execute());
	}

	private static boolean join(Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Predicate predicate) {
		if (predicate == null) {
			return false;
		} else if (join != null && entity != null && join.contains(entity)) {
			return false;
		}
		if (join != null) {
			join.add(entity);
		}
		return true;
	}

	private static boolean join(Set<EntityPathBase<?>> join, EntityPathBase<?> entity, Closure<Predicate> closure) {
		if (closure == null) {
			return false;
		} else if (join != null && entity != null && join.contains(entity)) {
			return false;
		}
		if (join != null) {
			join.add(entity);
		}
		return true;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class QueryRelation {
		private NumberPath<Long> field;
		private ClosureNoReturn join;
	}

	public static boolean isMasterFilterResource(String filterCode, MasterFilterResourceType resourceType) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return false;
		}
		QMasterFilterResource qResource = QMasterFilterResource.masterFilterResource;
		return BeanUtils.get(MasterFilterResourceRepository.class).exists(new BooleanBuilder()//
				.and(qResource.filterCode.eq(filterCode))//
				.and(qResource.userId.eq(userId))//
				.and(qResource.resourceType.eq(resourceType))//
		);
	}

	@SafeVarargs
	public static void applyCompanyFilter(BooleanBuilder filter, NumberPath<Long>... fields) {
		if (!AccessControlUtils.isAccessFilterRequired()) {
			return;
		}
		Long companyId = SecurityUtils.getCompanyId();
		if (companyId == null) {
			return;
		}

		ValueUtils.assertNotEmpty(filter, "filter");
		ValueUtils.assertNotEmpty(fields, "fields");

		List<Long> companyIds = AccessControlUtils.getAllAccessibleCompanyIds(companyId);

		BooleanBuilder filterCompanies = new BooleanBuilder();
		for (NumberPath<Long> field : fields) {
			filterCompanies.or(field.in(companyIds));
		}
		filter.and(filterCompanies);
	}

	private static <T> List<T> asList(@SuppressWarnings("unchecked") T... arg) {
		return arg == null || arg.length == 0 || (arg.length == 1 && arg[0] == null) ? null : Arrays.asList(arg);
	}

	public static void applyCompanyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, CompanyType... companyType) {
		_applyCompanyFilter(query, join, filterCode, asList(companyType), true);
	}

	public static void applyCompanyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, List<CompanyType> companyType) {
		_applyCompanyFilter(query, join, filterCode, companyType, true);
	}

	public static void applyCompanyDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, CompanyType... companyType) {
		_applyCompanyFilter(query, join, filterCode, asList(companyType), false);
	}

	private static void _applyCompanyFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, List<CompanyType> companyType, boolean enabeld) {
		QCompany qCompany = Q.company(companyType);

		BooleanBuilder filter = new BooleanBuilder();
		if (!ObjectUtils.isEmpty(companyType)) {
			filter.and(qCompany.companyType.in(companyType));
		}
		if (enabeld) {
			if (!join.contains(qCompany)) {
				QueryUtils.isCompany(filter, qCompany);
			}
		} else {
			filter.and(isDisabled(qCompany.enabled));
		}
		if (!join.contains(qCompany)) {
			join.add(qCompany);
		}
		QueryUtils.applyCompanyFilter(filter, qCompany.id);
		query.where(filter);

		// Master Filter
		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> joinLocationByCompany(query, join, qCompany))//
							.build())//
					.build();
			if (Q.supplier.equals(qCompany)) {
				relations.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder().field(qCompany.id).build());
				relations.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
						.field(Q.mold.id)//
						.join(() -> QueryUtils.joinMoldBySupplier(query, join))//
						.build());
				relations.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder()//
						.field(Q.product.id)//
						.join(() -> QueryUtils.joinProductBySupplier(query, join))//
						.build());
				relations.put(MasterFilterResourceType.PART, QueryRelation.builder()//
						.field(Q.part.id)//
						.join(() -> QueryUtils.joinPartBySupplier(query, join))//
						.build());
			} else if (Q.toolmaker.equals(qCompany)) {
				relations.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder().field(qCompany.id).build());
				relations.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
						.field(Q.mold.id)//
						.join(() -> QueryUtils.joinMoldByToolmaker(query, join))//
						.build());
			}

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applyPlantFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyPlantFilter(query, join, filterCode, true);
	}

	public static void applyPlantDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyPlantFilter(query, join, filterCode, false);
	}

	private static void _applyPlantFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, boolean enabled) {
		BooleanBuilder filter = new BooleanBuilder();
		if (enabled) {
			if (!join.contains(Q.location)) {
				QueryUtils.isLocation(filter);
			}
		} else {
			filter.and(isDisabled(Q.location.enabled));
		}
		if (!join.contains(Q.location)) {
			join.add(Q.location);
		}
		QueryUtils.applyCompanyFilter(filter, Q.location.companyId);
		query.where(filter);

		// Master Filter
		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(Q.supplier.id)//
							.join(() -> QueryUtils.joinSupplierByLocation(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder()//
							.field(Q.toolmaker.id)//
							.join(() -> QueryUtils.joinToolmakerByLocation(query, join))//
							.build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.build())//
					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
							.field(Q.mold.id)//
							.join(() -> QueryUtils.joinMoldByLocation(query, join))//
							.build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder().field(Q.product.id)//
							.join(() -> QueryUtils.joinProductByLocation(query, join))//
							.build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
							.field(Q.part.id)//
							.join(() -> QueryUtils.joinPartByLocation(query, join))//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applyProductFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyProductFilter(query, join, filterCode, true);
	}

	public static void applyProductDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyProductFilter(query, join, filterCode, false);
	}

	private static void _applyProductFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, boolean enabled) {
		BooleanBuilder filter = new BooleanBuilder();
		if (enabled) {
			if (!join.contains(Q.product)) {
				QueryUtils.isProduct(filter);
			}
		} else {
			filter.and(isDisabled(Q.product.enabled)).and(Q.product.level.eq(3));
		}
		if (!join.contains(Q.product)) {
			join.add(Q.product);
		}
		if (AccessControlUtils.isAccessFilterRequired()) {
			distinct(query);
			QueryUtils.joinSupplierByProduct(query, join);
			QueryUtils.applyCompanyFilter(filter, Q.supplier.id);
		}
		query.where(filter);

		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(Q.supplier.id)//
							.join(() -> QueryUtils.joinSupplierByProduct(query, join))//
							.build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> QueryUtils.joinLocationByProduct(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
							.field(Q.mold.id)//
							.join(() -> QueryUtils.joinMoldByProduct(query, join))//
							.build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder().field(Q.product.id).build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
							.field(Q.part.id)//
							.join(() -> QueryUtils.joinPartByProduct(query, join))//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applyPartFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyPartFilter(query, join, filterCode, true);
	}

	public static void applyPartDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyPartFilter(query, join, filterCode, false);
	}

	private static void _applyPartFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, boolean enabled) {
		BooleanBuilder filter = new BooleanBuilder();
		if (enabled) {
			if (!join.contains(Q.part)) {
				QueryUtils.isPart(filter);
			}
		} else {
			filter.and(isNotDeleted(Q.part.deleted)).and(isDisabled(Q.part.enabled));
		}
		if (!join.contains(Q.part)) {
			join.add(Q.part);
		}
		if (AccessControlUtils.isAccessFilterRequired()) {
			distinct(query);
			QueryUtils.joinSupplierByPart(query, join);
			QueryUtils.applyCompanyFilter(filter, Q.supplier.id);
		}
		query.where(filter);

		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(Q.supplier.id)//
							.join(() -> QueryUtils.joinSupplierByPart(query, join))//
							.build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> QueryUtils.joinLocationByPart(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
							.field(Q.mold.id)//
							.join(() -> QueryUtils.joinMoldByPart(query, join))//
							.build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder()//
							.field(Q.product.id)//
							.join(() -> QueryUtils.joinProductByPart(query, join))//
							.build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
							.field(Q.part.id)//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applyMoldFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyMoldFilter(query, join, filterCode, ActiveStatus.ENABLED);
	}

	public static void applyMoldDisposedFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyMoldFilter(query, join, filterCode, ActiveStatus.DISPOSED);
	}

	public static void applyMoldDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyMoldFilter(query, join, filterCode, ActiveStatus.DISABLED);
	}

	private static void _applyMoldFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, ActiveStatus activeStatus) {
		BooleanBuilder filter = new BooleanBuilder();
		if (ActiveStatus.ENABLED.equals(activeStatus)) {
			if (!join.contains(Q.mold)) {
				QueryUtils.isMold(filter);
			}
		} else if (ActiveStatus.DISPOSED.equals(activeStatus)) {
			filter.and(isEnabled(Q.mold.enabled)).and(Q.mold.equipmentStatus.in(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED));
		} else {
			filter.and(isDisabled(Q.mold.enabled));
		}
		if (!join.contains(Q.mold)) {
			join.add(Q.mold);
		}
		if (AccessControlUtils.isAccessFilterRequired()) {
			distinct(query);
			QueryUtils.joinSupplierByMold(query, join);
			QueryUtils.applyCompanyFilter(filter, Q.supplier.id);
			QueryUtils.joinCompanyByMold(query, join);
			QueryUtils.applyCompanyFilter(filter, Q.company.id);
		}
		query.where(filter);

		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(Q.supplier.id)//
							.join(() -> QueryUtils.joinSupplierByMold(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder()//
							.field(Q.toolmaker.id)//
							.join(() -> QueryUtils.joinToolmakerByMold(query, join))//
							.build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> QueryUtils.joinLocationByMold(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
							.field(Q.mold.id)//
							.build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder()//
							.field(Q.product.id)//
							.join(() -> QueryUtils.joinProductByMold(query, join))//
							.build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
							.field(Q.part.id)//
							.join(() -> QueryUtils.joinPartByMold(query, join))//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applyMachineFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyMachineFilter(query, join, filterCode, true);
	}

	public static void applyMachineDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyMachineFilter(query, join, filterCode, false);
	}

	public static void _applyMachineFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, boolean enabled) {
		BooleanBuilder filter = new BooleanBuilder();
		if (enabled) {
			if (!join.contains(Q.machine)) {
				QueryUtils.isMachine(filter);
			}
		} else {
			filter.and(isNotDeleted(Q.machine.deleted)).and(isDisabled(Q.machine.enabled));
		}
		if (!join.contains(Q.machine)) {
			join.add(Q.machine);
		}
		QueryUtils.applyCompanyFilter(filter, Q.machine.companyId);
		query.where(filter);

		// Master Filter
		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(Q.company.id)//
							.join(() -> QueryUtils.joinCompanyByMachine(query, join))//
							.build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> QueryUtils.joinLocationByMachine(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLING, QueryRelation.builder()//
							.field(Q.mold.id)//
							.join(() -> QueryUtils.joinMoldByMachine(query, join))//
							.build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder()//
							.field(Q.product.id)//
							.join(() -> QueryUtils.joinProductByMachine(query, join))//
							.build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
							.field(Q.part.id)//
							.join(() -> QueryUtils.joinPartByMachine(query, join))//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applySensorFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applySensorFilter(query, join, filterCode, true);
	}

	public static void applySensorDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applySensorFilter(query, join, filterCode, false);
	}

	private static void _applySensorFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, boolean enabled) {
		BooleanBuilder filter = new BooleanBuilder();
		if (enabled) {
			if (!join.contains(Q.counter)) {
				QueryUtils.isCounter(filter);
			}
		} else {
			filter.and(isDisabled(Q.counter.enabled));
		}
		if (!join.contains(Q.counter)) {
			join.add(Q.counter);
		}
		if (AccessControlUtils.isAccessFilterRequired()) {
			QueryUtils.joinSupplierByCounter(query, join);
			QueryUtils.applyCompanyFilter(filter, Q.supplier.id);
		}
		query.where(filter);

		// Master Filter
		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryUtils.QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryUtils.QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryUtils.QueryRelation.builder()//
							.field(Q.supplier.id)//
							.join(() -> QueryUtils.joinSupplierByCounter(query, join))//
							.build())//
					.put(MasterFilterResourceType.TOOLING, QueryUtils.QueryRelation.builder()//
							.field(Q.mold.id)//
							.join(() -> QueryUtils.joinMoldByCounter(query, join))//
							.build())//
//					.put(MasterFilterResourceType.PRODUCT, QueryUtils.QueryRelation.builder().field(Q.product.id)//
//							.join(() -> QueryUtils.joinProductByLocation(query, join))//
//							.build())//
//					.put(MasterFilterResourceType.PART, QueryUtils.QueryRelation.builder()//
//							.field(Q.part.id)//
//							.join(() -> QueryUtils.joinPartByLocation(query, join))//
//							.build())//
					.put(MasterFilterResourceType.PLANT, QueryUtils.QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> QueryUtils.joinLocationByCounter(query, join))//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static void applyTerminalFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyTerminalFilter(query, join, filterCode, true);
	}

	public static void applyTerminalDisabledFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		_applyTerminalFilter(query, join, filterCode, false);
	}

	private static void _applyTerminalFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode, boolean enabled) {
		BooleanBuilder filter = new BooleanBuilder();
		if (enabled) {
			if (!join.contains(Q.terminal)) {
				QueryUtils.isTerminal(filter);
			}
		} else {
			filter.and(isDisabled(Q.terminal.enabled));
		}
		if (!join.contains(Q.terminal)) {
			join.add(Q.terminal);
		}
		if (AccessControlUtils.isAccessFilterRequired()) {
			distinct(query);
			QueryUtils.joinSupplierByTerminal(query, join);
			QueryUtils.applyCompanyFilter(filter, Q.supplier.id);
		}
		query.where(filter);

		if (!ObjectUtils.isEmpty(filterCode)) {
			Map<MasterFilterResourceType, QueryRelation> relations = new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(Q.supplier.id)//
							.join(() -> QueryUtils.joinSupplierByTerminal(query, join))//
							.build())//
//					.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder()//
//							.field(Q.toolmaker.id)//
//							.join(() -> QueryUtils.joinToolmakerByTerminal(query, join))//
//							.build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(Q.location.id)//
							.join(() -> QueryUtils.joinLocationByTerminal(query, join))//
							.build())//
					.build();

			QueryUtils.applyMasterFilter(query, filterCode, relations);
		}
	}

	public static WorkOrderFilter buildWorkOrderFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, String filterCode) {
		if (!join.contains(Q.workOrder)) {
			join.add(Q.workOrder);
		}

		distinct(query);

		QueryUtils.leftJoin(query, join, qWorkOrderAsset4Mold, qWorkOrderAsset4Mold.workOrderId.eq(Q.workOrder.id).and(qWorkOrderAsset4Mold.type.eq(ObjectType.TOOLING)));
		QueryUtils.leftJoin(query, join, Q.mold, Q.mold.id.eq(qWorkOrderAsset4Mold.assetId));
		QueryUtils.leftJoinSupplierByMold(query, join, qSupplier4Mold);
		QueryUtils.leftJoinToolmakerByMold(query, join, qToolmaker4Mold);
		QueryUtils.leftJoinLocationByMold(query, join, qLocation4Mold);
		QueryUtils.leftJoinPartByMold(query, join, qMoldPart4Mold, qPart4Mold);
		QueryUtils.leftJoinProductByPart(query, join, qProduct4Mold, qPart4Mold);

		QueryUtils.leftJoin(query, join, qWorkOrderAsset4Machine, qWorkOrderAsset4Machine.workOrderId.eq(Q.workOrder.id).and(qWorkOrderAsset4Mold.type.eq(ObjectType.MACHINE)));
		QueryUtils.leftJoin(query, join, Q.machine, Q.machine.id.eq(qWorkOrderAsset4Machine.assetId));
		QueryUtils.leftJoinSupplierByMachine(query, join, qSupplier4Machine);
		QueryUtils.leftJoinLocationByMachine(query, join, qLocation4Machine);

		QueryUtils.leftJoin(query, join, qWorkOrderAsset4Counter, qWorkOrderAsset4Counter.workOrderId.eq(Q.workOrder.id).and(qWorkOrderAsset4Mold.type.eq(ObjectType.COUNTER)));
		QueryUtils.leftJoin(query, join, Q.counter, Q.counter.id.eq(qWorkOrderAsset4Counter.assetId));
		QueryUtils.leftJoinSupplierByCounter(query, join, qSupplier4Counter);
		QueryUtils.leftJoinLocationByCounter(query, join, qLocation4Counter);

		QueryUtils.leftJoin(query, join, qWorkOrderAsset4Terminal, qWorkOrderAsset4Terminal.workOrderId.eq(Q.workOrder.id).and(qWorkOrderAsset4Mold.type.eq(ObjectType.TERMINAL)));
		QueryUtils.leftJoin(query, join, Q.terminal, Q.terminal.id.eq(qWorkOrderAsset4Terminal.assetId));
		QueryUtils.leftJoinSupplierByTerminal(query, join, qSupplier4Terminal);
		QueryUtils.leftJoinLocationByTerminal(query, join, qLocation4Terminal);

		QueryUtils.leftJoin(query, join, Q.workOrderUser, Q.workOrderUser.workOrderId.eq(Q.workOrder.id));
		QueryUtils.leftJoin(query, join, Q.user, isEnabled(Q.user.enabled).and(Q.user.id.eq(Q.workOrderUser.userId)));
		QueryUtils.leftJoinSupplierByUser(query, join, qSupplier4User);

		BooleanBuilder moldFilter = new BooleanBuilder();
		BooleanBuilder machineFilter = new BooleanBuilder();
		BooleanBuilder counterFilter = new BooleanBuilder();
		BooleanBuilder terminalFilter = new BooleanBuilder();
		BooleanBuilder userFilter = new BooleanBuilder();

		if (AccessControlUtils.isAccessFilterRequired()) {
			QueryUtils.applyCompanyFilter(moldFilter, qSupplier4Mold.id);
			QueryUtils.applyCompanyFilter(machineFilter, qSupplier4Machine.id);
			QueryUtils.applyCompanyFilter(counterFilter, qSupplier4Counter.id);
			QueryUtils.applyCompanyFilter(terminalFilter, qSupplier4Terminal.id);
			QueryUtils.applyCompanyFilter(userFilter, qSupplier4User.id);
		}

		if (!ObjectUtils.isEmpty(filterCode)) {
			QueryUtils.applyMasterFilter(query, join, moldFilter, filterCode, new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(qSupplier4Mold.id).build())//
					.put(MasterFilterResourceType.TOOLMAKER, QueryRelation.builder()//
							.field(qToolmaker4Mold.id).build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(qLocation4Mold.id).build())//
					.put(MasterFilterResourceType.PART, QueryRelation.builder()//
							.field(qPart4Mold.id).build())//
					.put(MasterFilterResourceType.PRODUCT, QueryRelation.builder()//
							.field(qProduct4Mold.id).build())//
					.build());
			QueryUtils.applyMasterFilter(query, join, machineFilter, filterCode, new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(qSupplier4Machine.id).build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(qLocation4Machine.id).build())//
					.build());
			QueryUtils.applyMasterFilter(query, join, counterFilter, filterCode, new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(qSupplier4Counter.id).build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(qLocation4Counter.id).build())//
					.build());
			QueryUtils.applyMasterFilter(query, join, terminalFilter, filterCode, new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(qSupplier4Terminal.id).build())//
					.put(MasterFilterResourceType.PLANT, QueryRelation.builder()//
							.field(qLocation4Terminal.id).build())//
					.build());
			QueryUtils.applyMasterFilter(query, join, userFilter, filterCode, new MapBuilder<MasterFilterResourceType, QueryRelation>()//
					.put(MasterFilterResourceType.SUPPLIER, QueryRelation.builder()//
							.field(qSupplier4User.id).build())//
					.build());
		}

		WorkOrderFilter woFilter = new WorkOrderFilter();
		woFilter.setFilter(new BooleanBuilder());
		woFilter.setMoldFilter(moldFilter);
		woFilter.setMachineFilter(machineFilter);
		woFilter.setCounterFilter(counterFilter);
		woFilter.setTerminalFilter(terminalFilter);
		woFilter.setUserFilter(userFilter);
		return woFilter;
	}

	public void applyWorkOrderFilter(JPQLQuery<?> query, WorkOrderFilter woFilter) {
		BooleanBuilder filter = woFilter.getFilter();
		filter.or(woFilter.getMoldFilter());
		filter.or(woFilter.getMachineFilter());
		filter.or(woFilter.getCounterFilter());
		filter.or(woFilter.getTerminalFilter());
		filter.or(woFilter.getUserFilter());
		query.where(filter);
	}

	private static final QWorkOrderAsset qWorkOrderAsset4Mold = new QWorkOrderAsset("qWorkOrderAsset4Mold");
	private static final QLocation qLocation4Mold = new QLocation("qLocation4Mold");
	private static final QCompany qSupplier4Mold = new QCompany("qSupplier4Mold");
	private static final QCompany qToolmaker4Mold = new QCompany("qToolmaker4Mold");
	private static final QMoldPart qMoldPart4Mold = new QMoldPart("qMoldPart4Mold");
	private static final QPart qPart4Mold = new QPart("qPart4Mold");
	private static final QCategory qProduct4Mold = new QCategory("qProduct4Mold");

	private static final QWorkOrderAsset qWorkOrderAsset4Machine = new QWorkOrderAsset("qWorkOrderAsset4Machine");
	private static final QLocation qLocation4Machine = new QLocation("qLocation4Machine");
	private static final QCompany qSupplier4Machine = new QCompany("qSupplier4Machine");

	private static final QWorkOrderAsset qWorkOrderAsset4Terminal = new QWorkOrderAsset("qWorkOrderAsset4Terminal");
	private static final QLocation qLocation4Terminal = new QLocation("qLocation4Terminal");
	private static final QCompany qSupplier4Terminal = new QCompany("qSupplier4Terminal");

	private static final QWorkOrderAsset qWorkOrderAsset4Counter = new QWorkOrderAsset("qWorkOrderAsset4Counter");
	private static final QLocation qLocation4Counter = new QLocation("qLocation4Counter");
	private static final QCompany qSupplier4Counter = new QCompany("qSupplier4Counter");

	private static final QCompany qSupplier4User = new QCompany("qSupplier4User");

	@Data
	public static class WorkOrderFilter {
		private BooleanBuilder filter;
		private BooleanBuilder moldFilter;
		private BooleanBuilder machineFilter;
		private BooleanBuilder counterFilter;
		private BooleanBuilder terminalFilter;
		private BooleanBuilder userFilter;

		private QWorkOrderAsset qWorkOrderAsset4Mold = QueryUtils.qWorkOrderAsset4Mold;
		private QLocation qLocation4Mold = QueryUtils.qLocation4Mold;
		private QCompany qSupplier4Mold = QueryUtils.qSupplier4Mold;
		private QCompany qToolmaker4Mold = QueryUtils.qToolmaker4Mold;
		private QPart qPart4Mold = QueryUtils.qPart4Mold;
		private QCategory qProduct4Mold = QueryUtils.qProduct4Mold;

		private QWorkOrderAsset qWorkOrderAsset4Machine = QueryUtils.qWorkOrderAsset4Machine;
		private QLocation qLocation4Machine = QueryUtils.qLocation4Machine;
		private QCompany qSupplier4Machine = QueryUtils.qSupplier4Machine;

		private QWorkOrderAsset qWorkOrderAsset4Counter = QueryUtils.qWorkOrderAsset4Counter;
		private QLocation qLocation4Counter = QueryUtils.qLocation4Counter;
		private QCompany qSupplier4Counter = QueryUtils.qSupplier4Counter;

		private QWorkOrderAsset qWorkOrderAsset4Terminal = QueryUtils.qWorkOrderAsset4Terminal;
		private QLocation qLocation4Terminal = QueryUtils.qLocation4Terminal;
		private QCompany qSupplier4Terminal = QueryUtils.qSupplier4Terminal;

		private QCompany qSupplier4User = QueryUtils.qSupplier4User;
	}

	private static void joinCompanyByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.company, QueryUtils.isCompany().and(Q.company.id.eq(Q.mold.companyId)));
	}

	public static void joinCompanyByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.company, QueryUtils.isCompany().and(Q.company.id.eq(Q.machine.companyId)));
	}

	public static void joinSupplierByLocation(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.supplier, QueryUtils.isSupplier().and(Q.supplier.id.eq(Q.location.companyId)));
	}

	public static void joinSupplierByProduct(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldByProduct(query, join);
		QueryUtils.joinSupplierByMold(query, join);
	}

	public static void joinSupplierByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldByPart(query, join);
		QueryUtils.joinSupplierByMold(query, join);
	}

	public static void joinSupplierByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.supplier, isSupplier().and(Q.supplier.id.eq(Q.mold.supplierCompanyId)));
	}

	public static void leftJoinSupplierByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoinSupplierByMold(query, join, Q.supplier);
	}

	private static void leftJoinSupplierByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qSupplier) {
		QueryUtils.leftJoin(query, join, qSupplier, isSupplier(qSupplier).and(qSupplier.id.eq(Q.mold.supplierCompanyId)));
	}

	private static void leftJoinToolmakerByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qToolmaker) {
		QueryUtils.leftJoin(query, join, qToolmaker, isToolmaker(qToolmaker).and(qToolmaker.id.eq(Q.mold.toolMakerCompanyId)));
	}

	private static void leftJoinSupplierByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qSupplier) {
		QueryUtils.leftJoin(query, join, qSupplier, isSupplier(qSupplier).and(qSupplier.id.eq(Q.machine.companyId)));
	}

	// TODO Change 2 step -> 1 step
	public static void joinSupplierByTerminal(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinLocationByTerminal(query, join);
		QueryUtils.joinSupplierByLocation(query, join);
	}

	private static void leftJoinSupplierByTerminal(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qSupplier) {
		QueryUtils.leftJoin(query, join, qSupplier, isSupplier(qSupplier).and(qSupplier.id.eq(Q.terminal.companyId)));
	}

	public static void joinSupplierByCounter(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinLocationByCounter(query, join);
		QueryUtils.joinSupplierByLocation(query, join);
	}

	private static void leftJoinSupplierByCounter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qSupplier) {
		QueryUtils.leftJoin(query, join, qSupplier, isSupplier(qSupplier).and(qSupplier.id.eq(Q.counter.companyId)));
	}

	private static void leftJoinSupplierByUser(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qSupplier) {
		QueryUtils.leftJoin(query, join, qSupplier, isSupplier(qSupplier).and(qSupplier.id.eq(Q.user.companyId)));
	}

	public static void joinToolmakerByLocation(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.toolmaker, QueryUtils.isToolmaker().and(Q.toolmaker.id.eq(Q.location.companyId)));
	}

	public static void joinToolmakerByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.toolmaker, isToolmaker().and(Q.toolmaker.id.eq(Q.mold.toolMakerCompanyId)));
	}

	public static void joinLocationByCompany(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qCompany) {
		QueryUtils.join(query, join, Q.location, QueryUtils.isLocation().and(Q.location.companyId.eq(qCompany.id)));
	}

	public static void joinLocationByProduct(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartByProduct(query, join);
		QueryUtils.joinLocationByPart(query, join);
	}

	public static void joinLocationByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldByPart(query, join);
		QueryUtils.joinLocationByMold(query, join);
	}

	public static void joinLocationByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.location, Q.location.id.eq(Q.mold.locationId));
	}

	public static void leftJoinLocationByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoinLocationByMold(query, join, Q.location);
	}

	private static void leftJoinLocationByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QLocation qLocation) {
		QueryUtils.leftJoin(query, join, qLocation, qLocation.id.eq(Q.mold.locationId));
	}

	public static void joinLocationByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.location, Q.location.id.eq(Q.machine.locationId));
	}

	private static void leftJoinLocationByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QLocation qLocation) {
		QueryUtils.leftJoin(query, join, qLocation, qLocation.id.eq(Q.machine.locationId));
	}

	public static void joinLocationByCounter(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.location, Q.location.id.eq(Q.counter.locationId));
	}

	private static void leftJoinLocationByCounter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QLocation qLocation) {
		QueryUtils.leftJoin(query, join, qLocation, qLocation.id.eq(Q.counter.locationId));
	}

	public static void joinLocationByTerminal(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.location, Q.location.id.eq(Q.terminal.locationId));
	}

	private static void leftJoinLocationByTerminal(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QLocation qLocation) {
		QueryUtils.leftJoin(query, join, qLocation, qLocation.id.eq(Q.terminal.locationId));
	}

	public static void leftJoinAreaByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoin(query, join, Q.area, Q.area.id.eq(Q.mold.areaId));
	}

	public static void joinCategoryByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinProductByPart(query, join);
		QueryUtils.join(query, join, Q.category, QueryUtils.isCategory().and(Q.category.id.eq(Q.product.grandParentId).or(Q.category.id.eq(Q.product.parentId))));
	}

	public static void leftJoinCategoryByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoinProductByPart(query, join);
		QueryUtils.leftJoin(query, join, Q.category, QueryUtils.isCategory().and(Q.category.id.eq(Q.product.grandParentId).or(Q.category.id.eq(Q.product.parentId))));
	}

	public static void joinBrandByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinProductByPart(query, join);
		QueryUtils.joinBrandByProduct(query, join);
	}

	public static void leftJoinBrandByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoinProductByPart(query, join);
		QueryUtils.leftJoinBrandByProduct(query, join);
	}

//	public static void joinProductByCompany(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qCompany) {
//		QueryUtils.joinPartByCompany(query, join, qCompany);
//		QueryUtils.joinProductByPart(query, join);
//	}

	public static void joinProductBySupplier(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartBySupplier(query, join);
		QueryUtils.joinProductByPart(query, join);
	}

	public static void joinProductByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartByMold(query, join);
		QueryUtils.joinProductByPart(query, join);
	}

	public static void joinProductByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartByMachine(query, join);
		QueryUtils.joinProductByPart(query, join);
	}

	public static void joinProductByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.product, QueryUtils.isProduct().and(Q.product.id.eq(Q.part.categoryId)));
	}

	public static void leftJoinProductByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoinProductByPart(query, join, Q.product, Q.part);
	}

	private static void leftJoinProductByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCategory qProduct, QPart qPart) {
		QueryUtils.leftJoin(query, join, qProduct, QueryUtils.isProduct(qProduct).and(qProduct.id.eq(qPart.categoryId)));
	}

	public static void joinBrandByProduct(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.brand, QueryUtils.isBrand().and(Q.brand.id.eq(Q.product.parentId)));
	}

	public static void leftJoinBrandByProduct(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoin(query, join, Q.brand, QueryUtils.isBrand().and(Q.brand.id.eq(Q.product.parentId)));
	}

//	public static void joinPartByCompany(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qCompany) {
//		QueryUtils.joinMoldByCompany(query, join, qCompany);
//		QueryUtils.joinPartByMold(query, join);
//	}

	public static void joinPartBySupplier(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartBySupplier(query, join, Q.supplier);
	}

	public static void joinPartBySupplier(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qCompany) {
		QueryUtils.joinMoldBySupplier(query, join, qCompany);
		QueryUtils.joinPartByMold(query, join);
	}

	public static void joinPartByToolmaker(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldByToolmaker(query, join);
		QueryUtils.joinPartByMold(query, join);
	}

	public static void joinProductByLocation(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartByLocation(query, join);
		QueryUtils.joinProductByPart(query, join);
	}

	public static void joinPartByLocation(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldByLocation(query, join);
		QueryUtils.joinPartByMold(query, join);
	}

	public static void joinPartByProduct(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.part, QueryUtils.isPart().and(Q.part.categoryId.eq(Q.product.id)));
	}

	public static void joinPartByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.moldPart, Q.moldPart.moldId.eq(Q.mold.id));
		QueryUtils.join(query, join, Q.part, QueryUtils.isPart().and(Q.part.id.eq(Q.moldPart.partId)));
	}

	public static void leftJoinPartByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoinPartByMold(query, join, Q.moldPart, Q.part);
	}

	private static void leftJoinPartByMold(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QMoldPart qMoldPart, QPart qPart) {
		QueryUtils.leftJoin(query, join, qMoldPart, qMoldPart.moldId.eq(Q.mold.id));
		QueryUtils.leftJoin(query, join, qPart, QueryUtils.isPart(qPart).and(qPart.id.eq(qMoldPart.partId)));
	}

	public static void joinPartByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldByMachine(query, join);
		QueryUtils.joinPartByMold(query, join);
	}

//	public static void joinMoldByCompany(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qCompany) {
//		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.location.companyId.eq(qCompany.id)));
//	}

	public static void joinMoldBySupplier(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinMoldBySupplier(query, join, Q.supplier);
	}

	public static void joinMoldBySupplier(JPQLQuery<?> query, Set<EntityPathBase<?>> join, QCompany qCompany) {
		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.supplierCompanyId.eq(qCompany.id)));
	}

	public static void joinMoldByToolmaker(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.toolMakerCompanyId.eq(Q.toolmaker.id)));
	}

	public static void joinMoldByLocation(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.locationId.eq(Q.location.id)));
	}

	public static void joinMoldByProduct(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.joinPartByProduct(query, join);
		QueryUtils.joinMoldByPart(query, join);
	}

	public static void joinMoldByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.moldPart, Q.moldPart.partId.eq(Q.part.id));
		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.id.eq(Q.moldPart.moldId)));
	}

	public static void leftJoinMoldByPart(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.leftJoin(query, join, Q.moldPart, Q.moldPart.partId.eq(Q.part.id));
		QueryUtils.leftJoin(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.id.eq(Q.moldPart.moldId)));
	}

	public static void joinMoldByMachine(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.id.eq(Q.machine.mold.id)));
	}

	public static void joinMoldByCounter(JPQLQuery<?> query, Set<EntityPathBase<?>> join) {
		QueryUtils.join(query, join, Q.mold, QueryUtils.isMold().and(Q.mold.counterId.eq(Q.counter.id)));
	}

	private static final String INCLUDE_DISABLED_PROP = "QueryUtils.includeDisabled";

	public static void includeDisabled(EntityPathBase<?> entity) {
		@SuppressWarnings("unchecked")
		Set<EntityPathBase<?>> set = (Set<EntityPathBase<?>>) ThreadUtils.getProp(INCLUDE_DISABLED_PROP);
		if (set == null) {
			set = new HashSet<>();
			ThreadUtils.setProp(INCLUDE_DISABLED_PROP, set);
		}
		set.add(entity);
	}

	private static boolean isIncludeDisabled(EntityPathBase<?> entity) {
		@SuppressWarnings("unchecked")
		Set<EntityPathBase<?>> set = (Set<EntityPathBase<?>>) ThreadUtils.getProp(INCLUDE_DISABLED_PROP);
		return set != null && set.contains(entity);
	}

	private static void cleanIncludeDisabled() {
		ThreadUtils.setProp(INCLUDE_DISABLED_PROP, null);
	}

	public static BooleanBuilder isCompany(BooleanBuilder filter, QCompany qCompany) {
		return filter.and(isEnabled(qCompany.enabled));
	}

	public static BooleanExpression isCompany() {
		return isCompany(Q.company);
	}

	public static BooleanExpression isCompany(QCompany qCompany) {
		return isIncludeDisabled(qCompany) ? qCompany.id.isNotNull() //
				: isEnabled(qCompany.enabled);
	}

	public static BooleanBuilder isInhouse(BooleanBuilder filter) {
		filter.and(Q.inhouse.companyType.eq(CompanyType.IN_HOUSE));
		return isIncludeDisabled(Q.inhouse) ? filter//
				: filter.and(isEnabled(Q.inhouse.enabled));
	}

	public static BooleanExpression isInhouse() {
		return isCompany(Q.inhouse).and(Q.inhouse.companyType.eq(CompanyType.IN_HOUSE));
	}

	public static BooleanBuilder isSupplier(BooleanBuilder filter) {
		filter.and(Q.supplier.companyType.in(CompanyType.SUPPLIER, CompanyType.IN_HOUSE));
		return isIncludeDisabled(Q.supplier) ? filter//
				: filter.and(isEnabled(Q.supplier.enabled));
	}

	public static BooleanExpression isSupplier() {
		return QueryUtils.isSupplier(Q.supplier);
	}

	private static BooleanExpression isSupplier(QCompany qSupplier) {
		return isCompany(qSupplier).and(qSupplier.companyType.in(CompanyType.SUPPLIER, CompanyType.IN_HOUSE));
	}

	public static BooleanBuilder isToolmaker(BooleanBuilder filter) {
		filter.and(Q.toolmaker.companyType.eq(CompanyType.TOOL_MAKER));
		return isIncludeDisabled(Q.toolmaker) ? filter//
				: filter.and(isEnabled(Q.toolmaker.enabled));
	}

	public static BooleanExpression isToolmaker() {
		return QueryUtils.isToolmaker(Q.toolmaker);
	}

	public static BooleanExpression isToolmaker(QCompany qToolmaker) {
		return isCompany(qToolmaker).and(qToolmaker.companyType.eq(CompanyType.TOOL_MAKER));
	}

	public static BooleanBuilder isLocation(BooleanBuilder filter) {
		return isIncludeDisabled(Q.location) ? filter//
				: filter.and(isEnabled(Q.location.enabled));
	}

	public static BooleanExpression isLocation() {
		return isIncludeDisabled(Q.location) ? Q.location.id.isNotNull()//
				: isEnabled(Q.location.enabled);
	}

	public static BooleanBuilder isCategory(BooleanBuilder filter) {
		filter.and(Q.category.level.in(1));
		return isIncludeDisabled(Q.category) ? filter//
				: filter.and(isCategory(Q.category));
	}

	public static BooleanExpression isCategory() {
		return isCategory(Q.category).and(Q.category.level.in(1));
	}

	public static BooleanBuilder isBrand(BooleanBuilder filter) {
		filter.and(Q.brand.level.eq(2));
		return isIncludeDisabled(Q.brand) ? filter : //
				filter.and(isCategory(Q.brand));
	}

	public static BooleanExpression isBrand() {
		return isCategory(Q.brand).and(Q.brand.level.eq(2));
	}

	public static BooleanBuilder isProduct(BooleanBuilder filter) {
		filter.and(Q.product.level.eq(3));
		return isIncludeDisabled(Q.product) ? filter : //
				filter.and(isCategory(Q.product));
	}

	public static BooleanExpression isProduct() {
		return QueryUtils.isProduct(Q.product);
	}

	public static BooleanExpression isProduct(QCategory qCategory) {
		return isCategory(qCategory).and(qCategory.level.eq(3));
	}

	private static BooleanExpression isCategory(QCategory qCategory) {
		return isIncludeDisabled(qCategory) ? qCategory.id.isNotNull() //
				: isEnabled(qCategory.enabled);
	}

	public static BooleanBuilder isPart(BooleanBuilder filter) {
		return isIncludeDisabled(Q.part) ? filter //
				: filter.and(isNotDeleted(Q.part.deleted)).and(isEnabled(Q.part.enabled));
	}

	public static BooleanExpression isPart() {
		return isPart(Q.part);
	}

	private static BooleanExpression isPart(QPart qPart) {
		return isIncludeDisabled(qPart) ? qPart.id.isNotNull() //
				: isNotDeleted(qPart.deleted).and(isEnabled(qPart.enabled));
	}

	public static BooleanExpression isMold() {
		return isMold(Q.mold);
	}

	public static BooleanBuilder isMold(BooleanBuilder filter) {
		return isMold(filter, Q.mold);
	}

	public static BooleanExpression isMold(QMold qMold) {
		return isIncludeDisabled(qMold) ? qMold.id.isNotNull()//
				: isEnabled(qMold.enabled)//
						.and(qMold.equipmentStatus.notIn(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED));
	}

	public static BooleanBuilder isMold(BooleanBuilder filter, QMold qMold) {
		return isIncludeDisabled(qMold) ? filter//
				: filter.and(isEnabled(qMold.enabled))//
						.and(qMold.equipmentStatus.notIn(EquipmentStatus.DISPOSED, EquipmentStatus.DISCARDED));
	}

	public static BooleanBuilder isMachine(BooleanBuilder filter) {
		return isIncludeDisabled(Q.machine) ? filter //
				: filter.and(isNotDeleted(Q.machine.deleted)).and(isEnabled(Q.machine.enabled));
	}

	public static BooleanExpression isMachine() {
		return isIncludeDisabled(Q.machine) ? Q.machine.id.isNotNull() //
				: isNotDeleted(Q.machine.deleted).and(isEnabled(Q.machine.enabled));
	}

	public static BooleanBuilder isTerminal(BooleanBuilder filter) {
		return isIncludeDisabled(Q.terminal) ? filter //
				: filter.and(isEnabled(Q.terminal.enabled));
	}

	public static BooleanExpression isTerminal() {
		return isIncludeDisabled(Q.terminal) ? Q.terminal.id.isNotNull() //
				: isEnabled(Q.terminal.enabled);
	}

	public static BooleanBuilder isCounter(BooleanBuilder filter) {
		return isIncludeDisabled(Q.counter) ? filter //
				: filter.and(isEnabled(Q.counter.enabled));
	}

	public static BooleanExpression isCounter() {
		return isIncludeDisabled(Q.counter) ? Q.counter.id.isNotNull() //
				: isEnabled(Q.counter.enabled);
	}

	private static BooleanExpression isEnabled(BooleanPath field) {
		return field.isNull().or(field.isTrue());
	}

	private static BooleanExpression isDisabled(BooleanPath field) {
		return field.isNotNull().and(field.isFalse());
	}

	private static BooleanExpression isNotDeleted(BooleanPath field) {
		return field.isNull().or(field.isFalse());
	}

	@Deprecated
	/**
	 * @deprecated use TabUtils.findAll instead
	 */
	public static <I> List<Tab> findTabs(ObjectType objectType, I input, Page<?> page, Closure1Param<I, Long> closure) {
		return TabUtils.findAll(objectType, input, page, closure);
	}

	@Deprecated
	/**
	 * @deprecated use TabUtils.findTable instead
	 */
	public static TabTable findTabTable(ObjectType objectType, String tabName) {
		return TabUtils.findTable(objectType, tabName);
	}

	@Deprecated
	/**
	 * @deprecated use TabUtils.apply instead
	 */
	public static TabTable applyTabInput(Object input, ObjectType objectType, String tabName) {
		return TabUtils.applyInput(input, objectType, tabName);
	}

	@Deprecated
	/**
	 * @deprecated use TabUtils.applyFilter instead
	 */
	public static void applyTabFilter(JPQLQuery<?> query, Set<EntityPathBase<?>> join, TabTable tabTable, NumberPath<Long> idField) {
		TabUtils.applyFilter(query, join, tabTable, idField);
	}

	public static <T> JPQLQuery<T> applyPagination(JPQLQuery<T> query, Pageable pageable) {
		init();

		if (pageable == null || pageable.isUnpaged()) {
			return query;
		}

		query.offset(pageable.getOffset());
		query.limit(pageable.getPageSize());

		return query;
	}

	public static <T> JPQLQuery<T> applyPagination(JPQLQuery<T> query, Pageable pageable, Map<String, ComparableExpressionBase<?>> fieldMap, OrderSpecifier<?>... defaultOrder) {
		applyPagination(query, pageable);
		applySort(query, pageable, fieldMap, defaultOrder);
		return query;
	}

	public static String getFirstSortProperty(Pageable pageable) {
		Order order = getFirstOrder(pageable);
		return order == null ? null : order.getProperty();
	}

	public static Direction getFirstSortDirection(Pageable pageable) {
		Order order = getFirstOrder(pageable);
		return order == null ? null : order.getDirection();
	}

	private static Order getFirstOrder(Pageable pageable) {
		Sort sort = pageable.getSort();
		if (sort == null || sort.isUnsorted()) {
			return null;
		}
		Iterator<Order> itr = sort.iterator();
		return itr.hasNext() ? itr.next() : null;
	}

	public static Pageable applySortDefault(Pageable pageable, Direction direction, String... field) {
		init();
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), direction, field);
		}
		return pageable;
	}

	public static void applySort(JPQLQuery<?> query, OrderSpecifier<?>... order) {
		applySort(query, (Sort) null, null, order);
	}

	public static void applySort(JPQLQuery<?> query, Pageable pageable, Map<String, ComparableExpressionBase<?>> fieldMap, OrderSpecifier<?>... defaultOrder) {
		applySort(query, pageable == null ? (Sort) null : pageable.getSort(), fieldMap, defaultOrder);
	}

	public static void applySort(JPQLQuery<?> query, Sort sort, Map<String, ComparableExpressionBase<?>> fieldMap, OrderSpecifier<?>... defaultOrder) {
		init();
		if (sort == null || sort.isUnsorted()) {
			if (defaultOrder != null) {
				query.orderBy(defaultOrder);
			}
			return;
		} else if (fieldMap == null) {
			return;
		}

		sort.forEach(action -> {
			String field = action.getProperty();
			if (fieldMap.containsKey(field)) {
				ComparableExpressionBase<?> expr = fieldMap.get(field);
				boolean asc = action.getDirection() == null || action.getDirection().isAscending();
				query.orderBy(asc ? expr.asc() : expr.desc());
			} else if (fieldMap.containsKey(field + ".opposite")) {
				ComparableExpressionBase<?> expr = fieldMap.get(field + ".opposite");
				boolean asc = action.getDirection() == null || action.getDirection().isAscending();
				query.orderBy(asc ? expr.desc() : expr.asc());
			}
		});
	}

	private static void init() {
		setAutoDistinct(true);
		cleanIncludeDisabled();
	}

}
