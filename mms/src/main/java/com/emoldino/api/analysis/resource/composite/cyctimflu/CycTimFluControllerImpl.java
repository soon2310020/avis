package com.emoldino.api.analysis.resource.composite.cyctimflu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluGetIn;
import com.emoldino.api.analysis.resource.composite.cyctimflu.dto.CycTimFluItem;
import com.emoldino.api.analysis.resource.composite.cyctimflu.service.CycTimFluService;

@RestController
public class CycTimFluControllerImpl implements CycTimFluController {

	@Autowired
	private CycTimFluService service;

	@Override
	public Page<CycTimFluItem> get(CycTimFluGetIn input, Pageable pageable) {
		return service.getPage(input, pageable);
	}

	@Override
	public CycTimFluDetailsGetOut getDetails(CycTimFluDetailsGetIn input, Pageable pageable) {
		return service.getDetailsPage(input, pageable);
	}

}
