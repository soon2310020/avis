package saleson.mms.profile;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

	/**
	 * 목록
	 * @return
	 */
	@GetMapping("/account")
	public String account() {
//		return "front/profile/account";
		return "admin/profile/list";
	}
//	@GetMapping("/setting")
//	public String setting(){
//		return "admin/profile/list";
//	}



}
