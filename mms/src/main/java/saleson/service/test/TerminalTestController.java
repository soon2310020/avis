package saleson.service.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import saleson.api.preset.PresetRepository;
import saleson.model.Preset;
import saleson.model.Version;
import saleson.service.version.VersionRepository;

import javax.servlet.http.HttpSession;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("/terminal-test")
public class TerminalTestController {

	public static final String TEST_USER_ROLE = "TEST_USER_ROLE";
	public static final String PREFIX = "terminal-test/";
	public static final String REDIRECT_LOGIN = "redirect:/terminal-test/login";

	@Autowired
	PresetRepository presetRepository;

	@Autowired
	VersionRepository versionRepository;

	@GetMapping
	public String index(HttpSession session) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		return PREFIX + "preset";
	}

	private boolean isLogin(HttpSession session) {
		return session.getAttribute(TEST_USER_ROLE) != null;
	}

	@GetMapping("/login")
	public String login() {
		return PREFIX + "login";
	}

	@PostMapping("/login")
	public String login(@RequestParam("username") String username,
						@RequestParam("password") String password,
						HttpSession session,
						Model model) {
		final String testUsername = "test@emoldino.com";
		final String testPassword = "1111";

		if (testUsername.equals(username) && testPassword.equals(password)) {
			session.setAttribute(TEST_USER_ROLE, "ROLE_TESTER");
			return "redirect:/terminal-test";
		}
		model.addAttribute("error", "3");
		return PREFIX + "login";
	}



	@GetMapping("/preset")
	public String preset(HttpSession session, Model model) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		List<Preset> presetList = presetRepository.findAll(Sort.by("id"));
		model.addAttribute("presetList", presetList);
		return PREFIX + "preset";
	}

	@DeleteMapping("/preset")
	public String preset(HttpSession session, @RequestParam("id") int id) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		presetRepository.deleteById(id);
		return "redirect:/" + PREFIX + "preset";
	}

	@GetMapping("/preset/new")
	public String createPreset(HttpSession session, Model model) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		model.addAttribute("preset", new Preset());
		return PREFIX + "preset-form";
	}

	@PostMapping("/preset/new")
	public String createPreset(HttpSession session, Preset preset) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}

		presetRepository.save(preset);

		return "redirect:/" + PREFIX + "preset";
	}


	@GetMapping("/upurl")
	public String upurl(HttpSession session, Model model) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		List<Version> upurlList = versionRepository.findAll(Sort.by("id"));
		model.addAttribute("upurlList", upurlList);
		return PREFIX + "upurl";
	}

	@DeleteMapping("/upurl")
	public String upurl(HttpSession session, @RequestParam("id") int id) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		versionRepository.deleteById(id);
		return "redirect:/" + PREFIX + "upurl";
	}

	@GetMapping("/upurl/new")
	public String createUpurl(HttpSession session, Model model) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}
		model.addAttribute("upurl", new Version());
		return PREFIX + "upurl-form";
	}

	@PostMapping("/upurl/new")
	public String createUpurl(HttpSession session, Version version) {
		if (!isLogin(session)) {
			return REDIRECT_LOGIN;
		}

		versionRepository.save(version);

		return "redirect:/" + PREFIX + "upurl";
	}
}
