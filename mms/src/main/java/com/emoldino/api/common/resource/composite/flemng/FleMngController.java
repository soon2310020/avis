package com.emoldino.api.common.resource.composite.flemng;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.composite.flemng.dto.FleMngGetIn;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngGroup;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngPostOut;
import com.emoldino.api.common.resource.composite.flemng.dto.FleMngPutVersionIn;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / File Management")
@RequestMapping("api/common/fle-mng")
public interface FleMngController {
	String NAME = "File Group";
	String NAME_PLURAL = "File Groups";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping
	Page<FleMngGroup> get(FleMngGetIn input, Pageable pageable);

	@ApiOperation("Get " + NAME)
	@GetMapping("/{id}")
	FleMngGroup get(Long id);

	@ApiOperation("Post " + NAME)
	@PostMapping("/one")
	FleMngPostOut post(@RequestBody FleMngGroup data);

	@ApiOperation("Put " + NAME)
	@PutMapping("/{id}")
	SuccessOut put(@PathVariable("id") Long id, @RequestBody FleMngGroup data);

	@ApiOperation("Delete " + NAME)
	@DeleteMapping("/{id}")
	SuccessOut delete(@PathVariable("id") Long id);

	@ApiOperation("Disable " + NAME_PLURAL)
	@PutMapping("/disable")
	SuccessOut disable(@RequestParam List<Long> id);

	@ApiOperation("Enable " + NAME_PLURAL)
	@PutMapping("/enable")
	SuccessOut enable(@RequestParam List<Long> id);

	@ApiOperation("Release " + NAME)
	@PutMapping("/{id}/release")
	SuccessOut release(@PathVariable("id") Long id, @RequestBody FleMngGroup data);

	@ApiOperation("Unrelease " + NAME)
	@PutMapping("/{id}/unrelease")
	SuccessOut unrelease(@PathVariable("id") Long id, @RequestBody FleMngGroup data);

	@ApiOperation("Put " + NAME + " Version")
	@PutMapping("/{id}/version")
	SuccessOut putVersion(@PathVariable("id") Long id, @RequestBody FleMngPutVersionIn data);

	@ApiOperation("Get " + NAME + " Versions")
	@GetMapping("/{id}/versions")
	List<FleMngGroup> getVersions(@PathVariable("id") Long id);

}
