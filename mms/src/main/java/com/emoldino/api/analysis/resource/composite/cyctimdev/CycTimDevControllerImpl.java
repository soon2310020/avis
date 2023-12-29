package com.emoldino.api.analysis.resource.composite.cyctimdev;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevPostIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.service.CycTimDevService;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class CycTimDevControllerImpl implements CycTimDevController {
	@Autowired
	private CycTimDevService service;

	@Override
	public CycTimDevGetOut get(CycTimDevGetIn input, Pageable pageable) {
		return service.getPage(input, pageable);
	}

	@Override
	public CycTimDevDetailsGetOut getDetails(CycTimDevDetailsGetIn input, Pageable pageable) {
		return service.getDetailsPage(input, pageable);
	}

	@Override
	public SuccessOut post(CycTimDevPostIn input) {
		service.post(input);
		return SuccessOut.getDefault();
	}

}
