package com.stg.service3rd.mbal.dto;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.entity.quotation.QuotationSupporter;
import com.stg.errors.mbal.MbalValidationException;
import com.stg.service.dto.mbal.QuotationModelYearItem;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service3rd.mbal.constant.AssuredType;
import com.stg.service3rd.mbal.constant.BenefitType;
import com.stg.service3rd.mbal.constant.Common;
import com.stg.service3rd.mbal.constant.DiscountGroup;
import com.stg.service3rd.mbal.constant.PaymentPeriod;
import com.stg.service3rd.mbal.constant.ProductType;
import com.stg.service3rd.mbal.constant.RelationshipPolicyHolderType;
import com.stg.service3rd.mbal.constant.RelationshipType;
import com.stg.utils.DateUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.stg.service3rd.mbal.constant.Common.EMAIL_PATTERN;
import static com.stg.service3rd.mbal.constant.Common.PHONE_PATTERN;
import static com.stg.service3rd.mbal.constant.Common.generateUUIDId;
import static com.stg.constant.CommonMessageError.FL_MSG38;
import static com.stg.constant.CommonMessageError.FL_MSG40;
import static com.stg.constant.CommonMessageError.FL_MSG43;
import static com.stg.constant.CommonMessageError.FL_MSG46;
import static com.stg.constant.CommonMessageError.REGEX_DATE;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FlexibleCommon {

    private static final String FX_CREATE_QUOTE_PROCESS_PREFIX = "FLEX_CQ-";
    private static final String CIF_ASSURED_PREFIX = "FLEX_CAS-";
    private static final String CUSTOMER_ASSURED_PROCESS_ID_PREFIX = "FLEX_CUS-";
    private static final String FLEX_INSURANCE_REQUEST_PREFIX = "FLEX_IR-";
    public static final String ZERO_TIME = " 00:00:00";

    public static final String METADATA_FLEX_KEY = "FLEX_METADATA";
    public static final String DASH = "-";

    public static void validateFxMicInsuranceAge(String dob) {
        if (dob == null) {
            throw new MbalValidationException(FL_MSG40);
        }
        int insuranceAge = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge > 65) {
            throw new MbalValidationException(FL_MSG40);
        }
        if (insuranceAge < 1) {
            LocalDateTime birthday = DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME);
            LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
            LocalDate today = LocalDate.now();
            Period age = Period.between(birthDay, today);
            if (age.getMonths() < 1 && age.getDays() < 15) {
                throw new MbalValidationException(FL_MSG40);
            }
        }
    }

    public static void validateAdditionalInsuranceAge(String dob) {
        if (dob == null) {
            throw new MbalValidationException(FL_MSG43);
        }
        LocalDateTime birthday = DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME);
        LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDay, today);
        if (age.getYears() < 0 && age.getMonths() < 0 && age.getDays() < 30) {
            throw new MbalValidationException(FL_MSG43);
        }
        if (age.getYears() > 70) {
            throw new MbalValidationException(FL_MSG43);
        }
    }

    public static void validateMainInsuranceAge(String dob) {
        if (dob == null) {
            throw new MbalValidationException(FL_MSG38);
        }
        int insuranceAge = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge < 1 && getInsuranceMonth(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now()) < 1) {
            throw new MbalValidationException(FL_MSG38);
        }
        if (insuranceAge > 65) {
            throw new MbalValidationException(FL_MSG38);
        }
    }

    public static int insuranceAge(LocalDateTime birthday, LocalDateTime startDateInsurance) {
        int insuranceAge;
        LocalDate dateInsurance = startDateInsurance.toLocalDate();
        LocalDate firstDayOfMonth = LocalDate.of(startDateInsurance.getYear(), birthday.getMonth(), 1); // ngày đầu tiên của tháng sinh nhật của năm kí hợp đồng
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.getMonth().length(firstDayOfMonth.isLeapYear())); // ngày cuối cùng của tháng sinh nhật của năm kí hợp đồng
        LocalDate dboThisYear;
        if (lastDayOfMonth.getDayOfMonth() < birthday.getDayOfMonth()) {
            dboThisYear = lastDayOfMonth;
        } else {
            dboThisYear = LocalDate.of(startDateInsurance.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        }
        if (dboThisYear.isBefore(dateInsurance) || dboThisYear.isEqual(dateInsurance)) {
            insuranceAge = startDateInsurance.getYear() - birthday.getYear();
        } else {
            insuranceAge = startDateInsurance.getYear() - birthday.getYear() - 1;
        }
        return insuranceAge;
    }

    public static void validateAdditionalFxMicInsuranceAge(String dob) {
        if (dob == null) {
            throw new MbalValidationException(FL_MSG46);
        }
        int insuranceAge = insuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge < 18) {
            throw new MbalValidationException(FL_MSG46);
        }
    }

    public static String processCifCreateQuoteIdKey(String cif, String processId) {
        return FX_CREATE_QUOTE_PROCESS_PREFIX + cif + DASH + processId;
    }

    public static String fxCifAssuredKey(String mbId, String processId) {
        return CIF_ASSURED_PREFIX + mbId + DASH + processId;
    }

    public static String customerAssuredProcessIdKey(String mbId, String processId) {
        return CUSTOMER_ASSURED_PROCESS_ID_PREFIX + mbId + DASH + processId;
    }

    public static String fxTransactionMetadataKey(String mbId, String processId) {
        return METADATA_FLEX_KEY + DASH + mbId + DASH + processId;
    }

    public static String fxInsuranceRequestKey(String mbId, String processId) {
        return FLEX_INSURANCE_REQUEST_PREFIX + DASH + mbId + DASH + processId;
    }

    private static int getInsuranceMonth(LocalDateTime birthday, LocalDateTime startDateInsurance) {
        LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDay, today);
        return age.getMonths();
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class GcnMicCareDkbs {
        @ApiModelProperty(notes = "bs1", required = true)
        @Pattern(regexp = "C|K")
        private String bs1;
        @ApiModelProperty(notes = "bs2", required = true)
        @Pattern(regexp = "C|K")
        private String bs2;
        @ApiModelProperty(notes = "bs3", required = true)
        @Pattern(regexp = "C|K")
        private String bs3;
        @ApiModelProperty(notes = "bs4", required = true)
        @Pattern(regexp = "C|K")
        private String bs4;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Branch {
        private String code;
        private String bankName;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Referrer {
        private String code;
        private String name;
        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;
        @Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
        private String email;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class ReferrerInput extends Referrer {
        private String branchName;
        private String branchCode;
        private String departmentName;
        private String departmentCode;

        public QuotationSupporter toEntity() {
            QuotationSupporter entity = new QuotationSupporter();
            entity.setCode(this.getCode());
            entity.setName(this.getName());
            entity.setEmail(this.getEmail());
            entity.setPhone(this.getPhoneNumber());
            entity.setBranchCode(this.getBranchCode());
            entity.setBranchName(this.getBranchName());
            entity.setDepartmentCode(this.getDepartmentCode());
            entity.setDepartmentName(this.getDepartmentName());
            return entity;
        }

        public Referrer3rdInput to3rdInput() {
            Referrer3rdInput input = new Referrer3rdInput();
            input.setCode(this.getCode());
            input.setName(this.getName());
            input.setEmail(this.getEmail());
            input.setPhoneNumber(this.getPhoneNumber());

            Branch branch = new Branch(this.getBranchCode(), this.getBranchName());
            input.setBranch(branch);
            return input;
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Referrer3rdInput extends Referrer {
        private Branch branch;

        public FlexibleCommon.ReferrerInput toDto() {
            FlexibleCommon.ReferrerInput dto = new FlexibleCommon.ReferrerInput();
            dto.setCode(this.getCode());
            dto.setName(this.getName());
            dto.setEmail(this.getEmail());
            dto.setPhoneNumber(this.getPhoneNumber());
            if (branch != null) {
                dto.setBranchCode(this.getBranch().getCode());
                dto.setBranchName(this.getBranch().getBankName());
            }
            return dto;
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class Supporter {
        private String code;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class Sale {
        private String code;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Quizzes {
        private UUID id;
        @Size(max = 50)
        private String label;
        private List<Answers> answers;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Answers {
        private UUID id;
        @Size(max = 255)
        private String value;
        @Min(1)
        @Max(10)
        private Integer ordinal;
    }

    // Flexible
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class CustomerInfo extends CommonInfo {

        @ApiModelProperty
        @NotBlank(message = "Thông tin ngày cấp là bắt buộc")
        @Pattern(regexp = REGEX_DATE, message = "Ngày cấp format: YYYY-MM-dd")
        private String identificationDate;

        @ApiModelProperty
        @NotBlank(message = "Thông tin nơi cấp là bắt buộc")
        private String idIssuedPlace;

        //@NotNull(message = "Thông tin thu nhập theo năm là bắt buộc")
        private BigDecimal annualIncome;

        public CustomerInfo(Assured assured) {
            setFullName(assured.getFullName());
            setAddress(assured.getAddress());
            setDob(assured.getDob());
            setGender(assured.getGender());
            setEmail(assured.getEmail());
            setOccupationId(assured.getOccupationId());
            setPhoneNumber(assured.getPhoneNumber());
            setNationalityCode(assured.getNationalityCode());
            setIdentificationType(assured.getIdentificationType());
            //setIdentificationNumber(customerInfo.getIdentificationNumber());
            setIdentificationId(assured.getIdentificationNumber());
            setIdentificationDate(assured.getIdentificationDate());
            setIdIssuedPlace(assured.getIdIssuedPlace());
            setAnnualIncome(assured.getAnnualIncome());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class BeneficiaryOutput extends BeneficiaryInput {

//        private String id;
//
//        private String fullName;
//
//        private IdentificationType identificationType;
//
//        private String identificationNumber;
//
//        private String dob;
//
//        private Gender gender;
//
//        private Integer occupationId;
//
//        private Address address;
//
//        private String nationalityCode;
//
//        private String email;
//
//        private String phoneNumber;
//
//        private List<Common.Quizzes> quizzes;
//
//        private List<FamilyMembersOutput> familyMembers;
//
//        private List<OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims;
//
//        private List<OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts;
//
//        private List<OtherActiveContracts> otherActiveContracts;
//
//        private boolean usCitizen;
//
//        private RelationshipType relationshipWithMainAssured;
//
//        private BigDecimal annualIncome;
//
//        private Integer benefitRate;
//
//        private RelationshipPolicyHolderType relationshipWithPolicyHolder;
//
//        private AssuredType type;
//
//        public void setIdentificationNumber(String identificationId) {
//            super.setIdentificationNumber(identificationNumber);
//        }
//
//        public String getIdentificationNumber() {
//            return super.getIdentificationNumber();
//        }

        private String id;

        private List<Common.Quizzes> quizzes;

        private List<FamilyMembersOutput> familyMembers;

        private List<OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims;

        private List<OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts;

        private List<OtherActiveContracts> otherActiveContracts;

        private boolean usCitizen;

        private AssuredType type;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class BeneficiaryInput {

        @ApiModelProperty(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String fullName;

        //        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = EMAIL_PATTERN)
        private String email;

        //        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        @ApiModelProperty(required = true)
        @Pattern(regexp = REGEX_DATE, message = "Ngày sinh format: yyyy-MM-dd")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String dob;

        @ApiModelProperty(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Gender gender;

        @ApiModelProperty(required = true, notes = "Loại giấy tờ tùy thân")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private IdentificationType identificationType;

        @ApiModelProperty(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String identificationId;
        //private String identificationNumber; // todo...

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private Address address;

        @ApiModelProperty(notes = "Mã nghề nghiệp", required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Integer occupationId;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private BigDecimal annualIncome;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String nationalityCode;

        @ApiModelProperty(notes = "Mối quan hệ với người được bảo hiểm chính", example = "COUPLE|CHILDREN|PARENT|OTHER")
        private RelationshipType relationshipWithMainAssured;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private Integer benefitRate;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        @Pattern(regexp = REGEX_DATE, message = "Ngày cấp format: YYYY-MM-dd")
        private String identificationDate;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String idIssuedPlace;

        @ApiModelProperty(notes = "Mối quan hệ với người mua bảo hiểm", required = true)
        private RelationshipPolicyHolderType relationshipWithPolicyHolder;


        public void setIdentificationNumber(String identificationId) {
            this.identificationId = identificationId;
        }

        public String getIdentificationNumber() {
            return this.identificationId;
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class CommonInfo {

        @ApiModelProperty(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String fullName;

        @Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
        private String email;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        @ApiModelProperty(required = true)
        @Pattern(regexp = REGEX_DATE, message = "Ngày sinh format: yyyy-MM-dd")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String dob;

        @ApiModelProperty(required = true)
        private Gender gender;

        @ApiModelProperty(required = true, notes = "Loại giấy tờ tùy thân")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private IdentificationType identificationType;

        @ApiModelProperty
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String identificationId;
        //private String identificationNumber;

        @ApiModelProperty
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Address address;

        @ApiModelProperty(notes = "Mã nghề nghiệp", required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Integer occupationId;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(notes = "Mã quốc gia", required = true)
        private String nationalityCode;


        public void setIdentificationNumber(String identificationId) {
            this.identificationId = identificationId;
        }

        public String getIdentificationNumber() {
            return this.identificationId;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class Assured extends CommonInfo {

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String id;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String miniAssuredId = generateUUIDId(8);

        // tên nghề nghiệp
        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String occupationName;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String occupationGroup;

        //@ApiModelProperty(notes = "Thông tin bắt buộc với người bổ sung")
        //@NotNull(message = "Thông tin thu nhập theo năm là bắt buộc")
        private BigDecimal annualIncome;

        @ApiModelProperty(notes = "Người bảo hiểm chính hoặc bổ sung", example = "LIFE_ASSURED|ADDITIONAL_LIVES")
        private AssuredType type;

        @ApiModelProperty(notes = "Mối quan hệ với người được bảo hiểm chính", example = "COUPLE|CHILDREN|PARENT|OTHER", required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private RelationshipType relationshipWithMainAssured;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String identificationDate;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private String idIssuedPlace;

        @ApiModelProperty(notes = "Thông tin không bắt buộc")
        private Integer benefitRate;

        @ApiModelProperty(notes = "Thông tin không bắt buộc, Flag xác nhận là BMBH (Khách hàng)")
        private Boolean isCustomer = false;

        @ApiModelProperty(notes = "Mối quan hệ với người mua bảo hiểm", required = true)
        private RelationshipPolicyHolderType relationshipWithPolicyHolder;

        public Assured(CustomerInfo customerInfo) {
            setFullName(customerInfo.getFullName());
            setAddress(customerInfo.getAddress());
            setDob(customerInfo.getDob());
            setGender(customerInfo.getGender());
            setEmail(customerInfo.getEmail());
            setOccupationId(customerInfo.getOccupationId());
            setRelationshipWithMainAssured(RelationshipType.OTHER);
            setPhoneNumber(customerInfo.getPhoneNumber());
            setNationalityCode(customerInfo.getNationalityCode());
            setIdentificationType(customerInfo.getIdentificationType());
            //setIdentificationNumber(customerInfo.getIdentificationNumber());
            setIdentificationId(customerInfo.getIdentificationNumber());
            setAnnualIncome(customerInfo.getAnnualIncome());
        }

        public Assured(PolicyHolderAndLifeAssured lifeAssured) {
            setId(lifeAssured.getId());
            setGender(lifeAssured.getGender());
            setMiniAssuredId(lifeAssured.getMiniAssuredId());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class Address {

        private String title;

        private String name;

        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        private String zipcode;

        private String provinceName;

        private String districtName;

        private String wardName;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(required = true)
        private String line1; // địa chỉ address

        private String line2; // origin address from T24

        /***/
        public String fullAddress() {
            String addressDto = this.line1 == null ? "" : this.line1;
            if (StringUtils.hasText(this.wardName)) addressDto += " - " + this.wardName;
            if (StringUtils.hasText(this.districtName)) addressDto += " - " + this.districtName;
            if (StringUtils.hasText(this.provinceName)) addressDto += " - " + this.provinceName;
            return addressDto;
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class ProductPackageOutput extends ProductPackageInput {
        private DiscountGroup discountGroup;
        private BigDecimal baseInsuranceFee;
        private BigDecimal topupInsuranceFee;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class ProductPackageInput {
        @ApiModelProperty
        private Integer policyTerm;
        @ApiModelProperty
        private BenefitType insuredBenefit;
        @ApiModelProperty
        private Integer premiumTerm;
        @ApiModelProperty
        private PaymentPeriod paymentPeriod;
        @ApiModelProperty
        private BigDecimal sumAssured;
        @ApiModelProperty
        private BigDecimal periodicPremium;

        public ProductPackageInput(QuotationHeaderDto quotationDto) {
            this.setPolicyTerm(quotationDto.getPackagePolicyTerm());
            this.setInsuredBenefit(quotationDto.getPackageBenefitType());
            this.setPremiumTerm(quotationDto.getPackagePremiumTerm());
            this.setPaymentPeriod(quotationDto.getPackagePaymentPeriod());
            this.setSumAssured(quotationDto.getPackageSumAssured());
            this.setPeriodicPremium(quotationDto.getPackagePeriodicPremium());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class FamilyMembersInput {
        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(notes = "Họ và tên")
        private String fullName;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(notes = "Mối quan hệ với BMBH*")
        private String relationshipInsurancePerson;
        @Min(1)
        @Max(999)
        private Integer age;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @ApiModelProperty(notes = "Tình trạng sức khoẻ hoặc Nguyên nhân tử vong (nếu đã chết)*")
        private String healthDetail;

        private String relation;

        private String status;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class FamilyMembersOutput extends FamilyMembersInput {
        private String id;
        private MicInsuranceResultRespDto micFee;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class OtherInsuranceClaimsAndDeclinedContract {
        private String id;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String companyName;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private BigDecimal amount;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String dateOfIssue;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String status;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String reason;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String policyNumber;

        private String commenceDate;

        private BigDecimal insuranceFee;
        private String dateTime;
        private String insuranceNo;


        public OtherInsuranceClaimsAndDeclinedContract(OtherInsuranceClaimsAndDeclinedContract activeContract) {
            setAmount(activeContract.getInsuranceFee());
            setDateOfIssue(activeContract.getDateTime());
            setPolicyNumber(activeContract.getInsuranceNo());
            setStatus(activeContract.getStatus());
            setCompanyName(activeContract.getCompanyName());
            setDateTime(activeContract.getDateTime());
            setInsuranceFee(activeContract.getInsuranceFee());
            setInsuranceNo(activeContract.getInsuranceNo());
            setCommenceDate(activeContract.getDateTime());
            setReason(activeContract.getReason());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class OtherActiveContracts {
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String companyName;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private BigDecimal amount;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String policyEffectiveDate;
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String policyNumber;

        private BigDecimal insuranceFee;
        private String dateTime;
        private String insuranceNo;

        public OtherActiveContracts(OtherActiveContracts activeContract) {
            setAmount(activeContract.getInsuranceFee());
            setPolicyEffectiveDate(activeContract.getDateTime());
            setPolicyNumber(activeContract.getInsuranceNo());
            setCompanyName(activeContract.getCompanyName());
            setDateTime(activeContract.getDateTime());
            setInsuranceFee(activeContract.getInsuranceFee());
            setInsuranceNo(activeContract.getInsuranceNo());
        }

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class SubmitCustomer {
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Boolean usCitizen;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class MiniMicRequestDto {
        @ApiModelProperty(notes = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
        @NotNull(message = "Nhóm gói bảo hiểm tính phí bên MIC bắt buộc nhập")
        @Max(value = 5, message = "Giá trị lớn nhất là 5")
        @Min(value = 1, message = "Giá trị nhỏ nhất là 1")
        private int nhom;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(required = true)
        private String bs1;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(required = true)
        private String bs2;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @ApiModelProperty(required = true)
        private String bs3;
        @ApiModelProperty(required = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String bs4;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class PolicyHolderAndLifeAssured extends CommonInfo {
        private String id;
        private String miniAssuredId;
        private List<Common.Quizzes> quizzes;
        private List<FamilyMembersOutput> familyMembers;
        private List<OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims;
        private List<OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts;
        private List<OtherActiveContracts> otherActiveContracts;
        private boolean usCitizen;
        private RelationshipType relationshipWithMainAssured;
        private BigDecimal annualIncome;
        private Integer benefitRate;
        private Integer appQuestionNumber;
        private RelationshipPolicyHolderType relationshipWithPolicyHolder;
        private AssuredType type;

        private QuoteAssuredOutput additionalProduct;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PolicyHolderAndLifeAssured)) return false;
            PolicyHolderAndLifeAssured that = (PolicyHolderAndLifeAssured) o;
            return Objects.equals(getFullName(), that.getFullName())
                    && getIdentificationType() == that.getIdentificationType()
                    && Objects.equals(getIdentificationNumber(), that.getIdentificationNumber())
                    && Objects.equals(getDob(), that.getDob())
                    && getGender() == that.getGender()
                    && Objects.equals(getOccupationId(), that.getOccupationId())
                    && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPhoneNumber(), that.getPhoneNumber());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getFullName(), getIdentificationType(), getIdentificationNumber(), getDob(), getGender(), getOccupationId(), getEmail(), getPhoneNumber());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class CashFlow {
        private List<QuotationModelYearItem> yearItems;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class Rider {
        private String code;
        private Integer policyTerm;
        private Integer premiumTerm;
        private BigDecimal sumAssured;
        private BigDecimal sumAssuredOutput; /***/
        private PaymentPeriod paymentPeriod;
        private BenefitType insuredBenefit;
        private String beneficiaryName;
        private List<QuotationModelYearItem> yearItems;
        private BigDecimal basePremium;
        private BigDecimal regBasePrem;
        private String assuredId;
        private ProductType productType;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class YearItem {
        private Integer policyYear;
        private Integer insuredAge;
        private BigDecimal basePremium;
        private BigDecimal topupPremium;
        private BigDecimal withdrawal;
        private AccountByInterestRate accountByInterestRate;
        private BenefitByInvestmentRate benefitByInvestmentRate;

        private Integer yearPol;
        private BigDecimal periodicPremium;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class AccountByInterestRate {
        private Rate selectedRate;
        private Rate committedRate;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class BenefitByInvestmentRate {
        private Rate lowRate;
        private Rate highRate;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    public static class Rate {
        private BigDecimal baseAccountValue;
        private BigDecimal topupAccountValue;

        private BigDecimal deathBenefit;
        private BigDecimal loyaltyBonus;

        private BigDecimal accountValue;
        private BigDecimal surenderValue;
    }


    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @ApiModel
    @Valid
    public static class ParentContract {
        @ApiModelProperty(notes = "Số hợp đồng MIC Care còn hiệu lực tại MIC")
        @NotNull
        private String so_hd_bm;
        @ApiModelProperty(notes = "CMT/CCCD/Hộ chiếu")
        @NotNull
        private String cmt_bm;
        @ApiModelProperty(notes = "Tên bố/mẹ")
        @NotNull
        private String ten_bm;
        @ApiModelProperty(notes = "Số điện thoại")
        @NotNull
        private String mobi_bm;
    }

}
