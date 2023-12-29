package com.stg.service.dto.insurance;

import com.stg.service.dto.external.requestFlexible.FlexibleSubmitMicQuestionRequest;
import com.stg.service.dto.external.requestFlexible.FlexibleSubmitQuestionRequest;
import com.stg.utils.RelationshipPolicyHolderType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PrimaryInsuredDTO {

    private Long id;

    private String fullName;

    private String gender;

    private String email;

    private String birthday;

    private String phone;

    // giấy tờ tùy thân
    private String identification;

    private String idCardType;

    private String nationality;

    private String job;

    private String address;

    private String insuredId; // giá trị của mbal

    private boolean customerIsAssured; // true = Người mua bảo hiểm cũng là người hưởng bảo hiểm chính

    private List<AdditionalProductDTO> additionalProductDTOS;

    private FlexibleSubmitQuestionRequest.ComboSmallQuestion mbalComboSmallQuestion;// trả lời câu hỏi sức khỏe MBAL

    private FlexibleSubmitQuestionRequest.ComboBigQuestion mbalComboBigQuestion;// trả lời câu hỏi sức khỏe MBAL

    private FlexibleSubmitMicQuestionRequest.MicQuestion micQuestionResponse; // trả lời câu hỏi sức khỏe MIC

    private Integer appQuestionNumber;

    private String miniAssuredId;

    private BigDecimal micFee;

    private BigDecimal micSumBenefit ;

    // GCNBH MIC
    private String micContractNum;

    private RelationshipPolicyHolderType relationshipWithPolicyHolder;
}
