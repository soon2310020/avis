package com.emoldino.api.analysis.resource.composite.prdbtn;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetIn;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Production Bottleneck")
@RequestMapping("/api/analysis/prd-btn")
public interface PrdBtnController {

	@ApiOperation("Get Production Bottleneck Page")
	@GetMapping
	PrdBtnGetOut get(PrdBtnGetIn input, Pageable pageable);

	@ApiOperation("Get Production Bottleneck Details Page")
	@GetMapping("/details")
	PrdBtnDetailsGetOut getDetails(PrdBtnDetailsGetIn input, Pageable pageable);

}
