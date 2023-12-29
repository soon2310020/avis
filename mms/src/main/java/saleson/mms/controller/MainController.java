package saleson.mms.controller;

import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping
public class MainController {
	@GetMapping("/")
	public String dashboard(HttpServletRequest request, Locale locale, TimeZone tz, ZoneId zid, Model model) {
		return "common/dsh/index";
	}

	@GetMapping("/dashboard-old")
	public String index(HttpServletRequest request, Locale locale, TimeZone tz, ZoneId zid, Model model) {
		return "index";
	}

	@GetMapping("/login")
	public String login(Model model) {
		return "login";
	}

	@GetMapping("/fpassword")
	public String forgotPassword() {
		return "fpassword";
	}

	@GetMapping("/cpassword/{email:.+}")
	public ModelAndView changePassword(@PathVariable(value = "email") String email) {
		ModelAndView mav = new ModelAndView("resetpassword");
		mav.addObject("email", email);
		return mav;
	}

	@GetMapping("/register")
	public String register(Model model) {
		return "register";
	}

	@GetMapping("/create-account/{hash_code}")
	public String createaccount(@PathVariable(value = "hash_code") String hashCode) {
		return "create-account";
	}
}
