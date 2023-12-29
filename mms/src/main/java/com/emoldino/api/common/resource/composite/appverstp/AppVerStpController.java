package com.emoldino.api.common.resource.composite.appverstp;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion;
import com.emoldino.api.common.resource.composite.appverstp.dto.AppVerStpGetPageIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / App Version Config")
@RequestMapping("api/common/app-ver-stp")
public interface AppVerStpController {

	@ApiOperation("Get App Versions")
	@GetMapping
	Page<AppVersion> get(AppVerStpGetPageIn input, Pageable pageable);

	@ApiOperation("Post App Version")
	@PostMapping("/one")
	SuccessOut post(@RequestBody AppVersion data);

	@ApiOperation("Put App Version")
	@PutMapping("/{id}")
	SuccessOut put(@PathVariable("id") Long id, @RequestBody AppVersion data);

	@ApiOperation("Disable App Versions")
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam List<Long> id);

	@ApiOperation("Enable App Versions")
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam List<Long> id);

}
