package com.emoldino.api.common.resource.composite.datimp;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.datimp.dto.DatImpPostIn;
import com.emoldino.api.common.resource.composite.datimp.dto.DatImpPostOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Data Import Center")
@RequestMapping("/api/common/dat-imp")
public interface DatImpController {

	@ApiOperation("Process Data Import")
	@PostMapping
	DatImpPostOut post(@RequestBody DatImpPostIn input);

	@ApiOperation("Download Data Import Template")
	@GetMapping("/templates")
	void getTemplates(HttpServletResponse response);

}
