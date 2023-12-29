package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.enumtype.ForgotPasswordType;
import vn.com.twendie.avis.api.model.payload.VerifyChangePasswordPayload;
import vn.com.twendie.avis.api.rest.model.ApiResponse;

public interface ForgotPasswordService {

    ApiResponse<?> forgotPassword(String emailOrPhone, ForgotPasswordType type);

    ApiResponse<?> verifyAndChangePassword(VerifyChangePasswordPayload passwordPayload);

    ApiResponse<?> getEmailAndPhoneByUser(String username);
}
