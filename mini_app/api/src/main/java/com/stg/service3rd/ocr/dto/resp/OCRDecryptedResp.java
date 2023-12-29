package com.stg.service3rd.ocr.dto.resp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.constant.Gender;
import com.stg.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OCRDecryptedResp {

    private IdentificationType identificationType;

    private LocalDate identificationDate;

    private String provinceName;

    private String districtName;

    private String wardName;

    private String line1;

    private Gender gender;

    @JsonProperty("features")
    private String features; // đặc điểm nhận dạng

    @JsonProperty("issue_date")
    private String issueDate; // ngày cấp

    @JsonProperty("mrz")
    private String mrz; // mã hộ chiếu

    @JsonProperty("id_card")
    private String identificationId; // số cccd, cmnd ,

    @JsonProperty("name")
    private String fullName; // tên đầy đủ

    @JsonProperty("birth_day")
    private String birthDay; // ngày sinh

    @JsonProperty("birth_place")
    private String birthPlace; //nơi sinh

    @JsonProperty("gender")
    private String genderStr; // giới tính

    @JsonProperty("nationality")
    private String nationality; //  quốc tịch

    @JsonProperty("origin_location")
    private String originLocation; // nguyên quán

    @JsonProperty("recent_location")
    private String recentLocation; // thường trú

    @JsonProperty("valid_date")
    private String validDate; // ngày hết hạn

    @JsonProperty("ethnicity")
    private String ethnicity; // chủng tộc

    @JsonProperty("religion")
    private String religion; // tôn giáo

    @JsonProperty("issue_place")
    private String idIssuedPlace; // nơi cấp

    @JsonProperty("military_unit")
    private String militaryUnit;  // đơn vị tác chiến cho loại thẻ quân sự

    @JsonProperty("signee")
    private String signee; // người kí

    @JsonProperty("military_rank")
    private String militaryRank; // quân hàm

    @JsonProperty("blood_type")
    private String bloodType; // loại máu

    @JsonProperty("citizen_id")
    private String citizenId; // số hộ chiếu

    @JsonProperty("template_codes") //Loại giấy tờ
    private String templateCodes;

    public static OCRDecryptedResp getObjectFromJSonNode(JsonNode data) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        OCRDecryptedResp resp = objectMapper.treeToValue(data,OCRDecryptedResp.class);
        if (resp.getBirthPlace()!=null ){
            String[] address = resp.getBirthPlace().split(",");
            if (address.length>2){
                resp.setWardName(address[0]);
                resp.setDistrictName(address[1]);
                resp.setProvinceName(address[2]);
            }
        }
        if (resp.getOriginLocation()!=null){
            String[] address = resp.getOriginLocation().split(",");
            if (address.length>2){
                resp.setWardName(address[0]);
                resp.setDistrictName(address[1]);
                resp.setProvinceName(address[2]);
            }
        }
        if (resp.getIdentificationId()==null&&resp.getCitizenId()!=null){
                resp.setIdentificationId(resp.getCitizenId());
        }
        resp.setGender(recognizeGender(resp.getGenderStr()));
        resp.setNationality(recognizeNationality(resp.getNationality()));

        resp.setBirthDay(DateUtil.convertToCommonFormat(resp.getBirthDay()));
        resp.setIssueDate(DateUtil.convertToCommonFormat(resp.getIssueDate()));
        resp.setValidDate(DateUtil.convertToCommonFormat(resp.getValidDate()));
        if (resp.getTemplateCodes()!=null) {
            resp.setIdentificationType(convertIdentityType(resp.getTemplateCodes()));
        }

        return resp;
    }

    private static IdentificationType convertIdentityType(String templateCode) {
        switch (templateCode) {
            case "CMT_CU_MT":
            case "CMT_CU_MS":
                return IdentificationType.NATIONAL_ID;
            case "CCCD_CU_MT":
            case "CCCD_CU_MS":
            case "CCCD_CHIP_MT":
            case "CCCD_CHIP_MS":
                return IdentificationType.CITIZEN_ID;
            case "HO_CHIEU":
                return IdentificationType.PASSPORT;
            case "CMT_QD_SQ_MT":
            case "CMT_QD_SQ_MS":
            case "CMT_QD_HSQ_MT":
            case "CMT_QD_HSQ_MS":
            case "CMT_QD_MT":
            case "CMT_QD_MS":
                return IdentificationType.MILITARY_ID;
            default:
                log.error("convertIdentityType not found: {}", templateCode);
                return null;
        }
    }

    private static Gender recognizeGender(String input) {
        if (input == null)
            return null;
        Pattern patternFemale = Pattern.compile("(?i)Nam");
        Pattern  patternMale = Pattern.compile("(?i)Nữ");

        Matcher matcherMale = patternMale.matcher(input);
        Matcher matcherFemale = patternFemale.matcher(input);

        if (matcherMale.find()) {
            return Gender.MALE;
        } else if (matcherFemale.find()) {
            return Gender.FEMALE;
        } else {
            return null;
        }
    }

    private static String recognizeNationality(String input){
        if (input ==null)
            return null;
        Pattern vnPattern = Pattern.compile("(?i)Việt Nam");
        Matcher vnMatcher = vnPattern.matcher(input);
        if (vnMatcher.find()){
            return "VN";
        }else {
            return null;
        }
    }
}
