package com.emoldino.api.common.resource.composite.pmsstp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.accesscontrol.dto.RoleGetIn;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.Permission;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.PermissionItem;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.PermissionRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.QPermission;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControl;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControlRepository;
import com.emoldino.api.common.resource.base.accesscontrol.service.role.RoleAccessControlService;
import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.base.menu.service.MenuService;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpGetIn;
import com.emoldino.api.common.resource.composite.pmsstp.dto.PmsStpItem;
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
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

@Service
@Transactional
public class PmsStpService {

	@Autowired
	private RoleAccessControlService service;

	public ListOut<PmsStpResourceType> getResourceTypes() {
		return new ListOut<>(Arrays.asList(new PmsStpResourceType("MENU", "Menu")));
	}

	public Page<PmsStpRole> get(PmsStpGetIn input, Pageable pageable) {
		RoleGetIn reqin = new RoleGetIn();
		reqin.setEnabled(true);
		reqin.setQuery(input.getQuery());
		Page<PmsStpRole> page = service.getPage(reqin, pageable).map(role -> ValueUtils.map(role, PmsStpRole.class));
		return page;
	}

	public ListOut<PmsStpPermission> getPermissionTree(Long id, PmsStpPermissionGetIn input) {
		ValueUtils.assertNotEmpty(input.getResourceType(), "resourceType");

		Set<String> permitted = getPermittedById(id, input.getResourceType());

		ListOut<PmsStpPermission> output = new ListOut<>();
		if (PmsStpResourceTypeEnum.MENU.equals(input.getResourceType())) {
			List<MenuTreeNode> content = BeanUtils.get(MenuService.class).getTree(2).getContent();
			populateMenuPermissionTree(content, output.getContent(), permitted);
		}
		return output;
	}

	private static void populateMenuPermissionTree(List<MenuTreeNode> from, List<PmsStpPermission> to, Set<String> permitted) {
		if (ObjectUtils.isEmpty(from)) {
			return;
		}
		for (MenuTreeNode node : from) {
			if ("CATEGORY".equals(node.getType())) {
				populateMenuPermissionTree(node.getChildren(), to, permitted);
				continue;
			} else if ("FUNCTION".equals(node.getType())) {
				boolean permitAll = ValueUtils.toBoolean(node.getPermitAll(), false);
				PmsStpPermission perm = new PmsStpPermission();
				populate(node, perm, permitted);
				perm.setPermitted(permitted == null ? null : (permitAll || permitted.contains(node.getId())));
				perm.setEditable(!permitAll);
				to.add(perm);
				populateMenuPermissionTree(node.getChildren(), to, permitted);
			} else if ("SUBFUNCTION".equals(node.getType())) {
				boolean permitAll = ValueUtils.toBoolean(node.getPermitAll(), false);
				PmsStpPermission parent = to.get(to.size() - 1);
				PmsStpPermission perm = new PmsStpPermission();
				populate(node, perm, permitted);
				perm.setPermitted(permitted == null ? null : (permitAll || permitted.contains(node.getId())));
				perm.setEditable(!permitAll);
				parent.addSubpermission(perm);
			}
		}
	}

	private static void populate(MenuTreeNode from, PmsStpResource to) {
		if (from == null) {
			return;
		}

		to.setResourceId(from.getId());
		to.setName(from.getName());

		to.setFieldValue("enabled", from.isEnabled());
		to.setFieldValue("oemEnabled", from.getOemEnabled());
		to.setFieldValue("supplierEnabled", from.getSupplierEnabled());
		to.setFieldValue("toolmakerEnabled", from.getToolmakerEnabled());
	}

	private static void populate(MenuTreeNode from, PmsStpPermission to, Set<String> permitted) {
		populate(from, (PmsStpResource) to);

		if (ObjectUtils.isEmpty(from.getItems())) {
			return;
		}
		from.getItems().forEach(item -> {
			PmsStpItem permItem = new PmsStpItem();
			permItem.setId(item.getId());
			permItem.setName(item.getName());
			permItem.setType(item.getType());
			if (ObjectUtils.isEmpty(item.getIdRef())) {
				permItem.setPermitted(permitted == null ? null : permitted.contains(from.getId() + "::" + item.getId()));
				permItem.setEditable(true);
			} else {
				MenuTreeNode ref = BeanUtils.get(MenuService.class).get(item.getIdRef());
				if (ref == null) {
					permItem.setPermitted(false);
				} else {
					boolean permitAll = ValueUtils.toBoolean(ref.getPermitAll(), false);
					permItem.setPermitted(permitted == null ? null : (permitAll || permitted.contains(ref.getId())));
				}
				permItem.setEditable(false);
			}
			to.addItem(permItem);
		});
	}

	private static Map<String, Permission> getPermissions(Long id, PmsStpResourceTypeEnum resourceType, Boolean enabled) {
		Map<String, Permission> perms = new LinkedHashMap<>();
		QPermission table = QPermission.permission;
		BooleanBuilder filter = new BooleanBuilder();
		filter.and(table.role.id.eq(id));
		filter.and(table.resourceType.eq(resourceType.name()));
		if (enabled != null) {
			filter.and(table.enabled.eq(enabled));
		}
		BeanUtils.get(PermissionRepository.class).findAll(filter).forEach(perm -> perms.put(perm.getResourceId(), perm));
		return perms;
	}

	private static Set<String> getPermittedById(Long id, PmsStpResourceTypeEnum resourceType) {
		Set<String> permitted = new HashSet<>();
		getPermissions(id, resourceType, null).forEach((resourceId, perm) -> {
			if (!permitted.contains(resourceId) && ValueUtils.toBoolean(perm.isEnabled(), false)) {
				permitted.add(resourceId);
			}

			if (ObjectUtils.isEmpty(perm.getItems())) {
				return;
			}
			for (PermissionItem item : perm.getItems()) {
				if (!item.isEnabled()) {
					continue;
				}
				String key = resourceId + "::" + item.getItemId();
				if (!permitted.contains(key)) {
					permitted.add(key);
				}
			}
		});
		return permitted;
	}

	public void savePermissionTree(Long id, PmsStpPermissionSaveIn input) {
		// 1. Validation
		ValueUtils.assertNotEmpty(input.getResourceType(), "resourceType");
//		if (!ObjectUtils.isEmpty(input.getContent())) {
//			input.getContent().forEach(item -> ValueUtils.assertNotEmpty(item.getResourceId(), "resourceId"));
//		}

		RoleControl role = BeanUtils.get(RoleControlRepository.class).findById(id)//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(RoleControl.class, "id", id));

		Map<String, Permission> perms = getPermissions(id, input.getResourceType(), null);
		List<Permission> saves = new ArrayList<>();
		procPermissionTree(role, input.getResourceType(), input.getContent(), perms, saves);
		if (!perms.isEmpty()) {
			BeanUtils.get(PermissionRepository.class).deleteAll(perms.values());
		}
		BeanUtils.get(PermissionRepository.class).saveAll(saves);
	}

	public void resetPermissionTree(Long id, PmsStpPermissionResetIn input) {
		ValueUtils.assertNotEmpty(input.getResourceType(), "resourceType");

		RoleControl role = BeanUtils.get(RoleControlRepository.class).findById(id)//
				.orElseThrow(() -> DataUtils.newDataNotFoundException(RoleControl.class, "id", id));

		Map<String, Permission> perms = getPermissions(id, input.getResourceType(), null);
		BeanUtils.get(MenuService.class).getList().getContent().forEach(menu -> {
			if (ValueUtils.toBoolean(menu.getPermitAll(), false) || ObjectUtils.isEmpty(menu.getAuthorities()) || !menu.getAuthorities().contains(role.getAuthority())) {
				return;
			}
			Permission perm = perms.remove(menu.getId());
			if (perm != null) {
				perm.setEnabled(true);
				perm.setItems(null);
			}
			BeanUtils.get(MenuService.class).initMenuPermission(role, menu, perm);
		});
		if (!perms.isEmpty()) {
			BeanUtils.get(PermissionRepository.class).deleteAll(perms.values());
		}
	}

	private void procPermissionTree(RoleControl role, PmsStpResourceTypeEnum resourceType, List<PmsStpPermission> content, Map<String, Permission> perms, List<Permission> saves) {
		if (ObjectUtils.isEmpty(content)) {
			return;
		}
		for (PmsStpPermission item : content) {
			if (ObjectUtils.isEmpty(item.getResourceId())) {
				continue;
			}
			boolean permitted = ValueUtils.toBoolean(item.getPermitted(), true);
			Permission perm;
			if (!perms.containsKey(item.getResourceId())) {
				perm = new Permission();
				perm.setRole(role);
				perm.setResourceType(resourceType.name());
				perm.setResourceId(item.getResourceId());
				perm.setEnabled(permitted);
				saves.add(perm);
			} else {
				perm = perms.remove(item.getResourceId());
				if (perm.isEnabled() != permitted) {
					perm.setEnabled(permitted);
					if (!saves.contains(perm)) {
						saves.add(perm);
					}
				}
			}
			procPermissionItems(item.getItems(), perm, saves);
			procPermissionTree(role, resourceType, item.getSubpermissions(), perms, saves);
		}
	}

	private void procPermissionItems(List<PmsStpItem> list, Permission perm, List<Permission> saves) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}

		Map<String, PermissionItem> items = new LinkedHashMap<>();
		if (!ObjectUtils.isEmpty(perm.getItems())) {
			for (PermissionItem item : perm.getItems()) {
				items.put(item.getItemId(), item);
			}
		}

		List<PermissionItem> permItems = new ArrayList<>();
		for (PmsStpItem item : list) {
			if (ObjectUtils.isEmpty(item.getId())) {
				continue;
			}
			boolean permitted = ValueUtils.toBoolean(item.getPermitted(), true);
			PermissionItem permItem;
			if (!items.containsKey(item.getId())) {
				permItem = new PermissionItem();
				permItem.setItemId(item.getId());
				permItem.setEnabled(permitted);
				if (!saves.contains(perm)) {
					saves.add(perm);
				}
			} else {
				permItem = items.get(item.getId());
				if (permItem.isEnabled() != permitted) {
					permItem.setEnabled(permitted);
					if (!saves.contains(perm)) {
						saves.add(perm);
					}
				}
			}
			permItems.add(permItem);
		}
		perm.setItems(permItems);
	}

	@Deprecated
	public PmsStpPermissionGetOut getPermissions(Long id, PmsStpPermissionGetIn input) {
		ValueUtils.assertNotEmpty(input.getResourceType(), "resourceType");

		Set<String> permitted = getPermittedById(id, input.getResourceType());

		Map<String, PmsStpPermission> permissions = new TreeMap<>();
		Map<String, PmsStpResource> resources = new TreeMap<>();
		if (PmsStpResourceTypeEnum.MENU.equals(input.getResourceType())) {
			BeanUtils.get(MenuService.class).getList().getContent().forEach(node -> {
				PmsStpResource item;
				if (permitted.contains(node.getId())) {
					PmsStpPermission perm = new PmsStpPermission();
					perm.setPermitted(true);
					permissions.put(node.getName(), perm);
					item = perm;
				} else {
					item = new PmsStpResource();
					resources.put(node.getName(), item);
				}

				populate(node, item);
			});
		}

		PmsStpPermissionGetOut output = new PmsStpPermissionGetOut();
		output.setContent(new ArrayList<>(permissions.values()));
		output.setContentAvailable(new ArrayList<>(resources.values()));
		return output;
	}

	@Deprecated
	public ListOut<PmsStpPermission> getPermissionsAssinged(Long id, PmsStpPermissionGetIn input) {
		return new ListOut<>(getPermissions(id, input).getContent());
	}

	@Deprecated
	public ListOut<PmsStpResource> getPermissionsAvaliable(Long id, PmsStpPermissionGetIn input) {
		return new ListOut<>(getPermissions(id, input).getContentAvailable());
	}

	@Deprecated
	public void savePermissions(Long id, PmsStpPermissionSaveIn input) {
		savePermissionTree(id, input);
	}

}
