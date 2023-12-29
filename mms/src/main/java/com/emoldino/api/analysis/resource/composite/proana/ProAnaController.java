package com.emoldino.api.analysis.resource.composite.proana;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaGetOut;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaMoldsGetIn;
import com.emoldino.api.analysis.resource.composite.proana.dto.ProAnaMoldsItem;
import com.emoldino.api.analysis.resource.composite.proana.service.Range.ProAnaRangeService.Ranges;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Process Analysis")
@RequestMapping("/api/analysis/pro-ana")
public interface ProAnaController {
	public static final String NAME = "Process Analysis";
	public static final String NAME_PLURAL = "Process Analyses";

	@ApiOperation("Get Molds for " + NAME)
	@GetMapping("/molds")
	Page<ProAnaMoldsItem> getMolds(ProAnaMoldsGetIn input, Pageable pageable);

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	ProAnaGetOut get(ProAnaGetIn input, Pageable pageable);
	
	@ApiOperation("Get Range Test for Process Analysis")
	@GetMapping("/range-test")
	Ranges getRange(ProAnaGetIn input);	

}
