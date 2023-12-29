package com.emoldino.api.common.resource.composite.mdtadj;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Master Data Adjustment")
@RequestMapping("api/common/mdt-adj")
public interface MdtAdjController {

	@ApiOperation("Post Master Data Adjustment")
	@PostMapping
	SuccessOut post();

}
