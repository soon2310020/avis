package com.stg.service.dto.user;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.stg.service.dto.credential.CredentialDto;
import com.stg.utils.DateUtil;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class UserDto {

    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String PATTERN_ROLES = ROLE_SUPER_ADMIN + "|" + ROLE_ADMIN;

    private Long id;

    @NotEmpty
    @Size(max = 100)
    private String firstName;

    @NotEmpty
    @Size(max = 100)
    private String lastName;

    @Email
    private String email;

    private Boolean nonLocked;

    @NotEmpty
    @Pattern(regexp = PATTERN_ROLES)
    private String role;

    private Boolean enabled;

    private String creationTime;

    private List<String> features;

//    @NotEmpty
    private Set<CredentialDto> credentials = new HashSet<>();

    public <T> T accept(UserDtoVisitor<T> v) {
        return v.visit(this);
    }

    public void setCreationTime(LocalDateTime dateTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_YMD_HMS_01, dateTime);
    }
}
