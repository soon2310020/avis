package com.emoldino.api.common.resource.composite.codviw;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.code.dto.CodeType;
import com.emoldino.api.common.resource.composite.codviw.dto.CodViwCodeTypeDataIn;
import com.emoldino.api.common.resource.composite.codviw.dto.CodViwCodeTypeDataOut;
import com.emoldino.framework.dto.ListOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Code Selector")
@RequestMapping("api/common/cod-viw")
public interface CodViwController {

	@ApiOperation("Get Code Types")
	@GetMapping("/code-types")
	ListOut<CodeType> getCodeTypes();

	@ApiOperation("Get Code Data List by Code Type")
	@GetMapping("/code-types/{codeType}/data")
	CodViwCodeTypeDataOut getCodeTypeByIdData(@PathVariable(required = true) String codeType, CodViwCodeTypeDataIn input, Pageable pageable);

}
