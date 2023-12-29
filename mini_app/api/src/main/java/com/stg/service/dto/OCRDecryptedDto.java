package com.stg.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stg.constant.Gender;
import com.stg.service3rd.ocr.dto.resp.IdentificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OCRDecryptedDto {
    private String fullName;
    private String birthDay; // ngày sinh
    private Gender gender;
    private String nationality; //  quốc tịch

    private IdentificationType identificationType;
    private String identificationId; // số cccd, cmnd ,
    private String issueDate; // ngày cấp
    private String issuePlace; // nơi cấp

    private String provinceName;
    private String districtName;
    private String wardName;
    private String line1;


    @JsonIgnore
    private String recentLocation; // thường trú
    @JsonIgnore
    private String originLocation; // nguyên quán
    @JsonIgnore
    private String birthPlace; //nơi sinh

    @JsonIgnore
    public String getAddress() {
        if (recentLocation != null) return recentLocation;
        if (originLocation != null) return originLocation;
        return birthPlace;
    }

    @Override
    public String toString() {
        return "OCRDecryptedDto{" +
                "fullName='" + fullName + '\'' +
                ", birthDay='" + birthDay + '\'' +
                ", gender=" + gender +
                ", nationality='" + nationality + '\'' +
                ", identificationType=" + identificationType +
                ", identificationId='" + identificationId + '\'' +
                ", issueDate='" + issueDate + '\'' +
                ", issuePlace='" + issuePlace + '\'' +
                ", provinceName='" + provinceName + '\'' +
                ", districtName='" + districtName + '\'' +
                ", wardName='" + wardName + '\'' +
                ", line1='" + line1 + '\'' +
                ", recentLocation='" + recentLocation + '\'' +
                ", originLocation='" + originLocation + '\'' +
                ", birthPlace='" + birthPlace + '\'' +
                '}';
    }
}
