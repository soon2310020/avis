package com.stg.utils;

import com.google.common.io.Files;
import com.stg.errors.MicApiException;
import com.stg.service.impl.password.PasswordRequirements;
import com.stg.service.impl.password.SpecialCharacterData;
import com.stg.service.impl.password.ValidationRules;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.http.HttpHeaders;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.stg.utils.CommonMessageError.MSG25;
import static com.stg.utils.Constants.EMAIL_PATTERN;
import static com.stg.utils.Constants.PHONE_PATTERN;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;
import static com.stg.utils.FlexibleCommon.getInsuranceAge;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
public class Common {

    private static final String MONEY_PATTERN = "###,###.###";
    private static final String VND = " VND";
    private static final DecimalFormat DF_BILLION = new DecimalFormat("0.000");
    private static final DecimalFormat DF_MILLION = new DecimalFormat("0");
    private static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ILLUSTRATION_PREFIX = "ILL-";
    private static final String PROCESS_ID_PREFIX = "PR-";
    private static final String INSURANCE_REQUEST_PREFIX = "IR-";
    private static final String MIC_TRANSACTION_ID_PREFIX = "MIC_TRAN-";
    private static final String PROCESS_AMOUNT_PREFIX = "PRAM-";
    private static final String IC_CODE_PREFIX = "IC-";
    private static final String SEND_EMAIL_STATUS_PREFIX = "EMAIL-";
    private static final String CRM_PREFIX = "CRM-";
    private static final String SUPPORTER_PREFIX = "SP-";
    private static final String INSTALLMENT_PREFIX = "INSTALLMENT-";
    private static final String CARD_PREFIX = "CARD-";

    public static final String MERCHANT = "merchant";
    public static final DecimalFormat MONEY_DECIMAL_FORMAT = new DecimalFormat("#0.0");

    public static String generateUUIDId(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }

    public static String generateTransactionId() {
        PasswordGenerator generator = new PasswordGenerator();

        ValidationRules validationRules = PasswordRequirements.BAAS_TRANSACTION_RULE;

        List<CharacterRule> characterRules = createCharacterRules(validationRules);

        return generator.generatePassword(validationRules.getMinimumLength(), characterRules);
    }

    public static List<CharacterRule> createCharacterRules(ValidationRules validationRules) {
        List<CharacterRule> rules = new ArrayList<>();

        if (validationRules.getMinimumUpperCaseCount() != null) {
            rules.add(new CharacterRule(EnglishCharacterData.UpperCase, validationRules.getMinimumUpperCaseCount()));
        }
        if (validationRules.getMinimumLowerCaseCount() != null) {
            rules.add(new CharacterRule(EnglishCharacterData.LowerCase, validationRules.getMinimumLowerCaseCount()));
        }
        if (validationRules.getMinimumNumberCount() != null) {
            rules.add(new CharacterRule(EnglishCharacterData.Digit, validationRules.getMinimumNumberCount()));
        }
        if (validationRules.getMinimumSymbolCount() != null) {
            rules.add(new CharacterRule(SpecialCharacterData.INSTANCE, validationRules.getMinimumSymbolCount()));
        }
        return rules;
    }

    public static HttpHeaders generateDefaultHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }

    public static HttpHeaders generateMbalDefaultHeader(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        return headers;
    }

    public static HttpHeaders generateMicHeader(String merchant, String bearerToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.set(MERCHANT, merchant);
        headers.setBearerAuth(bearerToken);
        return headers;
    }

    public static String convertGroupMic(int nhom) {
        switch (nhom) {
            case 1:
                return "1/MBAL2";
            case 2:
                return "2/MBAL2";
            case 3:
                return "3/MBAL2";
            case 4:
                return "4/MBAL2";
            default:
                return "5/MBAL2";
        }
    }

    public static String formatCurrency(BigDecimal money) {
        if (money == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        DecimalFormat decimalFormat = new DecimalFormat(MONEY_PATTERN, symbols);
        return (decimalFormat.format(money) + VND).replace(",", ".");
    }

    public static Long convertToLong(String stringMoney) {
        try {
            if (stringMoney == null) {
                return 0L;
            }
            return Long.parseLong(stringMoney.replaceAll("\\D", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    public static Double convertToDouble(String stringMoney) {
        try {
            if (stringMoney == null) {
                return 0.0;
            }
            return Double.parseDouble(stringMoney.replaceAll("\\D", ""));
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static BigDecimal convertToBigDecimal(String stringMoney) {
        try {
            if (stringMoney == null) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(stringMoney.replaceAll("\\D", ""));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public static String convertToMoneyFormat(String strMoney) {
        double rate = convertToDouble(strMoney);
        if (rate / 1000000000 > 1) {
            return (DF_BILLION.format(Math.rint(rate) / 1000000000) + " tỷ")
                    .replace(".000 ", "")
                    .replace("00 ", " ")
                    .replace("0 ", " ");
        } else if (rate / 1000000 > 1) {
            return (DF_MILLION.format(Math.rint(rate) / 1000000) + " triệu");
        } else {
            return (DF_BILLION.format(Math.rint(rate) / 1000) + " nghìn")
                    .replace(".000", "")
                    .replace("0 ", " ");
        }
    }

    public static String getIsoTimeFromDate() {
        DateFormat df = new SimpleDateFormat(ISO_PATTERN);

        Date today = Calendar.getInstance().getTime();

        return df.format(today);
    }

    public static String getExtensionFile(String filename) {
        return Files.getFileExtension(filename);
    }

    public static void validateInsuranceAge(String dob) {
        if (dob == null) {
            throw new MicApiException(MSG25);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + " 00:00:00"), LocalDateTime.now());
        if (insuranceAge < 18 || insuranceAge > 60) {
            throw new MicApiException(MSG25);
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class GcnMicCareDkbs {
        @Schema(description = "bs1", required = true)
        @Pattern(regexp = "C|K")
        private String bs1;
        @Schema(description = "bs2", required = true)
        @Pattern(regexp = "C|K")
        private String bs2;
        @Schema(description = "bs3", required = true)
        @Pattern(regexp = "C|K")
        private String bs3;
        @Schema(description = "bs4", required = true)
        @Pattern(regexp = "C|K")
        private String bs4;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class ParentContract {
        @Schema(description = "Số hợp đồng MIC Care còn hiệu lực tại MIC")
        @NotNull
        private String so_hd_bm;
        @Schema(description = "CMT/CCCD/Hộ chiếu")
        @NotNull
        private String cmt_bm;
        @Schema(description = "Tên bố/mẹ")
        @NotNull
        private String ten_bm;
        @Schema(description = "Số điện thoại")
        @NotNull
        private String mobi_bm;
    }

    public static String illustrationKey(String mbId) {
        return ILLUSTRATION_PREFIX + mbId;
    }

    public static String insuranceRequestIdAndProcessIdKey(String processId) {
        return INSURANCE_REQUEST_PREFIX + processId;
    }

    public static String processIdKey(String mbId) {
        return PROCESS_ID_PREFIX + mbId;
    }

    public static String micTransactionIdKey(String cif) {
        return MIC_TRANSACTION_ID_PREFIX + cif;
    }

    public static String icCodeKey(String cif, String processId) {

        return IC_CODE_PREFIX + cif + "-" + processId;
    }

    public static String processAmountIdKey(String cif, String processId) {
        return PROCESS_AMOUNT_PREFIX + cif + "-" + processId;
    }

    public static String sendEmailProcessIdKey(String mbId, String processId) {
        return SEND_EMAIL_STATUS_PREFIX + mbId + "-" + processId;
    }

    public static String rmCifProcessIdKey(String mbId, String processId) {
        return CRM_PREFIX + mbId + "-" + processId;
    }

    public static String supporterCifProcessIdKey(String mbId, String processId) {
        return SUPPORTER_PREFIX + mbId + "-" + processId;
    }

    public static String installmentCifProcessIdKey(String mbId, String processId) {
        return INSTALLMENT_PREFIX + mbId + "-" + processId;
    }

    public static String listCardCifProcessIdKey(String mbId, String processId) {
        return CARD_PREFIX + mbId + "-" + processId;
    }

    public static String buildKeyPolicy(String mbId, String appNumber, String policyNumber) {
        return "cif_" + mbId + "_policy_" + policyNumber;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class Referer {
        private String code;
        private String name;
        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        @Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
        private String email;
        private String branchName;
        private String branchCode;
        private String departmentName;
        private String departmentCode;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Supporter {
        private String code;
        private String name;

        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;
        @Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
        private String email;
        private String branchName;
        private String branchCode;
        private String departmentName;
        private String departmentCode;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Sale {
        private String code;
        private String name;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
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
    @Schema
    @Valid
    public static class Answers {
        private UUID id;
        @Size(max = 255)
        private String value;
        @Min(1)
        @Max(10)
        private Integer ordinal;

        public String getValue() {
            if ("K".equals(this.value)) {
                return "NO";
            }
            if ("C".equals(this.value)) {
                return "YES";
            }
            return value;
        }
    }

    public static String convertIdentificationTypeMbalToMiniApp(String identificationType) {
        if (identificationType == null) {
            return "";
        }
        if (identificationType.equalsIgnoreCase("NATIONAL_ID")) {
            return "CMND";
        }
        if (identificationType.equalsIgnoreCase("CITIZEN_ID")) {
            return "CCCD";
        }
        if (identificationType.equalsIgnoreCase("MILITARY_ID")) {
            return "CMTQD";
        }
        return identificationType.toUpperCase();
    }

    public static String convertIdentificationTypeMbalToMiniApp(IdentificationType identificationType) {
        if (identificationType == null) {
            return "";
        }
        if (identificationType.name().equalsIgnoreCase("NATIONAL_ID")) {
            return "CMND";
        }
        if (identificationType.name().equalsIgnoreCase("CITIZEN_ID")) {
            return "CCCD";
        }
        if (identificationType.name().equalsIgnoreCase("MILITARY_ID")) {
            return "CMTQD";
        }
        return identificationType.name().toUpperCase();
    }

    public static String formatCurrencyNoUnit(BigDecimal money, String replaceCharacter) {
        if (money == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        DecimalFormat decimalFormat = new DecimalFormat(MONEY_PATTERN, symbols);
        return (decimalFormat.format(money)).replace(",", replaceCharacter);
    }
}
