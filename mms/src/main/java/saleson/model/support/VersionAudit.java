package saleson.model.support;

import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(//
		value = { "hibernateLazyInitializer", "handler" }, //
		allowGetters = true//
)
public abstract class VersionAudit {
	private Long originId;

	@CreatedBy
	@JsonIgnore
	@Column(updatable = false)
	private Long createdBy;
	@CreatedDate
	@JsonIgnore
	@Column(updatable = false)
	private Instant createdAt;
}
