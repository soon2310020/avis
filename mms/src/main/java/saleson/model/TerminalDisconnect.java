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
import javax.persistence.Transient;

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
public class TerminalDisconnect {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "TERMINAL_ID", insertable = false, updatable = false)
	private Long terminalId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TERMINAL_ID")
	private Terminal terminal;

	@Enumerated(EnumType.STRING)
	private NotificationStatus notificationStatus;
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

	@Transient
	private Instant lastAlertAt;

	public String getLastAlertDate() {
		return DateUtils2.format(getLastAlertAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getNotificationDate() {
		return DateUtils2.format(getNotificationAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getNotificationDateTime() {
		return DateUtils2.format(getNotificationAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
	}

	public String getConfirmedDate() {
		return DateUtils2.format(getConfirmedAt(), DatePattern.yyyy_MM_dd, Zone.SYS);
	}

	public String getConfirmedDateTime() {
		return DateUtils2.format(getConfirmedAt(), DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.SYS);
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
