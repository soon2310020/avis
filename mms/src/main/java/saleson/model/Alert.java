package saleson.model;

import saleson.common.enumeration.AlertType;
import saleson.model.support.DateAudit;

import javax.persistence.*;
import java.time.Instant;


/**
 * 2019-04-17 터미널, 카운터 관련 Admin alert 처리용으로 작업 중 우선 리스트만 보이도록 작업을 진행하여 현재 코드는 사용하지 않음.
 */
@Entity
public class Alert extends DateAudit {
	@Id
	private Long id;

	@Enumerated(EnumType.STRING)
	private AlertType alertType;

	private Instant notificationAt;

	@Lob
	private String message;
	private Instant confirmedAt;
	private String confirmedBy;

}
