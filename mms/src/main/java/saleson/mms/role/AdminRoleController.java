package saleson.mms.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import saleson.api.menu.MenuService;
import saleson.common.enumeration.mapper.Code;
import saleson.common.enumeration.mapper.CodeMapper;


@Controller
@RequestMapping("/admin/roles")
public class AdminRoleController {

	@Autowired
	MenuService menuService;

	@Autowired
	CodeMapper codeMapper;


	@RequestMapping
	public String list() {
		return "admin/roles/list";
	}

	@RequestMapping("/new")
	public String create(Model model) {
		model.addAttribute("menu", menuService.findById(0L));
		model.addAttribute("roleTypes", codeMapper.get(Code.ROLE_TYPE));
		return "admin/roles/form";
	}


	/**
	 * 수정
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String edit(@PathVariable("id") Long id, Model model) {

		model.addAttribute("menu", menuService.findById(0L));
		model.addAttribute("roleTypes", codeMapper.get(Code.ROLE_TYPE));
		return "admin/roles/form";
	}

}
