package com.emoldino.api.common.resource.composite.manpag.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.Language;

@Data
public class ManPagGetOut {
	private List<ManPagAppVersion> versions;
	@Deprecated
	@ApiModelProperty(value = "Server Name which indicates the Main Client")
	private String server;
	@ApiModelProperty(value = "Login User's Company Type")
	private String type;
	@Deprecated
	@ApiModelProperty(value = "Whether this system use local timezone or not")
	private boolean localTimeZone;
	@ApiModelProperty(value = "Representing Language at UI")
	private Language language;
	@ApiModelProperty(value = "Login User's Information")
	private ManPagUser me;
	@ApiModelProperty(value = "Configured Options")
	private Map<ConfigCategory, Object> options;
	private Map<String, ManPagMenuPermission> menuPermissions;
	@ApiModelProperty(value = "Key-Value Set of Messages")
	private Map<String, String> messages;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ManPagAppVersion {
		private String version;
		private String releaseDate;
		private List<ManPagAppVersionItem> items;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ManPagAppVersionItem {
		private String description;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ManPagUser {
		@Deprecated
		private Long id;
		private String name;
		@Deprecated
		private String email;
		@Deprecated
		private boolean admin;
		private ManPagCompany company;
		@Deprecated
		private boolean notify;
		@Deprecated
		private boolean accessRequest;
		@Deprecated
		private List<ManPagRole> roles;
		@Deprecated
		private List<String> roleIds;
		@Deprecated
		private String roleUserData;
		@Deprecated
		private String contactDialingCountry;
		@Deprecated
		private String mobileDialingCountry;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ManPagCompany {
		@Deprecated
		private Long id;
		@Deprecated
		private String companyType;
		@Deprecated
		private String companyCode;
		private String name;
		@Deprecated
		private String address;
	}

	@Deprecated
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ManPagRole {
		@Deprecated
		private Long id;
		private String authority;
		private String name;
		@Deprecated
		private String description;
		@Deprecated
		private String roleType;
		@Deprecated
		private String roleTypeName;
		@Deprecated
		private String dashboardGroupType;
		@Deprecated
		private List<String> menuIds;
	}

	@Data
	public static class ManPagMenuPermission {
		private String name;
		private String url;
		private String icon;
		private Map<String, ManPagMenuItem> items = new LinkedHashMap<>();
//		private List<ManPagMenuItem> itemsUnpermitted = new ArrayList<>();
		private Map<String, ManPagMenuPermission> children = new LinkedHashMap<>();
	}

	@Data
	public static class ManPagMenuItem {
		private String name;
		private String type;
		private boolean permitted;
	}
}
