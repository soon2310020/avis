package com.emoldino.api.integration.resource.composite.pcwrk;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.service.AiLaunchService;
import com.emoldino.api.integration.resource.composite.pcwrk.dto.AiPcResultIn;
import com.emoldino.api.integration.resource.composite.pcwrk.service.AiPcWrkService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.ReflectionUtils;

@RestController
public class AiPcWrkControllerImpl implements AiPcWrkController {

	@Override
	public SuccessOut pcWorkLaunch() {
		BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PC);
//		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiLaunchService.class, "launchPC"), () -> BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PC));
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut pcWorkResult(AiPcResultIn reqIn) {
		BeanUtils.get(AiPcWrkService.class).savePcResult(reqIn);
		return SuccessOut.getDefault();
	}

}
