package com.stg.service.dto.external.requestV2;

import com.stg.utils.Common;
import com.stg.utils.FlexibleCommon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class SubmitApplicationV2ReqDto {
    @Valid
    private Common.Referer referer;
    private Common.Supporter supporter;
    @Valid
    private List<Common.Quizzes> quizzes;
    @Valid
    private List<FlexibleCommon.FamilyMembersInput> familyMembers;
    @Valid
    private List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts;
    @Valid
    private List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims;
    @Valid
    private List<FlexibleCommon.OtherActiveContracts> otherActiveContracts;
    private FlexibleCommon.SubmitCustomer customer;


}
