package com.emoldino.api.common.resource.composite.datimp;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.datimp.dto.DatImpPostIn;
import com.emoldino.api.common.resource.composite.datimp.dto.DatImpPostOut;
import com.emoldino.api.common.resource.composite.datimp.service.DatImpService;

@RestController
public class DatImpControllerImpl implements DatImpController {
	@Autowired
	DatImpService service;

	@Override
	public DatImpPostOut post(DatImpPostIn input) {
		return service.post(input);
	}

	@Override
	public void getTemplates(HttpServletResponse response) {
		service.getTemplates(response);
	}
}
