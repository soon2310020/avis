package com.emoldino.api.common.resource.composite.usrstp.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Email;

import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.api.common.resource.composite.usrstp.enumeration.UsrStpAccessLevel;
import com.emoldino.api.common.resource.composite.usrstp.enumeration.UsrStpStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.CompanyType;

@Data
@NoArgsConstructor
public class UsrStpData {
	private Long id;

	/**
	 * Account
	 */

	private String name;
	@Email
	private String email;

	/**
	 * Security
	 */

	private String password;
	private boolean ssoEnabled;

	/**
	 * Profile
	 */

	private Long companyId;
	@JsonIgnore
	private FltCompany company;
	private String department;
	private String position;
	private String mobileDialingCode;
	private String mobileNumber;
	private Instant lastLogin;
	private String memo;

	/**
	 * Access
	 */

	private UsrStpStatus status;
//	private boolean enabled;
//	private boolean requested;
	private UsrStpAccessLevel accessLevel;
//	private boolean admin;
//	@Enumerated(EnumType.STRING)
//	private RoleUserData roleUserData;
	private List<UsrStpPlant> plants = new ArrayList<>();

	private List<UsrStpRole> availableRoles = new ArrayList<>();

	public String getCompanyName() {
		return company == null ? null : company.getName();
	}

	public String getCompanyCode() {
		return company == null ? null : company.getCompanyCode();
	}

	public CompanyType getCompanyType() {
		return company == null ? null : company.getCompanyType();
	}

	@Data
	public static class UsrStpPlant {
		private Long id;
		private String name;
		private String locationCode;
		private List<UsrStpRole> roles = new ArrayList<>();
	}
}
