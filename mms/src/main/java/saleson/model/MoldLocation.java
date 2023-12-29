package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.api.asset.resource.base.mold.enumeration.RelocationType;
import com.emoldino.api.common.resource.base.location.repository.area.Area;
import com.emoldino.api.common.resource.base.location.util.LocationUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.MoldLocationStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class MoldLocation {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "MOLD_ID", insertable = false, updatable = false)
	private Long moldId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID")
	private Mold mold;

	@Column(name = "LOCATION_ID", insertable = false, updatable = false)
	private Long locationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LOCATION_ID")
	private Location location;

	@Column(name = "AREA_ID", insertable = false, updatable = false)
	private Long areaId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "AREA_ID")
	private Area area;

	@Column(name = "PREVIOUS_LOCATION_ID", insertable = false, updatable = false)
	private Long previousLocationId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PREVIOUS_LOCATION_ID")
	private Location previousLocation;

	@Column(name = "PREVIOUS_AREA_ID", insertable = false, updatable = false)
	private Long previousAreaId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PREVIOUS_AREA_ID")
	private Area previousArea;

	@Enumerated(EnumType.STRING)
	private RelocationType relocationType;
	@Enumerated(EnumType.STRING)
	private MoldLocationStatus moldLocationStatus;
	private Instant notificationAt;
	private String confirmedBy;
	private Instant confirmedAt;

	@Lob
	private String message;

//	@Convert(converter = BooleanYnConverter.class)
//	private boolean deleted;

	@Deprecated
	@Convert(converter = BooleanYnConverter.class)
	private Boolean latest;

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

	public MoldLocation(Long id) {
		this.id = id;
	}

	public Machine getMachine() {
		return mold != null ? mold.getMachine() : null;
	}

	public String getCreatedDate() {
		return DateUtils2.format(createdAt, DatePattern.yyyy_MM_dd, LocationUtils.getZoneIdByLocationId(locationId));
	}

	public String getCreatedDateTime() {
		return DateUtils2.format(createdAt, DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByLocationId(locationId));
	}

	public String getUpdatedDate() {
		return DateUtils2.format(notificationAt, DatePattern.yyyy_MM_dd, LocationUtils.getZoneIdByLocationId(locationId));
	}

	public String getUpdatedDateTime() {
		return DateUtils2.format(updatedAt, DatePattern.yyyy_MM_dd_HH_mm_ss, LocationUtils.getZoneIdByLocationId(locationId));
	}

}
