package com.emoldino.api.analysis.resource.composite.datviw;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.composite.datviw.dto.DatColGetPageIn;
import com.emoldino.api.analysis.resource.composite.datviw.dto.DatColGetPageItem;
import com.emoldino.api.analysis.resource.composite.datviw.service.DatViwService;

@RestController
public class DatViwControllerImpl implements DatViwController {
	@Autowired
	private DatViwService service;

	@Override
	public Page<DatColGetPageItem> getPage(DatColGetPageIn input, Pageable pageable) {
		return service.getPage(input, pageable);
	}

}
