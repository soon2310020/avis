package vn.com.twendie.avis.authen.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.ValidObjectUtil;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.exception.UnauthorizedException;
import vn.com.twendie.avis.authen.model.payload.ChangePassRequest;
import vn.com.twendie.avis.authen.service.PasswordService;
import vn.com.twendie.avis.authen.validation.PasswordValidator;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.security.jdbc.repository.UserRepository;

import javax.transaction.Transactional;

@Service
public class PasswordServiceImpl implements PasswordService {

	private final UserRepository userRepository;

	private final PasswordEncoder passwordEncoder;

	private final PasswordValidator passwordValidator;

	public PasswordServiceImpl(UserRepository userRepository,
							   PasswordEncoder passwordEncoder,
							   PasswordValidator passwordValidator) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordValidator = passwordValidator;
	}

	@Transactional
	public void changePassword(ChangePassRequest request) {
		ValidObjectUtil.trimReflectAndNormalizeString(request);
		passwordValidator.validateChangePassRequest(request);
		User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(request.getUsername())
				.orElseThrow(() -> new NotFoundException().code(HttpStatus.NOT_FOUND.value())
						.displayMessage(Translator.toLocale("auth.user_not_exists")));

		if (!user.getActive()){
			throw new UnauthorizedException("User is inactive!!!")
					.code(HttpStatus.UNAUTHORIZED.value())
					.displayMessage(Translator.toLocale("auth.user_is_inactive"));
		}

		if (passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
			String newPass = passwordEncoder.encode(request.getNewPassword());
			user.setPassword(newPass);
			if (user.getLoginTimes().equals(0)) {
				user.setLoginTimes(1);
			}
			userRepository.save(user);
		} else {
			throw new NotFoundException().displayMessage(Translator.toLocale("auth.incorrect_old_pass"));
		}
	}

	@Override
	public void resetPassword(String username) {

	}

}
