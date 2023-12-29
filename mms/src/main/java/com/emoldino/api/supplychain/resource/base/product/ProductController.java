package com.emoldino.api.supplychain.resource.base.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.supplychain.resource.base.product.dto.ProductStatCleanBatchIn;

@RequestMapping("/api/supplychain/product")
public interface ProductController {

	@GetMapping("/summarize-batch")
	void getSummarizeBatch();

	@GetMapping("/clean-batch")
	void getCleanBatch(ProductStatCleanBatchIn input);

	@GetMapping("/summarize-mold-batch")
	void getSummarizeMoldBatch();

	@GetMapping("/clean-mold-batch")
	void getCleanMoldBatch();

}
