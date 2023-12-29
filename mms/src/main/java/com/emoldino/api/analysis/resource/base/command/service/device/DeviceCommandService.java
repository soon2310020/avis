package com.emoldino.api.analysis.resource.base.command.service.device;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommand;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommandRepository;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.SyncCtrlUtils;

@Service
public class DeviceCommandService {
	@Autowired
	DeviceCommandRepository repo;

	public void post(DeviceCommandDto data) {
		LogicUtils.assertNotEmpty(data.getDeviceId(), "deviceId");
		LogicUtils.assertNotEmpty(data.getDeviceType(), "deviceType");
		LogicUtils.assertNotEmpty(data.getCommand(), "command");

		DeviceCommand command = new DeviceCommand();
		command.setDeviceId(data.getDeviceId());
		command.setDeviceType(data.getDeviceType());
		command.setCommand(data.getCommand());
		command.setData(data.getData());
		command.setComment(data.getComment());

		SyncCtrlUtils.wrapWithLock("device-command." + data.getDeviceId(), () -> {
			int i[] = { 0 };
			repo.findFirstByDeviceIdOrderByIdDesc(data.getDeviceId()).ifPresent(item -> i[0] = item.getIndexNo());
			command.setIndexNo(i[0] >= 255 ? 1 : ++i[0]);
			command.setStatus("CREATED");
			repo.save(command);
		});
	}
}
