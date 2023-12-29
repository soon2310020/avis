package com.emoldino.api.analysis.resource.composite.mldcht;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetIn;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.get.MldChtGetOut;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions.MldChtGetOptionsIn;
import com.emoldino.api.analysis.resource.composite.mldcht.dto.getoptions.MldChtGetOptionsOut;
import com.emoldino.api.analysis.resource.composite.mldcht.service.MldChtService;

@RestController
public class MldChtControllerImpl implements MldChtController {
	@Autowired
	private MldChtService service;

	@Override
	public MldChtGetOut get(MldChtGetIn input) {
		return service.get(input);
	}

	@Override
	public MldChtGetOptionsOut getOptions(MldChtGetOptionsIn input) {
		return service.getOptions(input);
	}

}
