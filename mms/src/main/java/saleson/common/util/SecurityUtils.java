package saleson.common.util;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.emoldino.framework.util.ThreadUtils;

import saleson.api.user.UserService;
import saleson.common.enumeration.CompanyType;
import saleson.common.security.UserPrincipal;
import saleson.model.User;

public class SecurityUtils {

	private static final String AUTH = "SecurityUtils.authentication";

	public static Authentication getAuthentication() {
		return ThreadUtils.getProp(AUTH, () -> SecurityContextHolder.getContext().getAuthentication());
	}

	public static void setAuthentication(Authentication auth) {
		ThreadUtils.setProp(AUTH, auth);
	}

	public static UserPrincipal getUserPrincipal() {
		Authentication authentication = getAuthentication();
		if (authentication == null) {
			return null;
		}
		if (authentication.getPrincipal() instanceof UserPrincipal) {
			return (UserPrincipal) authentication.getPrincipal();
		}
		return null;
	}

	public static Long getUserId() {
		return isLogin() ? getUserPrincipal().getId() : null;
	}

	public static String getName() {
		return isLogin() ? getUserPrincipal().getName() : null;
	}

	public static List<Long> getRoleIds() {
		return isLogin() ? getUserPrincipal().getRoleIds() : null;
	}

	public static Long getCompanyId() {
		return isLogin() ? getUserPrincipal().getCompanyId() : null;
	}

	public static CompanyType getCompanyType() {
		return isLogin() ? getUserPrincipal().getCompanyType() : null;
	}

	public static String getCompanyName() {
		return isLogin() ? getUserPrincipal().getCompanyName() : null;
	}

	public static boolean isLogin() {
		return getUserPrincipal() != null;
	}

	public static boolean isGuest() {
		return !isLogin();
	}

	private static Long SUPPORT_USER_ID;
	private static boolean SUPPORT_USER_ID_LOADED = false;

	public static Long getSupportUserId() {
		if (SUPPORT_USER_ID_LOADED) {
			return SUPPORT_USER_ID;
		}
		return SyncCtrlUtils.wrap("SecurityUtils.getSupportUserId", () -> {
			if (SUPPORT_USER_ID_LOADED) {
				return SUPPORT_USER_ID;
			}
			try {
				User user = BeanUtils.get(UserService.class).findByLoginId("support@emoldino.com");
				SUPPORT_USER_ID = user == null ? null : user.getId();
			} catch (Exception e) {
				// Do Nothing
			}
			SUPPORT_USER_ID_LOADED = true;
			return SUPPORT_USER_ID;
		});
	}

	/**
	 * SSO LOGIN?
	 * @return
	 */
	public static boolean isSsoLogin() {
		return isLogin() && getUserPrincipal().isSsoLogin();
	}

	/**
	 * @return
	 * @deprecated Use isOemAdmin instead
	 */
	@Deprecated
	public static boolean isAdmin() {
		return isInHouseAdmin();
	}

	public static boolean isInHouseAdmin() {
		return isInHouse() && hasRole("ROLE_ADMIN");
	}

	public static boolean isInHouse() {
		return isLogin() && getCompanyType() == CompanyType.IN_HOUSE;
	}

	public static boolean isEmoldino() {
		CompanyType companyType = validateCompanyType();
		return isLogin() && companyType.equals(CompanyType.IN_HOUSE) && getUserPrincipal().getCompanyName().toLowerCase().startsWith("emoldino");
	}

	public static boolean isOem() {
		CompanyType companyType = validateCompanyType();
		return isLogin() && companyType.equals(CompanyType.IN_HOUSE) && !getUserPrincipal().getCompanyName().toLowerCase().startsWith("emoldino");
	}

	private static CompanyType validateCompanyType() {
		CompanyType companyType = getCompanyType();
		return companyType == null || ObjectUtils.isEmpty(getUserPrincipal().getCompanyName()) ? null : companyType;
	}

	public static boolean isSupplier() {
		return isLogin() && getCompanyType() == CompanyType.SUPPLIER;
	}

	public static boolean isToolmaker() {
		return isLogin() && getCompanyType() == CompanyType.TOOL_MAKER;
	}

	/*public static boolean isUser() {
		return hasRole("ROLE_USER");
	}
	
	public static boolean isManager() {
		return hasRole("ROLE_MANAGER");
	}
	
	public static boolean isSupervisor() {
		return hasRole("ROLE_SUPERVISOR");
	}*/

	public static List<String> getAuthorities() {
		List<String> result = new ArrayList<>();
		if (isLogin()) {
			List<GrantedAuthority> list = (List<GrantedAuthority>) getAuthentication().getAuthorities();
			for (GrantedAuthority grantedAuthority : list) {
				result.add(grantedAuthority.getAuthority());
			}
		}
		return result;
	}

	public static List<String> getMoldAuthorities() {
		List<String> result = new ArrayList<>();
		for (String authority : getAuthorities()) {
			if (!authority.startsWith("ROLE_GROUP_")) {
				continue;
			}
			result.add(authority);
		}
		return result;
	}

	public static boolean hasRole(String role) {
		if ("ANONYMOUS".equals(role))
			return true;

		List<String> authorities = getAuthorities();

		if (authorities.size() == 0) {
			if ("GUEST".equals(role)) {
				return true;
			} else {
				return false;
			}
		}

		long matchedCount = authorities.stream().filter(a -> a.equals(role)).count();

		if (matchedCount == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static void resetPwdExpiredAt(User user) {
		if (user == null) {
			return;
		}
		int days = ConfigUtils.getAccountPasswordExpirationDurationDays();
		user.setPwdExpiredAt(days < 1 ? null : Instant.now().plus(Duration.ofDays(days)));
	}
}
