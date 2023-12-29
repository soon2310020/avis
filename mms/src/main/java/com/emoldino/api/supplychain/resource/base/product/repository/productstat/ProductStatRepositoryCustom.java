package com.emoldino.api.supplychain.resource.base.product.repository.productstat;

import java.util.List;

public interface ProductStatRepositoryCustom {

	ProductStat getWeeklyByProduct(String week, Long productId);

	List<ProductStat> getWeeklyByBrand(String week, Long brandId);

}
