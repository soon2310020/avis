package com.stg.service.impl.auth;

import com.stg.entity.credential.Credential;
import com.stg.entity.user.User;
import com.stg.errors.CredentialHasExpiredException;
import com.stg.errors.UserInactiveException;
import com.stg.errors.UserLockedException;
import com.stg.errors.UserNotFoundException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.BooleanUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class AuthenticationUtils {

    private static final String FAILED_CREDENTIAL_VERIFICATION_PREFIX = "FCV:";

    static UserNotFoundException createUserNotFoundException(String identifier) {
        throw new UserNotFoundException("Không tìm thấy Người dùng có email là " + identifier);
    }

    static UserInactiveException createUserInactiveException(String identifier) {
        throw new UserInactiveException("Tài khoản của bạn chưa được kích hoạt. Vui lòng liên hệ với Super Admin để kích hoạt tài khoản");
    }

    static void verifyUserIsNotLocked(User user) {
        if (BooleanUtils.isFalse(user.getNonLocked())) {
            throw new UserLockedException("Account locked, please contact your administrator or contact us under Help & Feedback");
        }
    }

    static void verifyCredentialIsNonExpired(Credential credential) {
        if (BooleanUtils.isFalse(credential.getNonExpired())) {
            throw new CredentialHasExpiredException("The credential has expired for user with ID=" + credential.getUser().getId());
        }
    }

    static String createFailedCredentialVerificationKey(User user) {
        Long userId = user.getId();

        return FAILED_CREDENTIAL_VERIFICATION_PREFIX + "_" + userId;
    }
}
