package com.stg.service3rd.crm.dto.resp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class RmUserDetailResp {
    private String id;
    private String code;
    private String hrsCode;
    private String username;
    private String fullName;
    private String branch;
    private String branchLevel1;
    private String branchName;
    private String phoneNumber;
    private String email;
    private String phoneDevice;
    private String gender;
    private Boolean unLock;
    private String statusWork;
    private List<String> blockRmManager;
    private Boolean active;
    private Boolean domainAll;
    private Boolean adminRole;


    public RmUserDetailDto toDto() {
        RmUserDetailDto dto = new RmUserDetailDto();
        dto.setCode(this.getHrsCode());
        //dto.setHrsCode(this.getHrsCode());
        dto.setUsername(this.getUsername());
        dto.setFullName(this.getFullName());
        dto.setBranchCode(this.getBranch());
        dto.setPhoneNumber(this.getPhoneNumber());
        dto.setEmail(this.getEmail());
        dto.setActive(this.getActive());

        return dto;
    }
}
