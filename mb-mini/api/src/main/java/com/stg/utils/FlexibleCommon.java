package com.stg.utils;

import com.stg.errors.MiniApiException;
import com.stg.service.dto.external.requestFlexible.FlexibleSubmitQuestionRequest;
import com.stg.service.dto.external.requestFlexible.HealthInfoRequest;
import com.stg.service.dto.external.response.MicInsuranceResultRespDto;
import com.stg.service.dto.external.responseFlexible.QuoteAssuredOutput;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

import static com.stg.utils.Common.generateUUIDId;
import static com.stg.utils.CommonMessageError.*;
import static com.stg.utils.Constants.*;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;

@Slf4j
public class FlexibleCommon {

    private static final String FX_CREATE_QUOTE_PROCESS_PREFIX = "FLEX_CQ-";
    private static final String CIF_ASSURED_PREFIX = "FLEX_CAS-";
    private static final String CUSTOMER_ASSURED_PROCESS_ID_PREFIX = "FLEX_CUS-";
    private static final String FLEX_INSURANCE_REQUEST_PREFIX = "FLEX_IR-";
    public static final String ZERO_TIME =  " 00:00:00";

    public static final String METADATA_FLEX_KEY = "FLEX_METADATA";
    public static final String DASH = "-";

    public static void validateFxMicInsuranceAge(String dob) {
        if (dob == null) {
            throw new MiniApiException(FL_MSG40);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge > 65) {
            throw new MiniApiException(FL_MSG40);
        }
        if (insuranceAge < 1) {
            LocalDateTime birthday = DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME);
            LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
            LocalDate today = LocalDate.now();
            Period age = Period.between(birthDay, today);
            if (age.getMonths() < 1 && age.getDays() < 15) {
                throw new MiniApiException(FL_MSG40);
            }
        }
    }

    public static void validateAdditionalInsuranceAge(String dob) {
        if (dob == null) {
            throw new MiniApiException(FL_MSG43);
        }
        LocalDateTime birthday = DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME);
        LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDay, today);
        if (age.getYears() < 0 && age.getMonths() < 0 && age.getDays() < 30) {
            throw new MiniApiException(FL_MSG43);
        }
        if (age.getYears() > 70) {
            throw new MiniApiException(FL_MSG43);
        }
    }

    public static void validateMainInsuranceAge(String dob) {
        if (dob == null) {
            throw new MiniApiException(FL_MSG38);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge < 1 && getInsuranceMonth(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now()) < 1) {
            throw new MiniApiException(FL_MSG38);
        }
        if (insuranceAge > 65) {
            throw new MiniApiException(FL_MSG38);
        }
    }

    public static int getInsuranceAge(LocalDateTime birthday, LocalDateTime startDateInsurance) {
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
            throw new MiniApiException(FL_MSG46);
        }
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, dob + ZERO_TIME), LocalDateTime.now());
        if (insuranceAge < 18) {
            throw new MiniApiException(FL_MSG46);
        }
    }

    public static void validateAnnualIncome(Assured assured) {
        LocalDateTime birthday = DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assured.getDob() + ZERO_TIME);
        LocalDate birthDay = LocalDate.of(birthday.getYear(), birthday.getMonth(), birthday.getDayOfMonth());
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDay, today);

        if (age.getYears() > 18 && assured.getAnnualIncome() == null) {
            throw new MiniApiException(FL_MSG48);
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

    public static HealthInfoRequest genHealthInfoRequest(FlexibleSubmitQuestionRequest questionRequest,
                                                         Map<String, Assured> assuredMap) {
        FlexibleSubmitQuestionRequest.ComboSmallQuestion comboSmallQuestion = questionRequest.getComboSmallQuestion();
        HealthInfoRequest infoRequestAssured = new HealthInfoRequest();
        infoRequestAssured.setId(questionRequest.getAssuredId());
        if(questionRequest.getComboBigQuestion() != null && questionRequest.getComboSmallQuestion() != null) {
            log.error("[MINI]--Chỉ trả lời 1 combo câu hỏi sức khỏe");
            throw new MiniApiException(MSG12);
        }

        if(questionRequest.getComboBigQuestion() == null && questionRequest.getComboSmallQuestion() == null) {
            log.error("[MINI]--Bắt buộc trả lời 1 combo câu hỏi sức khỏe");
            throw new MiniApiException(MSG12);
        }
        List<Common.Quizzes> quizzes = new ArrayList<>();
        if(comboSmallQuestion != null) {
            Common.Answers answerC1 = new Common.Answers().setOrdinal(1).setValue(comboSmallQuestion.getC1());
            Common.Quizzes q1 = new Common.Quizzes().setLabel("1").setAnswers(List.of(answerC1));
            quizzes.add(q1);
            Common.Answers answerC2 = new Common.Answers().setOrdinal(1).setValue(comboSmallQuestion.getC2());
            Common.Quizzes q2 = new Common.Quizzes().setLabel("2").setAnswers(List.of(answerC2));
            quizzes.add(q2);
            Common.Answers answerC3 = new Common.Answers().setOrdinal(1).setValue(comboSmallQuestion.getC3());
            Common.Quizzes q3 = new Common.Quizzes().setLabel("3").setAnswers(List.of(answerC3));
            quizzes.add(q3);
            infoRequestAssured.setQuizzes(quizzes);
            infoRequestAssured.setUsCitizen(YES.equals(comboSmallQuestion.getOqs()));

            return infoRequestAssured;
        }

        FlexibleSubmitQuestionRequest.ComboBigQuestion comboBigQuestion = questionRequest.getComboBigQuestion();

        FlexibleSubmitQuestionRequest.QuestionOne c1 = comboBigQuestion.getC1();

        Common.Answers weight = new Common.Answers().setOrdinal(1).setValue(String.valueOf(c1.getHeight()));
        Common.Answers height = new Common.Answers().setOrdinal(2).setValue(String.valueOf(c1.getWeight()));
        Common.Quizzes quizzes1 = new Common.Quizzes().setLabel("1a").setAnswers(List.of(weight, height));
        quizzes.add(quizzes1);

        Common.Answers change = new Common.Answers().setOrdinal(1).setValue(String.valueOf(c1.getChange()));
        List<Common.Answers> answersC1b = new ArrayList<>();
        answersC1b.add(change);
        Common.Quizzes qWeight = new Common.Quizzes().setLabel("1b").setAnswers(answersC1b);
        if (!"K".equalsIgnoreCase(c1.getChange())) {
            Common.Answers detail = new Common.Answers().setOrdinal(2).setValue(c1.getChangeValue() + "-" + c1.getReasonChange());
            qWeight.getAnswers().add(detail);
        }
        quizzes.add(qWeight);

        //C2a
        genDefaultQuestion(quizzes, comboBigQuestion.getC2a(), "2a");

        //C2b
        genDefaultQuestion(quizzes, comboBigQuestion.getC2b(), "2b");
        //C2c
        FlexibleSubmitQuestionRequest.QuestionTwoC c2c = comboBigQuestion.getC2c();
        Common.Answers answerC2c1 = new Common.Answers().setOrdinal(1).setValue(c2c.getResponse());
        Common.Quizzes q2c;
        if (YES.equalsIgnoreCase(c2c.getResponse())) {
            Common.Answers answerC2c2 = new Common.Answers().setOrdinal(2).setValue(c2c.getNational());
            Common.Answers answerC2c3 = new Common.Answers().setOrdinal(3).setValue(c2c.getDetail());
            q2c = new Common.Quizzes().setLabel("2c").setAnswers(List.of(answerC2c1, answerC2c2, answerC2c3));
        } else {
            q2c = new Common.Quizzes().setLabel("2c").setAnswers(List.of(answerC2c1));
        }

        quizzes.add(q2c);
        //C3
        FlexibleSubmitQuestionRequest.QuestionThree c3 = comboBigQuestion.getC3();
        Common.Answers answerC3Resp = new Common.Answers().setOrdinal(1).setValue(c3.getResponse());
        List<Common.Answers> answersC3 = new ArrayList<>();
        answersC3.add(answerC3Resp);
        Common.Quizzes q3 = new Common.Quizzes().setLabel("3").setAnswers(answersC3);
        if (YES.equals(c3.getResponse())) {
            Common.Answers answerC3Type = new Common.Answers().setOrdinal(2).setValue(c3.getType());
            q3.getAnswers().add(answerC3Type);
            Common.Answers answerC3NumInDay = new Common.Answers().setOrdinal(3).setValue(c3.getNumInDay());
            q3.getAnswers().add(answerC3NumInDay);
            Common.Answers answerC3NumOfYears = new Common.Answers().setOrdinal(4).setValue(c3.getNumOfYears());
            q3.getAnswers().add(answerC3NumOfYears);
        }
        quizzes.add(q3);
        //C4
        FlexibleSubmitQuestionRequest.QuestionFour c4 = comboBigQuestion.getC4();
        Common.Answers answerC4Resp = new Common.Answers().setOrdinal(1).setValue(c4.getResponse().equals("K") ? "NO" : c4.getResponse());
        List<Common.Answers> answersC4 = new ArrayList<>();
        answersC4.add(answerC4Resp);
        Common.Quizzes q4 = new Common.Quizzes().setLabel("4").setAnswers(answersC4);
        if (YES.equals(c4.getResponse())) {
            Common.Answers answerC4Type = new Common.Answers().setOrdinal(2).setValue(c4.getType());
            q4.getAnswers().add(answerC4Type);
            Common.Answers answerC4Count = new Common.Answers().setOrdinal(4).setValue(c4.getCount());
            q4.getAnswers().add(answerC4Count);
            Common.Answers answerC4Frequency = new Common.Answers().setOrdinal(3).setValue(c4.getFrequency());
            q4.getAnswers().add(answerC4Frequency);
        }
        quizzes.add(q4);

        //C5
        genDefaultQuestion(quizzes, comboBigQuestion.getC5(), "5");

        //C6
        FlexibleSubmitQuestionRequest.QuestionSix c6 = comboBigQuestion.getC6();
        Common.Answers answerC6Resp = new Common.Answers().setOrdinal(1).setValue(c6.getResponse());
        List<Common.Answers> answersC6 = new ArrayList<>();
        answersC6.add(answerC6Resp);
        Common.Quizzes q6 = new Common.Quizzes().setLabel("6").setAnswers(answersC6);
        quizzes.add(q6);
        if (YES.equals(c6.getResponse())) {
            infoRequestAssured.setFamilyMembers(c6.getMembers());
        }

        //C7
        FlexibleSubmitQuestionRequest.QuestionSeven c7 = comboBigQuestion.getC7();;
        genDefaultQuestion(quizzes, c7.getC7a(), "7a");
        genDefaultQuestion(quizzes, c7.getC7b(), "7b");
        genDefaultQuestion(quizzes, c7.getC7c(), "7c");
        genDefaultQuestion(quizzes, c7.getC7d(), "7d");
        genDefaultQuestion(quizzes, c7.getC7e(), "7e");
        genDefaultQuestion(quizzes, c7.getC7f(), "7f");
        genDefaultQuestion(quizzes, c7.getC7g(), "7g");
        genDefaultQuestion(quizzes, c7.getC7h(), "7h");
        genDefaultQuestion(quizzes, c7.getC7i(), "7i");
        genDefaultQuestion(quizzes, c7.getC7j(), "7j");
        genDefaultQuestion(quizzes, c7.getC7k(), "7k");
        genDefaultQuestion(quizzes, c7.getC7l(), "7l");
        genDefaultQuestion(quizzes, c7.getC7m(), "7m");
        genDefaultQuestion(quizzes, c7.getC7n(), "7n");

        //C8
        FlexibleSubmitQuestionRequest.QuestionEight c8 = comboBigQuestion.getC8();
        Common.Answers answerC8Resp = new Common.Answers().setOrdinal(1).setValue(c8.getResponse());
        List<Common.Answers> answersC8 = new ArrayList<>();
        answersC8.add(answerC8Resp);
        Common.Quizzes q8 = new Common.Quizzes().setLabel("8").setAnswers(answersC8);
        if (YES.equals(c8.getResponse())) {
            Common.Answers answerC4Type = new Common.Answers().setOrdinal(2).setValue(c8.getMedicineType());
            q8.getAnswers().add(answerC4Type);
            Common.Answers answerC4Reason = new Common.Answers().setOrdinal(3).setValue(c8.getReason());
            q8.getAnswers().add(answerC4Reason);
        }
        quizzes.add(q8);

        //C9
        FlexibleSubmitQuestionRequest.QuestionNine c9 = comboBigQuestion.getC9();
        Common.Answers answerC9Resp = new Common.Answers().setOrdinal(1).setValue(c9.getResponse());
        List<Common.Answers> answersC9 = new ArrayList<>();
        answersC9.add(answerC9Resp);
        Common.Quizzes q9 = new Common.Quizzes().setLabel("9").setAnswers(answersC9);
        if (YES.equals(c9.getResponse())) {
            //C9
            genDefaultQuestion(quizzes, c9.getC9a(), "9a");
            genDefaultQuestion(quizzes, c9.getC9b(), "9b");
            genDefaultQuestion(quizzes, c9.getC9c(), "9c");

        }
        quizzes.add(q9);

        Assured assured = assuredMap.get(questionRequest.getAssuredId());
        int insuranceAge = getInsuranceAge(DateUtil.localDateTimeToString(DATE_YMD_HMS_01, assured.getDob() + ZERO_TIME), LocalDateTime.now());

        if (insuranceAge < 12) {
            // C10 children
            if (insuranceAge >= 4) {
                FlexibleSubmitQuestionRequest.QuestionTenB c10b = comboBigQuestion.getC10b();
                Common.Answers answerC10bResp = new Common.Answers().setOrdinal(1).setValue(c10b.getResponse());
                List<Common.Answers> answersC10b = new ArrayList<>();
                answersC10b.add(answerC10bResp);
                Common.Quizzes q10b = new Common.Quizzes().setLabel("10b").setAnswers(answersC10b);
                if (YES.equals(c10b.getResponse())) {
                    //C10b
                    Common.Answers answerC10bVersion = new Common.Answers().setOrdinal(2).setValue(c10b.getVersion());
                    q10b.getAnswers().add(answerC10bVersion);
                }
                quizzes.add(q10b);

                genDefaultQuestion(quizzes, comboBigQuestion.getC10c(), "10c");
                genDefaultQuestion(quizzes, comboBigQuestion.getC10d(), "10d");
            } else {
                String c10a = comboBigQuestion.getC10a();
                Common.Answers answerC10aResp = new Common.Answers().setOrdinal(1).setValue(c10a);
                List<Common.Answers> answersC10a = new ArrayList<>();
                answersC10a.add(answerC10aResp);
                Common.Quizzes q10a = new Common.Quizzes().setLabel("10a").setAnswers(answersC10a);
                quizzes.add(q10a);

                FlexibleSubmitQuestionRequest.QuestionTenB c10b = comboBigQuestion.getC10b();
                Common.Answers answerC10bResp = new Common.Answers().setOrdinal(1).setValue(c10b.getResponse());
                List<Common.Answers> answersC10b = new ArrayList<>();
                answersC10b.add(answerC10bResp);
                Common.Quizzes q10b = new Common.Quizzes().setLabel("10b").setAnswers(answersC10b);
                if (YES.equals(c10b.getResponse())) {
                    //C10b
                    Common.Answers answerC10bVersion = new Common.Answers().setOrdinal(2).setValue(c10b.getVersion());
                    q10b.getAnswers().add(answerC10bVersion);
                }
                quizzes.add(q10b);

                genDefaultQuestion(quizzes, comboBigQuestion.getC10c(), "10c");
                genDefaultQuestion(quizzes, comboBigQuestion.getC10d(), "10d");
            }
        } else { //C10 normal
            if (Gender.FEMALE.equals(assured.getGender())) {
                String c10a = comboBigQuestion.getC10a();
                Common.Answers answerC10aResp = new Common.Answers().setOrdinal(1).setValue(c10a);
                List<Common.Answers> answersC10a = new ArrayList<>();
                answersC10a.add(answerC10aResp);
                Common.Quizzes q10a = new Common.Quizzes().setLabel("10a").setAnswers(answersC10a);
                quizzes.add(q10a);

                FlexibleSubmitQuestionRequest.QuestionTenB c10b = comboBigQuestion.getC10b();
                Common.Answers answerC10bResp = new Common.Answers().setOrdinal(1).setValue(c10b.getResponse());
                List<Common.Answers> answersC10b = new ArrayList<>();
                answersC10b.add(answerC10bResp);
                Common.Quizzes q10b = new Common.Quizzes().setLabel("10b").setAnswers(answersC10b);
                if (YES.equals(c10b.getResponse())) {
                    //C10b
                    Common.Answers answerC10bVersion = new Common.Answers().setOrdinal(2).setValue(c10b.getVersion());
                    q10b.getAnswers().add(answerC10bVersion);
                    Common.Answers answerC10bDuSinh = new Common.Answers().setOrdinal(3).setValue(c10b.getDuSing());
                    q10b.getAnswers().add(answerC10bDuSinh);
                }
                quizzes.add(q10b);

                genDefaultQuestion(quizzes, comboBigQuestion.getC10c(), "10c");
                genDefaultQuestion(quizzes, comboBigQuestion.getC10d(), "10d");
                genDefaultQuestion(quizzes, comboBigQuestion.getC10e(), "10e");
            }
        }

        //C11b
        FlexibleSubmitQuestionRequest.DefaultQuestionEleven c11b = comboBigQuestion.getC11b();
        Common.Answers answerC11aResp = new Common.Answers().setOrdinal(1).setValue(c11b.getResponse());
        List<Common.Answers> answersC11a = new ArrayList<>();
        answersC11a.add(answerC11aResp);
        Common.Quizzes q11b = new Common.Quizzes().setLabel("11b").setAnswers(answersC11a);
        quizzes.add(q11b);
        if (YES.equals(c11b.getResponse())) {
            infoRequestAssured.setOtherInsuranceClaims(c11b.getInsuranceData());
        }

        //C11a
        FlexibleSubmitQuestionRequest.DefaultQuestionEleven c11a = comboBigQuestion.getC11a();
        Common.Answers answerC11bResp = new Common.Answers().setOrdinal(1).setValue(c11a.getResponse());
        List<Common.Answers> answersC11b = new ArrayList<>();
        answersC11b.add(answerC11bResp);
        Common.Quizzes q11a = new Common.Quizzes().setLabel("11a").setAnswers(answersC11b);
        quizzes.add(q11a);
        if (YES.equals(c11a.getResponse())) {
            infoRequestAssured.setOtherDeclinedContracts(c11a.getInsuranceData());
        }

        //C11c
        FlexibleSubmitQuestionRequest.OtherEleven c11c = comboBigQuestion.getC11c();
        Common.Answers answerC11cResp = new Common.Answers().setOrdinal(1).setValue(c11c.getResponse());
        List<Common.Answers> answersC11c = new ArrayList<>();
        answersC11c.add(answerC11cResp);
        Common.Quizzes q11c = new Common.Quizzes().setLabel("11c").setAnswers(answersC11c);
        quizzes.add(q11c);
        if (YES.equals(c11c.getResponse())) {
            infoRequestAssured.setOtherActiveContracts(c11c.getInsuranceData());
        }

        infoRequestAssured.setQuizzes(quizzes);
        infoRequestAssured.setUsCitizen(YES.equals(comboBigQuestion.getOqs()));
        log.info("AssuredId {} with question response {}", questionRequest.getAssuredId(), infoRequestAssured);
        return infoRequestAssured;
    }

    private static void genDefaultQuestion(List<Common.Quizzes> listQuizzes, FlexibleSubmitQuestionRequest.DefaultQuestion question, String label) {
        Common.Answers answerDefaultResp = new Common.Answers().setOrdinal(1).setValue(question.getResponse());
        List<Common.Answers> answersDefault = new ArrayList<>();
        answersDefault.add(answerDefaultResp);
        Common.Quizzes quizzes = new Common.Quizzes().setLabel(label).setAnswers(answersDefault);
        if (YES.equals(question.getResponse())) {
            Common.Answers answerC2aDetail = new Common.Answers().setOrdinal(2).setValue(question.getDetail());
            quizzes.getAnswers().add(answerC2aDetail);
        }
        listQuizzes.add(quizzes);
    }

    public static String convertIdentificationTypeMbalToMiniApp(IdentificationType identificationType) {
        if (IdentificationType.NATIONAL_ID.equals(identificationType)) {
            return "CMND";
        }
        if (IdentificationType.CITIZEN_ID.equals(identificationType)) {
            return "CCCD";
        }
        if (IdentificationType.MILITARY_ID.equals(identificationType)) {
            return "CMTQD";
        }
        return identificationType.name();
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
    public static class Branch {
        private String code;
        private String bankName;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    static class Referrer {
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
    @Schema
    @Valid
    public static class RefererInput extends Referrer {
        private String branchName;
        private String branchCode;
        private String departmentName;
        private String departmentCode;

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
    @Schema
    @Valid
    public static class Referrer3rdInput extends Referrer {
        private Branch branch;

        public FlexibleCommon.RefererInput toDto() {
            FlexibleCommon.RefererInput dto = new FlexibleCommon.RefererInput();
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
    @Schema
    public static class Supporter {
        private String code;
        private String name;
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
    }

    // Flexible
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class CustomerInfo extends CommonInfo {

        @Schema
        @NotBlank(message = "Thông tin ngày cấp là bắt buộc")
        @Pattern(regexp = REGEX_DATE, message = "Ngày cấp format: YYYY-MM-dd")
        private String identificationDate;

        @Schema
        @NotBlank(message = "Thông tin nơi cấp là bắt buộc")
        private String idIssuedPlace;

        @NotNull(message = "Thông tin thu nhập theo năm là bắt buộc đối với khách hàng")
        private BigDecimal annualIncome;

        @Schema(description = "Tên nghề nghiệp")
        private String job;

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
            setIdentificationNumber(assured.getIdentificationNumber());
            setIdentificationDate(assured.getIdentificationDate());
            setIdIssuedPlace(assured.getIdIssuedPlace());
            setAnnualIncome(assured.getAnnualIncome());
            setJob(assured.getOccupationName());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class BeneficiaryOutput {

        private String id;

        private String fullName;

        private IdentificationType identificationType;

        private String identificationNumber;

        private String dob;

        private Gender gender;

        private Integer occupationId;

        private Address address;

        private String nationalityCode;

        private String email;

        private String phoneNumber;

        private List<Common.Quizzes> quizzes;

        private List<FamilyMembersOutput> familyMembers;

        private List<OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims;

        private List<OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts;

        private List<OtherActiveContracts> otherActiveContracts;

        private boolean usCitizen;

        private RelationshipType relationshipWithMainAssured;

        private BigDecimal annualIncome;

        private Integer benefitRate;

        private RelationshipPolicyHolderType relationshipWithPolicyHolder;

        private AssuredType type;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class BeneficiaryInput {

        @Schema(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String fullName;

//        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = EMAIL_PATTERN)
        private String email;

//        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        @Schema(required = true)
        @Pattern(regexp = REGEX_DATE, message = "Ngày sinh format: yyyy-MM-dd")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String dob;

        @Schema(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Gender gender;

        @Schema(required = true, description = "Loại giấy tờ tùy thân")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private IdentificationType identificationType;

        @Schema(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String identificationNumber;

        @Schema(description = "Thông tin không bắt buộc")
        private Address address;

        @Schema(description = "Mã nghề nghiệp", required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Integer occupationId;

        @Schema(description = "Thông tin không bắt buộc")
        private BigDecimal annualIncome;

        @Schema(description = "Thông tin không bắt buộc")
        private String nationalityCode;

        @Schema(description = "Mối quan hệ với người được bảo hiểm chính", example = "COUPLE|CHILDREN|PARENT|OTHER")
        private RelationshipType relationshipWithMainAssured;

        @Schema(description = "Thông tin không bắt buộc")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Integer benefitRate;

        @Schema(description = "Thông tin không bắt buộc")
        @Pattern(regexp = REGEX_DATE, message = "Ngày cấp format: YYYY-MM-dd")
        private String identificationDate;

        @Schema(description = "Thông tin không bắt buộc")
        private String idIssuedPlace;

//        @Schema(description = "Mối quan hệ với người mua bảo hiểm", required = true)
//        private RelationshipPolicyHolderType relationshipWithPolicyHolder;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class CommonInfo {

        @Schema(required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String fullName;

        @Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
        private String email;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        @Schema(required = true)
        @Pattern(regexp = REGEX_DATE, message = "Ngày sinh format: yyyy-MM-dd")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String dob;

        @Schema(required = true)
        private Gender gender;

        @Schema(required = true, description = "Loại giấy tờ tùy thân")
        @NotNull(message = "Thông tin bắt buộc nhập")
        private IdentificationType identificationType;

        @Schema
        @NotNull(message = "Thông tin bắt buộc nhập")
        private String identificationNumber;

        @Schema
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Address address;

        @Schema(description = "Mã nghề nghiệp", required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Integer occupationId;

        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Mã quốc gia", required = true)
        private String nationalityCode;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class Assured extends CommonInfo {

        @Schema(description = "Thông tin không bắt buộc")
        private String id;

        @Schema(description = "Thông tin không bắt buộc")
        private String miniAssuredId = generateUUIDId(8);

        // tên nghề nghiệp
        @Schema(description = "Thông tin không bắt buộc")
        private String occupationName;

        @Schema(description = "Thông tin không bắt buộc")
        private String occupationGroup;

        @Schema(description = "Thông tin bắt buộc với người bổ sung trên 18 tuổi")
//        @NotNull(message = "Thông tin thu nhập theo năm là bắt buộc")
        private BigDecimal annualIncome;

        @Schema(description = "Người bảo hiểm chính hoặc bổ sung", example = "LIFE_ASSURED|ADDITIONAL_LIVES")
        private AssuredType type;

        @Schema(description = "Mối quan hệ với người được bảo hiểm chính", example = "COUPLE|CHILDREN|PARENT|OTHER", required = true)
//        @NotNull(message = "Thông tin bắt buộc nhập")
        private RelationshipType relationshipWithMainAssured;

        @Schema(description = "Thông tin không bắt buộc")
        private String identificationDate;

        @Schema(description = "Thông tin không bắt buộc")
        private String idIssuedPlace;

        @Schema(description = "Thông tin không bắt buộc")
        private Integer benefitRate;

        @Schema(description = "Thông tin không bắt buộc, Flag xác nhận là BMBH (Khách hàng)")
        private Boolean isCustomer = false;

        @Schema(description = "Mối quan hệ với người mua bảo hiểm", required = true)
        @NotNull(message = "Thông tin bắt buộc nhập")
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
            setIdentificationNumber(customerInfo.getIdentificationNumber());
            setAnnualIncome(customerInfo.getAnnualIncome());
        }

        public Assured(PolicyHolderAndLifeAssured lifeAssured) {
            setId(lifeAssured.getId());
            setGender(lifeAssured.getGender());
            setMiniAssuredId(lifeAssured.getMiniAssuredId());
            setDob(lifeAssured.getDob());
        }
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Address {

        private String title;

        private String name;

        @Pattern(regexp = PHONE_PATTERN, message = "Không đúng định dạng số điện thoại di động Việt Nam")
        private String phoneNumber;

        private String zipcode;

        private String provinceName;

        private String districtName;

        private String wardName;

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
    @Schema
    public static class ProductPackageOutput extends ProductPackageInput {
        private DiscountGroup discountGroup;
        private BigDecimal baseInsuranceFee;
        private BigDecimal topupInsuranceFee;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class ProductPackageInput {
        @Schema
        private Integer policyTerm;
        @Schema
        private BenefitType insuredBenefit;
        @Schema
        private Integer premiumTerm;
        @Schema
        private PaymentPeriod paymentPeriod;
        @Schema
        private BigDecimal sumAssured;
        @Schema
        private BigDecimal periodicPremium;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class FamilyMembersInput {
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Họ và tên")
        private String fullName;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Mối quan hệ với BMBH*")
        private String relationshipInsurancePerson;
        @Min(1)
        @Max(999)
        private Integer age;
        @NotNull(message = "Thông tin bắt buộc nhập")
        @Schema(description = "Tình trạng sức khoẻ hoặc Nguyên nhân tử vong (nếu đã chết)*")
        private String healthDetail;

        private String relation;

        private String status;

    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    @Valid
    public static class FamilyMembersOutput extends FamilyMembersInput {
        private String id;
        private MicInsuranceResultRespDto micFee;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
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
    @Schema
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
    @Schema
    @Valid
    public static class SubmitCustomer {
        @NotNull(message = "Thông tin bắt buộc nhập")
        private Boolean usCitizen;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class MiniMicRequestDto {
        @Schema(description = "Mức trách nhiệm", required = true, example = "1,2,3,4,5")
        @NotNull(message = "Nhóm gói bảo hiểm tính phí bên MIC bắt buộc nhập")
        @Max(value = 5, message = "Giá trị lớn nhất là 5")
        @Min(value = 1, message = "Giá trị nhỏ nhất là 1")
        private int nhom;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String bs1;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String bs2;
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        @Schema(required = true)
        private String bs3;
        @Schema(required = true)
        @Pattern(regexp = "C|K", message = "Chi chọn C (Có) hoặc K (Không)")
        private String bs4;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
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
    @Schema
    public static class CashFlow {
        private List<YearItem> yearItems;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Rider {
        private String code;
        private Integer policyTerm;
        private Integer premiumTerm;
        private BigDecimal sumAssured;
        private PaymentPeriod paymentPeriod;
        private BenefitType insuredBenefit;
        private String beneficiaryName;
        private List<YearItem> yearItems;
        private BigDecimal basePremium;
        private BigDecimal regBasePrem;
        private String assuredId;
        private ProductType productType;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
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
    @Schema
    public static class AccountByInterestRate {
        private Rate selectedRate;
        private Rate committedRate;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class BenefitByInvestmentRate {
        private Rate lowRate;
        private Rate highRate;
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema
    public static class Rate {
        private BigDecimal baseAccountValue;
        private BigDecimal topupAccountValue;

        private BigDecimal deathBenefit;
        private BigDecimal loyaltyBonus;

        private BigDecimal accountValue;
        private BigDecimal surenderValue;
    }

}
