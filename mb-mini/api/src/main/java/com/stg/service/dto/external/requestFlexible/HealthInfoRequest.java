package com.stg.service.dto.external.requestFlexible;

import com.stg.utils.Common;
import com.stg.utils.FlexibleCommon;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class HealthInfoRequest {

    @NotNull(message = "Giá trị id bắt buộc nhập")
    @Schema(name = "id của người hưởng bảo hiểm", required = true)
    private String id;

    @NotNull(message = "Giá trị assuredId bắt buộc nhập")
    private List<Common.Quizzes> quizzes;

    @Valid
    @Schema(description = "áp dụng cho luồng 12 câu", required = true)
    private List<FlexibleCommon.FamilyMembersInput> familyMembers;

    @Schema(description = "áp dụng cho luồng 12 câu", required = true)
    @Valid
    private List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims;

    @Schema(description = "áp dụng cho luồng 12 câu", required = true)
    @Valid
    private List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts;

    @Schema(description = "áp dụng cho luồng 12 câu", required = true)
    @Valid
    private List<FlexibleCommon.OtherActiveContracts> otherActiveContracts;

    private Boolean usCitizen;

    public void setFamilyMembers(List<FlexibleCommon.FamilyMembersInput> familyMembers) {
        familyMembers.forEach(familyMembersInput -> familyMembersInput
                .setRelationshipInsurancePerson(familyMembersInput.getRelation())
                .setHealthDetail(familyMembersInput.getStatus()));
        this.familyMembers = familyMembers;
    }

    public void setOtherDeclinedContracts(List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherDeclinedContracts) {
        this.otherDeclinedContracts = otherDeclinedContracts.stream().map(FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract::new).collect(Collectors.toList());
    }

    public void setOtherInsuranceClaims(List<FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract> otherInsuranceClaims) {
        this.otherInsuranceClaims = otherInsuranceClaims.stream().map(FlexibleCommon.OtherInsuranceClaimsAndDeclinedContract::new).collect(Collectors.toList());;
    }

    public void setOtherActiveContracts(List<FlexibleCommon.OtherActiveContracts> otherActiveContracts) {
        this.otherActiveContracts = otherActiveContracts.stream().map(FlexibleCommon.OtherActiveContracts::new).collect(Collectors.toList());
    }
}
