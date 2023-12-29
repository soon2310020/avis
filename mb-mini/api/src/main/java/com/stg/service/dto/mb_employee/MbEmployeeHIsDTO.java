package com.stg.service.dto.mb_employee;

import com.stg.service.dto.user.UserEditingDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MbEmployeeHIsDTO {
    private Long id;

    private String fileName;

    private String errorContent;

    private String errorSysDetail;

    @Enumerated(EnumType.STRING)
    private EmployeeHisResult status;

    private int totalSuccess;

    private int totalFailed;

    private UserEditingDto createdBy;

    private String createdAt;
}
