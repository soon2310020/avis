package com.emoldino.api.common.resource.composite.flt.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;
import com.emoldino.api.common.resource.base.option.repository.maserfilteritem.MasterFilterItem;
import com.emoldino.api.common.resource.base.option.repository.maserfilteritem.MasterFilterItemRepository;
import com.emoldino.api.common.resource.base.option.repository.masterfilterresource.MasterFilterResource;
import com.emoldino.api.common.resource.base.option.repository.masterfilterresource.MasterFilterResourceRepository;
import com.emoldino.api.common.resource.base.option.repository.masterfilterresource.QMasterFilterResource;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompanyIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltMachine;
import com.emoldino.api.common.resource.composite.flt.dto.FltMold;
import com.emoldino.api.common.resource.composite.flt.dto.FltPart;
import com.emoldino.api.common.resource.composite.flt.dto.FltPartIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlant;
import com.emoldino.api.common.resource.composite.flt.dto.FltPlantIn;
import com.emoldino.api.common.resource.composite.flt.dto.FltProduct;
import com.emoldino.api.common.resource.composite.flt.dto.FltResource;
import com.emoldino.api.common.resource.composite.flt.dto.FltResourceType;
import com.emoldino.api.common.resource.composite.flt.dto.FltUser;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MapBuilder;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.common.enumeration.CompanyType;
import saleson.common.util.SecurityUtils;
import saleson.model.QCompany;
import saleson.model.User;

@Service
@Transactional
public class FltService {

	public Page<FltCompany> getCompanies(FltCompanyIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		Long companyId = SecurityUtils.getCompanyId();
		if (userId == null || companyId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		QCompany qCompany = Q.company(input.getCompanyType());

		JPQLQuery<FltCompany> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FltCompany.class, qCompany))//
				.distinct()//
				.from(qCompany);
		Set<EntityPathBase<?>> join = new HashSet<>();

		BooleanBuilder filter = new BooleanBuilder();
		if (input.getId() != null) {
			filter.and(qCompany.id.eq(input.getId()));
		}
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			filter.and(//
					qCompany.id.stringValue().eq(searchWord)//
							.or(qCompany.name.containsIgnoreCase(searchWord))//
							.or(qCompany.companyCode.containsIgnoreCase(searchWord))//
			);
		}
		if (!ObjectUtils.isEmpty(input.getPartId())) {
			if (Q.toolmaker.equals(qCompany)) {
				QueryUtils.joinPartByToolmaker(query, join);
			} else {
				QueryUtils.joinPartBySupplier(query, join, qCompany);
			}
			filter.and(Q.part.id.in(input.getPartId()));
		}
		query.where(filter);

		QueryUtils.applyCompanyFilter(query, join, input.getFilterCode(), input.getCompanyType());

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", qCompany.id)//
				.put("companyCode", qCompany.companyCode)//
				.put("name", qCompany.name)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, qCompany.name.asc());
		QueryResults<FltCompany> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public Page<FltUser> getUsers(FltIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		// TODO Common Filter / getUsers
		UserParam param = new UserParam();
		param.setStatus("active");
		param.setQuery(input.getQuery());
		param.setId(input.getId());
		Page<User> page = BeanUtils.get(UserService.class).findAll(param, pageable);
		List<FltUser> content = page.getContent().stream().map(user -> new FltUser(user)).collect(Collectors.toList());

		return new PageImpl<>(content, pageable, page.getTotalElements());
	}

	public Page<FltProduct> getProducts(FltIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		JPQLQuery<FltProduct> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FltProduct.class, Q.product))//
				.distinct()//
				.from(Q.product);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = new BooleanBuilder();

		filter.and(Q.product.name.ne("Not specified"));

		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			filter.and(//
					Q.product.id.stringValue().eq(searchWord)//
							.or(Q.product.name.containsIgnoreCase(searchWord))//
			);
		}
		query.where(filter);

		QueryUtils.applyProductFilter(query, join, input.getFilterCode());

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("name", Q.product.name)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.product.name.asc());
		QueryResults<FltProduct> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public Page<FltPart> getParts(FltIn input, Pageable pageable) {
		return getParts(input instanceof FltPartIn ? (FltPartIn) input : ValueUtils.map(input, FltPartIn.class), pageable);
	}

	public Page<FltPart> getParts(FltPartIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null || SecurityUtils.isToolmaker()) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		// Query
		JPQLQuery<FltPart> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FltPart.class, Q.part))//
				.distinct()//
				.from(Q.part);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = new BooleanBuilder();

		if (input.getId() != null) {
			filter.and(Q.part.id.eq(input.getId()));
		}
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			filter.and(//
					Q.part.id.stringValue().eq(searchWord)//
							.or(Q.part.partCode.containsIgnoreCase(searchWord))//
							.or(Q.part.name.containsIgnoreCase(searchWord))//
			);
		}

		if (!ObjectUtils.isEmpty(input.getMoldId())) {
			QueryUtils.joinMoldByPart(query, join);
			filter.and(Q.mold.id.in(input.getMoldId()));
		}

		query.where(filter);

		QueryUtils.applyPartFilter(query, join, input.getFilterCode());

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.part.id)//
				.put("partId", Q.part.id)//
				.put("name", Q.part.name)//
				.put("partName", Q.part.name)//
				.put("partCode", Q.part.partCode)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.part.name.asc());
		QueryResults<FltPart> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public Page<FltCompany> getSuppliers(FltIn input, Pageable pageable) {
		FltCompanyIn fltCompanyIn = new FltCompanyIn(input, CompanyType.SUPPLIER, CompanyType.IN_HOUSE);
		Page<FltCompany> page = getCompanies(fltCompanyIn, pageable);
		return page;
	}

	public Page<FltCompany> getToolmakers(FltIn input, Pageable pageable) {
		FltCompanyIn reqin = new FltCompanyIn(input, CompanyType.TOOL_MAKER);
		Page<FltCompany> page = getCompanies(reqin, pageable);
		return page;
	}

	public Page<FltPlant> getPlants(FltIn input, Pageable pageable) {
		FltPlantIn reqin = input instanceof FltPlantIn ? (FltPlantIn) input : new FltPlantIn(input);
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		JPQLQuery<FltPlant> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FltPlant.class, Q.location))//
				.distinct()//
				.from(Q.location);
		Set<EntityPathBase<?>> join = new HashSet<>();

		BooleanBuilder filter = new BooleanBuilder();
		if (input.getId() != null) {
			filter.and(Q.location.id.eq(input.getId()));
		}
		if (!ObjectUtils.isEmpty(reqin.getQuery())) {
			String searchWord = reqin.getQuery();
			filter.and(//
					Q.location.id.stringValue().eq(searchWord)//
							.or(Q.location.name.containsIgnoreCase(searchWord))//
							.or(Q.location.locationCode.contains(searchWord))//
			);
		}
		if (!ObjectUtils.isEmpty(reqin.getPartId())) {
			QueryUtils.joinPartByLocation(query, join);
			filter.and(Q.part.id.in(reqin.getPartId()));
		}
		if (!ObjectUtils.isEmpty(reqin.getSupplierId())) {
			QueryUtils.joinSupplierByLocation(query, join);
			filter.and(Q.supplier.id.in(reqin.getSupplierId()));
		}
		query.where(filter);

		QueryUtils.applyPlantFilter(query, join, reqin.getFilterCode());

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.location.id)//
				.put("name", Q.location.name)//
				.put("locationName", Q.location.name)//
				.put("plantName", Q.location.name)//
				.put("locationCode", Q.location.locationCode)//
				.put("plantCode", Q.location.locationCode)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.location.name.asc());
		QueryResults<FltPlant> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public Page<FltMold> getMolds(FltIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		Long companyId = SecurityUtils.getCompanyId();
		if (userId == null || companyId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		JPQLQuery<FltMold> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FltMold.class, Q.mold))//
				.distinct()//
				.from(Q.mold);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = new BooleanBuilder();

		if (input.getId() != null) {
			filter.and(Q.mold.id.eq(input.getId()));
		}
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			filter.and(//
					Q.mold.equipmentCode.containsIgnoreCase(searchWord)//
			);
		}
		query.where(filter);

		QueryUtils.applyMoldFilter(query, join, input.getFilterCode());

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.mold.id)//
				.put("moldId", Q.mold.id)//
				.put("equipmentCode", Q.mold.equipmentCode)//
				.put("moldCode", Q.mold.equipmentCode)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.mold.equipmentCode.asc());
		QueryResults<FltMold> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public Page<FltMachine> getMachines(FltIn input, Pageable pageable) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new PageImpl<>(Collections.emptyList(), pageable, 0);
		}

		JPQLQuery<FltMachine> query = BeanUtils.get(JPAQueryFactory.class)//
				.select(Projections.constructor(FltMachine.class, Q.machine))//
				.distinct()//
				.from(Q.machine);
		Set<EntityPathBase<?>> join = new HashSet<>();
		BooleanBuilder filter = new BooleanBuilder();

		if (input.getId() != null) {
			filter.and(Q.machine.id.eq(input.getId()));
		}
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			String searchWord = input.getQuery();
			filter.and(//
					Q.machine.machineCode.containsIgnoreCase(searchWord)//
			);
		}
		query.where(filter);

		QueryUtils.applyMachineFilter(query, join, input.getFilterCode());

		Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
				.put("id", Q.machine.id)//
				.put("machineId", Q.machine.id)//
				.put("machineCode", Q.machine.machineCode)//
				.put("equipmentCode", Q.machine.machineCode)//
				.build();
		QueryUtils.applyPagination(query, pageable, fieldMap, Q.machine.machineCode.asc());
		QueryResults<FltMachine> results = query.fetchResults();
		return new PageImpl<>(results.getResults(), pageable, results.getTotal());
	}

	public ListOut<FltResource> get(String filterCode) {
		LogicUtils.assertNotNull(filterCode, "filterCode");
		List<FltResource> list = BeanUtils.get(MasterFilterResourceRepository.class).findAllByFilterCodeAndUserIdOrderByPosition(filterCode, SecurityUtils.getUserId())//
				.stream()//
				.map(resource -> {
					List<MasterFilterItem> items = BeanUtils.get(MasterFilterItemRepository.class)//
							.findAllByFilterCodeAndUserIdAndResourceType(filterCode, SecurityUtils.getUserId(), resource.getResourceType());
					int temporalSize[] = { 0 };
					List<Long> ids = items//
							.stream()//
							.filter(item -> (MasterFilterMode.SELECTED.equals(resource.getMode()) && ValueUtils.toBoolean(item.getSelected(), false))//
									|| (MasterFilterMode.UNSELECTED.equals(resource.getMode())))//
							.map(item -> {
								if (ValueUtils.toBoolean(item.getTemporal(), false)) {
									temporalSize[0]++;
								}
								return item.getResourceId();
							})//
							.collect(Collectors.toList());
					long selectedCount = 0;
					List<Long> selected = null;
					List<Long> unselected = null;
					if (MasterFilterMode.SELECTED.equals(resource.getMode())) {
						selected = ids;
						selectedCount = selected.size();
					} else if (MasterFilterMode.UNSELECTED.equals(resource.getMode())) {
						unselected = ids;
						ThreadUtils.setProp("FltService.resourceType", resource.getResourceType());
						try {
							long total = getTotal(filterCode, resource.getResourceType());
							selectedCount = total - temporalSize[0];
						} finally {
							ThreadUtils.setProp("FltService.resourceType", null);
						}
					}
					return new FltResource(resource.getResourceType(), resource.getMode(), selectedCount, selected, unselected);
				})//
				.collect(Collectors.toList());
		return new ListOut<>(list);
	}

	private long getTotal(String filterCode, MasterFilterResourceType resourceType) {
		long total = 0;
		FltIn reqin = FltIn.builder().filterCode(filterCode).build();
		switch (resourceType) {
		case SUPPLIER:
			total = getSuppliers(reqin, PageRequest.of(0, 1)).getTotalElements();
			break;
		case TOOLMAKER:
			total = getToolmakers(reqin, PageRequest.of(0, 1)).getTotalElements();
			break;
		case PLANT:
			total = getPlants(reqin, PageRequest.of(0, 1)).getTotalElements();
			break;
		case TOOLING:
			total = getMolds(reqin, PageRequest.of(0, 1)).getTotalElements();
			break;
		case PRODUCT:
			total = getProducts(reqin, PageRequest.of(0, 1)).getTotalElements();
			break;
		case PART:
			total = getParts(reqin, PageRequest.of(0, 1)).getTotalElements();
			break;
		}
		return total;
	}

	public void post(String filterCode, FltResource input) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		MasterFilterResourceType resourceType = input.getResourceType();
		MasterFilterMode mode = input.getMode();

		LogicUtils.assertNotNull(filterCode, "filterCode");
		LogicUtils.assertNotNull(resourceType, "resourceType");
		LogicUtils.assertNotNull(input.getMode(), "mode");

		MasterFilterResource resource = BeanUtils.get(MasterFilterResourceRepository.class).findByFilterCodeAndUserIdAndResourceType(filterCode, userId, resourceType).orElse(null);

		QMasterFilterResource table = QMasterFilterResource.masterFilterResource;
		if (resource == null) {
			int[] lastPosition = { 0 };
			BeanUtils.get(MasterFilterResourceRepository.class).findAll(new BooleanBuilder()//
					.and(table.filterCode.eq(filterCode))//
					.and(table.userId.eq(SecurityUtils.getUserId())), //
					PageRequest.of(0, 1, Direction.DESC, "position"))//
					.forEach(lastResource -> lastPosition[0] = lastResource.getPosition());

			resource = new MasterFilterResource();
			resource.setFilterCode(filterCode);
			resource.setUserId(userId);
			resource.setResourceType(resourceType);
			resource.setPosition(lastPosition[0] + 1);
			resource.setMode(mode);
			BeanUtils.get(MasterFilterResourceRepository.class).save(resource);

			boolean selected = MasterFilterMode.SELECTED.equals(mode);
			List<Long> resourceIds = selected ? input.getSelectedIds() : input.getUnselectedIds();
			if (!ObjectUtils.isEmpty(resourceIds)) {
				List<MasterFilterItem> items = resourceIds.stream().map(resourceId -> {
					MasterFilterItem item = new MasterFilterItem();
					item.setFilterCode(filterCode);
					item.setUserId(userId);
					item.setResourceType(resourceType);
					item.setSelected(selected);
					item.setTemporal(false);
					item.setResourceId(resourceId);
					return item;
				}).collect(Collectors.toList());
				BeanUtils.get(MasterFilterItemRepository.class).saveAll(items);
			}

		} else {
			if (!input.getMode().equals(resource.getMode())) {
				throw new LogicException("MODE_CHANGED");
			}

			List<MasterFilterItem> items = new ArrayList<>();
			List<MasterFilterItem> ditems = new ArrayList<>();
			boolean selected = MasterFilterMode.SELECTED.equals(mode);
			List<Long> ids = selected ? input.getSelectedIds() : input.getUnselectedIds();
			boolean idsEmpty = ObjectUtils.isEmpty(ids);

			if (selected) {
				BeanUtils.get(MasterFilterItemRepository.class).findAllByFilterCodeAndUserIdAndResourceType(filterCode, userId, resourceType).forEach(item -> {
					if (ValueUtils.toBoolean(item.getSelected(), false)) {
						if (idsEmpty || !ids.contains(item.getResourceId())) {
							item.setSelected(false);
							items.add(item);
						}
					} else {
						if (!idsEmpty && ids.contains(item.getResourceId())) {
							item.setSelected(true);
							items.add(item);
						}
					}
				});
			} else {
				Map<Long, MasterFilterItem> map = new LinkedHashMap<>();
				BeanUtils.get(MasterFilterItemRepository.class).findAllByFilterCodeAndUserIdAndResourceType(filterCode, userId, resourceType).forEach(item -> {
					map.put(item.getResourceId(), item);
					if (ValueUtils.toBoolean(item.getTemporal(), false) && (idsEmpty || !ids.contains(item.getResourceId()))) {
						ditems.add(item);
					}
				});

				if (!idsEmpty) {
					items.addAll(ids.stream().filter(id -> !map.containsKey(id)).map(id -> {
						MasterFilterItem item = new MasterFilterItem();
						item.setFilterCode(filterCode);
						item.setUserId(userId);
						item.setResourceType(resourceType);
						item.setResourceId(id);
						item.setSelected(false);
						item.setTemporal(true);
						return item;
					}).collect(Collectors.toList()));
				}
			}
			BeanUtils.get(MasterFilterItemRepository.class).saveAll(items);
			BeanUtils.get(MasterFilterItemRepository.class).deleteAll(ditems);

			BeanUtils.get(MasterFilterResourceRepository.class).flush();
			BeanUtils.get(MasterFilterItemRepository.class).flush();
		}
	}

//	static Map<String, Set<MasterFilterResourceType>> FILTER_AVAILABLE_MAP = new HashMap<>();
//
//	static {
//		FILTER_AVAILABLE_MAP.put("DEMAND_COMPLIANCE", new HashSet<>(Arrays.asList(//
//				MasterFilterResourceType.SUPPLIER, //
//				MasterFilterResourceType.PART, //
//				MasterFilterResourceType.TOOLING)//
//		));
//	}

	public ListOut<FltResourceType> getResourceTypes(String filterCode) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return new ListOut<>();
		}
		LogicUtils.assertNotNull(filterCode, "filterCode");

		Map<MasterFilterResourceType, FltResourceType> map = new MapBuilder<MasterFilterResourceType, FltResourceType>()//
				.put(MasterFilterResourceType.SUPPLIER, new FltResourceType(MasterFilterResourceType.SUPPLIER, "/api/common/flt/suppliers"))//
				.put(MasterFilterResourceType.TOOLMAKER, new FltResourceType(MasterFilterResourceType.TOOLMAKER, "/api/common/flt/toolmakers"))//
				.put(MasterFilterResourceType.PLANT, new FltResourceType(MasterFilterResourceType.PLANT, "/api/common/flt/plants"))//
				.put(MasterFilterResourceType.TOOLING, new FltResourceType(MasterFilterResourceType.TOOLING, "/api/common/flt/molds"))//
				.put(MasterFilterResourceType.PRODUCT, new FltResourceType(MasterFilterResourceType.PRODUCT, "/api/common/flt/products"))//
				.put(MasterFilterResourceType.PART, new FltResourceType(MasterFilterResourceType.PART, "/api/common/flt/parts"))//
				.build();

//		if (FILTER_AVAILABLE_MAP.containsKey(filterCode)) {
//			List<MasterFilterResourceType> inactiveFilter = map.keySet().stream()//
//					.filter(key -> FILTER_AVAILABLE_MAP.get(filterCode).contains(key))//
//					.collect(Collectors.toList());
//			inactiveFilter.forEach(key -> map.remove(key));
//		}

		BeanUtils.get(MasterFilterResourceRepository.class).findAllByFilterCodeAndUserIdOrderByPosition(filterCode, SecurityUtils.getUserId())
				.forEach(resource -> map.remove(resource.getResourceType()));
		return new ListOut<>(new ArrayList<>(map.values()));
	}

	public void delete(String filterCode, MasterFilterResourceType resourceType) {
		Long userId = SecurityUtils.getUserId();
		if (userId == null) {
			return;
		}
		LogicUtils.assertNotNull(filterCode, "filterCode");
		if (resourceType != null) {
			BeanUtils.get(MasterFilterResourceRepository.class).deleteAllByFilterCodeAndUserIdAndResourceType(filterCode, userId, resourceType);
			BeanUtils.get(MasterFilterItemRepository.class).deleteAllByFilterCodeAndUserIdAndResourceType(filterCode, userId, resourceType);
		} else {
			BeanUtils.get(MasterFilterResourceRepository.class).deleteAllByFilterCodeAndUserId(filterCode, userId);
			BeanUtils.get(MasterFilterItemRepository.class).deleteAllByFilterCodeAndUserId(filterCode, userId);
		}
		BeanUtils.get(MasterFilterResourceRepository.class).flush();
		BeanUtils.get(MasterFilterItemRepository.class).flush();
	}

}
