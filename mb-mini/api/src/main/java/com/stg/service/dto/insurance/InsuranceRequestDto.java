package com.stg.service.dto.insurance;

import com.stg.entity.QuestionResponse;
import com.stg.service.dto.customer.CustomerDto;
import com.stg.utils.DateUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class InsuranceRequestDto {

    @Schema(description = "ID của gói request")
    private Long id;

    private CustomerDto customer;

    @Schema(description = "Type gói bảo hiểm. Nếu FIX thì micPackage va mbalPackage null, con CUSTOM thi insurancePackage", required = true)
    @Pattern(regexp = "FIX_COMBO|FREE_STYLE", message = "Type must be FIX_COMBO or FREE_STYLE")
    private String type;

    @Schema(description = "Gói bảo hiểm FIX")
    private InsurancePackageDto insurancePackage;

    @Schema(description = "Gói bảo hiểm MIC cho CUSTOM type")
    private MicPackageDto micPackage;

    @Schema(description = "Gói bảo hiểm MBAL cho CUSTOM type")
    private MbalPackageDto mbalPackage;

    private String creationTime;

    @Schema(description = "Câu hỏi số 1", example = "true")
    private boolean questionOne;

    @Schema(description = "Câu hỏi số 2", example = "true")
    private boolean questionTwo;

    @Schema(description = "Câu hỏi số 3", example = "true")
    private boolean questionThree;

    @Schema(description = "Câu hỏi khác 1", example = "true")
    private boolean otherQuestion;

    private boolean status;

    private String segment;

    List<QuestionResponse> questionResponses;

   private CreateIllustrationTableDto.ComboBigQuestion comboBigQuestion;

    private CreateIllustrationTableDto.ComboSmallQuestion comboSmallQuestion;

    private PrimaryInsuredDTO primaryInsuredDTO;

    private List<InsuredDTO> insuredDTOS;

    private List<BeneficiaryDTO> beneficiaryDTOS;

    private List<AdditionalProductDTO> customerAdditionalProductDTOS;

    private PrimaryProductDTO primaryProductDTO;

    private List<AdditionalProductDTO> primaryInsuredAdditionalProductDTOS;

    private List<AdditionalInsuredDTO> additionalAssuredOutputs;

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = DateUtil.localDateTimeToString(DateUtil.DATE_DMY, creationTime);
    }
}
