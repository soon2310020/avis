package vn.com.twendie.avis.api.model.payload;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyChangePasswordPayload {

    private Long otpCode;
    private String emailOrPhone;
    private String newPassword;
    private String confirmPassword;

}
