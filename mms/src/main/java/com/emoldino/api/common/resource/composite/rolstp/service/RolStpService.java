package com.emoldino.api.common.resource.composite.rolstp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetIn;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControl;
import com.emoldino.api.common.resource.base.accesscontrol.service.role.RoleAccessControlService;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpGetPageIn;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpItem;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpLocation;
import com.emoldino.api.common.resource.composite.rolstp.dto.RolStpLocationGetIn;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.location.LocationRepository;
import saleson.common.util.SecurityUtils;
import saleson.model.QLocation;

@Service
public class RolStpService {

	@Autowired
	private RoleAccessControlService service;

	public Page<RolStpItem> getPage(RolStpGetPageIn input, Pageable pageable) {
		if (input.getEnabled() == null) {
			input.setEnabled(true);
		}

		RoleGetIn reqin = new RoleGetIn();
		reqin.setEnabled(input.getEnabled());
		reqin.setQuery(input.getQuery());
		Page<RolStpItem> page = service.getPage(reqin, pageable).map(role -> toItem(role));
		return page;
	}

	public RolStpItem get(Long id) {
		return toItem(service.get(id));
	}

	@Deprecated
	public void post(RolStpItem data) {
		throw new BizException("NOT_SUPPORTED_METHOD");
//		service.post(toRole(data));
	}

	public void put(Long id, RolStpItem data) {
		service.put(id, toRole(data));
	}

	private static RolStpItem toItem(RoleControl role) {
		if (role == null) {
			return null;
		}
		RolStpItem item = ValueUtils.map(role, RolStpItem.class);
		return item;
	}

	private static RoleControl toRole(RolStpItem data) {
		RoleControl role = null;
		if (data.getId() != null) {
			role = BeanUtils.get(RoleAccessControlService.class).get(data.getId());
		}
		if (role == null) {
			role = ValueUtils.map(data, RoleControl.class);
		} else {
			role.setDescription(data.getDescription());
		}
		return role;
	}

	public Page<RolStpLocation> getLocations(RolStpLocationGetIn input, Pageable pageable) {
		// 1. Validation
		Long companyId = SecurityUtils.getCompanyId();
		if (companyId == null || companyId == 0L) {
			return Page.empty(pageable);
		}
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.ASC, "name");
		}

		// 2. Logic
		QLocation table = QLocation.location;
		BooleanBuilder filter = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			List<Long> companyIds = AccessControlUtils.getAllAccessibleCompanyIds(companyId);
			filter.and(table.companyId.in(companyIds));
		}
		if (!ObjectUtils.isEmpty(input.getQuery())) {
			filter.and(table.name.likeIgnoreCase("%" + input.getQuery() + "%"));
		}

		// 3. Response
		Page<RolStpLocation> page = BeanUtils.get(LocationRepository.class).findAll(filter, pageable).map(item -> new RolStpLocation(item.getId(), item.getName()));
		return page;
	}

}
