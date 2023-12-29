package com.emoldino.api.common.resource.composite.idrstp;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.id.dto.IdGenIn;
import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;
import com.emoldino.api.common.resource.base.id.service.IdService;
import com.emoldino.api.common.resource.composite.idrstp.dto.IdrStpTestOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class IdrStpControllerImpl implements IdrStpController {

	@Override
	public IdrStpTestOut test(IdRuleCode idRuleCode) {
		IdGenIn reqin = new IdGenIn();
		reqin.setIdRuleCode(idRuleCode);
		reqin.setTest(true);
		String id = BeanUtils.get(IdService.class).gen(reqin);
		IdrStpTestOut output = new IdrStpTestOut();
		output.setId(id);
		return output;
	}

}
