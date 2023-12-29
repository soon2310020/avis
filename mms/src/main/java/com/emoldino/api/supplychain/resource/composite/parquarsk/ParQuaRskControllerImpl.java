package com.emoldino.api.supplychain.resource.composite.parquarsk;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetIn;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskGetOut;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskHeatmapOut;
import com.emoldino.api.supplychain.resource.composite.parquarsk.dto.ParQuaRskMold;
import com.emoldino.api.supplychain.resource.composite.parquarsk.service.ParQuaRskService;
import com.emoldino.framework.dto.TimeSetting;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class ParQuaRskControllerImpl implements ParQuaRskController {

	@Override
	public ParQuaRskGetOut get(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable) {
		return BeanUtils.get(ParQuaRskService.class).get(input, timeSetting, pageable);
	}

	@Override
	public Page<ParQuaRskMold> getMolds(ParQuaRskGetIn input, TimeSetting timeSetting, Pageable pageable) {
		return BeanUtils.get(ParQuaRskService.class).getMolds(input, timeSetting, pageable);
	}

	@Override
	public ParQuaRskHeatmapOut getHeatmap(Long moldId, TimeSetting timeSetting) {
		return BeanUtils.get(ParQuaRskService.class).getHeatmap(moldId, timeSetting);
	}

}
