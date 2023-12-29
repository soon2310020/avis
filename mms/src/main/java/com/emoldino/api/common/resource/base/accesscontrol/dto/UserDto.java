package com.emoldino.api.common.resource.base.accesscontrol.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
	private Long id;
	private String name;
	private String companyName;
	private String department;
	private String position;
	private String email;
	private String mobileNumber;
}
