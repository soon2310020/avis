package saleson.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.*;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import saleson.api.location.LocationRepository;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.customField.CustomFieldValue;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = { @Index(name = "IDX_COMPANY_ID", columnList = "COMPANY_ID") })
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonPropertyOrder(alphabetic = true)
public class Machine {
	@Id
	@GeneratedValue
	private Long id;

	private String machineCode;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "machine")
	private Mold mold;

	@Column(name = "COMPANY_ID", insertable = false, updatable = false)
	private Long companyId;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "COMPANY_ID")
	private Company company;

	@DataLeakDetector(disabled = true)
	@Column(name = "LOCATION_ID", insertable = false, updatable = false)
	private Long locationId;

	@DataLeakDetector(disabled = true)
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "LOCATION_ID")
	private Location location;

	private String line;

	@Convert(converter = BooleanYnConverter.class)
	private boolean enabled;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean deleted;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "MACHINE_ENGINEER", joinColumns = @JoinColumn(name = "MACHINE_ID"), inverseJoinColumns = @JoinColumn(name = "USER_ID"))
	private List<User> engineers = new ArrayList<>();

	private String machineMaker;
	private String machineType;
	private String machineModel;
	private Integer machineTonnage;

	@Transient
	private Long lastWorkOrderId;
	@Transient
	private Long workOrderHistory;

	@Transient
	private Map<Long, List<CustomFieldValue>> customFieldValueMap = new HashMap<>();

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

	public String getCompanyCode() {
		return Objects.nonNull(company) ? company.getCompanyCode() : "";
	}

	public String getCompanyName() {
		return Objects.nonNull(company) ? company.getName() : "";
	}

	public void setLocation(Location location) {
		this.location = location;
		if (location == null) {
			return;
		}
		if (location.getCompany() != null) {
			this.setCompanyId(location.getCompany().getId());
		} else if (location.getId() != null) {
			location = BeanUtils.get(LocationRepository.class).findById(location.getId()).orElse(null);
			if (location != null && location.getCompany() != null) {
				this.setCompanyId(location.getCompany().getId());
			}
		}
	}

	public String getLocationCode() {
		return Objects.nonNull(location) ? location.getLocationCode() : "";
	}

	public String getLocationName() {
		return Objects.nonNull(location) ? location.getName() : "";
	}

	public Boolean getLocationEnabled() {
		return Objects.nonNull(location) && location.isEnabled();
	}

	public List<String> getEngineerNames() {
		List<String> result = new ArrayList<>();
		if (engineers != null && engineers.size() > 0) {
			result = engineers.stream().map(x -> x.getName()).collect(Collectors.toList());
		}
		return result;
	}

	public List<Long> getEngineerIds() {
		List<Long> result = new ArrayList<>();
		if (engineers != null && engineers.size() > 0) {
			result = engineers.stream().map(x -> x.getId()).collect(Collectors.toList());
		}
		return result;
	}
}
