package com.emoldino.api.common.resource.composite.rolstp;

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

import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetUsersIn;
import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleSaveUsersIn;
import com.emoldino.api.common.resource.base.accesscontrol.repository.userlite.UserLite;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpGetPageIn;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpItem;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpLocation;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpLocationGetIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Role Config")
@RequestMapping("/api/common/rol-stp")
public interface RolStpController {

	@ApiOperation("Get Roles")
	@GetMapping
	Page<RolStpItem> getPage(RolStpGetPageIn input, Pageable pageable);

	@ApiOperation("Get Role by ID")
	@GetMapping("/{id}")
	RolStpItem get(@PathVariable Long id);

	@Deprecated
	@ApiOperation("Create One Role")
	@PostMapping("/one")
	SuccessOut post(@RequestBody RolStpItem data);

	@ApiOperation("Update Role by ID")
	@PutMapping("/{id}")
	SuccessOut put(@PathVariable("id") Long id, @RequestBody RolStpItem data);

	@ApiOperation("Disable Roles by ID")
	@PutMapping("/disable")
	SuccessOut disableList(@RequestParam List<Long> id);

	@ApiOperation("Enable Roles by ID")
	@PutMapping("/enable")
	SuccessOut enableList(@RequestParam List<Long> id);

	@ApiOperation("Get Users by Role")
	@GetMapping("/{id}/users")
	ListOut<UserLite> getUsers(@PathVariable("id") Long id, RoleGetUsersIn input);

	@ApiOperation("Get Available Users by Role")
	@GetMapping("/{id}/users/available")
	Page<UserLite> getUsersAvaliable(@PathVariable("id") Long id, RoleGetUsersIn input, Pageable pageable);

	@Deprecated
	@ApiOperation("Get Available Users by Role (Old)")
	@GetMapping("/{id}/available/users")
	Page<UserLite> getAvailableUsers(@PathVariable("id") Long id, RoleGetUsersIn input, Pageable pageable);

	@ApiOperation("Save Users by Role")
	@PostMapping("/{id}/users/save")
	SuccessOut saveUsers(@PathVariable("id") Long id, @RequestBody RoleSaveUsersIn data);

	@ApiOperation("Get Locations")
	@GetMapping("/locations")
	Page<RolStpLocation> getLocations(RolStpLocationGetIn input, Pageable pageable);

}
