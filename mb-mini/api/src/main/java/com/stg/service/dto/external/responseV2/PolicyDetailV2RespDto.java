package com.stg.service.dto.external.responseV2;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PolicyDetailV2RespDto {

    private String policyNumber;
    private String externalAppId;
    private String inceptionDate; //ngày hiệu lực
    private String status;
    private PolicyHolder policyHolder;
    private List<Coverages> coverages;
    private Premium premium;


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Coverages {
        private String productCode; //Mã sản phẩm
        private String productName; //Tên sản phẩm
        private Integer coverageYear; //Thời hạn bảo hiểm
        private String startDate; //Ngày bắt đầu hiệu lực
        private String endDate; //Ngày hết hạn
        private BigDecimal sumInsured; //Số tiền bảo hiểm
        private List<PolicyHolder> insureds;
        private boolean main;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class PolicyHolder {
        private String fullName; //Tên người được bảo hiểm
        private String fullAddress; //Địa chỉ chi tiết
        private String email;
        private String bpNumber;
        private Integer gender;
        private String dob;
        private String phone;
        private String job;
        private List<Identifications> identifications;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Identifications {
        @Pattern(regexp = "ZMBL02|ZMBL03|FS0002|ZMBL18|ZMBL01|ZMBL15", message = "Loại giấy tờ tùy thân")
        private String type;

        private String id; //Số giấy tờ
        private String issuePlace; //Nơi đăng ký
        private String issueDate; //Ngày đăng ký
        private String expiryDate; //Ngày hết hạn
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Premium {
        private Integer premiumTerm; //Thời hạn đóng phí
        private BigDecimal periodicPremium; //Phí bảo hiểm định kì
        private String paymentPeriod; //Định kì đóng phí
    }
}
