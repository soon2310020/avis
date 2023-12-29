package saleson.mms.part;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parts")
public class FrontPartController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "front/parts/list";
	}
}
