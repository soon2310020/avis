package com.stg.service.dto.external.request;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class MbalValidateBpReqDto {

    private String packageCode;
    private String deathNoAccidentFrom;
    private String deathNoAccidentTo;
    private String deathAccident;
    private String deathAccidentYesTraffic;
    private String deathAccidentNoTraffic;
    private String supInpatientHospitalFee;
    private String inpatientTreatmentBenefit;
    private String icuTreatmentBenefit;
    private String fatalIllnessTreatmentBenefit;
    private String surgeryCostBenefit;
    private String sumInsuranceForSurgery;

}
