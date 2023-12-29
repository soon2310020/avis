package com.emoldino.api.common.resource.composite.idrstp;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;
import com.emoldino.api.common.resource.composite.idrstp.dto.IdrStpTestOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / ID Rule Config")
@RequestMapping("api/common/idr-stp")
public interface IdrStpController {

	@ApiOperation("Test ID Gen")
	@PostMapping("/{idRuleCode}/test")
	IdrStpTestOut test(@PathVariable("idRuleCode") IdRuleCode idRuleCode);

}
