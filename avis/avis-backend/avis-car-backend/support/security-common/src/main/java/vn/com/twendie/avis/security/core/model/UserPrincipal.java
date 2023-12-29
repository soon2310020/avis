package vn.com.twendie.avis.security.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.com.twendie.avis.data.model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * This class extends UserDetails which use to hold user info and authority
 *
 * @author trungnt9
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = -949955411012753606L;

    private User user;

    private Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(User user) {
        String role = user.getUserRole() != null ? user.getUserRole().getName() : "anonym";
        return UserPrincipal.builder()
                .user(user)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
    }

    public Long getId() {
        return user.getId();
    }

    public String getName() {
        return user.getName();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getAvatar() {
        return "";
    }

    public int getLoginTimes() {
        return user.getLoginTimes();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserPrincipal that = (UserPrincipal) o;
        if (user == null || that.getUser() == null) {
            return false;
        }
        return Objects.equals(user.getId(), that.getUser().getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }

}
