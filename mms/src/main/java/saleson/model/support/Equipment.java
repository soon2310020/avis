package saleson.model.support;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.common.resource.base.location.enumeration.AreaType;
import com.emoldino.api.common.resource.base.location.repository.area.Area;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import saleson.api.location.LocationRepository;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.util.DateUtils;
import saleson.model.Location;

@Data
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class Equipment {
	private String equipmentCode;

	private Long companyId;

	@Column(name = "LOCATION_ID", insertable = false, updatable = false)
	private Long locationId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LOCATION_ID")
	private Location location;

	@Column(name = "AREA_ID", insertable = false, updatable = false)
	private Long areaId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AREA_ID")
	private Area area;

	@Enumerated(EnumType.STRING)
	private EquipmentStatus equipmentStatus;

	@Column(length = 20)
	@Enumerated(EnumType.STRING)
	private OperatingStatus operatingStatus;

	private String purchasedAt;
	private String installedBy;
	private String installedAt;
	private Instant activatedAt;
	private Instant operatedAt;

	@Lob
	private String memo;

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

	public String getOperatedDate() {
		return DateUtils.getDate(getOperatedAt());
	}

	public String getOperatedDateTime() {
		return DateUtils.getDateTime(getOperatedAt());
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
		return location == null ? "" : location.getLocationCode();
	}

	public String getLocationName() {
		return location == null ? "" : location.getName();
	}

	public Boolean getLocationEnabled() {
		return location == null ? false : location.isEnabled();
	}

	public String getAreaName() {
		return area == null ? null : area.getName();
	}

	public AreaType getAreaType() {
		return area == null ? null : area.getAreaType();
	}

	public String getCompanyName() {
		return location == null || location.getCompany() == null ? "" : location.getCompany().getName();
	}

	public String getCompanyCode() {
		return location == null || location.getCompany() == null ? "" : location.getCompany().getCompanyCode();
	}

	public Boolean getCompanyEnabled() {
		return location == null || location.getCompany() == null ? false : location.getCompany().isEnabled();
	}

	public String getCompanyTypeText() {
		return location == null || location.getCompany() == null ? "" : location.getCompany().getCompanyTypeText();
	}

	public Long getCompanyIdByLocation() {
		return location == null || location.getCompany() == null ? null : location.getCompany().getId();
	}

}
