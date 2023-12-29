package com.stg.service.dto.quotation;

import com.stg.entity.quotation.QuotationCustomer;
import com.stg.service.dto.quotation.validation.IPolicyTerm;
import com.stg.service.dto.quotation.validation.PolicyTerm;
import com.stg.service3rd.mbal.constant.RelationshipPolicyHolderType;
import com.stg.service3rd.mbal.constant.RelationshipType;
import com.stg.service3rd.mbal.dto.FlexibleCommon;
import com.stg.service3rd.mbal.dto.MicAdditionalProduct;
import com.stg.service3rd.mbal.dto.QuoteAssuredOutput;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.stg.service3rd.mbal.constant.Common.EMAIL_PATTERN;
import static com.stg.constant.CommonMessageError.REGEX_DATE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@PolicyTerm
public class QuotationAssuredDto extends QuotationCustomerDto implements IPolicyTerm {
	private String uuid;

	@Valid
	private List<QuotationProductDto> additionalProducts; /*Only MBAL*/

	@Valid
	@ApiModelProperty(notes = "Thông tin gói MIC cho người được BHBS")
	private MicAdditionalProduct micRequest; /*INPUT Of MIC*/

	private QuoteAssuredOutput micAdditionalProduct; /* OUTPUT Of MIC*/

	//@NotNull(message = "Thông tin bắt buộc nhập") //if flex => not null
	@ApiModelProperty(notes = "Mã quốc gia", required = true)
	private String nationalityCode;

	//@NotNull(message = "Thông tin bắt buộc nhập") //if flex => not null
	@ApiModelProperty(notes = "Địa chỉ", required = true)
	private FlexibleCommon.Address address;

	@Pattern(regexp = EMAIL_PATTERN, message = "Thông tin email không hợp lê. Vui lòng nhập lại thông tin.")
	private String email;

	private String type; /*flex-e-app*/
	private Integer appQuestionNumber; /*flex-e-app*/

	@ApiModelProperty
	//@NotBlank(message = "Thông tin ngày cấp là bắt buộc")
	@Pattern(regexp = REGEX_DATE, message = "Ngày cấp format: YYYY-MM-dd")
	private String identificationDate;

	@ApiModelProperty
	//@NotBlank(message = "Thông tin nơi cấp là bắt buộc")
	private String idIssuedPlace;

	@ApiModelProperty(notes = "Thông tin không bắt buộc")
	private BigDecimal annualIncome; /*flex-e-app*/
	@ApiModelProperty(notes = "Mối quan hệ với người được bảo hiểm chính", example = "COUPLE|CHILDREN|PARENT|OTHER", required = true)
	//@NotNull(message = "Thông tin bắt buộc nhập")
	private RelationshipType relationshipWithMainAssured; /*flex-e-app*/
	@ApiModelProperty(notes = "Mối quan hệ với người mua bảo hiểm", required = true)
	private RelationshipPolicyHolderType relationshipWithPolicyHolder; /*flex-e-app*/

	public QuotationAssuredDto(QuotationCustomer customer) {
		super(customer);

		if (customer.getAdditionalProducts() != null) {
			setAdditionalProducts(customer.getAdditionalProducts().stream().map(p -> new QuotationProductDto(
					p.getType(), p.getPolicyTerm(),
					p.getPremiumTerm(), p.getSumAssured())
			).collect(Collectors.toList()));
		}
		
		setEmail(customer.getEmail());
		
		if (StringUtils.hasText(customer.getAddressLine1())) {
		    FlexibleCommon.Address a = new FlexibleCommon.Address();
	        a.setLine1(customer.getAddressLine1());
	        a.setWardName(customer.getAddressWardName());
	        a.setDistrictName(customer.getAddressDistrictName());
	        a.setProvinceName(customer.getAddressProvinceName());
		    setAddress(a);
		}
	}

	public boolean validatePolicyTerm() {
		if (additionalProducts != null && !additionalProducts.isEmpty()) {
			for (QuotationProductDto product : additionalProducts) {
				if (product.getPolicyTerm() + calculateInsuranceAge(getDob(), LocalDateTime.now()) > 100) {
					return false;
				}
			}
		}
		return true;
	}
}
