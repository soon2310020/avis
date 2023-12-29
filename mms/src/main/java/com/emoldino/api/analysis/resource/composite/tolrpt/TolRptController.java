package com.emoldino.api.analysis.resource.composite.tolrpt;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptGetIn;
import com.emoldino.api.analysis.resource.composite.tolrpt.dto.TolRptItem;
import com.emoldino.framework.dto.BatchIn;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Tooling Report")
@RequestMapping("/api/analysis/tol-rpt")
public interface TolRptController {
	String NAME_PLURAL = "Tooling Report";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	Page<TolRptItem> get(TolRptGetIn input, BatchIn batchin, Pageable pageable);

	@ApiOperation("Export " + NAME_PLURAL)
	@GetMapping("/export")
	void export(TolRptGetIn input, BatchIn batchin, HttpServletResponse response) throws IOException;

}
