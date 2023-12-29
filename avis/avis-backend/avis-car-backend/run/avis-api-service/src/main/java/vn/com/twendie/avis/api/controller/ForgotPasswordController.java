package vn.com.twendie.avis.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.com.twendie.avis.api.model.enumtype.ForgotPasswordType;
import vn.com.twendie.avis.api.model.payload.VerifyChangePasswordPayload;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.ForgotPasswordService;
import vn.com.twendie.avis.security.annotation.RequirePermission;

@RestController
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping
    public ApiResponse<?> forgotPassword(@RequestParam("emailOrPhone") String emailOrPhone,
                                         @RequestParam("type") ForgotPasswordType type){
        return forgotPasswordService.forgotPassword(emailOrPhone, type);
    }

    @PostMapping("/verify-change-password")
    public ApiResponse<?> verifyChangePassword(@RequestBody VerifyChangePasswordPayload verifyChangePasswordPayload){
        return forgotPasswordService.verifyAndChangePassword(verifyChangePasswordPayload);
    }

    @GetMapping("/get-email-phone-by-username")
    public ApiResponse<?> getEmailAndPhoneByUser(@RequestParam("username") String username){
        return forgotPasswordService.getEmailAndPhoneByUser(username);
    }

}
