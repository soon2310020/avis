package com.emoldino.api.common.resource.composite.ipc;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersion;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsIn;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsPullIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Inter Process Communication")
@RequestMapping("/api/common/ipc")
public interface IpcController {

	@ApiOperation("Get Latest Application Version")
	@GetMapping("/app-versions/latest")
	Map<String, Object> getAppVersionsLatest(@RequestParam(name = "appCode", required = true, defaultValue = "MMS") String appCode);

	@ApiOperation("Get Application Versions")
	@GetMapping("/app-versions")
	Page<IpcAppVersion> getAppVersions(IpcAppVersionsIn input, Pageable pageable);

	@ApiOperation("Pull Application Versions (from  Central System)")
	@PostMapping("/app-versions/pull")
	SuccessOut pullAppVersions(IpcAppVersionsPullIn input);

}
