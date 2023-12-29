package com.emoldino.api.common.resource.composite.manpag.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dozer.DozerBeanMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.client.util.ClientUtils;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroup;
import com.emoldino.api.common.resource.base.file.repository.filegroup.FileGroupRepository;
import com.emoldino.api.common.resource.base.menu.dto.MenuItem;
import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.base.menu.service.MenuService;
import com.emoldino.api.common.resource.base.version.dto.AppVersionGetPageIn;
import com.emoldino.api.common.resource.base.version.service.app.AppVersionService;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetIn;
import com.emoldino.api.common.resource.composite.cfgstp.dto.CfgStpGetOut;
import com.emoldino.api.common.resource.composite.cfgstp.service.CfgStpService;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOptionsIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut.ManPagAppVersion;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut.ManPagMenuItem;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut.ManPagMenuPermission;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut.ManPagUser;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.resource.ResourceHandler;
import saleson.api.user.UserService;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.Language;
import saleson.common.util.SecurityUtils;
import saleson.model.User;

@Service
@Transactional
public class ManPagService {
	private static DozerBeanMapper MAPPER = new DozerBeanMapper();

	@SuppressWarnings("unchecked")
	public ManPagGetOut get(ManPagGetIn input) {
		ManPagGetOut output = new ManPagGetOut();

		// Versions
		{
			AppVersionGetPageIn reqin = new AppVersionGetPageIn();
			reqin.setAppCode("MMS");
			reqin.setEnabled(true);
			List<ManPagAppVersion> versions = BeanUtils.get(AppVersionService.class)//
					.get(reqin, PageRequest.of(0, 10))//
					.map(version -> ValueUtils.map(version, ManPagAppVersion.class))//
					.getContent();
			output.setVersions(versions);
		}

		// TODO Remove /api/users/server
		String server = ValueUtils.toString(ConfigUtils.getServerName(), "none");
		output.setServer(server);

		// /api/users/type
		{
			CompanyType companyType = SecurityUtils.getCompanyType();
			if (CompanyType.IN_HOUSE.equals(companyType)) {
				output.setType("OEM");
			} else if (CompanyType.TOOL_MAKER.equals(companyType)) {
				output.setType("TOOL_MAKER");
			} else {
				output.setType("SUPPLIER");
			}
		}

		// /api/users/my
		{
			User user = BeanUtils.get(UserService.class).findById(SecurityUtils.getUserId());
			ManPagUser me = MAPPER.map(user, ManPagUser.class);
			output.setMe(me);
			try {
				output.setLanguage(user == null || ObjectUtils.isEmpty(user.getLanguage()) ? Language.EN : Language.valueOf(user.getLanguage()));
			} catch (Exception e) {
				output.setLanguage(Language.EN);
			}
		}

		// /api/config/time-language
		{
			output.setLocalTimeZone(true);
		}

		// /api/config/...
		{
			List<ConfigCategory> configCategory = Arrays.asList(ConfigCategory.values());
			output.setOptions(getOptions(configCategory));

			Map<String, Object> options;
			if (output.getOptions().containsKey(ConfigCategory.CLIENT)) {
				options = (Map<String, Object>) output.getOptions().get(ConfigCategory.CLIENT);
			} else {
				options = new LinkedHashMap<String, Object>();
				output.getOptions().put(ConfigCategory.CLIENT, options);
			}

			setIfNotExists(options, "logoUrl", getLogoUrl(server));

			Map<String, String> worldMapTypes = new HashMap<>();
			worldMapTypes.put("zebra", "amchart");
			worldMapTypes.put("irobot", "amchart");
			worldMapTypes.put("ic0822", "amchart");
			worldMapTypes.put("dyson", "amchart");
			worldMapTypes.put("ns0407", "amchart");
			worldMapTypes.put("abb-china", "amchart");

			String worldMapType = worldMapTypes.getOrDefault(server, "google");
			setIfNotExists(options, "worldMap.type", worldMapType);

			setIfNotExists(options, "search.tabs.digital.enabled", false);
			setIfNotExists(options, "search.tabs.digital.active", !"dyson".equals(server));
			setIfNotExists(options, "search.tabs.nonDigital.enabled", false);
			setIfNotExists(options, "search.tabs.nonDigital.active", false);

			setIfNotExists(options, "moldList.tabs.digital.enabled", false);
			setIfNotExists(options, "moldList.tabs.digital.active", !"dyson".equals(server));
			setIfNotExists(options, "moldList.tabs.nonDigital.enabled", false);
			setIfNotExists(options, "moldList.tabs.nonDigital.active", false);
			setIfNotExists(options, "moldList.cols.dataSubmission.show", "dyson".equals(server));
			setIfNotExists(options, "moldList.features.toolingTabEnabled", !"dyson".equals(server));

			setIfNotExists(options, "moldDetail.toolingComplexityEnabled", "dyson".equals(server));
			setIfNotExists(options, "moldDetail.lifeYearsEnabled", !"dyson".equals(server));
			setIfNotExists(options, "moldDetail.usageTypeNameEnabled", !"dyson".equals(server));
			setIfNotExists(options, "moldDetail.statusEnabled", !"dyson".equals(server));
			setIfNotExists(options, "moldDetail.uptimeTargetRequired", "dyson".equals(server));
			setIfNotExists(options, "moldDetail.disapprovedReasonRequired", "dyson".equals(server));

			setIfNotExists(options, "chartMold.datePicker.hourlyEnabled", !"abb-pilot".equals(server));

			setIfNotExists(options, "moldMaintenance.cols.preventCycle.show", "abb-china".equals(server));
			setIfNotExists(options, "moldMaintenance.cols.pmCheckpoint.show", !"abb-china".equals(server));
			setIfNotExists(options, "moldMaintenance.cols.utilNextPm.show", "abb-china".equals(server));
			setIfNotExists(options, "moldMaintenance.cols.lastShotCheckpoint.show", !"abb-china".equals(server));
			setIfNotExists(options, "moldMaintenance.cols.dataSubmission.show", "dyson".equals(server));

			setIfNotExists(options, "requestMaintenance.numberOfBackupsRequired ", "dyson".equals(server));
			setIfNotExists(options, "requestMaintenance.maintenanceDocRequired ", "dyson".equals(server));
			setIfNotExists(options, "requestRefurbishment.workingCavityRequired ", "dyson".equals(server));

			setIfNotExists(options, "registerPm.maintenanceDocRequired", "dyson".equals(server));

			setIfNotExists(options, "dataCompletion.cols.dataSubmission.show", "dyson".equals(server));

			setIfNotExists(options, "dataRegistration.cols.dataSubmission.show", "dyson".equals(server));
			setIfNotExists(options, "userList.loginAuditTrailEnabled", "ns0407".equals(server));
			setIfNotExists(options, "oee.partProduced.colorIndicatorHidden", "mm0427".equals(server));
			setIfNotExists(options, "oee.partProduced.usePerformanceColorConfigForEHO", "mm0427".equals(server));
		}

		// menu-permissions
		{
			Map<String, ManPagMenuPermission> menuPermissions = new LinkedHashMap<>();
			List<MenuTreeNode> nodes = BeanUtils.get(MenuService.class).getListPermitted().getContent();
			putMenuPermissions(nodes, menuPermissions);
			output.setMenuPermissions(menuPermissions);
		}

		// /api/resources/getAllMessagesOfCurrentUser
		{
			Map<String, String> messages = BeanUtils.get(ResourceHandler.class).getAllMessagesOfCurrentUser();
			// TODO Improve TOL_RPT
			FileGroup fileGroup = BeanUtils.get(FileGroupRepository.class)//
					.findFirstByFileGroupTypeAndFileGroupCodeOrderByVersionNoDesc(FileGroupType.DATA_EXPORT_TEMPLATE, "TOL_RPT")//
					.orElse(null);
			if (fileGroup != null && !ObjectUtils.isEmpty(fileGroup.getFileGroupName())) {
				messages.put("DATA_EXPORT.TOL_RPT", fileGroup.getFileGroupName());
			} else if (!messages.containsKey("DATA_EXPORT.TOL_RPT")) {
				messages.put("DATA_EXPORT.TOL_RPT", "Tooling Report");
			}
			output.setMessages(messages);
		}

		return output;
	}

//	private final static List<String> REMOVE_K = Arrays.asList(//
//			"createdAt", //
//			"createdDate", //
//			"createdDateTime", //
//			"updatedAt", //
//			"updatedDate", //
//			"updatedDateTime"//
//	);

	private static void setIfNotExists(Map<String, Object> options, String key, Object value) {
		LogicUtils.assertNotNull(options, "options");
		LogicUtils.assertNotEmpty(key, "key");
		LogicUtils.assertNotNull(value, "value");

		Map<String, Object> map = options;
		String[] ks = StringUtils.tokenizeToStringArray(key, ".");
		int len = ks.length;
		int i = 0;
		for (String k : ks) {
			if (++i == len) {
				if (!map.containsKey(k)) {
					map.put(k, value);
				}
				break;
			}

			map = getMap(map, k);
		}
	}

//	private static void setShowTabIfNotExists(Map<String, Object> options, String menu, String name, boolean enabled, boolean active) {
//		LogicUtils.assertNotNull(options, "options");
//		LogicUtils.assertNotNull(menu, "menu");
//		LogicUtils.assertNotNull(name, "name");
//
//		Map<String, Object> map = getMap(getMap(options, menu), "showTab");
//	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> getMap(Map<String, Object> map, String k) {
		if (map == null || ObjectUtils.isEmpty(k)) {
			return null;
		}
		if (map.containsKey(k)) {
			Object v = map.get(k);
			if (!(v instanceof Map)) {
				return null;
			}
			return (Map<String, Object>) v;
		} else {
			Map<String, Object> v = new LinkedHashMap<>();
			map.put(k, v);
			return v;
		}
	}

	private static String getLogoUrl(String server) {
		LogicUtils.assertNotEmpty(server, "server");

		String logoUrl = null;
		if (ClientUtils.exists(server)) {
			logoUrl = ClientUtils.get(server).getLogoPath();
		}
		if (ObjectUtils.isEmpty(logoUrl)) {
			logoUrl = "/images/common/logo/emoldino-logo.svg";
		}
		return logoUrl;
	}

//	private static void setDefaultColumnIfNotExists(Map<String, Object> options, String menu, String field, boolean show) {
//
//	}

	public Map<ConfigCategory, Object> getOptions(ManPagGetOptionsIn input) {
		return getOptions(input.getConfigCategory());
	}

	private Map<ConfigCategory, Object> getOptions(List<ConfigCategory> configCategory) {
		if (ObjectUtils.isEmpty(configCategory)) {
			return Collections.emptyMap();
		}

		// TODO Replace with OptionService after this service be improved
		CfgStpGetIn reqin = new CfgStpGetIn();
		reqin.setConfigCategory(configCategory);
		CfgStpGetOut reqout = BeanUtils.get(CfgStpService.class).get(reqin);
		return reqout.getOptions();
	}

	private void putMenuPermissions(List<MenuTreeNode> from, Map<String, ManPagMenuPermission> to) {
		if (ObjectUtils.isEmpty(from)) {
			return;
		}

		Map<String, MenuTreeNode> permitted = new LinkedHashMap<>();
		putPermitted(from, permitted);

		for (MenuTreeNode node : from) {
			ManPagMenuPermission perm = toMenuPermission(node, permitted);
			if (perm == null) {
				continue;
			}
			to.put(node.getId(), perm);
		}
	}

	private void putPermitted(List<MenuTreeNode> from, Map<String, MenuTreeNode> permitted) {
		if (ObjectUtils.isEmpty(from)) {
			return;
		}
		for (MenuTreeNode node : from) {
			if (!ObjectUtils.isEmpty(node.getId())) {
				permitted.put(node.getId(), node);
			}
			putPermitted(node.getChildren(), permitted);
		}
	}

	private ManPagMenuPermission toMenuPermission(MenuTreeNode node, Map<String, MenuTreeNode> permitted) {
		if (ObjectUtils.isEmpty(node.getId())) {
			return null;
		}

		ManPagMenuPermission perm = new ManPagMenuPermission();
		perm.setName(node.getName());
		perm.setUrl(node.getUrl());
		perm.setIcon(node.getIcon());

		if (!ObjectUtils.isEmpty(node.getItems())) {
			for (MenuItem item : node.getItems()) {
				if (ObjectUtils.isEmpty(item.getId()) || //
						(!ObjectUtils.isEmpty(item.getIdRef()) && !permitted.containsKey(item.getIdRef()))) {
					continue;
				}
				ManPagMenuItem menuItem = ValueUtils.map(item, ManPagMenuItem.class);
				menuItem.setPermitted(true);
				perm.getItems().put(item.getId(), menuItem);
			}
		}
		if (!ObjectUtils.isEmpty(node.getItemsUnpermitted())) {
			for (MenuItem item : node.getItemsUnpermitted()) {
				if (ObjectUtils.isEmpty(item.getId())) {
					continue;
				}
				ManPagMenuItem menuItem = ValueUtils.map(item, ManPagMenuItem.class);
				menuItem.setPermitted(false);
				perm.getItems().put(item.getId(), menuItem);
			}
		}
		if (!ObjectUtils.isEmpty(node.getChildren())) {
			for (MenuTreeNode child : node.getChildren()) {
				ManPagMenuPermission subperm = toMenuPermission(child, permitted);
				if (subperm == null) {
					continue;
				}
				perm.getChildren().put(child.getId(), subperm);
			}
		}

		return perm;
	}

}
