package com.emoldino.api.analysis.resource.base.production;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.base.production.dto.ProductionSummarizeBatchIn;
import com.emoldino.api.analysis.resource.base.production.service.ProductionService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class ProductionControllerImpl implements ProductionController {

	@Override
	public void getSummarizeBatch(ProductionSummarizeBatchIn input) {
		BeanUtils.get(ProductionService.class).summarizeBatch(input);
	}

}
