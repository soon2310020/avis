package com.stg.service.dto.user;

import lombok.Data;

@Data
public class ChangePassDTO {
    String oldPassword;
    String newPassword;
}
