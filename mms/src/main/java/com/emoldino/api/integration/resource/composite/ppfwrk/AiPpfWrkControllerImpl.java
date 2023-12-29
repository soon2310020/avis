package com.emoldino.api.integration.resource.composite.ppfwrk;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.service.AiLaunchService;
import com.emoldino.api.integration.resource.composite.ppfwrk.dto.AiPpfResultIn;
import com.emoldino.api.integration.resource.composite.ppfwrk.service.AiPpfWrkService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AiPpfWrkControllerImpl implements AiPpfWrkController {

	@Override
	public SuccessOut ppfWorkResult(AiPpfResultIn reqIn) {
		BeanUtils.get(AiPpfWrkService.class).savePpfResult(reqIn);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut ppfWorkLaunch() {
		BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PPF);
		return SuccessOut.getDefault();
	}

}
