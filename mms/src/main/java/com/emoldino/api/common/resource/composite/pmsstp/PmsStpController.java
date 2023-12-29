package com.emoldino.api.common.resource.composite.pmsstp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpGetIn;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpPermission;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpPermissionGetIn;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpPermissionGetOut;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpPermissionResetIn;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpPermissionSaveIn;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpResource;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpResourceType;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpRole;
import com.emoldino.api.common.resource.composite.pmsstp.enumeration.PmsStpResourceTypeEnum;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Common / Permission Config")
@RequestMapping("/api/common/pms-stp")
public interface PmsStpController {

	@ApiOperation("Get Roles")
	@GetMapping
	Page<PmsStpRole> get(PmsStpGetIn input, Pageable pageable);

	@ApiOperation("Get Resource Types")
	@GetMapping("/resourceTypes")
	ListOut<PmsStpResourceType> getResourceTypes();

	@ApiOperation("Get Permission Tree by Role")
	@GetMapping("/{id}/permissionTree")
	ListOut<PmsStpPermission> getPermissionTree(@PathVariable("id") Long id, PmsStpPermissionGetIn input);

	@ApiOperation("Save Permission Tree by Role")
	@PostMapping("/{id}/permissionTree/save")
	SuccessOut savePermissionTree(//
			@PathVariable("id") Long id, //
			@RequestParam(name = "resourceType", required = false) PmsStpResourceTypeEnum resourceType, //
			@RequestBody PmsStpPermissionSaveIn data);

	@ApiOperation("Save Permission Tree by Role")
	@PostMapping("/{id}/permissionTree/reset")
	SuccessOut resetPermissionTree(//
			@PathVariable("id") Long id, //
			@RequestParam(name = "resourceType", required = false) PmsStpResourceTypeEnum resourceType, //
			@RequestBody PmsStpPermissionResetIn data);

	@Deprecated
	@ApiOperation("Get Permissions by Role")
	@GetMapping("/{id}/permissions")
	PmsStpPermissionGetOut getPermissions(@PathVariable("id") Long id, PmsStpPermissionGetIn input);

	@Deprecated
	@ApiOperation("Get Available Permissions by Role")
	@GetMapping("/{id}/permissions/available")
	ListOut<PmsStpResource> getPermissionsAvaliable(@PathVariable("id") Long id, PmsStpPermissionGetIn input);

	@Deprecated
	@ApiOperation("Get Assigned Permissions by Role")
	@GetMapping("/{id}/permissions/assigned")
	ListOut<PmsStpPermission> getPermissionsAssigned(@PathVariable("id") Long id, PmsStpPermissionGetIn input);

	@Deprecated
	@ApiOperation("Save Permissions by Role")
	@PostMapping("/{id}/permissions/save")
	SuccessOut savePermissions(@PathVariable("id") Long id, @RequestBody PmsStpPermissionSaveIn data);

}
