package saleson.mms.asset;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/asset")
public class AssetController {

	@GetMapping("/alr")
	public String alr() {
		// TODO change index file to "/asset/alr/index" and remove this method
		return "admin/asset-alerts/index";
	}
}
