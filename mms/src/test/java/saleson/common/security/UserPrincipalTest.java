package saleson.common.security;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import saleson.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class UserPrincipalTest {

	@Test
	void userPrincipalTest() {
		String emailAddress = "skc@emoldino.com";

		User user = new User();
		user.setId(1L);
		user.setEmail(emailAddress);
		user.setLoginId(emailAddress);
		user.setName("Shin");
		UserPrincipal userPrincipal = UserPrincipal.create(user, true);

		log.debug("user: {}", userPrincipal.getUsername());

		assertThat(userPrincipal.getUsername()).isEqualTo(emailAddress);
	}

}