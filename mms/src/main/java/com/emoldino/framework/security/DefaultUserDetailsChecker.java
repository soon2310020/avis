package com.emoldino.framework.security;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultUserDetailsChecker implements UserDetailsChecker {
	private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

	@Override
	public void check(UserDetails user) {
		if (!user.isAccountNonLocked()) {
			log.debug("User account is locked");
			throw new LockedException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked", "User account is locked"));
		} else if (!user.isEnabled()) {
			log.debug("User account is disabled");
			throw new DisabledException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled", "User is disabled"));
		} else if (!user.isAccountNonExpired()) {
			log.debug("User account is expired");
			throw new AccountExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired", "User account has expired"));
		} else if (!user.isCredentialsNonExpired()) {
			log.debug("User account credentials have expired");
			throw new CredentialsExpiredException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.credentialsExpired", "User credentials have expired"));
		}
	}

}
