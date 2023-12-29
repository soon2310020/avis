package com.emoldino.api.supplychain.resource.base.product;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.supplychain.resource.base.product.dto.ProductStatCleanBatchIn;
import com.emoldino.api.supplychain.resource.base.product.service.moldstat.ProdMoldStatService;
import com.emoldino.api.supplychain.resource.base.product.service.stat.ProductStatService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class ProductControllerImpl implements ProductController {

	@Override
	public void getSummarizeBatch() {
		BeanUtils.get(ProductStatService.class).summarizeBatch();
	}

	@Override
	public void getCleanBatch(ProductStatCleanBatchIn input) {
		BeanUtils.get(ProductStatService.class).cleanBatch(input);
	}

	@Override
	public void getSummarizeMoldBatch() {
		BeanUtils.get(ProdMoldStatService.class).summarizeBatch();
	}

	@Override
	public void getCleanMoldBatch() {
		BeanUtils.get(ProdMoldStatService.class).cleanBatch();
	}

}
