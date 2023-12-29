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
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.OnBoardingFeature;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class OnBoarding {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "USER_ID", insertable = false, updatable = false)
	private Long userId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	private User user;

	@Enumerated(EnumType.STRING)
	private OnBoardingFeature feature;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private boolean seen;

	@JsonIgnore
	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;
	@JsonIgnore
	@LastModifiedDate
	private Instant updatedAt;
}
