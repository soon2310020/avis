package com.stg.service3rd.toolcrm.dto.resp.quote;

import com.stg.service.dto.validator.Less;
import com.stg.utils.ProductType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Less(field = "premiumTerm", dependField = "policyTerm", orEqual = true, message = "premium_term must be less than or equal to policy_term")
public class QuotationProductDto {

	@NotNull
	private ProductType type;

	@NotNull
	private Integer policyTerm;

	@NotNull
	private Integer premiumTerm;

	@NotNull
	private BigDecimal sumAssured;

}
