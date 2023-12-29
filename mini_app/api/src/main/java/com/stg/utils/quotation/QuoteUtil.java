package com.stg.utils.quotation;

import com.stg.constant.quotation.QuotationState;
import com.stg.errors.ApplicationException;
import com.stg.errors.MicApiException;
import com.stg.errors.ValidationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.errors.mbal.MbalValidationException;
import com.stg.errors.mbal.MicValidationException;
import com.stg.errors.quote.CreateQuoteException;
import com.stg.service.dto.quotation.FlexibleSubmitQuestionInput;
import com.stg.service.dto.quotation.QuotationAssuredDto;
import com.stg.service.dto.quotation.QuotationCustomerDto;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.MbalAdditionalProductInput;
import com.stg.utils.DateUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.stg.constant.CommonMessageError.*;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QuoteUtil {
    private static final String ZERO_TIME = " 00:00:00";

    /***/
    public static void validateMainInsuranceAge(String dob) {
        if (dob == null) {
            throw new MbalValidationException(FL_MSG38);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge < 1 && getInsuranceMonth(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now()) < 1) {
            throw new MbalValidationException(FL_MSG38);
        }
        if (insuranceAge > 65) {
            throw new MbalValidationException(FL_MSG38);
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

    public static void validateInsuranceAge(String dob) {
        if (dob == null) {
            throw new MicApiException(MSG25);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + " 00:00:00"), LocalDateTime.now());
        if (insuranceAge < 18 || insuranceAge > 60) {
            throw new MicApiException(MSG25);
        }
    }

    private static int getInsuranceAge(LocalDateTime birthday, LocalDateTime startDateInsurance) {
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

    private static int getInsuranceMonth(LocalDateTime birthday, LocalDateTime startDateInsurance) {
        LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDay, today);
        return age.getMonths();
    }

    /***/
    public static void validateAdditionalFxMicInsuranceAge(String dob) {
        if (dob == null) {
            throw new MicValidationException(FL_MSG46);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge < 18) {
            throw new MicValidationException(FL_MSG46);
        }
    }

    public static void validateAdditionalProducts(List<MbalAdditionalProductInput> additionalProducts) {
        List<MbalAdditionalProductInput> duplicates = additionalProducts.stream()
                .collect(groupingBy(identity(), counting()))
                .entrySet().stream()
                .filter(n -> n.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(toList());
        if (!duplicates.isEmpty()) {
            log.error("Duplicate mua 1 mã gói bổ sung cho cùng 1 người hoặc mua quá 2 gói MIC cho 1 người.");
            throw new CreateQuoteException(FL_MSG35);
        }
    }

    public static String genIdentifyKey(String identifyType, String identifyNo) {
        return identifyType + ":" + identifyNo;
    }

    /***/
    public static boolean isMatchedIdentify(QuotationCustomerDto customerInfo, FlexibleCommon.CommonInfo assured) {
        if (customerInfo == null || assured == null) return false;
        return Objects.equals(customerInfo.getIdentificationId(), assured.getIdentificationNumber()) &&
                Objects.equals(customerInfo.getIdentificationType().toString(), assured.getIdentificationType().toString());
    }


    public static Long convertToLong(String stringMoney) {
        try {
            if (stringMoney == null) {
                return 0L;
            }
            return Long.parseLong(stringMoney.replaceAll("\\D", ""));
        } catch (Exception e) {
            log.error("Error convertToLong!", e);
            return 0L;
        }
    }

    public static void verifyHealths(QuotationDto quotationDto, List<FlexibleSubmitQuestionInput> healths) {
        // customer
        QuotationAssuredDto customer = quotationDto.getCustomer();
        // assured-main
        QuotationAssuredDto assured = quotationDto.getAssured();
        // additionalAssureds
        List<QuotationAssuredDto> additionalAssureds = quotationDto.getAdditionalAssureds();

        for (FlexibleSubmitQuestionInput questionInput : healths) {
            if (questionInput.getAssuredId().equals(customer.getUuid())) {
                FlexibleSubmitQuestionInput healthOfCustomer = healths.stream().filter(el -> el.getAssuredId().equals(customer.getUuid())).findFirst().orElse(null);
                verifyHealth(customer, healthOfCustomer);
            } else if (questionInput.getAssuredId().equals(assured.getUuid())) {
                FlexibleSubmitQuestionInput healthOfMain = healths.stream().filter(el -> el.getAssuredId().equals(assured.getUuid())).findFirst().orElse(null);
                verifyHealth(assured, healthOfMain);
            }
            additionalAssureds.stream()
                    .filter(as -> questionInput.getAssuredId().equals(as.getUuid()))
                    .findFirst()
                    .ifPresent(assuredDto -> verifyHealth(assuredDto, questionInput));
        }
    }

    private static void verifyHealth(QuotationAssuredDto assured, FlexibleSubmitQuestionInput health) {
        Integer appQuestNumber = assured.getAppQuestionNumber();
        if (appQuestNumber == null) {
            if (health != null) throw new ApplicationException(MSG_001, new ErrorDto(HttpStatus.BAD_REQUEST, MSG_001));
        } else {
            if (health == null) {
                throw new ApplicationException(MSG_001, new ErrorDto(HttpStatus.BAD_REQUEST, MSG_001));
            }
            if (health.getComboBigQuestion() != null) return; // là big quest luôn pass

            // health.getComboBigQuestion() = null
            if (appQuestNumber == 12) {
                throw new ApplicationException(MSG_001, new ErrorDto(HttpStatus.BAD_REQUEST, MSG_001));
            }
            if (appQuestNumber == 3) {
                if (health.isFrom3To12()) {
                    throw new ApplicationException(MSG_002, new ErrorDto(HttpStatus.BAD_REQUEST, MSG_002));
                } else if (health.getComboSmallQuestion() == null) {
                    throw new ApplicationException(MSG_001, new ErrorDto(HttpStatus.BAD_REQUEST, MSG_001));
                }
            }
        }
    }

    /*
    CREATED: Bạn chưa hoàn thành hồ sơ
    SUBMITTED, COMPLETED: Hồ sơ của bạn đang được xử lý, vui lòng xem trong Hợp đồng của tôi
    RE_CREATED, RE_CONFIRMED, RE_SUBMITTED, RE_COMPLETED: Hồ sơ đã được tạo mới
    */
    public static void verifyCreateQrCode(QuotationState quotationState) {
        if (quotationState == null) throw new ValidationException("Hồ sơ ở trạng thái không hợp lệ");
        switch (quotationState) {
            case CREATED:
                throw new ValidationException("Bạn chưa hoàn thành hồ sơ");
            case SUBMITTED:
            case COMPLETED:
                throw new ValidationException("Hồ sơ của bạn đang được xử lý, vui lòng xem trong Hợp đồng của tôi");
            case RE_CREATED:
            case RE_CONFIRMED:
            case RE_SUBMITTED:
            case RE_COMPLETED:
                throw new ValidationException("Hồ sơ đã được tạo mới");
            default: // pass!
        }
    }
}
