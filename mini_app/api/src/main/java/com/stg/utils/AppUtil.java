package com.stg.utils;

import com.stg.config.security.auth.UserCredential;
import com.stg.errors.AuthenticationException;
import com.stg.errors.dto.ErrorDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/***/
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppUtil {
    public static String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        throw new AuthenticationException("auth-current-user", new ErrorDto(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"));
    }
    
    public static UserCredential getCurrentCredentials() {
        Object credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof UserCredential) {
            return (UserCredential) credentials;
        }
        return null;
    }

    public static String getCurrentRmCode() {
        UserCredential userCredential = getCurrentCredentials();
        if (userCredential != null) {
            return userCredential.getRmCode();
        }
        return null;
    }

    public static String getCurrentIcCode() {
        UserCredential userCredential = getCurrentCredentials();
        if (userCredential != null) {
            return userCredential.getIcCode();
        }
        return null;
    }

    public static String getCurrentBranchCode() {
        UserCredential userCredential = getCurrentCredentials();
        if (userCredential != null) {
            return userCredential.getBranchCode();
        }
        return null;
    }
}
