package com.emoldino.api.asset.resource.base.terminal.util;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;

import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.IpType;
import saleson.common.enumeration.OperatingStatus;
import saleson.model.Location;
import saleson.model.Terminal;

public class TerminalUtils {

	public static void setTerminalOperated(String ti, String ip) {
		if (ObjectUtils.isEmpty(ti)) {
			return;
		}

		Terminal terminal = BeanUtils.get(TerminalRepository.class).findByEquipmentCode(ti).orElseGet(() -> {
			// Company / Location
			Location location = LocationUtils.getDefault();
			Terminal newTerminal = new Terminal();
			newTerminal.setEquipmentCode(ti);
			newTerminal.setEquipmentStatus(EquipmentStatus.INSTALLED);
			newTerminal.setGateway("");
			newTerminal.setDns("");
			newTerminal.setEnabled(true);
			newTerminal.setLocation(location);
			newTerminal.setCompanyId(location.getCompanyId());
			return newTerminal;
		});
		if (terminal.getActivatedAt() == null) {
			terminal.setActivatedAt(DateUtils2.newInstant());
		}
		if (ip != null && (terminal.getIpAddress() == null || !terminal.getIpAddress().equals(ip))) {
			terminal.setIpAddress(ip);
			terminal.setIpType(IpType.DYNAMIC);
		}
		terminal.setOperatingStatus(OperatingStatus.WORKING);
		terminal.setOperatedAt(DateUtils2.newInstant());
		BeanUtils.get(TerminalRepository.class).save(terminal);
	}

}
