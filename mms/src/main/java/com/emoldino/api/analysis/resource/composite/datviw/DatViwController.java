package com.emoldino.api.analysis.resource.composite.datviw;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.analysis.resource.composite.datviw.dto.DatColGetPageIn;
import com.emoldino.api.analysis.resource.composite.datviw.dto.DatColGetPageItem;

@RequestMapping("/api/analysis/dat-viw")
public interface DatViwController {

	@GetMapping
	Page<DatColGetPageItem> getPage(DatColGetPageIn input, Pageable pageable);

}
