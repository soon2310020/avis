package com.emoldino.api.common.resource.composite.sysmng;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngGetOut;
import com.emoldino.api.common.resource.composite.sysmng.dto.SysMngReportClientsOut;

/**
 * System Management APIs
 * 
 * @author steve.jung
 */
@RequestMapping("/api/common/sys-mng")
public interface SysMngController {

	@GetMapping
	SysMngGetOut get();

	@GetMapping("/clients")
	Map<String, SysMngGetOut> getClients();

	@GetMapping("/clients/export")
	void exportClients(HttpServletResponse response);

	@GetMapping("/clients/report")
	SysMngReportClientsOut reportClients();

	@GetMapping("menu-str")
	String getMenuStr();

}
