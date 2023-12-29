package com.stg.service.dto.quotation;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CrmToken {

	@NotBlank
	private String token;
	
}
