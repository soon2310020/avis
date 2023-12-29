package com.stg.controller;

import com.stg.utils.Endpoints;
import com.stg.service.UserService;
import com.stg.service.dto.email.ForgotPasswordDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Tag(name = "Sign-in APIs")
public class SignInController {

    private final UserService userService;

    @PostMapping(Endpoints.URL_FORGOT_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    public void forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        userService.forgotPassword(forgotPasswordDto);
    }

}
