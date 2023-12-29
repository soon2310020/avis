package saleson.mms.report;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportController {

	/**
	 * cm
	 * @return
	 */
	@GetMapping("/underutilization")
	public String underutilization() {
		return "front/reports/underutilization";
	}


	@GetMapping("/cm")
	public String cm() {
		return "front/reports/cm";
	}


	@GetMapping("/pm-execution-rate")
	public String pmExecutionRate() {
		return "front/reports/pm-execution-rate";
	}


	@GetMapping("/unauthorized-movement")
	public String unauthorizedMovement() {
		return "front/reports/unauthorized-movement";
	}


	@GetMapping("/cycle-time-deviation")
	public String cycleTimeEeviation() {
		return "front/reports/cycle-time-deviation";
	}


	@GetMapping("/extended-tool-life")
	public String extendedToolLife() {
		return "front/reports/extended-tool-life";
	}

	@GetMapping("/scrap-rate")
	public String scrapRate() {
		return "front/reports/scrap-rate";
	}

	@GetMapping("/tooling-benchmarking")
	public String toolingBenchMarking() {
		return "front/reports/landing";
	}

}
