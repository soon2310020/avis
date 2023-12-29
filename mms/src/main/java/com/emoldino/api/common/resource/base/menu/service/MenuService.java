package com.emoldino.api.common.resource.base.menu.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.accesscontrol.dto.PermissionDto;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.Permission;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.PermissionItem;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.PermissionRepository;
import com.emoldino.api.common.resource.base.accesscontrol.repository.permission.QPermission;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControl;
import com.emoldino.api.common.resource.base.accesscontrol.repository.rolecontrol.RoleControlRepository;
import com.emoldino.api.common.resource.base.accesscontrol.service.role.RoleAccessControlService;
import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.common.resource.base.license.dto.License;
import com.emoldino.api.common.resource.base.menu.dto.MenuItem;
import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.base.menu.service.structure.MenuStructure;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import saleson.api.user.UserRepository;
import saleson.common.util.SecurityUtils;
import saleson.model.User;

@Service("MenuService2")
@Transactional
@Slf4j
public class MenuService {
	@Value("${app.menu.structure.class-name:com.emoldino.api.common.resource.base.menu.service.structure.MenuStructure01}")
	private String menuStructureClassName;

	private MenuStructure menuStructure;

	private static List<MenuTreeNode> ALL_MENU_TREE;
	private static Map<String, MenuTreeNode> ALL_FUNCTION_MAP_BY_ID;
	private static List<MenuTreeNode> ALL_FUNCTION_LIST;
	private static Instant LAST_UPDATED_AT;

	@Data
	@NoArgsConstructor
	public static class TempMenu {
		private String path;
		private int position = -1;
		private String name;
		private boolean beta = true;
		private String message;
		private String url;
		private String icon;
		private boolean hidden = false;
		private List<TempSubmenu> children = new ArrayList<>();
	}

	@Data
	@NoArgsConstructor
	public static class TempSubmenu {
		private String name;
		private String message;
		private String url;
		private String icon;
		private boolean hidden = false;
	}

	private synchronized static void setConstants(List<MenuTreeNode> nodes) {
		ALL_MENU_TREE = nodes;
		Map<String, MenuTreeNode> idMap = new TreeMap<>();
		LAST_UPDATED_AT = extractFunctionsAndGetLastUpdatedAt(ALL_MENU_TREE, idMap, new HashSet<>());
		ALL_FUNCTION_MAP_BY_ID = idMap;
		ALL_FUNCTION_LIST = new ArrayList<>(idMap.values());
	}

	private static void putNode(List<MenuTreeNode> content, TempMenu temp) {
		if (ObjectUtils.isEmpty(content) || temp.isHidden()) {
			return;
		}

		// Get Parent
		List<MenuTreeNode> parent;
		{
			parent = content;
			String path = temp.getPath();
			if (!ObjectUtils.isEmpty(path)) {
				String[] strs = StringUtils.tokenizeToStringArray(path, "/");
				for (String name : strs) {
					MenuTreeNode category = getCategory(parent, name);
					if (category == null) {
						return;
					} else if (category.getChildren() == null) {
						category.setChildren(new ArrayList<>());
					}
					parent = category.getChildren();
				}
			}
		}

		// If not exists, Add Menu
		for (MenuTreeNode item : parent) {
			if (item.getName().equalsIgnoreCase(temp.getName())) {
				return;
			}
		}

		MenuTreeNode menu = MenuTreeNode.builder()//
				.name(temp.getName()).message(temp.getMessage()).type("FUNCTION").beta(temp.isBeta())//
				.url(temp.getUrl()).icon(temp.getIcon())//
				.enabled(true).emoldinoEnabled(true).oemEnabled(false).supplierEnabled(false).toolmakerEnabled(false)//
				.build();
		if (!ObjectUtils.isEmpty(temp.getChildren())) {
			temp.getChildren().forEach(child -> {
				if (child.isHidden()) {
					return;
				}
				MenuTreeNode submenu = MenuTreeNode.builder()//
						.name(child.getName()).message(child.getMessage()).type("SUBFUNCTION").beta(temp.isBeta())//
						.url(child.getUrl()).icon(child.getIcon())//
						.enabled(true).emoldinoEnabled(true).oemEnabled(false).supplierEnabled(false).toolmakerEnabled(false)//
						.build();
				menu.addChild(submenu);
			});
		}
//		private String name;
//		private String message;
//		private String url;
//		private String icon;
		if (temp.getPosition() >= 0 && parent.size() > temp.getPosition()) {
			parent.add(temp.getPosition(), menu);
		} else {
			parent.add(menu);
		}
	}

	private static MenuTreeNode getCategory(List<MenuTreeNode> content, String name) {
		if (content == null || ObjectUtils.isEmpty(name)) {
			return null;
		}
		MenuTreeNode category = null;
		for (MenuTreeNode item : content) {
			if (name.equalsIgnoreCase(item.getName()) && ("CATEGORY".equals(item.getType()) || "CATEGORY".equals(item.getType()))) {
				category = item;
				break;
			}
		}
		// If not exists yet, add 
//		if (category == null) {
//			category = new MenuTreeNode(name, null);
//			content.add(category);
//		}
		return category;
	}

	private static Instant extractFunctionsAndGetLastUpdatedAt(List<MenuTreeNode> from, Map<String, MenuTreeNode> idMap, Set<String> set) {
		if (ObjectUtils.isEmpty(from)) {
			return null;
		}
		Instant instant = null;
		for (MenuTreeNode item : from) {
			String name = (item.getParent() != null && ("CATEGORY".equals(item.getType()) || "SUBFUNCTION".equals(item.getType())) ? //
					item.getParent().getName() : item.getType())//
					+ "." + item.getName();
			if (ObjectUtils.isEmpty(item.getName())) {
				continue;
			} else if (set.contains(name)) {
				throw new LogicException("NAME_DUPLICATED", "Menu Name is duplicated!!: " + name, new Property("name", item.getName()));
			} else if (!ObjectUtils.isEmpty(item.getId()) && idMap.containsKey(item.getId())) {
				throw new LogicException("ID_DUPLICATED", "Menu ID is duplicated!!: " + item.getId(), new Property("id", item.getId()));
			}
			set.add(name);

			Instant cinst = extractFunctionsAndGetLastUpdatedAt(item.getChildren(), idMap, set);
			if (cinst != null && (instant == null || instant.compareTo(cinst) < 0)) {
				instant = cinst;
			}

			if (!ObjectUtils.isEmpty(item.getId()) && ("FUNCTION".equals(item.getType()) || "SUBFUNCTION".equals(item.getType()))) {
				idMap.put(item.getId(), item);
				if (!ObjectUtils.isEmpty(item.getUpdatedDate())) {
					Instant inst = toInstant(item);
					if (inst != null && (instant == null || instant.compareTo(inst) < 0)) {
						instant = inst;
					}
				}
			}
		}
		return instant;
	}

	private static Instant toInstant(MenuTreeNode item) {
		if (ObjectUtils.isEmpty(item.getUpdatedDate())) {
			return null;
		}
		Instant instant = DateUtils2.toInstant(item.getUpdatedDate(), DatePattern.yyyyMMdd, Zone.GMT).plus(Duration.ofDays(1L));
		return instant;
	}

	private static List<MenuTreeNode> clone(List<MenuTreeNode> list) {
		if (list == null) {
			return null;
		}
		List<MenuTreeNode> l = new ArrayList<>(list.size());
		list.forEach(node -> {
			if ("SUBFUNCTION".equals(node.getType()) && !node.isEnabled()) {
				return;
			}
			String icon = node.getIcon();
			if ("AS1030".equals(node.getId())) {
				if ("production".equals(ConfigUtils.getServerName())) {
					icon = "/images/icon/terminal-production.svg";
				}
			}
			l.add(new MenuTreeNode(node.getId(), node.getPath(), node.getName(), node.getMessage(), node.getType(), node.getUpdatedDate(), node.getBeta(), // 
					node.getUrl(), icon, node.getWidth(), node.getHeight(), //
					node.getActive(), //
					node.isEnabled(), node.getEmoldinoEnabled(), node.getOemEnabled(), node.getSupplierEnabled(), node.getToolmakerEnabled(), //
					node.getPermitAll(), node.isShowsEvenNoChildrenPermitted(), false, node.getAuthorities(), //
					cloneItems(node.getItems()), null, node.getParent(), clone(node.getChildren())));
		});
		return l;
	}

	private static List<MenuItem> cloneItems(List<MenuItem> list) {
		if (list == null) {
			return null;
		}
		List<MenuItem> l = new ArrayList<>(list.size());
		list.forEach(item -> l.add(new MenuItem(item.getId(), item.getName(), item.getMessage(), item.getType(), item.getIdRef())));
		return l;
	}

	public ListOut<MenuTreeNode> getTree() {
		return getTree(1);
	}

	public ListOut<MenuTreeNode> getTree(int level) {
		init();
		ListOut<MenuTreeNode> output = new ListOut<>();
		output.setContent(clone(ALL_MENU_TREE));
		clean(output.getContent(), level);
		return output;
	}

	public ListOut<MenuTreeNode> getList() {
		init();
		List<MenuTreeNode> content = clone(ALL_FUNCTION_LIST);
		Map<String, MenuTreeNode> map = new TreeMap<>();
		for (MenuTreeNode item : content) {
			item.setName(MessageUtils.get(item.getMessage(), item.getName(), null));
			String key = (item.getParent() != null && "SUBFUNCTION".equals(item.getType()) ? item.getParent().getName() + "." : "") + item.getName().toLowerCase();
			map.put(key, item);
		}
		content = new ArrayList<>(map.values());
		return new ListOut<>(content);
	}

	public ListOut<MenuTreeNode> getListPermitted() {
		Map<String, MenuTreeNode> map = new TreeMap<>();
		populatePermitted(map, getTree().getContent());
		return new ListOut<>(new ArrayList<>(map.values()));
	}

	private static void populatePermitted(Map<String, MenuTreeNode> map, List<MenuTreeNode> list) {
		if (ObjectUtils.isEmpty(list)) {
			return;
		}
		for (MenuTreeNode item : list) {
			item.setActive(null);
			if ("FUNCTION".equals(item.getType()) && !ObjectUtils.isEmpty(item.getName()) && !item.isUnpermitted()) {
				map.put(item.getName(), item);
			}
			populatePermitted(map, item.getChildren());
		}
	}

	public MenuTreeNode get(String id) {
		init();
		MenuTreeNode node = ObjectUtils.isEmpty(id) ? null : ALL_FUNCTION_MAP_BY_ID.get(id);
		return node;
	}

	public MenuTreeNode getPermitted(String id) {
		init();
		MenuTreeNode node = ObjectUtils.isEmpty(id) ? null : ALL_FUNCTION_MAP_BY_ID.get(id);
		if (node == null) {
			return null;
		}
		List<MenuTreeNode> list = clone(Arrays.asList(node));
		Map<String, MenuTreeNode> map = new TreeMap<>();
		populatePermitted(map, list);
		return map.isEmpty() ? null : map.values().iterator().next();
	}

	private static final Map<String, Boolean> IS_MENU = new LRUMap<>(1000);

	public boolean isMenu(String uri) {
		boolean menuFlag = SyncCtrlUtils.wrap("MenuService.isMenu", IS_MENU, uri, () -> {
			MenuTreeNode menu = getByUri(ALL_MENU_TREE, uri);
			return menu != null;
		});
		return menuFlag;
	}

	// TODO Removing Problem
	public MenuTreeNode getByUri(String uri) {
		init();
		MenuTreeNode menu = _clean(clone(ALL_MENU_TREE), uri, 1, SecurityUtils.isEmoldino(), SecurityUtils.isOem(), SecurityUtils.isSupplier(), getMyTotalPermissions());
		return menu;
	}

	@AllArgsConstructor
	private static class TotalPerms {
		boolean admin;
		Map<String, PermissionDto> perms;
	}

	private static TotalPerms getMyTotalPermissions() {
		Long userId = SecurityUtils.getUserId();
		BeanUtils.get(RoleAccessControlService.class).initRoleUserByUserId(userId);
		// Whether admin or not
		boolean admin = isAdmin(userId);
//		boolean admin = false;
		// New Permissions
		Map<String, PermissionDto> perms = admin ? Collections.emptyMap() : AccessControlUtils.getPermissions("MENU");
		return new TotalPerms(admin, perms);
	}

	private static boolean isAdmin(Long userId) {
		if (userId == null) {
			return false;
		}
		User user = BeanUtils.get(UserRepository.class).findById(userId).orElse(null);
		// Whether admin or not
		boolean admin = user == null ? false : user.isAdmin();
		return admin;
	}

	private static void clean(List<MenuTreeNode> items, int level) {
		if (ObjectUtils.isEmpty(items)) {
			return;
		}

		// 1.  Get URI
		String uri = HttpUtils.getRequestUrl();
		if (!ObjectUtils.isEmpty(uri)) {
			int index;
			index = uri.indexOf("://");
			if (index > 0) {
				uri = uri.substring(index + 3);
			}
			index = uri.indexOf("/");
			uri = uri.substring(index);
		}

		// 2. Clean
		boolean hasActive = _clean(items, uri, level, SecurityUtils.isEmoldino(), SecurityUtils.isOem(), SecurityUtils.isSupplier(), getMyTotalPermissions()) != null;

		// 3. Whether api or not
		boolean api = uri.startsWith("/api");
		String _uri = uri;
		while (!api && !hasActive && !ObjectUtils.isEmpty(items) && !ObjectUtils.isEmpty(_uri) && _uri.contains("/") && _uri.lastIndexOf("/") != 0) {
			_uri = _uri.substring(0, _uri.lastIndexOf("/"));
			hasActive = setActive(items, _uri);
		}

		// 4. Default Active
		MenuTreeNode item;
		while (!api && !hasActive && !ObjectUtils.isEmpty(items) && !(item = items.get(0)).getActive()) {
			item.setActive(true);
			items = item.getChildren();
		}
	}

	private static MenuTreeNode getByUri(List<MenuTreeNode> nodes, String uri) {
		LogicUtils.assertNotEmpty(uri, "uri");

		if (ObjectUtils.isEmpty(nodes)) {
			return null;
		}

		MenuTreeNode menu = null;
		for (MenuTreeNode node : nodes) {
			if ("FUNCTION".equals(node.getType()) || "SUBFUNCTION".equals(node.getType())) {
				if (!ObjectUtils.isEmpty(node.getUrl()) && uri.equals(node.getUrl())) {
					return node;
				}
			}
			menu = getByUri(node.getChildren(), uri);
			if (menu != null) {
				return menu;
			}
		}
		return null;
	}

	private static MenuTreeNode _clean(List<MenuTreeNode> nodes, String uri, //
			int level, boolean emoldino, boolean oem, boolean supplier, TotalPerms totalPerms) {
		if (ObjectUtils.isEmpty(nodes)) {
			return null;
		}

		MenuTreeNode active[] = { null };
		MenuTreeNode hasActive[] = { null };
		List<MenuTreeNode> rlist = new ArrayList<>();
		nodes.forEach(node -> {
			boolean hadChildren = !ObjectUtils.isEmpty(node.getChildren());
			active[0] = _clean(node.getChildren(), uri, level, emoldino, oem, supplier, totalPerms);
			if (hadChildren && !node.isShowsEvenNoChildrenPermitted() && ObjectUtils.isEmpty(node.getChildren())) {
				rlist.add(node);
				return;
			}
			hasActive[0] = hasActive[0] == null ? active[0] : hasActive[0];
			if ("CATEGORY".equals(node.getType()) || "UTILITY".equals(node.getType())) {
				if (ObjectUtils.isEmpty(node.getChildren())) {
					rlist.add(node);
					return;
				}
				node.setActive(active[0] != null);
			} else if ("FUNCTION".equals(node.getType()) || "SUBFUNCTION".equals(node.getType())) {
				if (!ObjectUtils.isEmpty(uri) && !ObjectUtils.isEmpty(node.getUrl()) && uri.equals(node.getUrl())) {
					node.setActive(true);
					active[0] = node;
					hasActive[0] = node;
				} else {
					node.setActive(false);
				}

				/**
				 * Add Unpermitted Menus at RemoveList(rlist)
				 */

				if (!ObjectUtils.isEmpty(node.getId())//
						&& !totalPerms.admin//
						&& !ValueUtils.toBoolean(node.getPermitAll(), false)//
						&& !totalPerms.perms.containsKey(node.getId())//
				) {
					rlist.add(node);
					node.setUnpermitted(true);
					return;
				}

				if (emoldino) {
					if (!ValueUtils.toBoolean(node.getEmoldinoEnabled(), false)) {
						rlist.add(node);
						node.setUnpermitted(true);
						return;
					}
				} else if (oem) {
					if (!ValueUtils.toBoolean(node.getOemEnabled(), false)) {
						rlist.add(node);
						return;
					}
				} else if (supplier) {
					if (!ValueUtils.toBoolean(node.getSupplierEnabled(), false)) {
						rlist.add(node);
						node.setUnpermitted(true);
						return;
					}
				} else {
					if (!ValueUtils.toBoolean(node.getToolmakerEnabled(), false)) {
						rlist.add(node);
						node.setUnpermitted(true);
						return;
					}
				}

				if (!ObjectUtils.isEmpty(node.getItems())) {
					List<MenuItem> irlist = new ArrayList<>();
					for (MenuItem item : node.getItems()) {
						if (!ObjectUtils.isEmpty(item.getId())//
								&& !totalPerms.admin) {
							if (ObjectUtils.isEmpty(item.getIdRef())) {
								if (!totalPerms.perms.containsKey(node.getId() + "::" + item.getId())) {
									irlist.add(item);
								}
							} else if (!totalPerms.perms.containsKey(item.getIdRef())) {
								irlist.add(item);
							}
						}
					}
					node.getItems().removeAll(irlist);
					node.setItemsUnpermitted(irlist);
				}

			} else {
				rlist.add(node);
				node.setUnpermitted(true);
			}

			if (!ObjectUtils.isEmpty(node.getMessage())) {
				node.setName(MessageUtils.get(node.getMessage(), node.getName(), null));
				node.setMessage(null);
			}

			if (!ObjectUtils.isEmpty(node.getItems())) {
				node.getItems().forEach(item -> {
					item.setName(MessageUtils.get(item.getMessage(), item.getName(), null));
					item.setMessage(null);
				});
			}

			// Never send these info to Client Side
			if (level <= 1) {
				node.setEmoldinoEnabled(null);
				node.setOemEnabled(null);
				node.setSupplierEnabled(null);
				node.setToolmakerEnabled(null);
				node.setPermitAll(null);
			} else {
				// When it is true, we should detect it for making the permission uneditable.
				if (!ValueUtils.toBoolean(node.getPermitAll(), false)) {
					node.setPermitAll(null);
				}
			}
		});
		nodes.removeAll(rlist);

		return hasActive[0];
	}

	private static boolean setActive(List<MenuTreeNode> items, String uri) {
		if (ObjectUtils.isEmpty(items) || ObjectUtils.isEmpty(uri)) {
			return false;
		}

		boolean active[] = { false };
		boolean hasActive[] = { false };
		items.forEach(item -> {
			active[0] = setActive(item.getChildren(), uri);
			hasActive[0] = hasActive[0] || active[0];
			if ("CATEGORY".equals(item.getType()) || "UTILITY".equals(item.getType())) {
				item.setActive(active[0]);
			} else if ("FUNCTION".equals(item.getType())) {
				if (!ObjectUtils.isEmpty(uri) && !ObjectUtils.isEmpty(item.getUrl()) && uri.equals(item.getUrl())) {
					item.setActive(true);
					hasActive[0] = true;
				} else {
					item.setActive(false);
				}
			}
		});
		return hasActive[0];
	}

	private static boolean initialized = false;

	private void init() {
		if (initialized) {
			return;
		}

		if (ThreadUtils.getProp("MenuService.initializing") != null) {
			return;
		}
		ThreadUtils.setProp("MenuService.initializing", true);

		SyncCtrlUtils.wrapWithLock("initPermissions", () -> {
			if (initialized) {
				return;
			}

			/**
			 * 1. Build Menus
			 */
			if (menuStructure == null) {
				try {
					menuStructure = (MenuStructure) ClassUtils.getClass(menuStructureClassName).getConstructor().newInstance();
				} catch (Exception e) {
					throw ValueUtils.toAe(e, null);
				}
			}
			List<MenuTreeNode> nodes = menuStructure.get();

			/**
			 * 2. Get and Put Temporal Menus (those are Under Development)
			 */
			{
				List<TempMenu> temps = new ArrayList<>();
				String pattern = "classpath:temp/menu/**/*.json";
				Resource[] resources;
				try {
					resources = new PathMatchingResourcePatternResolver().getResources(pattern);
					for (Resource resource : resources) {
						if (!resource.isReadable()) {
							continue;
						}
						String name = resource.getFilename();
						name = name.substring(0, name.lastIndexOf('.'));
						String str = IOUtils.toString(resource.getInputStream(), "UTF-8");
						TempMenu temp = ValueUtils.fromJsonStr(str, TempMenu.class);
						if (temp.isHidden()) {
							continue;
						}
						temp.setName(name);
						temps.add(temp);
					}
				} catch (Exception e) {
					log.warn(e.getMessage(), e);
				}
				temps.forEach(node -> putNode(nodes, node));
			}

			setConstants(nodes);

			initPermissions();
			initLicense();

			initialized = true;
		});
	}

	public void initPermissions() {
		boolean[] all = { false };
		boolean[] standardAll = { false };

		BeanUtils.get(RoleAccessControlService.class).initRoles();

		QPermission table = QPermission.permission;
		PermissionRepository repo = BeanUtils.get(PermissionRepository.class);
		if (!repo.exists(new BooleanBuilder())) {
			all[0] = true;
		} else if (!repo.exists(new BooleanBuilder().and(table.role.id.lt(900)))) {
			standardAll[0] = true;
		} else if (LAST_UPDATED_AT == null || LAST_UPDATED_AT.compareTo(DateUtils2.newInstant()) < 0) {
			return;
		}

		getList().getContent().forEach(menu -> {
			if (ObjectUtils.isEmpty(menu.getAuthorities())) {
				return;
			}
			if (!all[0] && !standardAll[0] && //
					(LAST_UPDATED_AT == null || ObjectUtils.isEmpty(menu.getUpdatedDate()) || DateUtils2.getInstant().compareTo(toInstant(menu)) > 0)) {
				return;
			}
			menu.getAuthorities().forEach(auth -> {
				RoleControl role = ThreadUtils.getProp("initPermissions.role::" + auth, () -> BeanUtils.get(RoleControlRepository.class).findByAuthority(auth));
				if (role == null) {
					return;
				}

				Permission perm = all[0] ? null
						: BeanUtils.get(PermissionRepository.class).findOne(//
								new BooleanBuilder()//
										.and(table.role.eq(role))//
										.and(table.resourceType.eq("MENU"))//
										.and(table.resourceId.eq(menu.getId())))//
								.orElse(null);

				initMenuPermission(role, menu, perm);
			});
		});
	}

	public void initMenuPermission(RoleControl role, MenuTreeNode menu, Permission perm) {
		LogicUtils.assertNotNull(role, "role");
		LogicUtils.assertNotNull(menu, "menu");

		if (perm == null) {
			perm = new Permission();
			perm.setRole(role);
			perm.setResourceType("MENU");
			perm.setResourceId(menu.getId());
		}

		if (!ObjectUtils.isEmpty(menu.getItems())) {
			Map<String, PermissionItem> items = new LinkedHashMap<>();
			if (!ObjectUtils.isEmpty(perm.getItems())) {
				perm.getItems().forEach(item -> items.put(item.getItemId(), item));
			}
			List<PermissionItem> list = new ArrayList<>();
			menu.getItems().forEach(item -> {
				if (!ObjectUtils.isEmpty(item.getIdRef())) {
					return;
				} else if (items.containsKey(item.getId())) {
					list.add(items.get(item.getId()));
				} else {
					PermissionItem permItem = new PermissionItem();
					permItem.setItemId(item.getId());
					permItem.setEnabled(true);
					list.add(permItem);
				}
			});
			if (list.isEmpty()) {
				perm.setItems(null);
			} else {
				perm.setItems(list);
			}
		}
		BeanUtils.get(PermissionRepository.class).save(perm);
	}

	@Value("${app.license.type:t000}")
	private String licenseType;

	private void initLicense() {
		try {
			String pattern = "classpath:license/" + licenseType + "/license.json";
			Resource resource = new PathMatchingResourcePatternResolver().getResource(pattern);
			if (!resource.isReadable()) {
				throw new SysException("LICENSE_NOT_FOUND", "lincenseType:" + licenseType + " is not found!!");
			}

			List<MenuTreeNode> menuTree = clone(ALL_MENU_TREE);

			String str = IOUtils.toString(resource.getInputStream(), "UTF-8");
			License license = ValueUtils.fromJsonStr(str, License.class);

			if (!ObjectUtils.isEmpty(license.getExcludeMenus())) {
				List<String> list = new ArrayList<>();
				license.getExcludeMenus().forEach(id -> {
					if (id.contains(":")) {
						id = id.substring(0, id.indexOf(":"));
					}
					list.add(id);
				});
				license.setExcludeMenus(list);
			}

			applyLicense(menuTree, license);
			setConstants(menuTree);

		} catch (AbstractException e) {
			throw e;
		} catch (Exception e) {
			throw new SysException("LICENSE_NOT_FOUND", "lincenseType:" + licenseType + " is not found!!", e);
		}
	}

	private static void applyLicense(List<MenuTreeNode> items, License license) {
		if (ObjectUtils.isEmpty(items) || ObjectUtils.isEmpty(license.getExcludeMenus())) {
			return;
		}

		List<MenuTreeNode> rlist = items.stream()//
				.filter(node -> ("FUNCTION".equals(node.getType()) || "SUBFUNCTION".equals(node.getType())) //
						&& !ObjectUtils.isEmpty(node.getId()) //
						&& license.getExcludeMenus().contains(node.getId()))//
				.collect(Collectors.toList());

		if (!ObjectUtils.isEmpty(rlist)) {
			items.removeAll(rlist);
		}

		items.forEach(node -> applyLicense(node.getChildren(), license));
	}

}
