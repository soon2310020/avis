package com.emoldino.api.common.resource.base.menu.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.framework.util.SyncCtrlUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@ToString(exclude = "parent")
@EqualsAndHashCode(exclude = "parent")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class MenuTreeNode {
	private String id;
	private String path;
	private String name;
	private String message;
	private String type;
	@JsonIgnore
	private String updatedDate;
	private Boolean beta;

	private String url;
	private String icon;
	private int width;
	private int height;

	private Boolean active;

	private boolean enabled;
	private Boolean emoldinoEnabled;
	private Boolean oemEnabled;
	private Boolean supplierEnabled;
	private Boolean toolmakerEnabled;

	private Boolean permitAll;
	private boolean showsEvenNoChildrenPermitted;
	@JsonIgnore
	private boolean unpermitted;
	@JsonIgnore
	private List<String> authorities;

	private List<MenuItem> items;
	private List<MenuItem> itemsUnpermitted;
	@JsonIgnore
	@Setter(AccessLevel.PRIVATE)
	private MenuTreeNode parent;
	private List<MenuTreeNode> children;

	public MenuTreeNode(String name, String message, MenuTreeNode... children) {
		this(name, message, "CATEGORY", null, children);
	}

	public MenuTreeNode(String name, String message, String type, String icon, MenuTreeNode... children) {
		this.name = name;
		this.message = message;
		this.type = type;
		this.icon = icon;
		this.enabled = true;
		addChildren(type, children);
	}

	public MenuTreeNode(String id, String name, String message, String updatedDate, //
			String url, String icon, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, "FUNCTION", updatedDate, (Boolean) null, //
				url, icon, 0, 0, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, false, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String updatedDate, //
			String url, String icon, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, boolean showsEvenNoChildrenPermitted, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, "FUNCTION", updatedDate, (Boolean) null, //
				url, icon, 0, 0, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, showsEvenNoChildrenPermitted, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String updatedDate, boolean beta, //
			String url, String icon, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, "FUNCTION", updatedDate, beta, //
				url, icon, 0, 0, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, false, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String updatedDate, //
			int width, int height, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, "FUNCTION", updatedDate, (Boolean) null, null, //
				null, width, height, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, false, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String updatedDate, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, "FUNCTION", updatedDate, (Boolean) null, //
				null, null, 0, 0, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, false, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String type, String updatedDate, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, type, updatedDate, (Boolean) null, //
				null, null, 0, 0, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, false, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String type, String updatedDate, //
			String url, String icon, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this(id, name, message, type, updatedDate, (Boolean) null, //
				url, icon, 0, 0, //
				enabled, oemEnabled, supplierEnabled, toolmakerEnabled, //
				permitAll, false, auth, items, children);
	}

	public MenuTreeNode(String id, String name, String message, String type, String updatedDate, Boolean beta, //
			String url, String icon, int width, int height, //
			boolean enabled, Boolean oemEnabled, Boolean supplierEnabled, Boolean toolmakerEnabled, //
			Boolean permitAll, boolean showsEvenNoChildrenPermitted, List<String> auth, List<MenuItem> items, MenuTreeNode... children) {
		this.id = id;
		this.name = name;
		this.message = message;
		this.type = type;
		this.updatedDate = updatedDate;
		this.beta = beta;

		this.url = url;
		this.icon = icon;
		this.width = width;
		this.height = height;

		this.enabled = enabled;
		this.emoldinoEnabled = true;
		this.oemEnabled = oemEnabled;
		this.supplierEnabled = supplierEnabled;
		this.toolmakerEnabled = toolmakerEnabled;
		this.permitAll = permitAll;
		this.showsEvenNoChildrenPermitted = showsEvenNoChildrenPermitted;
		this.authorities = toAuthorities(auth);
		this.items = items;
		addChildren(type, children);
	}

	public void setChildren(List<MenuTreeNode> children) {
		if (!ObjectUtils.isEmpty(this.children)) {
			this.children.forEach(child -> child.setParent(null));
		}
		this.children = new ArrayList<>();
		if (!ObjectUtils.isEmpty(children)) {
			addChildren(null, children.toArray(new MenuTreeNode[children.size()]));
		}
	}

	private void addChildren(String parentType, MenuTreeNode... children) {
		if (ObjectUtils.isEmpty(children)) {
			return;
		}
		for (MenuTreeNode item : children) {
			if ("FUNCTION".equals(parentType) && "FUNCTION".equals(item.getType())) {
				item.setType("SUBFUNCTION");
			}
			addChild(item);
		}
	}

	public MenuTreeNode addChild(MenuTreeNode item) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(item);
		item.setParent(this);
		return item;
	}

	private static final Map<String, List<String>> AUTH = new ConcurrentHashMap<>();

	private static List<String> toAuthorities(List<String> auth) {
		if (ObjectUtils.isEmpty(auth)) {
			return Collections.emptyList();
		}
		List<String> list = SyncCtrlUtils.wrap("MenuTreeNode.AUTH", AUTH, StringUtils.collectionToCommaDelimitedString(auth), () -> auth);
		return list;
	}

//	public void setEnabled(boolean enabled) {
//		this.enabled = enabled;
//		setEmoldinoEnabled(emoldinoEnabled);
//	}
//
//	public void setAuthorities(List<String> authorities) {
//		this.authorities = toAuthorities(authorities);
//	}
}
