package saleson.model;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.CountryCode;
import saleson.common.enumeration.RoleUserData;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.common.util.Md5Utils;
import saleson.common.util.StringUtils;
import saleson.model.support.DateAudit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder(alphabetic = true)
@SuppressWarnings("serial")
public class User extends DateAudit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 40)
	private String name;

	@NotBlank
	@Size(max = 50)
	private String loginId;

	// @NaturalId
	@NotBlank
	@Size(max = 50)
	@Email
	private String email;

	@NotBlank
	@JsonIgnore
	@Size(max = 100)
	private String password;

	private String department;
	private String position;

	private String contactDialingCode;
	private String contactNumber;

	private String mobileDialingCode;
	private String mobileNumber;
	private String memo;

	private String ssoId; // SAML NAME ID

	private String language;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@Convert(converter = BooleanYnConverter.class)
	private boolean admin;

	@Convert(converter = BooleanYnConverter.class)
	private boolean accountLocked;

	private int failedAttempt;
	private Instant lockTime;

	@Column(name = "COMPANY_ID", insertable = false, updatable = false)
	private Long companyId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "COMPANY_ID")
	private Company company;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean notify;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean accessRequest;

	// @JoinColumn(name="USER_ID")
	// @ManyToMany(fetch = FetchType.LAZY)

	// @JsonIgnore
	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "ROLE_ID"))
	private Set<Role> roles = new HashSet<>();

	private Instant lastLogin;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean requested;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean disableNotificationSystemNode;

	@Enumerated(EnumType.STRING)
	private RoleUserData roleUserData;

	private Instant pwdExpiredAt;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean deleted;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean isPublic;

	public User(String name, String loginId, String email, String password) {
		this.name = name;
		this.loginId = loginId;
		this.email = email;
		this.password = password;
	}

	/**
	 * Gravatar Image Src
	 *
	 * @return
	 */
	// Prevent null in email value
	// 2022.03.31 modified by mickey.park
	public String getGravatar() {
		return ObjectUtils.isEmpty(email) ? "" : "//www.gravatar.com/avatar/" + Md5Utils.md5Hex(email) + "?d=mm";
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isAdmin() {
		return admin;
	}

	public String[] getRoleIds() {

		if (roles == null || roles.isEmpty()) {
			return new String[0];
		}
		String[] roleIds = StreamSupport.stream(roles.spliterator(), false).map(m -> String.valueOf(m.getId())).toArray(String[]::new);
		return roleIds;
	}

	public String getContactDialingCountry() {
		if (contactDialingCode == null)
			return "";
		return getCountryCode(contactDialingCode);
	}

	public String getMobileDialingCountry() {
		if (mobileDialingCode == null)
			return "";
		return getCountryCode(mobileDialingCode);
	}

	private String getCountryCode(String dialingCode) {
		Optional<CountryCode> optionalCountryCode = Arrays.stream(CountryCode.values()).filter(c -> c.getDescription().equals(dialingCode)).findFirst();

		if (optionalCountryCode.isPresent()) {
			return optionalCountryCode.get().getCode().toLowerCase();
		}

		return "";
	}

	public String getDisplayName() {
		if (!StringUtils.isEmpty(name))
			return name;
		return email;
	}
}
