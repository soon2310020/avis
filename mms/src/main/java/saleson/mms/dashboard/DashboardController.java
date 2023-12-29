package saleson.mms.dashboard;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/common/dsh")
public class DashboardController {
	@GetMapping
	public String list() {
		return "common/dsh/index";
	}
}
