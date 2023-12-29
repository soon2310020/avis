package com.emoldino.api.analysis.resource.composite.toldat;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetIn;
import com.emoldino.api.analysis.resource.composite.toldat.dto.TolDatGetOut;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.TimeSetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Tooling Data")
@RequestMapping("/api/analysis/tol-dat")
public interface TolDatController {
	String NAME_PLURAL = "Tooling Data";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	TolDatGetOut get(TolDatGetIn input, TimeSetting timeSetting, Pageable pageable);

//	@ApiOperation("Get " + NAME_PLURAL + " Charts")
//	@GetMapping("/charts")
//	TolDatGetChartsOut getCharts(TolDatGetIn input, TimeSetting timeSetting);

	@ApiOperation("Export " + NAME_PLURAL)
	@GetMapping("/export")
	void export(TolDatGetIn input, TimeSetting timeSetting, BatchIn batchin, HttpServletResponse response);

}
