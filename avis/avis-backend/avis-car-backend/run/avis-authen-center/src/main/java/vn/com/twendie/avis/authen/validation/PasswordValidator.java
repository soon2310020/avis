package vn.com.twendie.avis.authen.validation;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.api.rest.exception.BadRequestException;
import vn.com.twendie.avis.authen.model.payload.ChangePassRequest;
import vn.com.twendie.avis.locale.config.Translator;

@Component
public class PasswordValidator {

    public void validateChangePassRequest(ChangePassRequest request) {
        if (StringUtils.isBlank(request.getUsername())) {
            throw new BadRequestException("Username is blank").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("auth.require_username"));
        }
        if (StringUtils.isBlank(request.getOldPassword())) {
            throw new BadRequestException("Old password is blank").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("auth.require_old_pass"));
        }
        if (StringUtils.isBlank(request.getNewPassword())) {
            throw new BadRequestException("New password is blank").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("auth.require_new_pass"));
        }
        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new BadRequestException("New password equals old password").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("auth.new_pass_equals_old_pass"));
        }
        if (request.getNewPassword().length() < 8) {
            throw new BadRequestException("New password is too short").code(HttpStatus.BAD_REQUEST.value())
                    .displayMessage(Translator.toLocale("auth.too_short_new_pass"));
        }
//        if (request.getNewPassword().length() > 12) {
//            throw new BadRequestException("New password is too long").code(HttpStatus.BAD_REQUEST.value())
//                    .displayMessage(Translator.toLocale("auth.too_long_new_pass"));
//        }
    }

}
