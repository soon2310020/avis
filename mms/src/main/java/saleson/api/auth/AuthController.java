package saleson.api.auth;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.common.resource.composite.manpag.dto.ManPagLogoutIn;
import com.emoldino.api.common.resource.composite.manpag.service.login.ManPagLoginService;
import com.emoldino.framework.util.BeanUtils;

import saleson.api.user.UserRepository;
import saleson.api.user.payload.LoginRequest;
import saleson.api.user.payload.UserPayload;
import saleson.common.config.Const;
import saleson.common.payload.JwtAuthenticationResponse;
import saleson.common.security.JwtTokenProvider;
import saleson.common.util.SecurityUtils;
import saleson.model.User;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

//	@Autowired
//	RoleRepository roleRepository;

//	@Autowired
//	PasswordEncoder passwordEncoder;

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	AuthService authService;
	@Autowired
	AccessTokenRepository accessTokenRepository;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwt = tokenProvider.generateToken(authentication);
		if (SecurityUtils.getUserId() != null) {
			User user = userRepository.findById(SecurityUtils.getUserId()).orElse(null);
			authService.genToken(user.getId(), loginRequest.getDeviceToken(), loginRequest.getUdid(), loginRequest.getDeviceType());
		}
		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
	}

//	@PostMapping("/signup")
//	public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
//		if (userRepository.existsLoginId(signUpRequest.getUsername())) {
//			return new ResponseEntity(new ApiResponse(false, "Username is already taken!"), HttpStatus.BAD_REQUEST);
//		}
//
//		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
//			return new ResponseEntity(new ApiResponse(false, "Email Address already in use!"), HttpStatus.BAD_REQUEST);
//		}
//
//		// Creating user's account
//		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
//
//		user.setPassword(passwordEncoder.encode(user.getPassword()));
//
//		Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new AppException("User Role not set."));
//
//		user.setRoles(Collections.singleton(userRole));
//
//		User result = userRepository.save(user);
//
//		URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/users/{username}").buildAndExpand(result.getUsername()).toUri();
//
//		return ResponseEntity.created(location).body(new ApiResponse(true, "User registered successfully"));
//	}

	@PostMapping("/login")
	public ResponseEntity login(@RequestBody UserPayload payload) {
		return authService.login(payload);
	}

	@PostMapping(value = "/logout")
	public ResponseEntity<String> logout(HttpServletRequest httpServletRequest) {
		String token = httpServletRequest.getHeader(Const.HEADER_TOKEN);

		try {
			BeanUtils.get(ManPagLoginService.class).logout(ManPagLogoutIn.builder().sessionId(token).build());
		} catch (Exception e) {
		}

		try {
			if (StringUtils.isEmpty(token)) {
				return ResponseEntity.badRequest().body(Const.ERROR_CODE.EMPTY_TOKEN);
			}
			accessTokenRepository.deleteAllByAccessToken(token);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Const.ERROR_CODE.INVALID_TOKEN);
		}
		return ResponseEntity.ok(Const.SUCCESS);
	}

}
