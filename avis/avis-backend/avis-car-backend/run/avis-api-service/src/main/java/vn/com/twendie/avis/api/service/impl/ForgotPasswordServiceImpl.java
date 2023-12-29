package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.model.enumtype.ForgotPasswordType;
import vn.com.twendie.avis.api.model.payload.VerifyChangePasswordPayload;
import vn.com.twendie.avis.api.model.response.ForgotPasswordResponse;
import vn.com.twendie.avis.api.repository.OtpCodeRepo;
import vn.com.twendie.avis.api.repository.UserRepo;
import vn.com.twendie.avis.api.repository.UserRoleRepo;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.ForgotPasswordService;
import vn.com.twendie.avis.api.util.PasswordUtil;
import vn.com.twendie.avis.data.enumtype.UserRoleEnum;
import vn.com.twendie.avis.data.model.OTPCode;
import vn.com.twendie.avis.data.model.User;
import vn.com.twendie.avis.locale.config.Translator;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private OtpCodeRepo otpCodeRepo;

    @Value("${forgot-password.otp.expiration-date}")
    private Long expirationDate;

    @Value("${forgot-password.reply-to}")
    private String replyToMail;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private UserRoleRepo userRoleRepo;

    private OTPCode buildOtpCode(User user){
        long otp = ThreadLocalRandom.current().nextInt(100000, 999999);
        return OTPCode.builder()
                .code(otp)
                .active(true)
                .expirationDate(new Date(new Date().getTime() + expirationDate))
                .user(user)
                .build();
    }

    @Override
    public ApiResponse<?> forgotPassword(String emailOrPhone, ForgotPasswordType type) {
        try {
            if (ForgotPasswordType.EMAIL.equals(type)) {
                User user = userRepo.findByEmailAndActiveIsTrueAndUserRoleNot(emailOrPhone, userRoleRepo.findById(UserRoleEnum.ADMIN.getId()).orElse(null));
                if (user == null || user.getEmail() == null) {
                    return ApiResponse.fail("username not found by email " + emailOrPhone);
                }
                otpCodeRepo.updateOtpActiveFalseByUser(user.getId());

                OTPCode otpCode = buildOtpCode(user);

                otpCodeRepo.save(otpCode);

                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "utf-8");
                String message = "Mã OTP của bạn là: " + otpCode.getCode();
                mimeMessage.setContent(message, "text/plain; charset=UTF-8");
                helper.setReplyTo(replyToMail);
                helper.setTo(user.getEmail());
                helper.setSubject("[AVIS] Yêu cầu lấy lại mật khẩu");

                javaMailSender.send(mimeMessage);
            }else{

            }
        }catch (Exception e){
            e.printStackTrace();
            return ApiResponse.error("error " + e.getMessage());
        }
        return ApiResponse.success(true);
    }

    @Override
    public ApiResponse<?> verifyAndChangePassword(VerifyChangePasswordPayload passwordPayload) {
        User user = userRepo.findByEmailOrMobileFullAndActiveIsTrueAndUserRoleNot(passwordPayload.getEmailOrPhone(),
                passwordPayload.getEmailOrPhone(),
                userRoleRepo.findById(UserRoleEnum.ADMIN.getId()).orElse(null));
        if(user == null){
            throw new BadRequestException("User not found").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(String.format(Translator.toLocale("forgot_password.username_not_found_by_email_or_phone"), passwordPayload.getEmailOrPhone()));
        }

        if(StringUtils.isEmpty(passwordPayload.getNewPassword()) || !passwordPayload.getNewPassword().equals(passwordPayload.getConfirmPassword())){
            throw new BadRequestException("Password not matcher").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("forgot_password.password_not_matcher"));
        }

        if (passwordPayload.getNewPassword().length() < 8) {
            throw new BadRequestException("New password is too short").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("auth.too_short_new_pass"));
        }

        OTPCode otpCode = otpCodeRepo.findByCodeAndUserAndActiveIsTrue(passwordPayload.getOtpCode(), user);

        if(otpCode == null || (otpCode.getActive() != null &&  !otpCode.getActive())){
            throw new BadRequestException("Opt incorrect").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("forgot_password.otp_incorrect"));
        }

        if(otpCode.getExpirationDate() != null && otpCode.getExpirationDate().getTime() - new Date().getTime() < 0){
            throw new BadRequestException("Opt expiration date").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("forgot_password.otp_expiration_date"));
        }

        user.setPassword(passwordUtil.encryptPassword(passwordPayload.getNewPassword()));
        user.setLoginFailedTimes(0);
        if (user.getLoginTimes().equals(0)) {
            user.setLoginTimes(1);
        }
        userRepo.save(user);

        otpCode.setActive(false);
        otpCodeRepo.save(otpCode);

        return ApiResponse.success("success");
    }

    @Override
    public ApiResponse<?> getEmailAndPhoneByUser(String username) {
        User user = userRepo.findByUsername(username);
        if(user == null){
            throw new BadRequestException("User not found")
                    .code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("user.error.not_found"));
        }
        String email = user.getEmail();
        if(email == null && user.getMemberCustomer() != null){
            email = user.getMemberCustomer().getEmail();
        }

        String countryCode = user.getCountryCode();
        if(countryCode == null && user.getMemberCustomer() != null){
            countryCode = user.getMemberCustomer().getCountryCode();
        }

        String mobile = user.getMobile();

        if(mobile == null && user.getMemberCustomer() != null){
            mobile = user.getMemberCustomer().getMobile();
        }

        return ApiResponse.success(ForgotPasswordResponse.builder()
                .email(email)
                .countryCode(countryCode)
                .mobile(mobile).build()
        );
    }
}

