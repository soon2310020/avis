package saleson.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import saleson.api.user.UserRepository;
import saleson.model.User;

import java.util.Optional;

@RestController
@RequestMapping("/saml/skc")
public class SsoController {
	@Autowired
	private UserRepository userRepository;

	@RequestMapping
	public String index() {
		Optional<User> optionalUser = userRepository.findById(85L);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			user.setLoginId("test@tes.com");
			user.setEmail("test@tes.com");
			user.setName("홍홍홍");

			userRepository.save(user);
		}

		return "ok";
	}
}
