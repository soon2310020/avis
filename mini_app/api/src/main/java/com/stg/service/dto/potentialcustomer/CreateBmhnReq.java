package com.stg.service.dto.potentialcustomer;

import javax.validation.constraints.NotNull;

import com.stg.constant.ComboCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBmhnReq {

    @NotNull
	private Long directId;
    @NotNull
	private ComboCode comboCode;

}
