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
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.NotificationStatus;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MoldDataSubmission {
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
	private Instant notificationAt;

	@Convert(converter = BooleanYnConverter.class)
	private Boolean confirmed;
	private Instant confirmedAt;
	private String confirmedBy;
	private String approvedBy;
	private Instant approvedAt;

	@Lob
	private String message;

	@Lob
	private String reason;

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
