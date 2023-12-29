package com.emoldino.api.analysis.resource.composite.cdtisp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspAdjustIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspGetPageIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspHourly;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspItem;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspPostIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / CDATA Inspection")
@RequestMapping("/api/analysis/cdt-isp")
public interface CdtIspController {

	@ApiOperation("Get CDATA Inspection List: Transfer -> Cdata -> Statistics")
	@GetMapping
	Page<CdtIspItem> getPage(CdtIspGetPageIn input, Pageable pageable);

	@ApiOperation("Post CDATA: Transfer -> Cdata -> Statistics")
	@PostMapping
	SuccessOut post(@RequestBody CdtIspPostIn input);

	@GetMapping("/statistics")
	ListOut<CdtIspHourly> getStatisticsPage(CdtIspGetPageIn input, Pageable pageable);

	@GetMapping("/adjust-1st-gen-data")
	SuccessOut adjust1stGenData(CdtIspAdjustIn input);

	@GetMapping("/adjust-1st-gen-log")
	SuccessOut adjust1stGenLog(CdtIspAdjustIn input);

}
