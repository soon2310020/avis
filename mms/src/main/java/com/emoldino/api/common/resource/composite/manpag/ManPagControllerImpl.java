package com.emoldino.api.common.resource.composite.manpag;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.base.menu.service.MenuService;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetMenuTreeIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOptionsIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginOut;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLogoutIn;
import com.emoldino.api.common.resource.composite.manpag.service.ManPagService;
import com.emoldino.api.common.resource.composite.manpag.service.login.ManPagLoginService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;

import saleson.common.enumeration.ConfigCategory;

@RestController
public class ManPagControllerImpl implements ManPagController {
	@Autowired
	private ManPagService service;

	@Override
	public ManPagLoginOut login(ManPagLoginIn input) {
		return BeanUtils.get(ManPagLoginService.class).login(input);
	}

	@Override
	public SuccessOut logout(ManPagLogoutIn input) {
		BeanUtils.get(ManPagLoginService.class).logout(input);
		return SuccessOut.getDefault();
	}

	@Override
	public ManPagGetOut get(ManPagGetIn input) {
		return service.get(input);
	}

	@Override
	public Map<ConfigCategory, Object> getOptions(ManPagGetOptionsIn input) {
		return service.getOptions(input);
	}

	@Override
	public ListOut<MenuTreeNode> getMenuTree(ManPagGetMenuTreeIn input) {
		return BeanUtils.get(MenuService.class).getTree();
	}

	@Override
	public ListOut<MenuTreeNode> getMenus() {
		return BeanUtils.get(MenuService.class).getList();
	}

	@Override
	public ListOut<MenuTreeNode> getMenusPermitted() {
		return BeanUtils.get(MenuService.class).getListPermitted();
	}

}
