package com.stg.service3rd.hcm.dto.resp;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stg.service.dto.mb_employee.IMbEmployeeDataDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HcmEmployeeInfo implements IMbEmployeeDataDTO {
    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("employeeCode")
    private String employeeCode;

    private int status;

    @JsonProperty("flagStatus")
    private int flagStatus;

    @JsonProperty("empTypeCode")
    private int empTypeCode;

    @JsonProperty("personalId")
    private String personalId;

    @JsonProperty("isDecpline")
    private String isDiscipline;

    @JsonProperty("jobCode")
    private String jobCode;

    @JsonProperty("jobName")
    private String jobName;

    @JsonProperty("contractTypeId")
    private int contractTypeId;

    @JsonProperty("contractName")
    private String contractName;

    @JsonProperty("joinCompanyDate")
    private String joinCompanyDate;
}
