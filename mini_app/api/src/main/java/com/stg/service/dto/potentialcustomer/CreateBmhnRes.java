package com.stg.service.dto.potentialcustomer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateBmhnRes {
	private Long Id;
	private byte[] data;
}
