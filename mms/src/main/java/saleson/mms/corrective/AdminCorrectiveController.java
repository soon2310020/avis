package saleson.mms.corrective;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/corrective")
public class AdminCorrectiveController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "admin/corrective/list";
	}


	/**
	 * 등록
	 * @param model
	 * @return
	 */
	@GetMapping("/new")
	public String create(Model model) {

		return "admin/corrective/form";
	}

	/**
	 * 수정
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String edit(@PathVariable("id") Long id) {


		return "admin/corrective/form";
	}

}
