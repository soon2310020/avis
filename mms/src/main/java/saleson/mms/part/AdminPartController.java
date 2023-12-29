package saleson.mms.part;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/parts")
public class AdminPartController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "admin/parts/list";
	}


	/**
	 * 등록
	 * @param model
	 * @return
	 */
	@GetMapping("/new")
	public String create(Model model) {

		return "admin/parts/form";
	}

	/**
	 * 수정
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String edit(@PathVariable("id") Long id) {


		return "admin/parts/form";
	}

	/**
	 * import part
	 * @return
	 */
	@GetMapping("/import")
	public String importExcel() {
		return "admin/parts/import";
	}

	/**
	 * 차트 상세 페이지 
	 * @param chartParam
	 * @return
	 */
	@GetMapping("/chart/{chart-param}")
	public String partDetailChart(@PathVariable("chart-param") String chartParam ) {
		return "admin/parts/chart";
	}

}
