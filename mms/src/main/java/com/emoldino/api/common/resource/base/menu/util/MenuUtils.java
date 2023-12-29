package com.emoldino.api.common.resource.base.menu.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.menu.service.MenuService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ThreadUtils;

public class MenuUtils {

	public static boolean isPermitted(String id) {
		Set<String> permitted = ThreadUtils.getProp("MenuUtils.permittedSet", () -> {
			Set<String> set = new HashSet<>();
			BeanUtils.get(MenuService.class).getListPermitted().getContent().forEach(menu -> {
				if (!ObjectUtils.isEmpty(menu.getId())) {
					set.add(menu.getId());
				}
				if (!ObjectUtils.isEmpty(menu.getChildren())) {
					menu.getChildren().forEach(item -> set.add(item.getId()));
				}
			});
			return set;
		});
		return permitted.contains(id);
	}

}
