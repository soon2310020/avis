package com.stg.service.dto.potentialcustomer;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitDirectReq {
    @NotNull
	private Long potentialCustomerId;
}
