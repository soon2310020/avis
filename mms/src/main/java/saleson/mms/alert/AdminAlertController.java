package saleson.mms.alert;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/alerts")
public class AdminAlertController {

	/**
	 * 연결 끊긴 터미널 목록
	 * @return
	 */
	@GetMapping("/terminal")
	public String terminal() {
		return "admin/alerts/terminal";
	}


	/**
	 * 금형과 연결이 안된 채 동작하거나 등록이 안된채 동작하는 카운터 목록
	 * @param model
	 * @return
	 */
	@GetMapping("/counter")
	public String counter(Model model) {

		return "admin/alerts/counter";
	}

	/**
	 * 카운터가 인스톨 되었는데 정상적으로 인지가 안되는 금형 리스트
	 * @param id
	 * @return
	 */
	@GetMapping("/tooling")
	public String tooling() {
		return "admin/alerts/tooling";
	}

}
