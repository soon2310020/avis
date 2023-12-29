package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
public class SystemNoteRead {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;
	private Long systemNoteId;
	@Convert(converter = BooleanYnConverter.class)
	private boolean isRead;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	public SystemNoteRead(Long userId, Long systemNoteId) {
		this.userId = userId;
		this.systemNoteId = systemNoteId;
		this.isRead = true;
	}
}
