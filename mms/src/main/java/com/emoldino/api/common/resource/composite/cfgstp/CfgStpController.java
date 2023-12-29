package com.emoldino.api.common.resource.composite.cfgstp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetOut;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpPostIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpPostOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Option Configuration")
@RequestMapping("api/common/cfg-stp")
public interface CfgStpController {

	@ApiOperation("Get Options")
	@GetMapping
	CfgStpGetOut get(CfgStpGetIn input);

	@ApiOperation("Post Options")
	@PostMapping
	CfgStpPostOut post(@RequestBody CfgStpPostIn input);

}
