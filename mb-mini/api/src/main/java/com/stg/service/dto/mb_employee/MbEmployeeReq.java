package com.stg.service.dto.mb_employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MbEmployeeReq {
    private String employeeName;
    private String managingUnit;
    private String email;
    private String identityCardNumber;
    private String employeeCode;
    private Boolean status;
}
