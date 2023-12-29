package saleson.common.security;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.userdetails.*;

import com.emoldino.framework.util.*;
import com.fasterxml.jackson.annotation.*;

import lombok.*;
import saleson.common.enumeration.*;
import saleson.model.User;

@ToString
public class UserPrincipal implements UserDetails {
    private Long id;

    private String name;

    private String loginId;

    @JsonIgnore
    private String email;

    @JsonIgnore
    private String password;

	@JsonIgnore
	private boolean enabled;

	@JsonIgnore
    private boolean accountNonLocked;
    @JsonIgnore
    private int failedAttempt;

	private boolean ssoLogin;

    private String language;

	private Long companyId;
	private CompanyType companyType;
    private String companyName;

    private Instant pwdExpiredAt;

	private Collection<? extends GrantedAuthority> authorities;

	private List<Long> roleIds;

    public UserPrincipal(Long id, String name, String loginId, String email, String password,
						 Long companyId,
						 CompanyType companyType,
						 String companyName,
						 boolean enabled,
                         boolean accountNonLocked,
                         int failedAttempt,
                         String language,
                         Instant pwdExpiredAt,
						 Collection<? extends GrantedAuthority> authorities,
						 List<Long> roleIds, boolean ssoLogin) {
        this.id = id;
        this.name = name;
        this.loginId = loginId;
        this.email = email;
        this.password = password;
        this.companyId = companyId;
        this.companyType = companyType;
        this.companyName = companyName;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.failedAttempt = failedAttempt;
        this.authorities = authorities;
        this.roleIds = roleIds;

        this.ssoLogin = ssoLogin;
        this.language = language;
        this.pwdExpiredAt = pwdExpiredAt;
    }

	public static UserPrincipal create(User user) {
    	return create(user, false);
	}

    public static UserPrincipal create(User user, boolean ssoLogin) {
        List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getAuthority())
        ).collect(Collectors.toList());
        //add role for check rest role
        if (user.getRoleUserData() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRoleUserData().getCode()));
        }

		List<Long> roleIds = user.getRoles().stream().map(role ->
				role.getId()
		).collect(Collectors.toList());

        return new UserPrincipal(
                user.getId(),
                user.getName(),
                user.getLoginId(),
                user.getEmail(),
                user.getPassword(),
                user.getCompany() != null ? user.getCompany().getId() : null,
				user.getCompany() != null ? user.getCompany().getCompanyType() : null,
                user.getCompany() != null ? user.getCompany().getName() : null,
                user.isEnabled(),
                !user.isAccountLocked(),
                user.getFailedAttempt(),
                user.getLanguage(),
                user.getPwdExpiredAt(),
                authorities,
				roleIds,
				ssoLogin
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public String getPassword() {
        return password;
    }


	public List<Long> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(List<Long> roleIds) {
		this.roleIds = roleIds;
	}

	public Long getCompanyId() {
		return companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public CompanyType getCompanyType() {
		return companyType;
	}

	public void setCompanyType(CompanyType companyType) {
		this.companyType = companyType;
	}

	public String getCompanyName() { return companyName; }

	public void setCompanyName(String companyName) { this.companyName = companyName; }

	@Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public int getFailedAttempt() {
        return failedAttempt;
    }

    public Instant getPwdExpiredAt() {
        return pwdExpiredAt;
    }

	@Override
	public boolean isCredentialsNonExpired() {
		if (ConfigUtils.getAccountPasswordExpirationDurationDays() < 1) {
			return true;
		}else if (this.pwdExpiredAt == null || this.pwdExpiredAt.toEpochMilli() < System.currentTimeMillis()) {
			return false;
		}
		return true;
	}

    @Override
    public boolean isEnabled() {
        return enabled;
    }

	public boolean isSsoLogin() {
		return ssoLogin;
	}

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return language;
    }

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }
}
