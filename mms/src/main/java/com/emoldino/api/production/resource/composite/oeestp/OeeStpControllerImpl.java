package com.emoldino.api.production.resource.composite.oeestp;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.production.resource.composite.oeestp.dto.OeeStpGetOut;
import com.emoldino.api.production.resource.composite.oeestp.dto.OeeStpPostIn;
import com.emoldino.api.production.resource.composite.oeestp.service.OeeStpService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class OeeStpControllerImpl implements OeeStpController {

	@Override
	public OeeStpGetOut get() {
		return BeanUtils.get(OeeStpService.class).get();
	}

	@Override
	public SuccessOut post(OeeStpPostIn input) {
		BeanUtils.get(OeeStpService.class).post(input);
		return SuccessOut.getDefault();
	}

}
