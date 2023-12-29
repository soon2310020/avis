package saleson.mms.aihub;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/aihub")
public class AihubController {

	/**
	 * cm
	 * @return
	 */
	@GetMapping("/dashboard")
	public String dashboard() {
		return "front/aihub/dashboard";
	}


	@GetMapping("/quality")
	public String quality() {
		return "front/aihub/quality";
	}


	@GetMapping("/end-of-life-cycle")
	public String endOfLifeCycle() {
		return "front/aihub/end-of-life-cycle";
	}

}
