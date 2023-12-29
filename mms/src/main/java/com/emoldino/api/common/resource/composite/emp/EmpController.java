package com.emoldino.api.common.resource.composite.emp;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Empty(Dummy) Service")
@RequestMapping("api/common/emp")
public interface EmpController {

	@ApiOperation("Get Empty")
	@GetMapping
	Map<String, Object> get();

	@ApiOperation("Get BizException")
	@GetMapping("/exceptions/biz")
	Map<String, Object> getBizException();

	@ApiOperation("Get LogicException")
	@GetMapping("/exceptions/logic")
	Map<String, Object> getLogicException();

	@ApiOperation("Get SysException")
	@GetMapping("/exceptions/sys")
	Map<String, Object> getSysException();

}
