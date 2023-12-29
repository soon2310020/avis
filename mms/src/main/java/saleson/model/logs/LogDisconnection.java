package saleson.model.logs;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import saleson.common.enumeration.EquipmentType;
import saleson.common.enumeration.Event;
import saleson.common.enumeration.NotificationStatus;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class LogDisconnection {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Enumerated(EnumType.STRING)
	private EquipmentType equipmentType;
	private Long equipmentId;

	@Enumerated(EnumType.STRING)
	private Event event;
	private Long alertId;

	@Enumerated(EnumType.STRING)
	private NotificationStatus notificationStatus;
	private Instant eventAt;
	@CreatedDate
	private Instant createdAt;
	private String confirmedBy;
	private Instant confirmedAt;

	private Integer shots;
	private Integer gap;

	@Lob
	private String message;
}
