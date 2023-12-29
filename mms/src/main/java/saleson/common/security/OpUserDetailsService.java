package saleson.common.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.framework.util.ThreadUtils;

import saleson.api.user.UserRepository;
import saleson.common.exception.ResourceNotFoundException;
import saleson.model.User;

@Service
public class OpUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly = true)
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		ThreadUtils.setProp("username", username);

		// Let people login with either username or email
		Optional<User> user = userRepository.findByLoginIdAndDeletedIsFalse(username);
		user.orElseThrow(() -> new UsernameNotFoundException("User not found with username or email : " + username));

		boolean ssoLogin = false;
//		if ("skc@emoldino.com".equals(user.get().getLoginId())) {
//			ssoLogin = true;
//		}
		UserDetails userDetails = UserPrincipal.create(user.get(), ssoLogin);
//		new DefaultUserDetailsChecker().check(userDetails);
		return userDetails;
	}

	@Transactional
	public UserDetails loadUserById(Long id) {
		User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
		return UserPrincipal.create(user);
	}
}
