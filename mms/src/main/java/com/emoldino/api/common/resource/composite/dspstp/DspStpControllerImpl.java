package com.emoldino.api.common.resource.composite.dspstp;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.dspstp.dto.DspStpGetOut;
import com.emoldino.api.common.resource.composite.dspstp.dto.DspStpPostIn;
import com.emoldino.api.common.resource.composite.dspstp.service.DspStpService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class DspStpControllerImpl implements DspStpController {

	@Override
	public DspStpGetOut get() {
		return BeanUtils.get(DspStpService.class).get();
	}

	@Override
	public SuccessOut post(DspStpPostIn input) {
		BeanUtils.get(DspStpService.class).post(input);
		return SuccessOut.getDefault();
	}

}
