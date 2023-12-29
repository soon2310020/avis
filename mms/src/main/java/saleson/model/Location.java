package saleson.model;

import java.time.Instant;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.composite.pltstp.dto.PltStpArea;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(indexes = { @Index(name = "IDX_COMPANY_ID", columnList = "COMPANY_ID") })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder(alphabetic = true)
public class Location {
	@Id
	@GeneratedValue
	private Long id;

	private String name;
	private String locationCode;
	private String address;

	@Lob
	private String memo;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@Column(name = "COMPANY_ID", insertable = false, updatable = false)
	private Long companyId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "COMPANY_ID")
	private Company company;

	private String countryCode;
	private String timeZoneId;

	private Double latitude;
	private Double longitude;

	@Transient
	private Long numberOfTerminal;

	@JsonIgnore
	@CreatedBy
	@Column(updatable = false)
	private Long createdBy;
	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedBy
	private Long updatedBy;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	@Transient
	private List<PltStpArea> areas;

	public Location(Long id) {
		this.id = id;
	}

	@Deprecated
	// TODO Check where this is used and Remove
	public String getLocationTitle() {
		return name;
	}

	public String getCompanyCode() {
		return getCompany() == null ? "" : getCompany().getCompanyCode();
	}

	// 회사명
	public String getCompanyName() {
		return getCompany() == null ? "" : getCompany().getName();
	}

	// 회사 enabled
	public Boolean getCompanyEnabled() {
		return getCompany() == null ? false : getCompany().isEnabled();
	}

	// 회사구분
	public String getCompanyTypeText() {
		return getCompany() == null ? "" : getCompany().getCompanyTypeText();
	}

}
