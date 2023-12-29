package saleson.model;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
public class LogTransfer {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	private String at;
	private String ti;
	private String ci;

	@Column(length = 500)
	private String es;

	@Column(length = 500)
	private String ds;

	// R2 - 2019.11.27.
	private String tv; // tv 정보를 추가로 등록

	private Instant createdAt;
}
