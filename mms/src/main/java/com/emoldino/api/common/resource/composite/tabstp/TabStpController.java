package com.emoldino.api.common.resource.composite.tabstp;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpData;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpGetIn;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpItem;
import com.emoldino.api.common.resource.composite.tabstp.dto.TabStpPostData;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import saleson.common.enumeration.ObjectType;

@Api(protocols = "http, https", tags = "Common / Tab Config")
@RequestMapping("/api/common/tab-stp")
public interface TabStpController {
	String NAME = "Tab";
	String NAME_PLURAL = "Tabs";

	@ApiOperation("Get " + NAME_PLURAL)
	@GetMapping("/{objectType}")
	ListOut<TabStpItem> get(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			TabStpGetIn input//
	);

	@ApiOperation("Get " + NAME)
	@GetMapping("/{objectType}/{id}")
	TabStpData get(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "id", required = true) Long id//
	);

	@ApiOperation("Post " + NAME)
	@PostMapping("/{objectType}/one")
	TabStpData post(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@RequestBody(required = true) TabStpPostData data//
	);

	@ApiOperation("Put " + NAME)
	@PutMapping("/{objectType}/{id}")
	TabStpData put(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "id", required = true) Long id, //
			@RequestBody(required = true) TabStpPostData data//
	);

	@ApiOperation("Let " + NAME + " Hidden")
	@PutMapping("/{objectType}/{id}/hidden")
	SuccessOut hidden(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "id", required = true) Long id//
	);

	@ApiOperation("Let " + NAME + " Shown")
	@PutMapping("/{objectType}/{id}/shown")
	SuccessOut shown(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "id", required = true) Long id//
	);

	@ApiOperation("Let " + NAME + " Hidden by Name")
	@PutMapping("/{objectType}/names/{name}/hidden")
	SuccessOut hiddenByName(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "name", required = true) String name//
	);

	@ApiOperation("Let " + NAME + " Shown by Name")
	@PutMapping("/{objectType}/names/{name}/shown")
	SuccessOut shownByName(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "name", required = true) String name//
	);

	@ApiOperation("Delete " + NAME)
	@DeleteMapping("/{objectType}/{id}")
	void delete(//
			@PathVariable(name = "objectType", required = true) ObjectType objectType, //
			@PathVariable(name = "id", required = true) Long id//
	);

}
