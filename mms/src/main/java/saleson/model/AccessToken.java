package saleson.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
public class AccessToken {
	@Id
	@GeneratedValue
	private Long id;

	private Long userId;
	private String accessToken;
	private Instant expiredTime;
	private boolean deleted;

	//mobile device
	private String deviceToken;
	private String deviceType;
	private String udid;

	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;

	public AccessToken(Long userId, String accessToken, Integer expiredValue) {
		this.expiredTime = Instant.now().plus(expiredValue, ChronoUnit.MINUTES);
		this.userId = userId;
		this.accessToken = accessToken;
	}

	public AccessToken(Long userId, String accessToken, String deviceToken, String udid, String deviceType, Integer expiredValue) {
		this.userId = userId;
		this.accessToken = accessToken;
		this.expiredTime = Instant.now().plus(expiredValue, ChronoUnit.MINUTES);
		this.deviceToken = deviceToken;
		this.udid = udid;
		this.deviceType = deviceType;
	}

}
