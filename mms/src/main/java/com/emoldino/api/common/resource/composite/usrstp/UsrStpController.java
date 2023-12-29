package com.emoldino.api.common.resource.composite.usrstp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpData;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpGetPlantsOut;
import com.emoldino.api.common.resource.composite.usrstp.dto.UsrStpRole;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / User Config")
@RequestMapping("/api/common/usr-stp")
public interface UsrStpController {

	@ApiOperation("Get User by ID")
	@GetMapping("/{id}")
	UsrStpData get(@PathVariable Long id);

	@ApiOperation("Get User Plants")
	@GetMapping("/{id}/plants")
	UsrStpGetPlantsOut getPlants(@PathVariable Long id, @RequestParam(name = "companyId", required = true) Long companyId);

	@ApiOperation("Get Plants")
	@GetMapping("/plants")
	UsrStpGetPlantsOut getPlants(@RequestParam(name = "companyId", required = true) Long companyId);

	@ApiOperation("Get Roles")
	@GetMapping("/roles")
	ListOut<UsrStpRole> getRoles(@RequestParam(name = "companyId", required = true) Long companyId);

	@ApiOperation("Post One User")
	@PostMapping("/one")
	SuccessOut post(@RequestBody UsrStpData data);

	@ApiOperation("Put User by ID")
	@PutMapping("/{id}")
	SuccessOut put(@PathVariable("id") Long id, @RequestBody UsrStpData data);

}
