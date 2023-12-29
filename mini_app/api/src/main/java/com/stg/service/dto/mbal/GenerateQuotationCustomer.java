package com.stg.service.dto.mbal;

import com.stg.constant.Gender;
import com.stg.constant.IdentificationType;
import com.stg.entity.quotation.QuotationCustomer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenerateQuotationCustomer {

	private String fullName;
	private String dob;
	private Gender gender;
	private Integer occupationId;
	private Boolean married;
	private IdentificationType identificationType;
	private String identificationId;
	private String phoneNumber;
	
	public GenerateQuotationCustomer(QuotationCustomer customer) {
		setFullName(customer.getFullName());
		setDob(customer.getDob().toString());
		setGender(customer.getGender());
		setOccupationId(customer.getOccupationId());
		setMarried(customer.getMarried());
		setIdentificationType(customer.getIdentificationType());
		setIdentificationId(customer.getIdentificationId());
		setPhoneNumber(customer.getPhoneNumber());
	}
}
