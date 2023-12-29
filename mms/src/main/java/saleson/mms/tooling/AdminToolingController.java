package saleson.mms.tooling;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/tooling")
public class AdminToolingController {

	@GetMapping
	public String list() {
		return "admin/tooling/list";
	}

	@GetMapping("/new")
	public String create(Model model) {
		return "admin/tooling/form";
	}

	@GetMapping("/import")
	public String importExcel() {
		return "admin/tooling/import";
	}

	@GetMapping("/{id}")
	public String edit(@PathVariable("id") Long id) {
		return "admin/tooling/form";
	}

	@GetMapping("/chart/{chart-param}")
	public String partDetailChart(@PathVariable("chart-param") String chartParam) {
		return "admin/tooling/chart";
	}

}
