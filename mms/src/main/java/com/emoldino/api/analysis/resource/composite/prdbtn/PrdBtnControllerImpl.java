package com.emoldino.api.analysis.resource.composite.prdbtn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetIn;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnDetailsGetOut;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetIn;
import com.emoldino.api.analysis.resource.composite.prdbtn.dto.PrdBtnGetOut;
import com.emoldino.api.analysis.resource.composite.prdbtn.service.PrdBtnService;

@RestController
public class PrdBtnControllerImpl implements PrdBtnController {
	@Autowired
	private PrdBtnService service;

	@Override
	public PrdBtnGetOut get(PrdBtnGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public PrdBtnDetailsGetOut getDetails(PrdBtnDetailsGetIn input, Pageable pageable) {
		return service.getDetails(input, pageable);
	}

}
