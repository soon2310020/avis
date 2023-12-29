package com.stg.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RmInfoDto {
    private String code;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String icCode;
    private String branchCode;
    private String branchName;
}
