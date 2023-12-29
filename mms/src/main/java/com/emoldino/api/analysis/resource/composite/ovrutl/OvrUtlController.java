package com.emoldino.api.analysis.resource.composite.ovrutl;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetIn;
import com.emoldino.api.analysis.resource.composite.ovrutl.dto.OvrUtlGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Overall Utilization")
@RequestMapping("/api/analysis/ovr-utl")
public interface OvrUtlController {

	@ApiOperation("Get Overall Utilization")
	@GetMapping
	OvrUtlGetOut get(OvrUtlGetIn input, Pageable pageable);

	@ApiOperation("Get Overall Utilization Details")
	@GetMapping("/details")
	OvrUtlDetailsGetOut getDetails(OvrUtlDetailsGetIn input, Pageable pageable);

}
