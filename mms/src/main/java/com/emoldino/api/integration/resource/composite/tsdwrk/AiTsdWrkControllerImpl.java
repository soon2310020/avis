package com.emoldino.api.integration.resource.composite.tsdwrk;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.service.AiLaunchService;
import com.emoldino.api.integration.resource.composite.tsdwrk.dto.AiTsdResultIn;
import com.emoldino.api.integration.resource.composite.tsdwrk.service.AiTsdWrkService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AiTsdWrkControllerImpl implements AiTsdWrkController {

	private final AiTsdWrkService service;

	@Override
	public SuccessOut tsdWorkLaunch() {
		BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_TSD);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut tsdWorkResult(AiTsdResultIn reqIn) {
		service.saveTsdResult(reqIn);
		return SuccessOut.getDefault();
	}

}
