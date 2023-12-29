package com.emoldino.api.analysis.resource.composite.cyctimflu;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluItem;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Cycle Time Fluctuation")
@RequestMapping("/api/analysis/cyc-tim-flu")
public interface CycTimFluController {

	@ApiOperation("Get Cycle Time Fluctuation Page")
	@GetMapping
	Page<CycTimFluItem> get(CycTimFluGetIn input, Pageable pageable);

	@ApiOperation("Get Cycle Time Fluctuation Details Page")
	@GetMapping("/details")
	CycTimFluDetailsGetOut getDetails(CycTimFluDetailsGetIn input, Pageable pageable);

}
