package com.stg.service.dto.external;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class RmInfoResp {
    private String mbCode;
    private HrisEmployee hrisEmployee;
    private T24Employee t24Employee;

    @Getter
    @Setter
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static final class HrisEmployee {
        private String userName;

        // more
        private String fullName;
        private String email;
        private String phoneNumber;
        private String phoneNumber2;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @Schema
    public static final class T24Employee {
        private String employeeCode;
        private String branchCode;
        private String branchName;
        private String branchLevel2;
        private String branchNameLevel2;
    }
}
