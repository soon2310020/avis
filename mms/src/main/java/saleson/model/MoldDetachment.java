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

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.enumeration.EquipmentStatus;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MoldDetachment {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "MOLD_ID", insertable = false, updatable = false)
	private Long moldId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MOLD_ID")
	private Mold mold;

	@Enumerated(EnumType.STRING)
	private NotificationStatus notificationStatus;
	private Instant detachmentTime;
	private String confirmedBy;
	private Instant confirmedAt;
	private Instant repairTime;

	@Enumerated(EnumType.STRING)
	private EquipmentStatus equipmentStatus;

	@Lob
	private String message;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "LOCATION_ID")
	private Location location;

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

	public MoldDetachment(Mold mold, Instant detachmentTime, Instant repairTime) {
		this.mold = mold;
		this.notificationStatus = NotificationStatus.ALERT;
		this.location = mold.getLocation();
		this.equipmentStatus = EquipmentStatus.DETACHED;
		this.latest = true;
		this.detachmentTime = detachmentTime;
		this.repairTime = repairTime;
	}

	public Machine getMachine() {
		return mold != null ? mold.getMachine() : null;
	}

	public String getCreatedDate() {
		return DateUtils2.format(getCreatedAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getCreatedDateTime() {
		return DateUtils2.format(getCreatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
	}

	public String getUpdatedDate() {
		return DateUtils2.format(getUpdatedAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getUpdatedDateTime() {
		return DateUtils2.format(getUpdatedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
	}
}
