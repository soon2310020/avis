package saleson.mms.search;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/search")
public class SearchController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "front/search/list";
	}



}
