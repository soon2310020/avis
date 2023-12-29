package saleson.common.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.security.DefaultUserDetailsChecker;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import lombok.extern.slf4j.Slf4j;
import saleson.api.user.UserRepository;
import saleson.api.user.UserService;
import saleson.model.QUser;
import saleson.model.User;

@Slf4j
@Service
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService {

	private UserDetailsChecker userDetailsChecker = new DefaultUserDetailsChecker();

	@Autowired
	private UserRepository userRepository;

	@Transactional
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		log.debug("[SSO-CREDENTIAL] loadUserBySAML(SAMLCredential credential) -----------");

		String username = credential.getNameID().getValue();
		ThreadUtils.setProp("username", username);

		// Azure or ADFS
		String remoteEntityId = credential.getRemoteEntityID();

		boolean dyson = "dyson".equalsIgnoreCase(ConfigUtils.getServerName());
		boolean pg = "p&g".equalsIgnoreCase(ConfigUtils.getServerName());
		boolean nestle = "ns0407".equalsIgnoreCase(ConfigUtils.getServerName());
		boolean electrolux = "eb0702".equalsIgnoreCase(ConfigUtils.getServerName());
		boolean idpAwared = false;
		boolean azure = false;
		boolean adfs = false;
		if (remoteEntityId != null) {
			if (remoteEntityId.startsWith("https://sts.windows.net")) { // https://sts.windows.net/b6e8236b-ceb2-401d-9169-2917d0b07d48/
				idpAwared = true;
				azure = true;
			} else if (remoteEntityId.indexOf("/adfs/services/trust") > -1) {
				idpAwared = true;
				adfs = true;
			}
		}
		log.debug("[SSO-CREDENTIAL] credential: {}", credential);
		log.debug("[SSO-CREDENTIAL] localEntityID: {}", credential.getLocalEntityID());
		log.debug("[SSO-CREDENTIAL] remoteEntityId: {}", remoteEntityId);
		log.debug("[SSO-CREDENTIAL] username: {}", username);
		log.debug("[SSO-CREDENTIAL] azure?: {}", azure);
		log.debug("[SSO-CREDENTIAL] adfs?: {}", adfs);

		String email = null;
		String name = null;
		String hhmmss = DateUtils2.getString("HHmmss", Zone.GMT);
		if (idpAwared) {
			String prefix = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/";
			email = credential.getAttributeAsString(prefix + "emailaddress");
			String givenname = credential.getAttributeAsString(prefix + "givenname");
			String surname = credential.getAttributeAsString(prefix + "surname");
			String displayName = credential.getAttributeAsString(prefix + "name");

			if (azure) {
				String prefixMs = "http://schemas.microsoft.com/identity/claims/";
				displayName = credential.getAttributeAsString(prefixMs + "displayname");
			}

			name = givenname != null && !givenname.isEmpty() ? givenname : "";
			name = surname != null && !surname.isEmpty() ? name.trim() + " " + surname : "";

			if (name.trim().isEmpty() && displayName != null && !displayName.isEmpty()) {
				name = displayName;
			}

			log.debug("[SSO-CREDENTIAL] email: {}", email);
			log.debug("[SSO-CREDENTIAL] givenname: {}", givenname);
			log.debug("[SSO-CREDENTIAL] surname: {}", surname);
			log.debug("[SSO-CREDENTIAL] displayName: {}", displayName);
			log.debug("[SSO-CREDENTIAL] name: {}", name);
		}

		if (ObjectUtils.isEmpty(name)) {
			name = "SSO-" + username;
		}

		User ssoUser = userRepository.findBySsoIdAndDeletedIsFalse(username).orElse(null);
		User user = null;
		boolean accessRequest = false;
		if (ObjectUtils.isEmpty(email)) {
			// Dyson
			if (dyson) {
				email = username + "@dyson.com";
			}
			// Nestle
			else if (nestle) {
				email = username + "@nestle.com";
			}
			//Electrolux
			else if (electrolux) {
				email = username + "@electrolux.com";
			}
			// P&G
			else if (pg) {
				user = ssoUser;
				if (ssoUser != null && !ObjectUtils.isEmpty(ssoUser.getEmail())) {
					email = ssoUser.getEmail();
				} else {
					email = hhmmss + "@pg.com";
					while (userRepository.exists(QUser.user.loginId.eq(email).or(QUser.user.email.eq(email)))) {
						hhmmss = (Integer.parseInt(hhmmss) + 1) + "";
						email = hhmmss + "@pg.com";
					}
					SyncCtrlUtils.lock("SSO." + hhmmss);
					while (userRepository.exists(QUser.user.loginId.eq(email).or(QUser.user.email.eq(email)))) {
						hhmmss = (Integer.parseInt(hhmmss) + 1) + "";
						email = hhmmss + "@pg.com";
					}
				}
			}
			// Others
			else {
				email = username;
			}
		}
		if (user == null) {
			user = userRepository.findByLoginIdOrEmailAndDeletedIsFalse(email, email).orElse(null);
		}

		if (ssoUser != null) {
			// Combine user -> SSO user
			if (user != null && !ValueUtils.equals(user.getId(), ssoUser.getId())) {
				user.setLoginId(hhmmss + "-" + email);
				user.setEmail(hhmmss + "-" + email);
				userRepository.save(user);
			}

			ssoUser.setLoginId(email);
			ssoUser.setEmail(email);
			ssoUser.setName(name);
			if (ssoUser != null && !ssoUser.isEnabled() && ValueUtils.toBoolean(ssoUser.getRequested(), false)) {
				ssoUser.setRequested(true);
				accessRequest = true;
			}
			user = userRepository.save(ssoUser);

		} else if (user != null) {
			user.setSsoId(username);
			if (user != null && !user.isEnabled() && ValueUtils.toBoolean(user.getRequested(), false)) {
				user.setRequested(true);
				accessRequest = true;
			}
			user = userRepository.save(user);

		} else {
			String _email = email;
			String _name = name;
			user = TranUtils.doNewTran(() -> {
				User newUser = new User();

				newUser.setSsoId(username);
				newUser.setPassword("<PASSWORD>");
				newUser.setLoginId(_email);
				newUser.setEmail(_email);
				newUser.setName(_name);

				newUser.setCompany(null);

				newUser.setEnabled(false);
				newUser.setAdmin(false);
				newUser.setRequested(true);

				newUser = userRepository.save(newUser);
				BeanUtils.get(UserService.class).updateDefaultAlertConfig(newUser);
				return newUser;
			});
			accessRequest = true;
		}

		if (accessRequest) {
			BeanUtils.get(UserService.class).sendAccessRequest(user);
		}

		UserPrincipal userPrincipal = UserPrincipal.create(user, true);
		log.debug("[SSO-CREDENTIAL] userPrincipal: {}", userPrincipal);
		userDetailsChecker.check(userPrincipal);
		return userPrincipal;
	}
}
