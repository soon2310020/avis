package com.emoldino.api.common.resource.composite.sysmng;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngGetOut;
import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngReportClientsOut;
import com.emoldino.api.common.resource.composite.sysmng.service.SysMngService;

@RestController
public class SysMngControllerImpl implements SysMngController {
	@Autowired
	private SysMngService service;

	@Override
	public SysMngGetOut get() {
		return service.get();
	}

	@Override
	public Map<String, SysMngGetOut> getClients() {
		return service.getClients();
	}

	@Override
	public void exportClients(HttpServletResponse response) {
		service.exportClients(response);
	}

	@Override
	public SysMngReportClientsOut reportClients() {
		return service.reportClients();
	}

	@Override
	public String getMenuStr() {
		return service.getMenuStr();
	}

}
