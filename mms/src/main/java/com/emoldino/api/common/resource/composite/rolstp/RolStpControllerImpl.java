package com.emoldino.api.common.resource.composite.rolstp;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetUsersIn;
import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleSaveUsersIn;
import com.emoldino.api.common.resource.base.accesscontrol.repository.userlite.UserLite;
import com.emoldino.api.common.resource.base.accesscontrol.service.role.RoleAccessControlService;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpGetPageIn;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpItem;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpLocation;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpLocationGetIn;
import com.emoldino.api.common.resource.composite.rolstp.service.RolStpService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

@RestController
public class RolStpControllerImpl implements RolStpController {

	@Autowired
	private RolStpService service;

	@Autowired
	private RoleAccessControlService roleService;

	@Override
	public Page<RolStpItem> getPage(RolStpGetPageIn input, Pageable pageable) {
		return service.getPage(input, pageable);
	}

	@Override
	public RolStpItem get(Long id) {
		return service.get(id);
	}

	@Deprecated
	@Override
	public SuccessOut post(RolStpItem data) {
		service.post(data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut put(Long id, RolStpItem data) {
		service.put(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut disableList(List<Long> id) {
		roleService.disableList(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut enableList(List<Long> id) {
		roleService.enableList(id);
		return SuccessOut.getDefault();
	}

	@Override
	public ListOut<UserLite> getUsers(Long id, RoleGetUsersIn input) {
		return roleService.getUsers(id, input);
	}

	@Override
	public Page<UserLite> getUsersAvaliable(Long id, RoleGetUsersIn input, Pageable pageable) {
		return roleService.getUsersAvaliable(id, input, pageable);
	}

	@Deprecated
	@Override
	public Page<UserLite> getAvailableUsers(Long id, RoleGetUsersIn input, Pageable pageable) {
		return roleService.getUsersAvaliable(id, input, pageable);
	}

	@Override
	public SuccessOut saveUsers(Long id, RoleSaveUsersIn data) {
		roleService.saveUsers(id, data);
		return SuccessOut.getDefault();
	}

	@Override
	public Page<RolStpLocation> getLocations(RolStpLocationGetIn input, Pageable pageable) {
		return service.getLocations(input, pageable);
	}

}
