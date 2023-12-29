package saleson.mms.mold;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mold-locations")
public class FrontMoldLocationController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "front/mold-locations/list";
	}

}
