package com.emoldino.api.analysis.resource.composite.prochg;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetIn;
import com.emoldino.api.analysis.resource.composite.prochg.dto.ProChgGetOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.dto.TimeSetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Process Change")
@RequestMapping("/api/analysis/pro-chg")
public interface ProChgController {
	public static final String NAME = "Process Change";
	public static final String NAME_PLURAL = "Process Changes";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	ProChgGetOut get(ProChgGetIn input, TimeSetting timeSetting, Pageable pageable);

	@ApiOperation("Get " + NAME_PLURAL + " Details")
	@GetMapping("/details")
	ProChgDetailsGetOut getDetails(ProChgDetailsGetIn input, Pageable pageable);

	@PostMapping("/summarize-batch")
	SuccessOut summarizeBatch();

}
