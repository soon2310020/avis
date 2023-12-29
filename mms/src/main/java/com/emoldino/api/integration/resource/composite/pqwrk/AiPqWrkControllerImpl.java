package com.emoldino.api.integration.resource.composite.pqwrk;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.service.AiLaunchService;
import com.emoldino.api.integration.resource.composite.pqwrk.dto.AiPqResultIn;
import com.emoldino.api.integration.resource.composite.pqwrk.service.AiPqWrkService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class AiPqWrkControllerImpl implements AiPqWrkController {

	@Override
	public SuccessOut pqWorkResult(AiPqResultIn reqIn) {
		BeanUtils.get(AiPqWrkService.class).savePqResult(reqIn);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut pqWorkLaunch() {
		BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PQ);
		return SuccessOut.getDefault();
	}

}
