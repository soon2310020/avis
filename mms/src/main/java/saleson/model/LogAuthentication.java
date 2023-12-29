package saleson.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import saleson.common.enumeration.AuthAction;

import javax.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class LogAuthentication {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	@Enumerated(EnumType.STRING)
	private AuthAction action;

	@Column
	private String username;		//loginId

	@Column
	private String reason;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@CreatedDate
	private Instant createdAt;

	public LogAuthentication() {}

	public LogAuthentication(String username, AuthAction action, String reason) {
		this.username = username;
		this.action = action;
		this.reason = reason;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuthAction getAction() {
		return action;
	}

	public void setAction(AuthAction action) {
		this.action = action;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
