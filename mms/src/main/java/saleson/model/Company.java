package saleson.model;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.CompanyType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.data.CompanyLiteData;
import saleson.model.support.UserDateAudit;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder(alphabetic = true)
public class Company extends UserDateAudit {
	public Company(Long id, CompanyType companyType, String companyCode, String name, String address, String manager, String phone, String email, String memo, boolean enabled,
			boolean isEmoldino, Instant createdAt, int moldCount) {
		this.id = id;
		this.companyType = companyType;
		this.companyCode = companyCode;
		this.name = name;
		this.address = address;
		this.manager = manager;
		this.phone = phone;
		this.email = email;
		this.memo = memo;
		this.enabled = enabled;
		this.isEmoldino = isEmoldino;
		this.setCreatedAt(createdAt);
		this.moldCount = moldCount;
	}

	@Id
	@GeneratedValue
	private Long id;

	@Column(length = 20)
	@Enumerated(EnumType.STRING)
	private CompanyType companyType;

	@Column(unique = true, length = 20)
	private String companyCode;
	private String name;
	private String address;
	private String manager;
	private String phone;
	private String email;

	@Lob
	private String memo;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean isEmoldino;

	@Transient
	private List<CompanyLiteData> upperTierCompanies;

	@Transient
	private long moldCount;
	@Transient
	private long partCount;
	@Transient
	private long totalProduced;
	@Transient
	private long totalMaxCapacity;
	@Transient
	private long predictedQuantity;

	public Company(Long id) {
		this.id = id;
	}

	public String getCompanyTypeText() {
		return this.companyType != null ? this.companyType.getTitle() : null;
	}
}
