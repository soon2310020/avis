package com.emoldino.api.analysis.resource.composite.cyctimdev;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevPostIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Cycle Time Deviation")
@RequestMapping("/api/analysis/cyc-tim-dev")
public interface CycTimDevController {

	@ApiOperation("Get Cycle Time Deviation Page")
	@GetMapping
	CycTimDevGetOut get(CycTimDevGetIn input, Pageable pageable);

	@ApiOperation("Get Cycle Time Deviation Details Page")
	@GetMapping("/details")
	CycTimDevDetailsGetOut getDetails(CycTimDevDetailsGetIn input, Pageable pageable);

	@ApiOperation("Post Cycle Time Deviation Data")
	@PostMapping
	SuccessOut post(@RequestBody CycTimDevPostIn input);

}
