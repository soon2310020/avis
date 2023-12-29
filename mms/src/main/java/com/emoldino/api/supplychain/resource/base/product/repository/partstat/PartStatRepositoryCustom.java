package com.emoldino.api.supplychain.resource.base.product.repository.partstat;

import java.util.List;

public interface PartStatRepositoryCustom {

	List<PartStat> findAllWeeklyByProduct(String week, Long productId, Long partId, List<Long> supplierId, Long moldId);

	List<PartStat> findAllWeeklyByBrand(String week, Long brandId, Long partId, List<Long> supplierId, Long moldId);

}
