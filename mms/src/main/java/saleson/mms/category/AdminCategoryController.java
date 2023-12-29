package saleson.mms.category;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "admin/categories/list";
	}


	/**
	 * 등록
	 * @param model
	 * @return
	 */
	@GetMapping("/new")
	public String create(Model model) {

		return "admin/categories/form";
	}

	/**
	 * 수정
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String edit(@PathVariable("id") Long id) {


		return "admin/categories/form";
	}

}
