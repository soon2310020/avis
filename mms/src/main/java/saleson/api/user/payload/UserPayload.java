package saleson.api.user.payload;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.util.ObjectUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.RoleUserData;
import saleson.model.Company;
import saleson.model.Role;
import saleson.model.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPayload {
	private Long id;
	private String name;
	private String email;
	private String password;
	private String department;
	private String position;
	private String contactDialingCode;
	private String contactNumber;
	private String language;

	private String mobileDialingCode;
	private String mobileNumber;
	private String memo;

	private boolean enabled;
	private boolean admin;
	private Boolean requested;

	private Long companyId;
	private String companyCode;
	private String companyName;

	private Long[] roleIds;

	private Map<AlertType, Boolean> alertStatus;
	private Boolean accessRequest;
	@Enumerated(EnumType.STRING)
	private RoleUserData roleUserData;

	private String deviceToken;
	private String udid;
	private String deviceType;

	private List<Long> ids;

	private Integer hashCode;

	private boolean validImportCheck;// use for special validation case

	/**
	 * 회원 간편 가입용 데이터
	 * @return
	 */
	public User getModel() {
		User user = new User();
		bindData(user);

		return user;
	}

	public User getModel(User user) {
		bindData(user);
		return user;
	}

	private void bindData(User user) {
		user.setEmail(email);
		user.setLoginId(email);
		user.setName(name);
		if (password != null && !"".equals(password)) {
			user.setPassword(password);
		}
		user.setEnabled(enabled);
		//user.setCompanyId(companyId);
		user.setAdmin(admin);
		user.setDepartment(department);
		user.setPosition(position);
		user.setContactDialingCode(contactDialingCode);
		user.setContactNumber(contactNumber);
		user.setMobileDialingCode(mobileDialingCode);
		user.setMobileNumber(mobileNumber);
		if (!ObjectUtils.isEmpty(language)) {
			user.setLanguage(language);
		}
		user.setMemo(memo);
		user.setRequested(requested);
		user.setAccessRequest(accessRequest);

		if (companyId != null) {
			user.setCompany(new Company(companyId));
			user.setCompanyId(companyId);
		}
		if (roleIds != null) {
			Set<Role> roleSet = new HashSet<>();

			for (Long id : roleIds) {
				Role role = new Role();
				role.setId(id);
				roleSet.add(role);
			}
			user.setRoles(roleSet);
		}
		user.setRoleUserData(roleUserData);

	}

	public User getMyModel(User user) {
		if (password != null && !"".equals(password)) {
			user.setPassword(password);
		}
		user.setDepartment(department);
		user.setPosition(position);
		user.setContactDialingCode(contactDialingCode);
		user.setContactNumber(contactNumber);
		user.setMobileDialingCode(mobileDialingCode);
		user.setMobileNumber(mobileNumber);
		if (!ObjectUtils.isEmpty(language)) {
			user.setLanguage(language);
		}
		if (companyId != null) {
			user.setCompany(new Company(companyId));
			user.setCompanyId(companyId);
		}
		return user;
	}

	public User getModelImport(User user) {
		bindDataImport(user);
		return user;
	}

	private void bindDataImport(User user) {
		user.setName(name);
		if (password != null && !"".equals(password)) {
			user.setPassword(password);
		}
		user.setEnabled(enabled);

		user.setDepartment(department);
		user.setPosition(position);
		user.setContactDialingCode(contactDialingCode);
		user.setContactNumber(contactNumber);
		user.setMobileDialingCode(mobileDialingCode);
		user.setMobileNumber(mobileNumber);
		if (!ObjectUtils.isEmpty(language)) {
			user.setLanguage(language);
		}
		user.setMemo(memo);

		if (companyId != null) {
			user.setCompany(new Company(companyId));
			user.setCompanyId(companyId);
		}

	}

}
