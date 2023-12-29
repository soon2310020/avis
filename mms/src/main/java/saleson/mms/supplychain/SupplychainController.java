package saleson.mms.supplychain;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/supplychain")
public class SupplychainController {

	@GetMapping("/dem-cpl/input")
	public String demcplInput() {
		// TODO
		return "supplychain/dem-cpl/dem-cpl-input/index";
	}

}