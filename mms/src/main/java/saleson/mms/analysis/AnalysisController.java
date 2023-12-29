package saleson.mms.analysis;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {

	@GetMapping("/pro-ana/chart")
	public String proanaChart() {
		return "analysis/pro-ana/chart";
	}
}
