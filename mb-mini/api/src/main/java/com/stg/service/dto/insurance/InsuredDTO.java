package com.stg.service.dto.insurance;

import com.stg.service.dto.external.requestFlexible.FlexibleSubmitQuestionRequest;
import com.stg.utils.RelationshipPolicyHolderType;
import com.stg.utils.RelationshipType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuredDTO {

    private Long id;

    private String fullName;

    private String birthday;

    private String gender;

    private String idCardType;

    // giấy tờ tùy thân
    private String identification;

    private String phone;

    private String insuredId; // giá trị của mbal

    private String email;

    private String address;

    private RelationshipType relationshipWithMainAssured;

    private String nationality;

    private FlexibleSubmitQuestionRequest.ComboSmallQuestion mbalComboSmallQuestion;// trả lời câu hỏi sức khỏe MBAL

    private FlexibleSubmitQuestionRequest.ComboBigQuestion mbalComboBigQuestion;// trả lời câu hỏi sức khỏe MBAL

    private String micQuestionResponse; // trả lời câu hỏi sức khỏe MIC

    private Integer appQuestionNumber;

    private String miniAssuredId; // giá trị của miniApp

    private String job;

    private Boolean customerIsAssured;

    private RelationshipPolicyHolderType relationshipWithPolicyHolder;

}
