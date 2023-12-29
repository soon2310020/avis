package com.emoldino.api.common.resource.composite.codviw;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.code.dto.CodeDataGetIn;
import com.emoldino.api.common.resource.base.code.dto.CodeType;
import com.emoldino.api.common.resource.base.code.repository.codedata.CodeData;
import com.emoldino.api.common.resource.base.code.service.data.CodeDataService;
import com.emoldino.api.common.resource.base.code.service.type.CodeTypeService;
import com.emoldino.api.common.resource.composite.codviw.dto.CodViwCodeTypeDataIn;
import com.emoldino.api.common.resource.composite.codviw.dto.CodViwCodeTypeDataOut;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;

import saleson.common.util.SecurityUtils;

@RestController
public class CodViwControllerImpl implements CodViwController {

	@Override
	public ListOut<CodeType> getCodeTypes() {
		return BeanUtils.get(CodeTypeService.class).getList();
	}

	@Override
	public CodViwCodeTypeDataOut getCodeTypeByIdData(String codeType, CodViwCodeTypeDataIn input, Pageable pageable) {
		if (ObjectUtils.isEmpty(codeType)) {
			return new CodViwCodeTypeDataOut(Collections.emptyList(), pageable, 0);
		}

		CodeDataGetIn reqin = new CodeDataGetIn();
		reqin.setCompanyId(SecurityUtils.getCompanyId());
		reqin.setCodeType(codeType);
		reqin.setQuery(input.getQuery());
		reqin.setGroup1Code(input.getGroup1Code());
		reqin.setGroup2Code(input.getGroup2Code());
		reqin.setStatus(input.getEnabled());
		Page<CodeData> page = BeanUtils.get(CodeDataService.class).getPage(reqin, pageable);
		CodViwCodeTypeDataOut output = new CodViwCodeTypeDataOut(page.getContent(), page.getPageable(), page.getTotalElements());
		output.setCodeType(BeanUtils.get(CodeTypeService.class).get(codeType));
		return output;
	}

}