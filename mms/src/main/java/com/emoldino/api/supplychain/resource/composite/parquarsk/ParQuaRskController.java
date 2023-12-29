package com.emoldino.api.supplychain.resource.composite.parquarsk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetIn;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetOut;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskHeatmapOut;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskMold;
import com.emoldino.framework.dto.TimeSetting;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Supply Chain / Part Quality Risk")
@RequestMapping("/api/supplychain/par-qua-rsk")
public interface ParQuaRskController {
	public static final String NAME = "Part Quality Risk";
	public static final String NAME_PLURAL = "Part Quality Risks";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	ParQuaRskGetOut get(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable);

	@ApiOperation("Get " + NAME_PLURAL + " Molds(Toolings)")
	@GetMapping("/molds")
	Page<ParQuaRskMold> getMolds(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable);

	@ApiOperation("Get " + NAME_PLURAL + " Heatmap")
	@GetMapping("/heatmap")
	ParQuaRskHeatmapOut getHeatmap(@RequestParam(name = "moldId", required = true) Long moldId, TimeSetting timeSetting);

}
