package com.emoldino.api.analysis.resource.base.production;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.base.production.dto.ProductionSummarizeBatchIn;

@RequestMapping("/api/analysis/production")
public interface ProductionController {

	@Deprecated
	@GetMapping("/summarizeBatch")
	void getSummarizeBatch(ProductionSummarizeBatchIn input);

}
