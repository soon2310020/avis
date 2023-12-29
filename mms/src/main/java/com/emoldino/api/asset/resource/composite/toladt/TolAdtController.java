package com.emoldino.api.asset.resource.composite.toladt;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtGetOut;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetIn;
import com.emoldino.api.asset.resource.composite.toladt.dto.TolAdtRelocationHistoriesGetOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Asset Management / Tooling Audit")
@RequestMapping("/api/asset/tol-adt")
public interface TolAdtController {

	@ApiOperation("Get Tooling Audit Data")
	@GetMapping
	TolAdtGetOut get(TolAdtGetIn input, Pageable pageable);

	@ApiOperation("Get Tooling Relocation Histories")
	@GetMapping("/{moldId}/relocation-histories")
	TolAdtRelocationHistoriesGetOut getRelocationHistories(@PathVariable("moldId") Long moldId, TolAdtRelocationHistoriesGetIn input, Pageable pageable);

}
