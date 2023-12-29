package saleson.mms;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/common")
public class CommonController {

	@GetMapping("/dat-fam")
	public String datfam() {
		// TODO change index file to "/common/dat-fam/index" and remove this method
		return "admin/data-family/index";
	}

	@GetMapping("/dvc")
	public String dvc() {
		// TODO change index file to "/common/dvc/index" and remove this method
		return "admin/devices/index";
	}

	@GetMapping("/alr")
	public String alr() {
		// TODO change index file to "/common/alr/index" and remove this method
		return "admin/general-alerts/index";
	}

}
