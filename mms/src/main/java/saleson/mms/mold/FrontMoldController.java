package saleson.mms.mold;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/molds")
public class FrontMoldController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "front/molds/list";
	}



	/**
	 * 위치변경
	 * @return
	 */
	@GetMapping("/locations")
	public String locations() {
		return "front/molds/locations";
	}
}
