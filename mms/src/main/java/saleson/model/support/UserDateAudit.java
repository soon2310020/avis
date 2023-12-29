package saleson.model.support;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class UserDateAudit extends DateAudit {

	@CreatedBy
	@JsonIgnore
	@Column(updatable = false)
	private Long createdBy;

	@LastModifiedBy
	@JsonIgnore
	private Long updatedBy;
}
