package com.emoldino.api.analysis.resource.composite.bud;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetIn;
import com.emoldino.api.analysis.resource.composite.bud.dto.BudGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Budgeting")
@RequestMapping("/api/analysis/bud")
public interface BudController {

	@ApiOperation("Get Budgeting")
	@GetMapping
	BudGetOut get(BudGetIn input, Pageable pageable);

	@ApiOperation("Get Budgeting Details")
	@GetMapping("/details")
	BudDetailsGetOut getDetails(BudDetailsGetIn input, Pageable pageable);

}
