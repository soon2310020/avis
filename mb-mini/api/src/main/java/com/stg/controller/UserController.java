package com.stg.controller;

import com.stg.utils.Endpoints;
import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.service.UserService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.user.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.stg.utils.Endpoints.URL_RESET_PASSWORD_CONFIRM;
import static com.stg.utils.Endpoints.URL_RESET_PASSWORD_REQUEST;

@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "User Apis")
class UserController {

    private final UserService userService;

    @PostMapping(Endpoints.URL_SIGN_IN)
    @ResponseStatus(HttpStatus.OK)
    public UserDto signingNewUser(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody UserDto userDto) {
        return userService.createUser(Long.valueOf(user.getUserId()), userDto);
    }

    @PutMapping(Endpoints.URL_USER_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto updateUser(@AuthenticationPrincipal CustomUserDetails user,
                              @Valid @RequestBody UserEditingDto editingDto,
                              @PathVariable(value = "id") Long userId) {
        return userService.editUser(Long.valueOf(user.getUserId()), editingDto, userId);
    }

    @GetMapping(Endpoints.URL_USER_DETAIL)
    @ResponseStatus(HttpStatus.OK)
    public UserDto detailUser(@AuthenticationPrincipal CustomUserDetails user,
                              @PathVariable(value = "id") Long userId) {
        return userService.detailUser(Long.valueOf(user.getUserId()), userId);
    }

    @GetMapping(Endpoints.URL_USER_ME)
    @ResponseStatus(HttpStatus.OK)
    public UserDto detailMe(@AuthenticationPrincipal CustomUserDetails user) {
        return userService.detailMe(Long.valueOf(user.getUserId()));
    }

    @GetMapping(Endpoints.URL_USER_LIST)
    @ResponseStatus(HttpStatus.OK)
    public PaginationResponse<UserDto> listUsers(@AuthenticationPrincipal CustomUserDetails user,
                                                 @RequestParam(name = "offset", required = false, defaultValue = "1") int offset,
                                                 @RequestParam(name = "size", required = false, defaultValue = "10") int size,
                                                 @RequestParam(name = "query", required = false) String query) {
        return userService.listUsers(Long.valueOf(user.getUserId()), offset, size, query);
    }

    @PutMapping(Endpoints.URL_FEATURE)
    @ResponseStatus(HttpStatus.OK)
    public void updateFeatureUsers(@AuthenticationPrincipal CustomUserDetails superUser,
                                   @Valid @RequestBody UserFeatureDto userFeatureDto) {
        userService.updateFeatureUsers(Long.valueOf(superUser.getUserId()), userFeatureDto);
    }

    @GetMapping(Endpoints.URL_FEATURE)
    @ResponseStatus(HttpStatus.OK)
    public List<FeatureDto> getFeatureAdmin(@AuthenticationPrincipal CustomUserDetails superUser) {
        return userService.getAllFeatures(Long.valueOf(superUser.getUserId()));
    }

    @PostMapping(Endpoints.URL_USER_REVOKE)
    public void revokeToken(@AuthenticationPrincipal CustomUserDetails user,
                            @RequestParam("token") String token) {
        userService.revoke(token, Long.valueOf(user.getUserId()));
    }

    @PutMapping(path = Endpoints.URL_CHANGE_PASSWORD)
    @ResponseStatus(HttpStatus.OK)
    public void changePassword(@AuthenticationPrincipal CustomUserDetails user,
                               @Valid @RequestBody ChangePassDTO changePassDTO) {
        userService.changePassword(Long.valueOf(user.getUserId()), changePassDTO);
    }


    @PostMapping(URL_RESET_PASSWORD_REQUEST)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createEmailAndPasswordCredentialReset(@RequestBody ChangePassRequestDto changePassRequestDto) {
        userService.createEmailAndPasswordCredentialReset(changePassRequestDto);
    }

    @PostMapping(URL_RESET_PASSWORD_CONFIRM)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmailAndPasswordCredentialReset(@RequestBody ChangePassConfirmDto passConfirmDto) {
        userService.confirmEmailAndPasswordCredentialReset(passConfirmDto);
    }

}
