package saleson.mms.production;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/production")
public class ProductionController {

	@GetMapping("/alr")
	public String alr() {
		// TODO change index file to "/production/alr/index" and remove this method
		return "admin/production-alerts/index";
	}

}
