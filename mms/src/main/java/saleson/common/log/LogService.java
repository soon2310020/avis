package saleson.common.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import saleson.common.enumeration.AuthAction;
import saleson.model.LogAuthentication;

@Service
public class LogService {

	@Autowired
	LogAuthenticationReposigory logAuthenticationReposigory;

	public void saveAuthenticationLog(String username, AuthAction action, String reason) {
		LogAuthentication log = new LogAuthentication(username, action, reason);
		logAuthenticationReposigory.save(log);
	}
}
