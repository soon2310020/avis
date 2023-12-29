package com.emoldino.api.asset.resource.composite.wgttolrlo;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.asset.resource.composite.wgttolrlo.dto.WgtTolRloGetOut;
import com.emoldino.api.asset.resource.composite.wgttolrlo.service.WgtTolRloService;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class WgtTolRloControllerImpl implements WgtTolRloController {

	@Override
	public WgtTolRloGetOut get() {
		return BeanUtils.get(WgtTolRloService.class).get();
	}

}
