package com.emoldino.api.common.resource.composite.manpag;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetMenuTreeIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOptionsIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagGetOut;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginIn;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLoginOut;
import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLogoutIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import saleson.common.enumeration.ConfigCategory;

@Api(protocols = "http, https", tags = "Common / Main Page")
@RequestMapping("/api/common/man-pag")
public interface ManPagController {

	@ApiOperation("Login")
	@PostMapping("/login")
	ManPagLoginOut login(@RequestBody ManPagLoginIn input);

	@ApiOperation("Logout")
	@PutMapping("/logout")
	SuccessOut logout(@RequestBody ManPagLogoutIn input);

	@ApiOperation("Get Main Page Default(on-load) Data - Company Type, User Info, Language, Configs, Messages,...")
	@GetMapping
	ManPagGetOut get(ManPagGetIn input);

	@ApiOperation("Get Main Page Options by Config Category (Option Type)")
	@GetMapping("/options")
	Map<ConfigCategory, Object> getOptions(ManPagGetOptionsIn input);

	@ApiOperation("Get Menu Tree")
	@GetMapping("/menu-tree")
	ListOut<MenuTreeNode> getMenuTree(ManPagGetMenuTreeIn input);

	@ApiOperation("Get All Menus")
	@GetMapping("/menus")
	ListOut<MenuTreeNode> getMenus();

	@ApiOperation("Get All Permitted Menus")
	@GetMapping("/menus-permitted")
	ListOut<MenuTreeNode> getMenusPermitted();

}
