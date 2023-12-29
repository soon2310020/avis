package com.stg.utils.ocr;

import com.stg.constant.Gender;
import com.stg.service.dto.OCRDecryptedDto;
import com.stg.service3rd.ocr.dto.resp.IdentificationType;
import com.stg.service3rd.ocr.dto.resp.ProcessedOcrResp.Doc;
import com.stg.service3rd.ocr.dto.resp.ProcessedOcrResp.SingleResult;
import com.stg.utils.DateUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.stg.utils.ocr.ECDHUtils.decrypt;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OcrDecryptedDataUtil {
    public static final String fullName = "name"; // tên đầy đủ
    public static final String birthDay = "birth_day"; // ngày sinh
    public static final String gender = "gender"; // giới tính
    public static final String nationality = "nationality"; //  quốc tịch

    public static final String templateCodes = "template_codes"; //Loại giấy tờ
    public static final String idCard = "id_card"; // số cccd, cmnd ,
    public static final String citizenId = "citizen_id"; // số hộ chiếu
    public static final String issueDate = "issue_date"; // ngày cấp
    public static final String issuedPlace = "issue_place"; // nơi cấp

    public static final String birthPlace = "birth_place"; //nơi sinh
    public static final String originLocation = "origin_location"; // nguyên quán
    public static final String recentLocation = "recent_location"; // thường trú


    public static final String features = "features"; // đặc điểm nhận dạng
    public static final String mrz = "mrz"; // mã hộ chiếu
    public static final String validDate = "valid_date"; // ngày hết hạn
    public static final String ethnicity = "ethnicity"; // chủng tộc
    public static final String religion = "religion"; // tôn giáo
    public static final String militaryUnit = "military_unit";  // đơn vị tác chiến cho loại thẻ quân sự
    public static final String signee = "signee"; // người kí
    public static final String militaryRank = "military_rank"; // quân hàm
    public static final String bloodType = "blood_type"; // loại máu


    public static final String NATIONALITY_CODE_DEFAULT = "VN";
    public static IdentificationType convertIdentityType(String templateCode) {
        if (templateCode == null) return null;

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
                log.warn("convertIdentityType not found: {}", templateCode);
        }
        return null;
    }

    static final Pattern patternFemale = Pattern.compile("(?i)Nam");
    static final Pattern patternMale = Pattern.compile("(?i)Nữ");
    static final Pattern vnPattern = Pattern.compile("(?i)Việt Nam");

    public static Gender recognizeGender(String input) {
        if (input == null) return null;

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

    public static String recognizeNationality(String input) {
        if (input == null) return null;

        Matcher vnMatcher = vnPattern.matcher(input);
        if (vnMatcher.find()) {
            return "VN";
        } else {
            return null;
        }
    }


    /*public OCRDecryptedDto toDto() {
        OCRDecryptedDto dto = new OCRDecryptedDto();
        dto.setFullName(this.getFullName());
        dto.setBirthDay(DateUtil.convertToCommonFormat(this.getBirthDay()));
        dto.setGender(recognizeGender(this.getGender()));
        dto.setNationality(recognizeNationality(this.getNationality()));

        dto.setIdentificationType(convertIdentityType(this.getTemplateCodes()));
        dto.setIdentificationId(this.getIdCard() == null ? this.getCitizenId() : this.getIdCard());
        dto.setIssueDate(DateUtil.convertToCommonFormat(this.getIssueDate()));
        dto.setIssuePlace(this.getIssuedPlace());

        // detect address
        *//*dto.setProvinceName(this.getProvinceName());
        dto.setDistrictName(this.getDistrictName());
        dto.setWardName(this.getWardName());
        dto.setLine1(this.getLine1());*//*

        return dto;
    }*/

    public static OCRDecryptedDto decryptData(Doc result, byte[] sharedKey) throws Exception {
        OCRDecryptedDto dto = new OCRDecryptedDto();

        for (SingleResult rs : result.getSingleResult()) {
            //System.out.printf("Key=%s, Value=%s%n", rs.getName(), decrypt(rs.getValue(), sharedKey));
            switch (rs.getName()) { //NOSONAR
                case fullName:
                    dto.setFullName(decrypt(rs.getValue(), sharedKey));
                    break;
                case birthDay:
                    dto.setBirthDay(DateUtil.convertToCommonFormat(decrypt(rs.getValue(), sharedKey)));
                    break;
                case gender:
                    dto.setGender(recognizeGender(decrypt(rs.getValue(), sharedKey)));
                    break;
                /*case nationality:
                    dto.setNationality(recognizeNationality(decrypt(rs.getValue(), sharedKey)));
                    break;*/

                    // IDENTITY
                case idCard:
                case citizenId:
                    if (StringUtils.hasText(rs.getValue())) {
                        dto.setIdentificationId(decrypt(rs.getValue(), sharedKey));
                    }
                    break;
                case issueDate:
                    dto.setIssueDate(DateUtil.convertToCommonFormat(decrypt(rs.getValue(), sharedKey)));
                    break;
                case issuedPlace:
                    dto.setIssuePlace(decrypt(rs.getValue(), sharedKey));
                    break;

                // ADDRESS
                case recentLocation:
                    dto.setRecentLocation(decrypt(rs.getValue(), sharedKey));
                    break;
                case originLocation:
                    dto.setOriginLocation(decrypt(rs.getValue(), sharedKey));
                    break;
                case birthPlace:
                    dto.setBirthPlace(decrypt(rs.getValue(), sharedKey));
                    break;
            }
        }

        for (String templateCode : result.getTemplateCodes()) {
            IdentificationType identityType = convertIdentityType(decrypt(templateCode, sharedKey));
            if (identityType != null) {
                dto.setIdentificationType(identityType);
                break; // match thì thôi k check nữa
            }
        }

        dto.setNationality(NATIONALITY_CODE_DEFAULT); // default VN
        return dto;
    }
}
