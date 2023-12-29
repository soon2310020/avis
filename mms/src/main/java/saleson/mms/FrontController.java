package saleson.mms;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/front")
public class FrontController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping("/mold-locations")
	public String list() {
		return "front/mold-locations/list";
	}


	/**
	 * 예방정비 목록
	 * @return
	 */
	@GetMapping("/mold-maintenance")
	public String maintenance() {
		return "front/mold-maintenance/list";
	}

	/**
	 * 예방정비 목록
	 * @return
	 */
	@GetMapping("/mold-maintenance/history")
	public String maintenanceHistory() {
		return "front/mold-maintenance/history";
	}

	@GetMapping("/mold-maintenance/register-pm")
	public String registerPM() {
		return "front/mold-maintenance/register-pm";
	}

	/**
	 * Disconnected
	 * @return
	 */
	@GetMapping("/mold-disconnected")
	public String disconnected() {
		return "front/mold-disconnected/list";
	}


	/**
	 * Cycle Time
	 * @return
	 */
	@GetMapping("/mold-cycle-time")
	public String cyclTime() {
		return "front/mold-cycle-time/list";
	}


	/**
	 * Efficiency
	 * @return
	 */
	@GetMapping("/mold-efficiency")
	public String efficiency() {
		return "front/mold-efficiency/list";
	}


	/**
	 * Misconfigured
	 * @return
	 */
	@GetMapping("/mold-misconfigured")
	public String misconfigured() {
		return "front/mold-misconfigured/list";
	}

	/**
	 * Data submission
	 * @return
	 */
	@GetMapping("/mold-data-submission")
	public String submission() {
		return "front/mold-data-submission/list";
	}

	/**
	 *
	 * @return
	 */
	@GetMapping("/mold-refurbishment")
	public String refurbishment() {
		return "front/mold-refurbishment/list";
	}

	@GetMapping("/mold-detachment")
	public String detachment() {
		return "front/mold-detachment/list";
	}

	@GetMapping("/mold-downtime")
	public String downtime() {
		return "front/mold-downtime/list";
	}

	@GetMapping("alert-center")
	public String index(){
		return "front/alert-center/index";
	}
	
	@GetMapping("tabbed-overview")
	public String tabbedOverview(){
		return "front/tabbed-overview/index";
	}

	@GetMapping("oee-center")
	public String oeeIndex(){
		return "front/oee-center/index";
	}


}
