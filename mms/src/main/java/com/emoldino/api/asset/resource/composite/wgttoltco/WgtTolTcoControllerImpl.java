package com.emoldino.api.asset.resource.composite.wgttoltco;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.wgttoltco.dto.WgtTolTcoGetIn;
import com.emoldino.api.asset.resource.composite.wgttoltco.dto.WgtTolTcoGetOut;
import com.emoldino.api.asset.resource.composite.wgttoltco.service.WgtTolTcoService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class WgtTolTcoControllerImpl implements WgtTolTcoController {

	@Override
	public WgtTolTcoGetOut get(WgtTolTcoGetIn input) {
		return BeanUtils.get(WgtTolTcoService.class).get(input);
	}

}
