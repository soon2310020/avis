
package com.emoldino.api.common.resource.composite.pmsstp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

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
import com.emoldino.api.common.resource.composite.pmsstp.service.PmsStpService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class PmsStpControllerImpl implements PmsStpController {

	@Autowired
	private PmsStpService service;

	@Override
	public Page<PmsStpRole> get(PmsStpGetIn input, Pageable pageable) {
		return service.get(input, pageable);
	}

	@Override
	public ListOut<PmsStpResourceType> getResourceTypes() {
		return service.getResourceTypes();
	}

	@Override
	public ListOut<PmsStpPermission> getPermissionTree(Long id, PmsStpPermissionGetIn input) {
		return service.getPermissionTree(id, input);
	}

	@Override
	public SuccessOut savePermissionTree(Long id, PmsStpResourceTypeEnum resourceType, PmsStpPermissionSaveIn data) {
		if (resourceType != null) {
			data.setResourceType(resourceType);
		}
		service.savePermissionTree(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut resetPermissionTree(Long id, PmsStpResourceTypeEnum resourceType, PmsStpPermissionResetIn data) {
		if (resourceType != null) {
			data.setResourceType(resourceType);
		}
		service.resetPermissionTree(id, data);
		return SuccessOut.getDefault();
	}

	@Deprecated
	@Override
	public PmsStpPermissionGetOut getPermissions(Long id, PmsStpPermissionGetIn input) {
		return service.getPermissions(id, input);
	}

	@Deprecated
	@Override
	public ListOut<PmsStpResource> getPermissionsAvaliable(Long id, PmsStpPermissionGetIn input) {
		return service.getPermissionsAvaliable(id, input);
	}

	@Deprecated
	@Override
	public ListOut<PmsStpPermission> getPermissionsAssigned(Long id, PmsStpPermissionGetIn input) {
		return service.getPermissionsAssinged(id, input);
	}

	@Deprecated
	@Override
	public SuccessOut savePermissions(Long id, PmsStpPermissionSaveIn data) {
		service.savePermissions(id, data);
		return SuccessOut.getDefault();
	}

}
