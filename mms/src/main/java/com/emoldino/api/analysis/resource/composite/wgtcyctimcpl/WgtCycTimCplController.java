package com.emoldino.api.analysis.resource.composite.wgtcyctimcpl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.WgtCycTimCplGetIn;
import com.emoldino.api.analysis.resource.composite.wgtcyctimcpl.dto.WgtCycTimCplGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Widget / Cycle Time Compliance")
@RequestMapping("/api/analysis/wgt-cyc-tim-cpl")
public interface WgtCycTimCplController {
//	public static final String NAME = "Cycle Time Compliance";
	public static final String NAME_PLURAL = "Cycle Time Compliance";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	WgtCycTimCplGetOut get(WgtCycTimCplGetIn input);

}
