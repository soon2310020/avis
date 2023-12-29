package saleson.api.category;

import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStatRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.ProductStat;
import com.emoldino.api.supplychain.resource.base.product.repository.productstat.ProductStatRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.repository.Q;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.Predicate;

import lombok.extern.slf4j.Slf4j;
import saleson.api.category.payload.*;
import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.SupplierGetIn;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataCompletionRate.payload.DataCompletionRatePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartProjectProducedRepository;
import saleson.api.part.PartRepository;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.StorageType;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageRepository;
import saleson.common.service.FileStorageService;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.dto.CategoryFullDTO;
import saleson.model.Category;
import saleson.model.Company;
import saleson.model.FileStorage;
import saleson.model.Mold;
import saleson.model.Part;
import saleson.model.StatisticsPart;
import saleson.model.customField.CustomFieldValue;
import saleson.model.data.MiniComponentData;
import saleson.model.data.completionRate.CompletionRateData;
import saleson.service.util.DateTimeUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static saleson.common.config.Const.*;

@Slf4j
@Service
public class CategoryService {

	@Autowired
	CategoryRepository categoryRepository;

	@Autowired
	PartRepository partRepository;

	@Autowired
	VersioningService versioningService;

	@Autowired
	DataCompletionRateService dataCompletionRateService;

	@Autowired
	FileStorageService fileStorageService;

	@Autowired
	FileStorageRepository fileStorageRepository;

	@Autowired
	CompanyRepository companyRepository;

	@Autowired
	MoldRepository moldRepository;

	@Autowired
	StatisticsPartRepository statisticsPartRepository;

	@Autowired
	PartProjectProducedRepository partProjectProducedRepository;

	@Autowired
	CustomFieldValueService customFieldValueService;

	@Value("${file.storage.location}")
	private String fileStorageLocation;

	@Value("${system.storage.type}")
	private String systemStorageType;

	public Page<Category> findAll(Predicate predicate, Pageable pageable) {
		return findAll(predicate, pageable, null, null);
	}

	public Page<Category> findAll(Predicate predicate, Pageable pageable, String periodType, String periodValue) {
		// TODO improve sort logic simpler with the correct UI params
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.ASC, "name");
		} else if (!ObjectUtils.isEmpty(periodValue) && pageable.getSort().getOrderFor("name") == null) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.ASC, "name");
		}
		Page<Category> page = categoryRepository.findAll(predicate, pageable);
		Pageable _pageable = pageable;
		page.filter(category -> category.getLevel() == 1).forEach(category -> {
			if (!ObjectUtils.isEmpty(category.getGrandchildren())) {
				category.getGrandchildren().forEach(product -> populateProductStat(product, periodType, periodValue));
				sortProducts(category.getGrandchildren(), _pageable);
			}
			if (!ObjectUtils.isEmpty(category.getChildren())) {
				category.getChildren().forEach(brand -> {
					if (!ObjectUtils.isEmpty(brand.getChildren())) {
						brand.getChildren().forEach(product -> populateProductStat(product, periodType, periodValue));
						sortProducts(brand.getChildren(), _pageable);
					}
				});
			}
		});
		return page;
	}

	public Page<CategoryFullDTO> findAll(Predicate predicate, Pageable pageable,
										 String periodType, String periodValue,
										 Long brandId, Boolean enabled, Long productId) {
		Page<Category> categoryPage = findAll(predicate, pageable, periodType, periodValue);
		List<CategoryFullDTO> categoryFullDTOList = mappingCategoryToPojo(categoryPage.getContent(), enabled);
		loadValueCustomField(categoryFullDTOList);
		categoryFullDTOList = setChildCategory(categoryFullDTOList, brandId, productId);

		return new PageImpl<>(categoryFullDTOList, categoryPage.getPageable(), categoryPage.getTotalElements());
	}

	private List<CategoryFullDTO> setChildCategory(List<CategoryFullDTO> categoryFullDTOList, Long brandId, Long productId) {
		if (brandId != null) {
			return categoryFullDTOList.stream()
					.filter(category -> category.getChildren().stream().anyMatch(brand -> brandId.equals(brand.getId())))
					.map(category -> {
						List<CategoryFullDTO> brandList = category.getChildren()
								.stream()
								.filter(brand -> brandId.equals(brand.getId()))
								.collect(Collectors.toList());
						category.setChildren(brandList);
						return category;
					}).collect(Collectors.toList());
		}

		if (productId != null) {
			return categoryFullDTOList.stream()
					.filter(category -> category.getChildren()
							.stream()
							.anyMatch(brand -> brand.getChildren().stream().anyMatch(product -> productId.equals(product.getId()))))
					.map(category -> {
						List<CategoryFullDTO> brandList = category.getChildren()
								.stream()
								.filter(brand -> brand.getChildren().stream().anyMatch(product -> productId.equals(product.getId())))
								.map(brand -> {
									List<CategoryFullDTO> productList = brand.getChildren().stream().filter(product -> productId.equals(product.getId())).collect(Collectors.toList());
									brand.setChildren(productList);
									return brand;
								}).collect(Collectors.toList());
						category.setChildren(brandList);
						return category;
					}).collect(Collectors.toList());
		}
		return categoryFullDTOList;
	}
	public List<Long> findAllIds (CategoryParam payload) {
		BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)
				.select(Projections.constructor(Long.class, Q.category.id))
				.from(Q.category);
		query.where(builder);
		QueryResults<Long> results = query.fetchResults();
		return results.getResults();
	}

	private List<CategoryFullDTO> mappingCategoryToPojo(List<Category> categoryList, Boolean enabled) {
		List<CategoryFullDTO> categoryFullDTOList = Lists.newArrayList();
		for (Category category : categoryList) {
			List<CategoryFullDTO> brandList = Lists.newArrayList();
			CategoryFullDTO categoryFullDTO = new CategoryFullDTO(category);

			category.getGrandchildren().stream().filter(c -> c.getLevel().equals(2)).forEach(child -> {
				CategoryFullDTO branch = new CategoryFullDTO(child);
				if (enabled != null) {
					if (CollectionUtils.isNotEmpty(child.getChildren())
							&& child.getChildren().stream().anyMatch(item -> item.isEnabled() == enabled)) {
						branch.setChildren(child.getChildren().stream().filter(item -> item.isEnabled() == enabled).map(CategoryFullDTO::new).collect(Collectors.toList()));
					}
					setNumberDataToCategory(branch);
					if (enabled && child.isEnabled()) {
						brandList.add(branch);
					}
					if (!enabled && (!child.isEnabled() || CollectionUtils.isNotEmpty(branch.getChildren()))) {
						brandList.add(branch);
					}
				} else {
					branch.setChildren(child.getChildren().stream().map(CategoryFullDTO::new).collect(Collectors.toList()));
					setNumberDataToCategory(branch);
					brandList.add(branch);
				}

			});
			List<Category> productNoBrand = category.getGrandchildren() != null ?
				category.getGrandchildren().stream().
					filter(item -> (enabled == null || item.isEnabled() == enabled) & item.getLevel().equals(3) && item.getParent() == null)
					.collect(Collectors.toList()) : new ArrayList<>();
			if(!productNoBrand.isEmpty()){
				CategoryFullDTO dummyBranch = new CategoryFullDTO();
				dummyBranch.setEnabled(true);
				dummyBranch.setLevel(2);
				dummyBranch.setParentId(category.getId());
				dummyBranch.setParentName(category.getName());
				dummyBranch.setName(CommonMessage.NO_BRAND);
				dummyBranch.setChildren(productNoBrand.stream().map(CategoryFullDTO::new).collect(Collectors.toList()));
				setNumberDataToCategory(dummyBranch);
				brandList.add(dummyBranch);
			}

			/*
			if (category.getGrandchildren().stream().anyMatch(item -> item.getLevel().equals(3) && item.getParent() == null))
				if (enabled != null && CollectionUtils.isNotEmpty(category.getGrandchildren())
						&& category.getGrandchildren().stream().anyMatch(item -> item.isEnabled() == enabled)) {
					CategoryFullDTO dummyBranch = new CategoryFullDTO();
					dummyBranch.setEnabled(true);
					dummyBranch.setLevel(2);
					dummyBranch.setParentId(category.getId());
					dummyBranch.setParentName(category.getName());
					dummyBranch.setName(CommonMessage.NO_BRAND);
					dummyBranch.setChildren(category.getGrandchildren().stream().filter(item -> item.isEnabled() == enabled && item.getLevel().equals(3) && item.getParent() == null).map(CategoryFullDTO::new).collect(Collectors.toList()));
					setNumberDataToCategory(dummyBranch);
				brandList.add(dummyBranch);
			} else if (enabled == null && CollectionUtils.isNotEmpty(category.getGrandchildren())) {
				CategoryFullDTO dummyBranch = new CategoryFullDTO();
				dummyBranch.setEnabled(true);
				dummyBranch.setLevel(2);
				dummyBranch.setParentId(category.getId());
				dummyBranch.setParentName(category.getName());
					dummyBranch.setName(CommonMessage.NO_BRAND);
					dummyBranch.setChildren(category.getGrandchildren().stream().filter(item -> item.getLevel().equals(3) && item.getParent() == null).map(CategoryFullDTO::new).collect(Collectors.toList()));
					setNumberDataToCategory(dummyBranch);
				brandList.add(dummyBranch);
			}
			*/
			categoryFullDTO.setChildren(brandList);
			setNumberDataToCategory(categoryFullDTO);
			categoryFullDTOList.add(categoryFullDTO);
		}

		return categoryFullDTOList;
	}

	private void setNumberDataToCategory(CategoryFullDTO data) {
		data.setPartCount(data.getChildren().stream().map(CategoryFullDTO::getPartCount).reduce(Integer::sum).orElse(0));
		data.setMoldCount(data.getChildren().stream().map(CategoryFullDTO::getMoldCount).reduce(Integer::sum).orElse(0));
		data.setSupplierCount(data.getChildren().stream().map(CategoryFullDTO::getSupplierCount).reduce(Integer::sum).orElse(0));
	}

	private void loadValueCustomField(List<CategoryFullDTO> categoryList){
		//get data for custom field
		Map<Long, Map<Long,List<CustomFieldValue>>> mapValueCustomField=customFieldValueService.mapValueCustomField(ObjectType.CATEGORY,categoryList.stream().map(CategoryFullDTO::getId).collect(Collectors.toList()));
		categoryList.stream().forEach(category -> {
			if(mapValueCustomField.containsKey(category.getId())){
				category.setCustomFieldValueMap(mapValueCustomField.get(category.getId()));
			}
			if(CollectionUtils.isNotEmpty(category.getChildren())) {
				loadValueCustomField(category.getChildren());
			}
		});
	}

	public void removeUnnecessaryInfo(List<CategoryFullDTO> categories) {
		categories.forEach(category -> {
//			category.setPartCount(null);
//			category.setSupplierCount(null);
//			category.setMoldCount(null);
			category.setTotalProduced(null);
			category.setPredictedQuantity(null);
			category.setTotalProductionDemand(null);
			category.setTotalMaxCapacity(null);
			category.getChildren().forEach(brand -> {
//				brand.setPartCount(null);
//				brand.setSupplierCount(null);
//				brand.setMoldCount(null);
				brand.setTotalProduced(null);
				brand.setPredictedQuantity(null);
				brand.setTotalProductionDemand(null);
				brand.setTotalMaxCapacity(null);
			});
		});
	}

	private static void sortProducts(List<Category> categories, Pageable _pageable) {
		categories.sort((a, b) -> {
			String prop = "name";
			boolean asc = true;
			if (_pageable.getSort() != null && _pageable.getSort().isSorted()) {
				Order order = _pageable.getSort().getOrderFor("name");
				if (order != null) {
					asc = order.isAscending();
				} else {
					order = _pageable.getSort().getOrderFor("deliveryRiskLevel");
					if (order != null) {
						prop = "deliveryRiskLevel";
						asc = order.isAscending();
					}
				}
			}
			if ("deliveryRiskLevel".equals(prop)) {
				if (a.getDeliverableRate() > b.getDeliverableRate()) {
					return asc ? -1 : 1;
				} else if (a.getDeliverableRate() == b.getDeliverableRate()) {
					return 0;
				} else {
					return asc ? 1 : -1;
				}
			} else {
				if (b.getName() == null) {
					return asc ? 1 : a.getName() == null ? 1 : -1;
				} else if (a.getName() == null) {
					return asc ? -1 : 1;
				} else {
					return asc ? a.getName().compareTo(b.getName()) : b.getName().compareTo(a.getName());
				}
			}
		});
	}

	private static void populateProductStat(Category product, String periodType, String periodValue) {
		String _periodValue = ObjectUtils.isEmpty(periodValue) ? ProductUtils.getThisWeek() : periodValue;
		ProductStat stat = BeanUtils.get(ProductStatRepository.class).getWeeklyByProduct(_periodValue, product.getId());
		if (stat == null || stat.getProductId() == null) {
			product.setPartCount(BeanUtils.get(PartRepository.class).countByProduct(product.getId(), null, null).intValue());
			product.setSupplierCount(BeanUtils.get(CompanyRepository.class).countByProduct(product.getId(), null, null).intValue());
			product.setMoldCount(BeanUtils.get(MoldRepository.class).countByProduct(product.getId(), null, null).intValue());
		} else {
			product.setPartCount(stat.getPartCount());
			product.setSupplierCount(stat.getSupplierCount());
			product.setMoldCount(stat.getMoldCount());
			product.setTotalProduced(stat.getProducedVal());
//			product.setTotalMaxCapacity(stat.getDailyCapacity() * 7);
//			product.setTotalMaxCapacity(ProductUtils.getWeeklyCapacity(_periodValue, product.getId(), null, null, product.getTotalProduced()));
		}
//		BeanUtils.get(ProductStatRepository.class).findAllByProductIdAndWeekOrderByDay(product.getId(), _periodValue).forEach(stat -> {
//			product.setPartCount(Math.max(product.getPartCount(), stat.getPartCount()));
//			product.setSupplierCount(Math.max(product.getSupplierCount(), stat.getSupplierCount()));
//			product.setMoldCount(Math.max(product.getMoldCount(), stat.getMoldCount()));
//			product.setTotalProduced(product.getTotalProduced() + stat.getProduced());
//			product.setTotalMaxCapacity(product.getWeeklyMaxCapacity() + stat.getDailyCapacity());
//		});
		product.setTotalMaxCapacity(ProductUtils.getWeeklyCapa(_periodValue, product.getId(), null, null, product.getTotalProduced()));
		product.setTotalProductionDemand(ProductUtils.getDemandQty(product.getId(), _periodValue));
		ProductUtils.setPredicted(product, _periodValue, product.getTotalProduced());
		ProductUtils.setDeliveryRiskLevel(product);
		product.setProductionChart(ProductUtils.getProgressBarChart(product));
	}

	private static void populateProductStatByBrand(Category brand, String periodType, String periodValue) {
		String _periodValue = ObjectUtils.isEmpty(periodValue) ? ProductUtils.getThisWeek() : periodValue;
		List<ProductStat> stats = BeanUtils.get(ProductStatRepository.class).getWeeklyByBrand(_periodValue, brand.getId());
		if (CollectionUtils.isNotEmpty(stats)) {
			brand.setPartCount(BeanUtils.get(PartRepository.class).countByBrand(brand.getId(), null, null).intValue());
			brand.setSupplierCount(BeanUtils.get(CompanyRepository.class).countByBrand(brand.getId(), null, null).intValue());
			brand.setMoldCount(BeanUtils.get(MoldRepository.class).countByBrand(brand.getId(), null, null).intValue());
		} else {
			brand.setPartCount(stats.stream().mapToInt(ProductStat::getPartCount).sum());
			brand.setSupplierCount(stats.stream().mapToInt(ProductStat::getSupplierCount).sum());
			brand.setMoldCount(stats.stream().mapToInt(ProductStat::getMoldCount).sum());
			brand.setTotalProduced(stats.stream().mapToLong(ProductStat::getProducedVal).sum());
			brand.setTotalMaxCapacity(ProductUtils.getWeeklyCapaByBrand(_periodValue, brand.getId(), null, null, brand.getTotalProduced()));
		}

		brand.setTotalProductionDemand(ProductUtils.getDemandQtyByBrand(brand.getId(), _periodValue));
		ProductUtils.setPredicted(brand, _periodValue, brand.getTotalProduced());
		ProductUtils.setDeliveryRiskLevel(brand);
		brand.setProductionChart(ProductUtils.getProgressBarChart(brand));
	}

	public List<MiniComponentData> findAllMiniData() {
		List<Category> categories = categoryRepository.findAll();
		List<MiniComponentData> componentData = categories.stream().map(x -> {
			MiniComponentData component = new MiniComponentData();
			component.setId(x.getId());
			component.setName(x.getName());
			return component;
		}).collect(Collectors.toList());
		return componentData;
	}

	public void deleteById(Long id) {
		boolean result= deleteCategoryById(id);
		if(!result)
			throw new RuntimeException("You can not delete it because it has a category in use.");
		//update completion rate
		//updateCompletionData();
	}
	private boolean deleteCategoryById(Long id) {
		Optional<Category> optional = categoryRepository.findById(id);

		if (optional.isPresent()) {
			Category category = optional.get();
			boolean exists = partRepository.existsByCategoryId(category.getId());

			if (!exists && category.getChildren() != null) {
				for (Category childCategory : category.getChildren()) {
					exists = partRepository.existsByCategoryId(childCategory.getId());

					if (exists) {
						break;
					}
				}
			}
			if (exists) {
				return false;
			} else {
				// 자식 지우고..
				if (category.getChildren() != null) {
					for (Category childCategory : category.getChildren()) {
						categoryRepository.deleteById(childCategory.getId());
					}
				}
				// 나 지우고.
				categoryRepository.deleteById(id);
			}
		}
		return true;
	}

	public Category findById(Long id) {
		Optional<Category> category = categoryRepository.findById(id);
		if (category.isPresent()) {
			return category.get();
		} else {
			return null;
		}

	}

	@Transactional
	public void save(Category category) {
		save(category, null);
	}

	public void saveOld(Category category, CategoryRequest payload) {

			if (category.getId() == null || category.getLevel() == null || category.getLevel() == 0) {
				category.setLevel(1);
			}
			if (category.getParentId() == null) {
				category.setParent(null);
				if (payload != null && payload.getGrandParentId() != null) {
					category.setGrandParentId(payload.getGrandParentId());
					category.setGrandParent(categoryRepository.getOne(payload.getGrandParentId()));
					category.setLevel(3);
				}
			} else {
				Category parent = categoryRepository.getOne(category.getParentId());
				category.setParent(parent);
				category.setGrandParent(null);
//			if (payload.getGrandParentId() != null) {
//				category.setGrandParentId(payload.getGrandParentId());
//				category.setGrandParent(categoryRepository.getOne(payload.getGrandParentId()));
//			}
				category.setLevel(parent.getLevel() + 1);

				// sortOrder
				if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
					category.setSortOrder(1);
				} else {
					Category lastCategory = parent.getChildren().get(parent.getChildren().size() - 1);
					category.setSortOrder(lastCategory.getSortOrder() + 1);
				}
			}

			boolean newObj = category.getId() == null;

			Category categoryNew = categoryRepository.save(category);
			if (payload != null) {
				payload.setId(categoryNew.getId());
			}

			if (payload != null && CollectionUtils.isNotEmpty(payload.getChildIds())) {
				List<Category> categoriesLevel2 = categoryRepository.findAllById(payload.getChildIds());
				categoriesLevel2.forEach(c -> {
					c.setParentId(categoryNew.getId());
					c.setParent(categoryNew);
					c.setGrandParent(null);
					c.setGrandParentId(null);
				});

				categoryRepository.saveAll(categoriesLevel2);
			}

			//		Write history
			if (newObj)
				versioningService.writeHistory(categoryNew);
			//update completion rate
//		updateCompletionData();
			//update data completion order
//		dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.CATEGORY, category.getId());
			//upload project image
			if (payload != null && payload.getProjectImage() != null) {
				fileStorageService.save(new FileInfo(StorageType.PROJECT_IMAGE, category.getId(), payload.getProjectImage()));
			}

	}

	public void save(Category category, CategoryRequest payload) {

		if (category.getId() == null || category.getLevel() == null || category.getLevel() == 0) {
			if (payload != null && payload.getLevel() != null) {
				category.setLevel(payload.getLevel());
			} else {
				category.setLevel(CATEGORY_LEVEL);
			}
		}

		if (category.getParentId() == null) {
			category.setParent(null);
			if(category.getLevel().equals(PRODUCT_LEVEL))
				category.setSortOrder(1);

		} else {
			Category parent = categoryRepository.getOne(category.getParentId());
			category.setParent(parent);
			if (category.getLevel().equals(PRODUCT_LEVEL)) {
				category.setGrandParent(parent.getGrandParent());
				category.setGrandParentId(parent.getGrandParentId());
			}

				// sortOrder
				if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
					category.setSortOrder(1);
				} else {
					Category lastCategory = parent.getChildren().get(parent.getChildren().size() - 1);
					category.setSortOrder(ValueUtils.toInteger(lastCategory.getSortOrder(),0) + 1);
				}

		}
		if (payload != null && payload.getGrandParentId() != null) {
			category.setGrandParentId(payload.getGrandParentId());
			Category grandParent = categoryRepository.getOne(payload.getGrandParentId());
			category.setGrandParent(grandParent);
			if (category.getLevel().equals(BRAND_LEVEL) && !ObjectUtils.isEmpty(category.getChildren())) {
				category.getChildren().forEach(b ->
				{
					b.setGrandParentId(payload.getGrandParentId());
					b.setGrandParent(grandParent);
				});
			}
		}

		boolean newObj = category.getId() == null;

		Category categoryNew = categoryRepository.save(category);
		if (payload != null)
			payload.setId(categoryNew.getId());

		//add brand to category
		if (payload != null && CollectionUtils.isNotEmpty(payload.getChildIds()) && categoryNew.getLevel().equals(1)) {
			List<Category> categoriesLevel2 = categoryRepository.findAllById(payload.getChildIds());
			categoriesLevel2.forEach(c -> {
				c.setParentId(null);
				c.setParent(null);
				c.setGrandParent(categoryNew);
				c.setGrandParentId(categoryNew.getId());
			});

			categoryRepository.saveAll(categoriesLevel2);
		}

		//		Write history
		if (newObj)
			versioningService.writeHistory(categoryNew);
		//update completion rate
//		updateCompletionData();
		//update data completion order
//		dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.CATEGORY, category.getId());
		//upload project image
		if (payload != null && payload.getProjectImage() != null) {
			fileStorageService.save(new FileInfo(StorageType.PROJECT_IMAGE, category.getId(), payload.getProjectImage()));
		}
}


	public Category saveNew(Category category, CategoryRequest payload) {
		if (category.getId() == null || category.getLevel() == null || category.getLevel() == 0) {
			category.setLevel(1);
		}
		if (category.getParentId() == null) {
			category.setParent(null);
			if (payload.getGrandParentId() != null) {
				category.setGrandParentId(payload.getGrandParentId());
				category.setGrandParent(categoryRepository.getOne(payload.getGrandParentId()));
				category.setLevel(3);
			}
		} else {
			Category parent = categoryRepository.getOne(category.getParentId());
			category.setParent(parent);
			category.setGrandParent(null);
//			if (payload.getGrandParentId() != null) {
//				category.setGrandParentId(payload.getGrandParentId());
//				category.setGrandParent(categoryRepository.getOne(payload.getGrandParentId()));
//			}
			category.setLevel(parent.getLevel() + 1);

			// sortOrder
			if (parent.getChildren() == null || parent.getChildren().isEmpty()) {
				category.setSortOrder(1);
			} else {
				Category lastCategory = parent.getChildren().get(parent.getChildren().size() - 1);
				category.setSortOrder(lastCategory.getSortOrder() + 1);
			}
		}

		boolean newObj = category.getId() == null;

		Category categoryNew = categoryRepository.save(category);
		if (payload != null) {
			payload.setId(categoryNew.getId());
		}

		if (CollectionUtils.isNotEmpty(payload.getChildIds())) {
			List<Category> categoriesLevel2 = categoryRepository.findAllById(payload.getChildIds());
			categoriesLevel2.forEach(c -> {
				c.setParentId(categoryNew.getId());
				c.setParent(categoryNew);
			});

			categoryNew.setChildren(categoriesLevel2);
		}

		//		Write history
		if (newObj)
			versioningService.writeHistory(categoryNew);
		//update completion rate
//		updateCompletionData();
		//update data completion order
//		dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.CATEGORY, category.getId());
		//upload project image
		if (payload != null && payload.getProjectImage() != null) {
			fileStorageService.save(new FileInfo(StorageType.PROJECT_IMAGE, category.getId(), payload.getProjectImage()));
		}
		return categoryNew;
	}

	public boolean existsName(String name, Long id) {
		if (id != null)
			return categoryRepository.existsCategoryByNameAndIdNot(name, id);
		return categoryRepository.existsCategoryByName(name);
	}

	public ApiResponse valid(CategoryRequest payload, Long id) {
		payload.setName(StringUtils.trimWhitespace(payload.getName()));
		boolean existsName = existsName(payload.getName(), id);
		if (existsName) {
			return new ApiResponse(false, "This name is already registered in the system.");
		}
		return null;
	}

	public boolean existsNameAndLevel(String name, Integer level, Long id) {
		if(id == null) {
			return categoryRepository.existsCategoryByNameAndLevel(name, level);
		}
		return categoryRepository.existsCategoryByNameAndLevelAndIdNot(name, level, id);
	}

	public boolean validateBranchName(String name, Integer level, Long parentId, Long id) {
		if (id == null)
			return categoryRepository.existsCategoryByNameAndLevelAndParentId(name, level, parentId);
		return categoryRepository.existsCategoryByNameAndLevelAndParentIdAndIdNot(name, level, parentId, id);
	}

	public ApiResponse validateCreateBranchProduct(CategoryRequest payload, Long id) {
		payload.setName(StringUtils.trimWhitespace(payload.getName()));
/*
		int level = payload.getGrandParentId() == null ? 2 : 3;
		if(level == 2 && validateBranchName(payload.getName(), level, payload.getParentId(), id) && payload.getParentId() != 0) {
			Optional<Category> categoryOptional = categoryRepository.findById(payload.getParentId());
			return new ApiResponse(false, String.format("Brand %s already exists in %s category", payload.getName(), categoryOptional.isPresent() ? categoryOptional.get().getName() : ""));
		}
		if (level == 3) {
			boolean existsName = existsNameAndLevel(payload.getName(), level, id);
			if (existsName) {
				return new ApiResponse(false, "This name is already registered in the system.");
			}
		}

*/
		if(payload.getLevel() == null){
			return new ApiResponse(false, "level is required!");
		}
		if(payload.getLevel() == 1 && existsName(payload.getName(), id)){
				return new ApiResponse(false, "This name is already registered in the system.");
		}
		if(payload.getLevel() == 2 && validateBranchName(payload.getName(), payload.getLevel(), payload.getGrandParentId(), id) && payload.getGrandParentId() != 0) {
			Optional<Category> categoryOptional = categoryRepository.findById(payload.getParentId());
			return new ApiResponse(false, String.format("Brand %s already exists in %s category", payload.getName(), categoryOptional.isPresent() ? categoryOptional.get().getName() : ""));
		}
		if (payload.getLevel() == 3) {
			boolean existsName = existsNameAndLevel(payload.getName(), payload.getLevel(), id);
			if (existsName) {
				return new ApiResponse(false, "This name is already registered in the system.");
			}
		}

		if (CollectionUtils.isNotEmpty(payload.getChildIds())) {
			List<Category> childList = categoryRepository.findAllById(payload.getChildIds());
			List<Category> duplicateChildList = childList.stream()
					.filter(child -> childList.stream().filter(childItem -> childItem.getName().equals(child.getName())).count() > 1)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(duplicateChildList)) {
				return new ApiResponse(false, String.format("Brand %s already exists in %s category",
						duplicateChildList.stream().map(Category::getName).distinct().collect(Collectors.joining(", ")),
						payload.getName()));
			}

		}
		return null;
	}


	public ApiResponse deleteInBatch(BatchUpdateDTO dto) {
		Map<String, List<Long>> map = new HashMap<>();
		map.put(CommonMessage.DELETED, new ArrayList<>());
		map.put(CommonMessage.NOT_DELETED, new ArrayList<>());
		dto.getIds().forEach(id -> {
			boolean result= deleteCategoryById(id);
			if(result){
				map.get(CommonMessage.DELETED).add(id);
			}else {
				map.get(CommonMessage.NOT_DELETED).add(id);
			}
		});

		if (CollectionUtils.isNotEmpty(map.get(CommonMessage.DELETED))) {
			//update completion rate
//			updateCompletionData();
			if (CollectionUtils.isNotEmpty(map.get(CommonMessage.NOT_DELETED))) {
				return ApiResponse.success(CommonMessage.SOME_ITEMS_IN_USE, map);
			} else {
				return ApiResponse.success(CommonMessage.OK, map);
			}
		} else {
			return ApiResponse.error(CommonMessage.ALL_ITEMS_IN_USE);
		}
	}

	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
		try {
			List<Category> categories = categoryRepository.findAllById(dto.getIds());
			categories.forEach(category -> {
				category.setEnabled(dto.isEnabled());

				// 자식이 있는 경우 자식 까지 같이 변경
				if (category.getChildren() != null && !category.getChildren().isEmpty()) {
					for (Category child : category.getChildren()) {
						child.setEnabled(dto.isEnabled());
						save(child);
					}

				}
				save(category);
				Category categoryFinal = findById(category.getId());
				versioningService.writeHistory(categoryFinal);
			});
			return ApiResponse.success(CommonMessage.OK, categories);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	private void updateCompletionData() {
		List<Company> companies = companyRepository.findByEnabled(true);
		companies.forEach(company -> {
			dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.CATEGORY, company.getId());
		});
	}

	public Page<CompletionRateData> getUncompletedCategories(Pageable pageable) {
		DataCompletionRatePayload payload = new DataCompletionRatePayload();
		payload.setUncompletedData(true);
		return categoryRepository.getCompletionRateData(payload, pageable, false);
	}

	public ApiResponse uploadProjectImage(Long id, MultipartFormData formData) {
		try {
			Category category = findById(id);
			if (category == null) {
				return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
			}
			List<FileStorage> oldProjectImages = fileStorageRepository.findByRefIdAndStorageType(id, StorageType.PROJECT_IMAGE);
			if (CollectionUtils.isNotEmpty(oldProjectImages))
				fileStorageRepository.deleteAll(oldProjectImages);
			fileStorageService.save(new FileInfo(StorageType.PROJECT_IMAGE, id, formData.getThirdFiles()));
			return ApiResponse.success(CommonMessage.OK);
		} catch (Exception e) {
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	public ApiResponse getCategoryLibrary(Long id, boolean enabled) {
		try {
			Category mainCategory = categoryRepository.getOne(id);
			ParentLibrary result = new ParentLibrary(mainCategory);

			//Filter Category (Level 2)
			List<ChildProfile> childProfiles;
			// Tab Enabled
			if (enabled) {
				childProfiles = mainCategory.getGrandchildren()
						.stream()
						.filter(category -> category.getLevel() == 2 && category.isEnabled())
						.map(product -> getBrandProfile(product.getId()))
						.collect(Collectors.toList());
			// Tab Disabled
			} else {
				childProfiles = mainCategory.getGrandchildren()
						.stream()
						.filter(category -> category.getLevel() == 2 && !category.isEnabled() || hasDisabledChild(category))
						.map(product -> getBrandProfile(product.getId()))
						.collect(Collectors.toList());
			}
            result.getChildProfiles().addAll(childProfiles);

            // Filter Product (Level 3)
			List<Category> levelThreeCategories = mainCategory.getGrandchildren()
					.stream()
					.filter(product -> product.getParent() == null && product.getLevel() == 3 && enabled == product.isEnabled())
					.collect(Collectors.toList());

			if (CollectionUtils.isNotEmpty(levelThreeCategories)) {
				ChildProfile childProfile = new ChildProfile();
				childProfile.setIsNoBrand(true);
				childProfile.setName(CommonMessage.NO_BRAND);
				childProfile.setGrandParentId(id);
				childProfile.setGrandParentName(mainCategory.getName());
				childProfile.setProductCount(levelThreeCategories.size());
				List<Long> productIdList = levelThreeCategories.stream().map(Category::getId).collect(Collectors.toList());

				childProfile.setPartCount(partRepository.countByProductIdIn(productIdList).intValue());
				childProfile.setSupplierCount(companyRepository.countByProductIdIn(productIdList).intValue());
				childProfile.setMoldCount(moldRepository.countByProductIdIn(productIdList).intValue());
				result.getChildProfiles().add(childProfile);

			}
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	private boolean hasDisabledChild(Category category) {
		return category.getChildren().stream().anyMatch(child -> !child.isEnabled());
	}

	public ApiResponse getBrandLibrary(Long id, boolean enabled) {
		try {
			Category brand = categoryRepository.getOne(id);
			ParentLibrary result = new ParentLibrary(brand);
			brand.getChildren().stream()
					.filter(product -> product.isEnabled() == enabled)
					.map(product -> getProjectProfile(product.getId(), false, null, null))
					.forEach(childProfile -> result.getChildProfiles().add(childProfile));
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}
	public ApiResponse getNoBrandProfile(Long grandParentId, boolean enabled) {
		try {
			List<Category> brand = categoryRepository.findByGrandParentId(grandParentId, enabled);
			List<Long> productIdList = brand.stream().map(Category::getId).collect(Collectors.toList());
			ParentLibrary result = new ParentLibrary();
			result.setName(CommonMessage.NO_BRAND);
			brand.forEach(product -> {
				ChildProfile childProfile = createChildProfile(product, productIdList);
				result.getChildProfiles().add(childProfile);
			});
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	public ChildProfile createChildProfile(Category product, List<Long> productIdList) {
		ChildProfile childProfile = getProjectProfile(product.getId(), false, null, null);
		childProfile.setIsNoBrand(product.isEnabled());
		childProfile.setGrandParentId(product.getGrandParentId());
		childProfile.setGrandParentName(product.getName());
		childProfile.setProductCount(productIdList.size());
		childProfile.setPartCount(partRepository.countByProductIdIn(productIdList).intValue());
		childProfile.setSupplierCount(companyRepository.countByProductIdIn(productIdList).intValue());
		childProfile.setMoldCount(moldRepository.countByProductIdIn(productIdList).intValue());
		return childProfile;
	}


	public ChildProfile getProjectProfile(Long id, String periodType, String periodValue) {
		return getProjectProfile(id, true, periodType, periodValue);
	}

	private ChildProfile getProjectProfile(Long id, boolean prod, String periodType, String periodValue) {
		Category product = categoryRepository.getOne(id);
		ChildProfile data = new ChildProfile(product);

		List<FileStorage> projectImage = fileStorageRepository.findByRefIdAndStorageType(id, StorageType.PROJECT_IMAGE);
		if (CollectionUtils.isNotEmpty(projectImage)) {
			data.setProjectImage(projectImage.get(0));
			if (systemStorageType.equalsIgnoreCase("cloud")) {
				data.setProjectImagePath(projectImage.get(0).getSaveLocation());
			} else {
				data.setProjectImagePath(fileStorageLocation + projectImage.get(0).getSaveLocation());
			}
		}

		if (!prod) {
			data.setPartCount(partRepository.countByProduct(id, null, null).intValue());
			data.setSupplierCount(companyRepository.countByProduct(id, null, null).intValue());
			data.setMoldCount(moldRepository.countByProduct(id, null, null).intValue());
			return data;
		}

		String _periodValue = ObjectUtils.isEmpty(periodValue) ? ProductUtils.getThisWeek() : periodValue;

		populateProductStat(product, periodType, _periodValue);
		fillChildrenInfo(data, product);

		if (data.getWeeklyProductionDemand() != null) {
			long weeklyDemand = ProductUtils.getDemandQty(id, _periodValue);
			if (!data.getWeeklyProductionDemand().equals(weeklyDemand)) {
				data.setSpecificWeeklyProductionDemand(weeklyDemand);
			}
		}

		return data;
	}

	private ChildProfile getBrandProfile(Long id) {
		Category brand = categoryRepository.getOne(id);
		ChildProfile data = new ChildProfile(brand);

		List<FileStorage> projectImage = fileStorageRepository.findByRefIdAndStorageType(id, StorageType.PROJECT_IMAGE);
		if (CollectionUtils.isNotEmpty(projectImage)) {
			data.setProjectImage(projectImage.get(0));
			if (systemStorageType.equalsIgnoreCase("cloud")) {
				data.setProjectImagePath(projectImage.get(0).getSaveLocation());
			} else {
				data.setProjectImagePath(fileStorageLocation + projectImage.get(0).getSaveLocation());
			}
		}

		data.setProductCount(CollectionUtils.size(brand.getChildren()));
		data.setPartCount(partRepository.countByBrand(id, null, null).intValue());
		data.setSupplierCount(companyRepository.countByBrand(id, null, null).intValue());
		data.setMoldCount(moldRepository.countByBrand(id, null, null).intValue());

		return data;
	}

	private void fillChildrenInfo(ChildProfile data, Category child) {
		data.setPartCount(child.getPartCount());
		data.setSupplierCount(child.getSupplierCount());
		data.setMoldCount(child.getMoldCount());

		data.setTotalProduced(child.getTotalProduced());
		data.setPredictedQuantity(child.getPredictedQuantity());
		data.setTotalProductionDemand(child.getTotalProductionDemand());
		data.setTotalMaxCapacity(child.getTotalMaxCapacity());
		data.setDeliveryRiskLevel(child.getDeliveryRiskLevel());
	}

	public ApiResponse getPartByProject(ProfileFilter filter, Pageable pageable) {
		try {
			Page<Part> result = getPartPageByProduct(filter, pageable);
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	public ApiResponse getPartByBrand(ProfileFilter filter, Pageable pageable) {
		try {
			Page<Part> result = getPartPageByBrand(filter, pageable);
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	public Page<Part> getPartPageByBrand(ProfileFilter filter, Pageable pageable) {
		Long brandId = filter.getBrandId();
		Page<Part> page = partRepository.findAllByBrand(brandId, filter.getSupplierId(), filter.getMoldId(), PageRequest.of(0, 1000));

		if (ObjectUtils.isEmpty(filter.getPeriodValue())) {
			filter.setPeriodValue(ProductUtils.getThisWeek());
		}
		page.forEach(part -> {
			// Production
			part.setProducedQuantity(0L);
			BeanUtils.get(PartStatRepository.class).findAllWeeklyByBrand(filter.getPeriodValue(), brandId, part.getId(), filter.getSupplierId(), filter.getMoldId())
					.forEach(stat -> {
						part.setProducedQuantity(part.getProducedQuantity() + stat.getProducedVal());
					});
			part.setTotalMaxCapacity(
					ProductUtils.getWeeklyCapaByBrand(filter.getPeriodValue(), brandId, part.getId(), null, ValueUtils.toLong(part.getTotalProduced(), 0L)));
			part.setTotalProductionDemand(ProductUtils.getPartDemandQtyByBrand(brandId, part.getId(), filter.getPeriodValue()));
			ProductUtils.setPredicted(part, filter.getPeriodValue(), ValueUtils.toLong(part.getTotalProduced(), 0L));
			ProductUtils.setDeliveryRiskLevel(part);
			part.setProductionChart(ProductUtils.getProgressBarChart(part));
		});

		Map<Double, List<Part>> map = new TreeMap<>();
		page.forEach(part -> {
			List<Part> list;
			if (map.containsKey(part.getDeliverableRate())) {
				list = map.get(part.getDeliverableRate());
			} else {
				list = new ArrayList<>();
				map.put(part.getDeliverableRate(), list);
			}
			list.add(part);
		});
		List<Part> parts = new ArrayList<>();
		int i = 0;
		int start = pageable.getPageSize() * pageable.getPageNumber();
		int end = pageable.getPageSize() * pageable.getPageNumber() + pageable.getPageSize();
		for (List<Part> list : map.values()) {
			for (Part part : list) {
				if (i++ < start) {
					continue;
				} else if (i > end) {
					break;
				}
				parts.add(part);
			}
			if (i > end) {
				break;
			}
		}

		return new PageImpl<>(parts, pageable, page.getTotalElements());
	}

	public Page<Part> getPartPageByProduct(ProfileFilter filter, Pageable pageable) {
		Long productId = filter.getProjectId();
		Page<Part> page = partRepository.findAllByProduct(filter.getProjectId(), filter.getSupplierId(), filter.getMoldId(), PageRequest.of(0, 1000));

		if (ObjectUtils.isEmpty(filter.getPeriodValue())) {
			filter.setPeriodValue(ProductUtils.getThisWeek());
		}
		page.forEach(part -> {
			// Production
			part.setProducedQuantity(0L);
			BeanUtils.get(PartStatRepository.class).findAllWeeklyByProduct(filter.getPeriodValue(), productId, part.getId(), filter.getSupplierId(), filter.getMoldId())
					.forEach(stat -> {
						part.setProducedQuantity(part.getProducedQuantity() + stat.getProducedVal());
					});
			part.setTotalMaxCapacity(
					ProductUtils.getWeeklyCapa(filter.getPeriodValue(), filter.getProjectId(), part.getId(), null, ValueUtils.toLong(part.getTotalProduced(), 0L)));
			part.setTotalProductionDemand(ProductUtils.getPartDemandQty(productId, part.getId(), filter.getPeriodValue()));
			ProductUtils.setPredicted(part, filter.getPeriodValue(), ValueUtils.toLong(part.getTotalProduced(), 0L));
			ProductUtils.setDeliveryRiskLevel(part);
			part.setProductionChart(ProductUtils.getProgressBarChart(part));
		});

//		List<StatisticsPartData> statisticsPartDataList = partProjectProducedRepository.getPartProducedByProjectId(filter.getProjectId(), null);
//		page.getContent().stream().forEach(part -> {
//			ProductUtils.setPredicted(part, filter.getPeriodValue());
//			ProductUtils.setDeliveryRiskLevel(part);
//			if (!ObjectUtils.isEmpty(filter.getPeriodValue())) {
//				part.setProductionChart(ProductUtils.getProgressBarChart(part));
//			}
//			statisticsPartDataList.stream().forEach(sp -> {
//				if (part.getId().equals(sp.getPartId())) {
//					part.setStatisticsPartData(sp);
//				}
//			});
//		});

		Map<Double, List<Part>> map = new TreeMap<>();
		page.forEach(part -> {
			List<Part> list;
			if (map.containsKey(part.getDeliverableRate())) {
				list = map.get(part.getDeliverableRate());
			} else {
				list = new ArrayList<>();
				map.put(part.getDeliverableRate(), list);
			}
			list.add(part);
		});
		List<Part> parts = new ArrayList<>();
		int i = 0;
		int start = pageable.getPageSize() * pageable.getPageNumber();
		int end = pageable.getPageSize() * pageable.getPageNumber() + pageable.getPageSize();
		for (List<Part> list : map.values()) {
			for (Part part : list) {
				if (i++ < start) {
					continue;
				} else if (i > end) {
					break;
				}
				parts.add(part);
			}
			if (i > end) {
				break;
			}
		}

		return new PageImpl<Part>(parts, pageable, page.getTotalElements());
	}

	public ApiResponse getSupplierByProject(ProfileFilter filter, Pageable pageable) {
		try {
			SupplierGetIn reqin = new SupplierGetIn();
			reqin.setProductId(filter.getProjectId());
			reqin.setPartId(filter.getPartId());
			reqin.setMoldId(filter.getMoldId());
			reqin.setPeriodType(filter.getPeriodType());
			reqin.setPeriodValue(filter.getPeriodValue());
			Page<Company> result = BeanUtils.get(CompanyService.class).getSuppliers(reqin, pageable);
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	public ApiResponse getSupplierByBrand(ProfileFilter filter, Pageable pageable) {
		try {
			SupplierGetIn reqin = new SupplierGetIn();
			reqin.setProductId(filter.getBrandId());
			reqin.setPartId(filter.getPartId());
			reqin.setMoldId(filter.getMoldId());
			reqin.setPeriodType(filter.getPeriodType());
			reqin.setPeriodValue(filter.getPeriodValue());
			Page<Company> result = BeanUtils.get(CompanyService.class).getSuppliersByBrand(reqin, pageable);
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	public ApiResponse getMoldByProject(ProfileFilter filter, Pageable pageable) {
		Long productId = filter.getProjectId();
		try {
			Page<Mold> result = moldRepository.findByProject(filter.getProjectId(), filter.getPartId(), filter.getSupplierId(), pageable);
			result.getContent().forEach(mold -> {
				// Production
				mold.setTotalProduced(0L);
				BeanUtils.get(PartStatRepository.class).findAllWeeklyByProduct(filter.getPeriodValue(), productId, filter.getPartId(), filter.getSupplierId(), mold.getId())
						.forEach(stat -> {
							mold.setTotalProduced(mold.getTotalProduced() + stat.getProducedVal());
						});
				mold.setTotalMaxCapacity(mold.getMaxCapacityPerWeek());
				ProductUtils.setPredicted(mold, filter.getPeriodValue(), mold.getTotalProduced());

//				mold.setPartsByProject(
//						mold.getParts().stream().filter(p -> p.getProjectId() != null && p.getProjectId().equals(filter.getProjectId())).collect(Collectors.toList()));
//				List<Long> partIds = mold.getPartsByProject().stream().map(PartData::getPartId).collect(Collectors.toList());
//				List<StatisticsPartData> statisticsPartDataList = partProjectProducedRepository.getPartProducedByProjectId(filter.getProjectId(), partIds);
//				mold.setTotalProduced(statisticsPartDataList.stream().mapToInt(StatisticsPartData::getProducedQuantity).sum());
//				if (mold.getLastCycleTime() != null && mold.getLastCycleTime() != 0) {
//					mold.setLastCycleTime(mold.getLastCycleTime() / 10);
//				}
//				// TODO capacity
//				mold.setTotalMaxCapacity(ValueUtils.toLong(mold.getMaxCapacityPerWeek(), 0L));
//				ProductUtils.setPredicted(mold, null);
			});
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	public ApiResponse getMoldByBrand(ProfileFilter filter, Pageable pageable) {
		Long brandId = filter.getBrandId();
		try {
			Page<Mold> result = moldRepository.findByBrand(brandId, filter.getPartId(), filter.getSupplierId(), pageable);
			result.getContent().forEach(mold -> {
				// Production
				mold.setTotalProduced(0L);
				BeanUtils.get(PartStatRepository.class).findAllWeeklyByBrand(filter.getPeriodValue(), brandId, filter.getPartId(), filter.getSupplierId(), mold.getId())
						.forEach(stat -> {
							mold.setTotalProduced(mold.getTotalProduced() + stat.getProducedVal());
						});
				mold.setTotalMaxCapacity(mold.getMaxCapacityPerWeek());
				ProductUtils.setPredicted(mold, filter.getPeriodValue(), mold.getTotalProduced());
			});
			return ApiResponse.success(CommonMessage.OK, result);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ApiResponse.error();
		}
	}

	private Long getTotalProductProduced(Long productId) {
		boolean somePartsNotProduced = partRepository.checkIfSomePartsNotProducedByProjectId(productId);
		if (somePartsNotProduced) {
			return 0L;
		} else {
			Long totalProduced = partProjectProducedRepository.getProjectProduced(productId);
			return totalProduced;
		}
	}

	private Long getTotalProductProductionDemand(Long productId) {
		Category project = categoryRepository.getOne(productId);
		if (project.getWeeklyProductionDemand() == null || project.getWeeklyProductionDemand() == 0) {
			return 0L;
		}

		Optional<StatisticsPart> firstProduced = statisticsPartRepository.findFirstByProjectIdOrderByCreatedAtAsc(productId);
		Long result;
		if (firstProduced.isPresent()) {
			Integer diffDate = DateTimeUtils.diffBetweenTwoDates(firstProduced.get().getCreatedAt(), Instant.now(), true);
			result = (long) (project.getWeeklyProductionDemand() / 7) * diffDate;
		} else {
			result = (long) (project.getWeeklyProductionDemand() / 7);
		}
		return result;
	}

	public List<Category> getAllProductActive(){
		return categoryRepository.findAllByLevelAndEnabledIsTrue(PRODUCT_LEVEL);
	}

}
