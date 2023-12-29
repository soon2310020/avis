package saleson.mms.mold;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/molds")
public class AdminMoldController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "admin/molds/list";
	}


	/**
	 * 등록
	 * @param model
	 * @return
	 */
	@GetMapping("/new")
	public String create(Model model) {

		return "admin/molds/form";
	}

	/**
	 * import tooling
	 * @return
	 */
	@GetMapping("/import")
	public String importExcel() {

		return "admin/molds/import";
	}

	/**
	 * 수정
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String edit(@PathVariable("id") Long id) {


		return "admin/molds/form";
	}

	/**
	 * 차트 상세 페이지 
	 * @param chartParam
	 * @return
	 */
	@GetMapping("/chart/{chart-param}")
	public String partDetailChart(@PathVariable("chart-param") String chartParam ) {
		return "admin/molds/chart";
	}

}
