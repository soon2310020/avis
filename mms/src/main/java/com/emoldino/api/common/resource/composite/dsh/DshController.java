package com.emoldino.api.common.resource.composite.dsh;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.dsh.dto.DshConfigsGetOut;
import com.emoldino.api.common.resource.composite.dsh.dto.DshWidget;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Dashboard")
@RequestMapping("/api/common/dsh")
public interface DshController {

	@ApiOperation("Get Dashboard")
	@GetMapping
	ListOut<DshWidget> get();

	@ApiOperation("Get Dashboard Configs (All Widgets and Enabled Widgets)")
	@GetMapping("/configs")
	DshConfigsGetOut getConfigs();

	@ApiOperation("Post Dashboard Configs")
	@PostMapping("/configs")
	SuccessOut postConfigs(@RequestBody ListIn<DshWidget> input);

}
