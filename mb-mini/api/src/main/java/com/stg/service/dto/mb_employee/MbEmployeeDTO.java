package com.stg.service.dto.mb_employee;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MbEmployeeDTO implements IMbEmployeeDataDTO {
    private String identityCardNumber;
    private String mbId;
    private String employeeCode;
    private String employeeName;
    private String managingUnit;
    private String email;
    private Boolean status;
    private String createdAt;
    private String updatedAt;
}
