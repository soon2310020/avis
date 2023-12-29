package com.stg.service.dto.external.responseV2;

import com.stg.utils.Common;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CreateQuoteV2RespDto {

    private String id;
    private String quotationDate;
    private String productPackageCode;
    private PolicyHolder policyHolder;
    private LifeAssured lifeAssured;
    private ProductPackage productPackage;
    private Common.Referer referer;
    private Common.Supporter supporter;
    private Common.Sale sale;
    private String quotationCode;
    private String submissionCode;
    private String applicationNumber;
    private String micTransactionId;
    private String policyNumber;
    private String quotationStatus;
    private String applicationStatus;
    private Integer appQuestionNumber;
    private String strMixInsuranceFee;
    private BigDecimal mixInsuranceFee;

    private String strMicInsuranceFee;
    private String strTopupInsuranceFee;

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class PolicyHolder {
        private String id;
        private String fullName;
        private String identificationType;
        private String identificationNumber;
        private String dob;
        @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
        private String gender;
        private Integer occupationId;
        private Address address;
        private String nationalityCode;
        private String email;
        private String phoneNumber;
        private List<Common.Quizzes> quizzes;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Address {
        private String addressType;

        private String title;

        private String name;

        private String phoneNumber;

        private String zipcode;

        private String provinceName;

        private String districtName;

        private String wardName;

        private String line1; // địa chỉ address

        private String line2; // origin address from T24
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class LifeAssured {
        private UUID id;
        private String fullName;
        private String identificationType;
        private String identificationNumber;
        private String dob;
        @Pattern(regexp = "MALE|FEMALE", message = "Giới tính là MALE hoặc FEMALE")
        private String gender;
        private Integer occupationId;
        private Address address;
        private String nationalityCode;
        private String email;
        private String phoneNumber;
        private List<Common.Quizzes> quizzes;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class ProductPackage {
        private Integer policyTerm;
        private Integer premiumTerm;
        private String paymentPeriod;
        private Long sumAssured;
        private Long periodicPremium;
        private String discountGroup;
        private Long baseInsuranceFee;
        private Long topupInsuranceFee;
    }
}
