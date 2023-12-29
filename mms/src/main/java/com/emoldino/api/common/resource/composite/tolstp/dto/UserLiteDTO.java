package com.emoldino.api.common.resource.composite.tolstp.dto;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.model.User;
import saleson.model.data.CompanyLiteData;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLiteDTO {
	private Long id;
	private String name;
	private String department;
	private String position;
	private String mobileNumber;
	private String email;
	private CompanyLiteData company;

	public String getCompanyName() {
		return company == null ? null : company.getName();
	}

	public UserLiteDTO(User user) {
		ValueUtils.map(user, this);
	}
}
