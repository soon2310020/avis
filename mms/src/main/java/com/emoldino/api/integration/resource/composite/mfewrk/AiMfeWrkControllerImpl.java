package com.emoldino.api.integration.resource.composite.mfewrk;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.service.AiLaunchService;
import com.emoldino.api.integration.resource.composite.mfewrk.dto.AiMfeResultIn;
import com.emoldino.api.integration.resource.composite.mfewrk.service.AiMfeWrkService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AiMfeWrkControllerImpl implements AiMfeWrkController {
    private final AiMfeWrkService service;

    @Override
    public SuccessOut mfeWorkLaunch() {
        BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_MFE);
        return SuccessOut.getDefault();
    }

    @Override
    public SuccessOut mfeWorkResult(AiMfeResultIn reqIn) {
        service.saveMfeResult(reqIn);
        return null;
    }

}
