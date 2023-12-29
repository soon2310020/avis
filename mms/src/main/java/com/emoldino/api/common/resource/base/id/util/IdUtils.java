package com.emoldino.api.common.resource.base.id.util;

import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.id.dto.IdGenIn;
import com.emoldino.api.common.resource.base.id.enumeration.IdRuleCode;
import com.emoldino.api.common.resource.base.id.service.IdService;
import com.emoldino.framework.util.BeanUtils;

public class IdUtils {

	public static String gen(IdRuleCode idRuleCode, Map<String, Object> params) {
		IdGenIn input = new IdGenIn();
		input.setIdRuleCode(idRuleCode);
		if (!ObjectUtils.isEmpty(params)) {
			input.putAll(params);
		}
		return BeanUtils.get(IdService.class).gen(input);
	}

}
