package com.emoldino.api.common.resource.base.accesscontrol.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.base.accesscontrol.dto.PermissionDto;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.PermissionRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.QPermission;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.QRoleUser;
import com.emoldino.api.common.resource.base.accesscontrol.repository.roleuser.RoleUserRepository;
import com.emoldino.api.common.resource.base.accesscontrol.service.dataleak.DataLeakAccessControlService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.accessHierachy.AccessHierarchyRepository;
import saleson.api.role.RoleRepository;
import saleson.common.util.SecurityUtils;
import saleson.model.AccessHierarchy;
import saleson.model.QRole;

public class AccessControlUtils {

	public static Map<String, PermissionDto> getPermissions(String resourceType) {
		Map<String, PermissionDto> permissions = ThreadUtils.getProp("AccessControlUtils.getPermissions." + resourceType, () -> {
			Long userId = SecurityUtils.getUserId();
			if (userId == null) {
				return Collections.emptyMap();
			}

			List<Long> roleIds = new ArrayList<>();
			{
				QRoleUser table = QRoleUser.roleUser;
				BooleanBuilder filter = new BooleanBuilder();
				filter.and(table.userId.eq(userId));
				BeanUtils.get(RoleUserRepository.class).findAll(filter).forEach(item -> {
					QRole roleTable = QRole.role;
					if (BeanUtils.get(RoleRepository.class).exists(new BooleanBuilder()//
							.and(roleTable.id.eq(item.getRoleId()))//
							.and(roleTable.enabled.isTrue())//
					)) {
						roleIds.add(item.getRoleId());
					}
				});
			}
			if (roleIds.isEmpty()) {
				return Collections.emptyMap();
			}

			Map<String, PermissionDto> map = new LinkedHashMap<>();
			{
				QPermission table = QPermission.permission;
				BooleanBuilder filter = new BooleanBuilder();
				filter.and(table.enabled.eq(true));
				filter.and(table.resourceType.eq(resourceType));
				filter.and(table.role.id.in(roleIds));
				BeanUtils.get(PermissionRepository.class).findAll(filter).forEach(perm -> {
					if (map.containsKey(perm.getResourceId())) {
						return;
					}
					PermissionDto data = new PermissionDto(perm.getResourceId());
					map.put(perm.getResourceId(), data);
					if (!ObjectUtils.isEmpty(perm.getItems())) {
						perm.getItems().forEach(item -> {
							if (!item.isEnabled()) {
								return;
							}
							String id = perm.getResourceId() + "::" + item.getItemId();
							PermissionDto itemData = new PermissionDto(id);
							map.put(id, itemData);
						});
					}
				});
			}
			return map;
		});

		return permissions;
	}

	public static void checkDataLeak(Method method, Object data) {
		// 1. Check Skip Cases

		if (isDisabled(method)) {
			return;
		}

		if (SecurityUtils.isInHouse() || !SecurityUtils.isLogin() || ObjectUtils.isEmpty(data) || ValueUtils.isPrimitiveType(data)) {
			return;
		}

		// 2. Check
		BeanUtils.get(DataLeakAccessControlService.class).check(data);
	}

	private static boolean isDisabled(Method target) {
		if (target == null) {
			return false;
		}

		DataLeakDetector detector = target.getAnnotation(DataLeakDetector.class);
		if (detector != null) {
			return detector.disabled();
		}

		return false;
	}

	public static boolean isAccessFilterRequired() {
		Long companyId = SecurityUtils.getCompanyId();
		if (companyId == null) {
			return false;
		}
		return ThreadUtils.getProp("ACU.isCompanyFilterRequired." + companyId, () -> {
			if (!SecurityUtils.isInHouse()) {
				return true;
			} else if (SecurityUtils.isEmoldino()) {
				return false;
			}
			AccessHierarchy hier = BeanUtils.get(AccessHierarchyRepository.class).findFirstByCompanyId(companyId).orElse(null);
			return hier != null && ValueUtils.toLong(hier.getLevel(), 0L) != 0;
		});
	}

	public static List<Long> getAllAccessibleCompanyIds() {
		return getAllAccessibleCompanyIds(SecurityUtils.getCompanyId());
	}

	public static List<Long> getAllAccessibleCompanyIds(Long companyId) {
		List<Long> list = ThreadUtils.getProp("ACU.getAllAccessibleCompanyIds." + companyId, () -> {
			List<Long> companyIds = new ArrayList<>();
			if (companyId == null) {
				return companyIds;
			}
			addAllAccessibleCompanyIds(companyIds, companyId);
			return companyIds;
		});
		return list;
	}

	// TODO Replace with New Implementation of Company Hierarchy Later
	private static void addAllAccessibleCompanyIds(List<Long> companyIds, Long companyId) {
		if (companyIds.contains(companyId)) {
			return;
		}
		companyIds.add(companyId);
		BeanUtils.get(AccessHierarchyRepository.class).findFirstByCompanyId(companyId).ifPresent(data -> {
			if (!ObjectUtils.isEmpty(data.getAccessCompanyChildRelations())) {
				data.getAccessCompanyChildRelations().forEach(item -> {
					addAllAccessibleCompanyIds(companyIds, item.getCompanyId());
				});
			}
		});
	}

}
