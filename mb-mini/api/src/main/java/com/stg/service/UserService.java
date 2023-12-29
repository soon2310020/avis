package com.stg.service;

import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.credential.CredentialDto;
import com.stg.service.dto.credential.PasswordGenerationResponseDto;
import com.stg.service.dto.email.ForgotPasswordDto;
import com.stg.service.dto.user.*;

import java.util.List;

public interface UserService {

    UserDto createUser(Long userId, UserDto userDto);

    UserDto editUser(Long superUserId, UserEditingDto userDto, Long userId);

    void addCredential(Long userId, CredentialDto socialCredentialDto);

    PasswordGenerationResponseDto generateRandomPassword(String email);

    UserDto detailMe(Long userId);

    List<UserDto> listUsersScroll(int count, Long lastId);

    PaginationResponse<UserDto> listUsers(Long userId, int offset, int size, String query);

    void updateFeatureUsers(Long userId, UserFeatureDto userFeatureDto);

    void forgotPassword(ForgotPasswordDto forgotPasswordDto);

    void revoke(String token, Long userId);

    void changePassword(Long userId, ChangePassDTO changePassDTO);

    List<FeatureDto> getAllFeatures(Long userId);

    UserDto detailUser(Long superAdminId, Long userId);

    void createEmailAndPasswordCredentialReset(ChangePassRequestDto changePassRequestDto);

    void confirmEmailAndPasswordCredentialReset(ChangePassConfirmDto passConfirmDto);

}
