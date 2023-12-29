package com.stg.service.dto.external.request;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class InsertUpdateQuestionReqDto {
    private String gender;
    private String packageCode;
    private String productCode;
    private Double weight;
    private Double height;
    private Integer isChangeInWeight;
    private Integer isRiskInOccupation;
    private Integer isRiskInHobby;
    private Integer isTravelAbroad;
    private Integer isTobacco;
    private Integer isAlcohol;
    private Integer isDrugs;
    private Integer isFamilyIllness;
    private Integer heartIllnessIndicator_7a;
    private Integer hormonalDisorderIndicator_7b;
    private Integer entIllnessIndicator_7c;
    private Integer respiratoryIllnessIndicator_7d;
    private Integer bloodIllnessIndicator_7e;
    private Integer digestiveIllnessIndicator_7f;
    private Integer kidneyIllnessIndicator_7g;
    private Integer nervousIllnessIndicator_7h;
    private Integer cancerIllnessIndicator_7i;
    private Integer musculoskeletalIllnessIndicator_7j;
    private Integer mentallIllnessDescription_7k;
    private Integer sexualIllnessIndicator_7l;
    private Integer physicalIllnessIndicator_7m;
    private Integer dermatologicalProblemsIndicator_7n;
    private Integer drugIndicator_8;
    private Integer medicalExamIndicator_9;
    private Integer noOfChildren_11b;
    private Integer pregnantIndicator_11c;
    private Integer gynaecologicalDisordersIndicator_11d;
    private Integer abnormalBleedingDisordersIndicator_11e;
    private Integer breastUltrasoundIndicator_11f;
    private Integer declinedClaimDetailAddFlag;
    private Integer insuranceDetailAddFlag;
    private Integer acceptanceClaimDetailAddFlag;

}