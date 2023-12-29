package com.stg.service3rd.crm.dto.resp;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RmInfoResp {
    private HrisEmployee hrisEmployee;
    private T24Employee t24Employee;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static final class HrisEmployee {
        private String userName;

        private String fullName;
        private String email;
        private String phoneNumber;
        private String phoneNumber2;
    }

    @Data
    @Accessors(chain = true)
    @ApiModel
    public static final class T24Employee {
        private String employeeCode;
        private String branchCode;
        private String branchName;
        private String branchLevel2;
        private String branchNameLevel2;
    }
}
