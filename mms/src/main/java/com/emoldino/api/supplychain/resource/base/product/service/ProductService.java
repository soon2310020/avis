package com.emoldino.api.supplychain.resource.base.product.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.supplychain.resource.base.product.dto.PartLite;
import com.emoldino.api.supplychain.resource.base.product.dto.PartLiteGetIn;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductLite;
import com.emoldino.api.supplychain.resource.base.product.dto.ProductLiteGetIn;
import com.emoldino.api.supplychain.resource.base.product.repository.partdemand.PartDemand;
import com.emoldino.api.supplychain.resource.base.product.repository.partdemand.PartDemandRepository;
import com.emoldino.api.supplychain.resource.base.product.repository.productdemand.ProductDemand;
import com.emoldino.api.supplychain.resource.base.product.repository.productdemand.ProductDemandRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.enumeration.TimeScale;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.category.CategoryRepository;
import saleson.api.part.PartRepository;
import saleson.model.Category;
import saleson.model.Part;
import saleson.model.QCategory;

@Service
public class ProductService {

	public ProductLite getLite(Long id) {
		Category product = BeanUtils.get(CategoryRepository.class).findById(id).orElse(null);
		return product == null ? null : new ProductLite(product.getId(), product.getName());
	}

	public Page<ProductLite> getPageLite(ProductLiteGetIn input, Pageable pageable) {
		BooleanBuilder filter = new BooleanBuilder();
		QCategory table = QCategory.category;
		filter.and(table.level.eq(3));
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(table.name.likeIgnoreCase("%" + input.getQuery() + "%"));
		}
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.ASC, "name");
		}
		Page<Category> page = BeanUtils.get(CategoryRepository.class).findAll(filter, pageable);

		List<ProductLite> list = new ArrayList<>();
		page.forEach(product -> list.add(new ProductLite(product.getId(), product.getName())));

		return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
	}

	public Page<PartLite> getPartPageLite(PartLiteGetIn input, Pageable pageable) {
		Assert.notNull(input.getProductId(), "productId is required!!");
		Page<Part> page = BeanUtils.get(PartRepository.class).findAllByProduct(input.getProductId(), null, null, input.getQuery(), pageable);

		List<PartLite> list = new ArrayList<>();
		page.forEach(part -> list.add(new PartLite(part.getId(), part.getName(), part.getPartCode())));
		return new PageImpl<>(list, page.getPageable(), page.getTotalElements());
	}

	public long getDemandQty(Long productId, String periodType, String periodValue) {
		if (productId == null) {
			return 0;
		}

		// Get Demand
		ProductDemand demand = getDemand(productId, periodType, periodValue);
		if (demand != null) {
			return demand.getQuantity();
		}

		Category category = BeanUtils.get(CategoryRepository.class).findById(productId).orElse(null);
		if (category == null) {
			return 0;
		}
		return ValueUtils.toLong(category.getWeeklyProductionDemand(), 0L);
	}

	public long getDemandQtyByBrand(Long brandId, String periodType, String periodValue) {
		if (brandId == null) {
			return 0;
		}

		// Get Demand
		List<ProductDemand> demand = getDemandByBrand(brandId, periodType, periodValue);
		if (CollectionUtils.isNotEmpty(demand)) {
			return demand.stream().mapToLong(ProductDemand::getQuantity).sum();
		}

		List<Category> category = (List<Category>) BeanUtils.get(CategoryRepository.class).findAll(QCategory.category.id.in(ProductUtils.filterProductByBrand(brandId)));
		if (CollectionUtils.isEmpty(category)) {
			return 0;
		}
		return ValueUtils.toLong(category.stream().filter(c -> c.getWeeklyProductionDemand() != null).mapToLong(Category::getWeeklyProductionDemand).sum(), 0L);
	}

	private ProductDemand getDemand(Long productId, String periodType, String periodValue) {
		LogicUtils.assertNotEmpty(productId, "productId");
		LogicUtils.assertNotEmpty(periodType, "periodType");
		LogicUtils.assertNotEmpty(periodValue, "periodValue");

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.productDemand.productId.eq(productId));
		filter.and(Q.productDemand.periodType.eq(periodType));
		filter.and(Q.productDemand.periodValue.eq(periodValue));
		return BeanUtils.get(ProductDemandRepository.class).findOne(filter).orElse(null);
	}

	private List<ProductDemand> getDemandByBrand(Long brandId, String periodType, String periodValue) {
		LogicUtils.assertNotEmpty(brandId, "brandId");
		LogicUtils.assertNotEmpty(periodType, "periodType");
		LogicUtils.assertNotEmpty(periodValue, "periodValue");

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.productDemand.productId.in(ProductUtils.filterProductByBrand(brandId)));
		filter.and(Q.productDemand.periodType.eq(periodType));
		filter.and(Q.productDemand.periodValue.eq(periodValue));
		return (List<ProductDemand>) BeanUtils.get(ProductDemandRepository.class).findAll(filter);
	}

	public void saveDemand(ProductDemand data) {
		Assert.notNull(data.getQuantity(), "quantity is required!!");

		// Get Demand
		ProductDemand demand = getDemand(data.getProductId(), data.getPeriodType(), data.getPeriodValue());

		// Set Demand
		if (demand == null) {
			demand = new ProductDemand();
			demand.setProductId(data.getProductId());
			demand.setPeriodType(data.getPeriodType());
			demand.setPeriodValue(data.getPeriodValue());
		}
		if (!data.getQuantity().equals(demand.getQuantity())) {
			demand.setQuantity(data.getQuantity());
			// Save Demand
			BeanUtils.get(ProductDemandRepository.class).save(demand);
		}

		String day = DateUtils2.toDateRange(TimeSetting.builder()//
				.timeScale(TimeScale.WEEK)//
				.timeValue(data.getPeriodValue())//
				.build()).getSecond();

		ProductUtils.getProducableParts(data.getProductId(), day).forEach(part -> {
			PartDemand partDemand = new PartDemand();
			partDemand.setProductId(data.getProductId());
			partDemand.setPartId(part.getId());
			partDemand.setPeriodType(data.getPeriodType());
			partDemand.setPeriodValue(data.getPeriodValue());
			partDemand.setQuantity(data.getQuantity() * ValueUtils.toLong(part.getQuantityRequired(), 0L));
			savePartDemand(partDemand);
		});
	}

	public long getPartDemandQty(Long productId, Long partId, String periodType, String periodValue) {
		// Get Part Demand
		PartDemand partDemand = getPartDemand(productId, partId, periodType, periodValue);
		if (partDemand != null) {
			return partDemand.getQuantity();
		}

		if (productId == null) {
			return 0;
		}

		// Get Demand
		long quantity = getDemandQty(productId, periodType, periodValue);

		Part part = BeanUtils.get(PartRepository.class).findById(partId).orElse(null);
		if (part == null || part.getQuantityRequired() == null || part.getQuantityRequired() <= 0) {
			return 0L;
		}

		return quantity * part.getQuantityRequired();
	}

	public long getPartDemandQtyByBrand(Long brandId, Long partId, String periodType, String periodValue) {
		// Get Part Demand
		PartDemand partDemand = getPartDemandByBrand(brandId, partId, periodType, periodValue);
		if (partDemand != null) {
			return partDemand.getQuantity();
		}

		// Get Demand
		long quantity = getDemandQtyByBrand(brandId, periodType, periodValue);

		Part part = BeanUtils.get(PartRepository.class).findById(partId).orElse(null);
		if (part == null || part.getQuantityRequired() == null || part.getQuantityRequired() <= 0) {
			return 0L;
		}

		return quantity * part.getQuantityRequired();
	}

	private PartDemand getPartDemand(Long productId, Long partId, String periodType, String periodValue) {
		LogicUtils.assertNotNull(partId, "partId");
		LogicUtils.assertNotNull(periodType, "periodType");
		LogicUtils.assertNotNull(periodValue, "periodValue");

		BooleanBuilder filter = new BooleanBuilder();
		if (productId == null) {
			filter.and(Q.partDemand.productId.isNull());
		} else {
			filter.and(Q.partDemand.productId.eq(productId));
		}
		filter.and(Q.partDemand.partId.eq(partId));
		filter.and(Q.partDemand.periodType.eq(periodType));
		filter.and(Q.partDemand.periodValue.eq(periodValue));
		return BeanUtils.get(PartDemandRepository.class).findOne(filter).orElse(null);
	}

	private PartDemand getPartDemandByBrand(Long brandId, Long partId, String periodType, String periodValue) {
		LogicUtils.assertNotNull(brandId, "brandId");
		LogicUtils.assertNotNull(partId, "partId");
		LogicUtils.assertNotNull(periodType, "periodType");
		LogicUtils.assertNotNull(periodValue, "periodValue");

		BooleanBuilder filter = new BooleanBuilder();
		filter.and(Q.partDemand.productId.in(ProductUtils.filterProductByBrand(brandId)));
		filter.and(Q.partDemand.partId.eq(partId));
		filter.and(Q.partDemand.periodType.eq(periodType));
		filter.and(Q.partDemand.periodValue.eq(periodValue));
		return BeanUtils.get(PartDemandRepository.class).findOne(filter).orElse(null);
	}

	private void savePartDemand(PartDemand data) {
		Assert.notNull(data.getQuantity(), "quantity is required!!");

		// Get Demand
		PartDemand demand = getPartDemand(data.getProductId(), data.getPartId(), data.getPeriodType(), data.getPeriodValue());

		// Set Demand
		if (demand == null) {
			demand = new PartDemand();
			demand.setProductId(data.getProductId());
			demand.setPartId(data.getPartId());
			demand.setPeriodType(data.getPeriodType());
			demand.setPeriodValue(data.getPeriodValue());
		} else {
			// We save this data for quantity value only;
			if (data.getQuantity().equals(demand.getQuantity())) {
				return;
			}
		}
		demand.setQuantity(data.getQuantity());

		// Save Demand
		BeanUtils.get(PartDemandRepository.class).save(demand);
	}

	public static void savePartDemand(Long partId, List<String> periodValue, long quantity, String periodType) {
		if (partId == null || ObjectUtils.isEmpty(periodValue)) {
			return;
		}
		Part part = BeanUtils.get(PartRepository.class).findById(partId).orElse(null);
		periodValue.forEach(week -> {
			PartDemand partDemand = new PartDemand();
			partDemand.setProductId(part.getCategoryId());
			partDemand.setPartId(partId);
			partDemand.setPeriodType(periodType);
			partDemand.setPeriodValue(week);
			partDemand.setQuantity(quantity * part.getQuantityRequired());
			BeanUtils.get(ProductService.class).savePartDemand(partDemand);
		});
	}

}
