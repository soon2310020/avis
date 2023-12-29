package com.stg.service3rd.toolcrm.dto.resp.quote;

import com.stg.service3rd.toolcrm.constant.RelationshipPolicyHolderType;
import com.stg.service3rd.toolcrm.validation.PolicyTerm;
import com.stg.utils.FlexibleCommon;
import com.stg.utils.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

import static com.stg.utils.CommonMessageError.REGEX_DATE;
import static com.stg.utils.Constants.EMAIL_PATTERN;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@PolicyTerm
public class QuotationAssuredDto extends QuotationCustomerDto {

	@Valid
	private List<QuotationProductDto> additionalProducts; /*Only MBAL*/

	//  todo?
//	@Valid
//	@Schema(description = "Thông tin gói MIC cho người được BHBS")
//	private MicAdditionalProduct micRequest; /*INPUT Of MIC*/

	//private QuoteAssuredOutput micAdditionalProduct; /* OUTPUT Of MIC*/

	//@NotNull(message = "Thông tin bắt buộc nhập") //if flex => not null
	@Schema(description = "Mã quốc gia", required = true)
	private String nationalityCode;

	//@NotNull(message = "Thông tin bắt buộc nhập") //if flex => not null
	@Schema(description = "Địa chỉ", required = true)
	private FlexibleCommon.Address address;

	@Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
	private String email;

	private String type; /*flex-e-app*/
	private Integer appQuestionNumber; /*flex-e-app*/

	@Schema
	//@NotBlank(message = "Thông tin ngày cấp là bắt buộc")
	@Pattern(regexp = REGEX_DATE, message = "Ngày cấp format: YYYY-MM-dd")
	private String identificationDate;

	@Schema
	//@NotBlank(message = "Thông tin nơi cấp là bắt buộc")
	private String idIssuedPlace;

	@Schema(description = "Thông tin không bắt buộc")
	private BigDecimal annualIncome; /*flex-e-app*/
	@Schema(description = "Mối quan hệ với người được bảo hiểm chính", example = "COUPLE|CHILDREN|PARENT|OTHER", required = true)
	//@NotNull(message = "Thông tin bắt buộc nhập")
	private RelationshipType relationshipWithMainAssured; /*flex-e-app*/
	@Schema(description = "Mối quan hệ với người mua bảo hiểm", required = true)
	private RelationshipPolicyHolderType relationshipWithPolicyHolder; /*flex-e-app*/
}
