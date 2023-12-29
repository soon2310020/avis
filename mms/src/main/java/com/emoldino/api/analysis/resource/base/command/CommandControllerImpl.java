package com.emoldino.api.analysis.resource.base.command;

import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.api.analysis.resource.base.command.service.device.DeviceCommandService;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

@RestController
public class CommandControllerImpl implements CommandController {

	@Override
	public SuccessOut postDevice(DeviceCommandDto data) {
		BeanUtils.get(DeviceCommandService.class).post(data);
		return SuccessOut.getDefault();
	}

}
