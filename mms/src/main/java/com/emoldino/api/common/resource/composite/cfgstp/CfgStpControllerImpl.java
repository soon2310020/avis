package com.emoldino.api.common.resource.composite.cfgstp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetOut;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpPostIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpPostOut;
import com.emoldino.api.common.resource.composite.cfgstp.service.CfgStpService;

@RestController
public class CfgStpControllerImpl implements CfgStpController {
	@Autowired
	private CfgStpService service;

	@Override
	public CfgStpGetOut get(CfgStpGetIn input) {
		return service.get(input);
	}

	@Override
	public CfgStpPostOut post(CfgStpPostIn input) {
		return service.post(input);
	}

}
