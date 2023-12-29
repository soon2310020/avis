package com.stg.service3rd.crm.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RmUserDetailDto {
    private String code;
    private String username;
    private String fullName;
    private String branchCode;
    private String branchName;
    private String phoneNumber;
    private String email;
    private Boolean active;
}
