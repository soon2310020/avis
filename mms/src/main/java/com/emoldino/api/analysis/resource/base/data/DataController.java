package com.emoldino.api.analysis.resource.base.data;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.framework.dto.SuccessOut;

@RequestMapping("/api/analysis/data")
public interface DataController {

	@GetMapping("/clean-batch")
	SuccessOut cleanBatch();

	@GetMapping("/rebuild-batch")
	SuccessOut rebuildBatch();

	@GetMapping("/adjust-batch")
	SuccessOut adjustBatch();

	//	@GetMapping("/adjust-new-device")
	//	SuccessOut adjustNewDevice();

}
