package com.stg.service.dto.quotation;

import com.stg.constant.quotation.QuotationState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class QuotationSearchDto {

	private Long id;
	private String name;
	private String phoneNumber;
	private LocalDate createdDate;
	private String comboName;

	private QuotationState quotationState; /*FLEX*/
}
