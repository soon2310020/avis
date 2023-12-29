package com.emoldino.api.integration.resource.composite.inftol;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolDailySummaryIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolGetIn;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolPostItem;
import com.emoldino.api.integration.resource.composite.inftol.dto.InfTolSummaryItem;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Integration / Toolings")
@RequestMapping("/api/integration/toolings")
public interface InfTolController {
	public static final String NAME = "Tooling";
	public static final String NAME_PLURAL = "Toolings";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	Page<InfTolItem> get(InfTolGetIn input, Pageable pageable);

	@ApiOperation("Get " + NAME)
	@GetMapping("/{toolingId}")
	InfTolItem get(@PathVariable("toolingId") String toolingId);

	@ApiOperation("Post " + NAME)
	@PostMapping("/{toolingId}")
	SuccessOut post(@PathVariable("toolingId") String toolingId, @RequestBody InfTolPostItem tooling);

	@ApiOperation("Get " + NAME_PLURAL + " Daily Summary")
	@GetMapping("/daily-summary")
	Page<InfTolSummaryItem> getDailySummary(InfTolDailySummaryIn input, Pageable pageable);

}
