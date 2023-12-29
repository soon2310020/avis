package vn.com.twendie.avis.authen.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author trungnt9
 *
 */
public class AuthenticationProcessingException extends AuthenticationException {
	private static final long serialVersionUID = 1L;

	public AuthenticationProcessingException(String msg) {
		super(msg);
	}

}
