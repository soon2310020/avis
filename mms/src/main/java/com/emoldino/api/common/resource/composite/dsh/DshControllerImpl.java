package com.emoldino.api.common.resource.composite.dsh;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.dsh.dto.DshConfigsGetOut;
import com.emoldino.api.common.resource.composite.dsh.dto.DshWidget;
import com.emoldino.api.common.resource.composite.dsh.service.DshService;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
@Transactional
public class DshControllerImpl implements DshController {

	@Override
	public ListOut<DshWidget> get() {
		return BeanUtils.get(DshService.class).get();
	}

	@Override
	public DshConfigsGetOut getConfigs() {
		return BeanUtils.get(DshService.class).getConfigs();
	}

	@Override
	public SuccessOut postConfigs(ListIn<DshWidget> input) {
		BeanUtils.get(DshService.class).postConfigs(input);
		return SuccessOut.getDefault();
	}

}
